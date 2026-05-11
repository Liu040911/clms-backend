package com.clms.scheduler;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.clms.entity.po.LectureTable;
import com.clms.enums.LectureStatusEnum;
import com.clms.mapper.LectureTableMapper;
import com.clms.service.IUserLectureRegistrationService;
import com.clms.service.data.ILectureTableService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LectureStatusScheduler {

    @Resource
    private LectureTableMapper lectureTableMapper;

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private IUserLectureRegistrationService userLectureRegistrationService;

    @Resource(name = "asyncPoolTaskExecutor")
    private ThreadPoolTaskExecutor asyncPoolTaskExecutor;

    @Scheduled(fixedDelayString = "${lecture.status-check-interval-ms:30000}")
    public void checkAndUpdateLectureStatuses() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        int publishedToRegistering = transitionPublishedToRegistering(now);
        int registeringToReady = transitionRegisteringToReady(now);
        int readyToOngoing = transitionReadyToOngoing(now);
        int ongoingToFinished = transitionOngoingToFinished(now);

        if (publishedToRegistering > 0 || registeringToReady > 0 || readyToOngoing > 0 || ongoingToFinished > 0) {
            log.info("讲座状态转换: PUBLISHED→REGISTERING={}, REGISTERING→READY={}, READY→ONGOING={}, ONGOING→FINISHED={}",
                    publishedToRegistering, registeringToReady, readyToOngoing, ongoingToFinished);
        }
    }

    private int transitionPublishedToRegistering(Timestamp now) {
        LambdaUpdateWrapper<LectureTable> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(LectureTable::getStatus, LectureStatusEnum.REGISTERING.getStatus())
               .eq(LectureTable::getStatus, LectureStatusEnum.PUBLISHED.getStatus())
               .le(LectureTable::getRegistrationStartsTime, now);
        return lectureTableMapper.update(null, wrapper);
    }

    private int transitionRegisteringToReady(Timestamp now) {
        LambdaUpdateWrapper<LectureTable> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(LectureTable::getStatus, LectureStatusEnum.READY.getStatus())
               .eq(LectureTable::getStatus, LectureStatusEnum.REGISTERING.getStatus())
               .le(LectureTable::getRegistrationEndsTime, now);
        return lectureTableMapper.update(null, wrapper);
    }

    private int transitionReadyToOngoing(Timestamp now) {
        LambdaUpdateWrapper<LectureTable> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(LectureTable::getStatus, LectureStatusEnum.ONGOING.getStatus())
               .eq(LectureTable::getStatus, LectureStatusEnum.READY.getStatus())
               .le(LectureTable::getLectureStartTime, now);
        return lectureTableMapper.update(null, wrapper);
    }

    private int transitionOngoingToFinished(Timestamp now) {
        List<LectureTable> endedLectures = lectureTableService.lambdaQuery()
                .eq(LectureTable::getStatus, LectureStatusEnum.ONGOING.getStatus())
                .le(LectureTable::getLectureEndTime, now)
                .list();

        if (endedLectures.isEmpty()) {
            return 0;
        }

        List<String> lectureIds = endedLectures.stream()
                .map(LectureTable::getId)
                .toList();

        LambdaUpdateWrapper<LectureTable> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(LectureTable::getStatus, LectureStatusEnum.FINISHED.getStatus())
               .eq(LectureTable::getStatus, LectureStatusEnum.ONGOING.getStatus())
               .in(LectureTable::getId, lectureIds);
        int updated = lectureTableMapper.update(null, wrapper);

        for (LectureTable lecture : endedLectures) {
            CompletableFuture.runAsync(
                    () -> userLectureRegistrationService.markAbsentRegistrations(lecture.getId()),
                    asyncPoolTaskExecutor
            ).exceptionally(ex -> {
                log.error("异步标记未签到失败, lectureId={}, error={}",
                        lecture.getId(), ex.getMessage(), ex);
                return null;
            });
        }

        return updated;
    }
}
