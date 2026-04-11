package com.clms.service.impl;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.RegistrationBO;
import com.clms.entity.bo.UserLectureAppointmentBO;
import com.clms.entity.po.ClassTable;
import com.clms.entity.po.LectureClassTable;
import com.clms.entity.dto.LectureRegistrationDTO;
import com.clms.entity.po.LectureTable;
import com.clms.entity.po.RegistrationTable;
import com.clms.entity.po.UserTable;
import com.clms.enums.RegistrationStatusEnum;
import com.clms.exception.BusinessException;
import com.clms.service.IUserLectureRegistrationService;
import com.clms.service.data.ILectureTableService;
import com.clms.service.data.ILectureClassTableService;
import com.clms.service.data.IRegistrationTableService;
import com.clms.service.data.IUserTableService;
import com.clms.service.data.IClassTableService;
import com.clms.utils.CommonUtil;
import com.clms.utils.RedisConstants;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

@Service
public class UserLectureRegistrationServiceImpl implements IUserLectureRegistrationService {

    // Lua脚本：原子尝试获取分布式锁（含过期时间设置）和安全释放分布式锁（含持有者校验）。
    private static final DefaultRedisScript<Long> ACQUIRE_LOCK_SCRIPT;
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT;

    static {
        ACQUIRE_LOCK_SCRIPT = new DefaultRedisScript<>();
        ACQUIRE_LOCK_SCRIPT.setResultType(Long.class);
        ACQUIRE_LOCK_SCRIPT.setScriptText(
                "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                "return 1; " +
                "else return 0; end");

        RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>();
        RELEASE_LOCK_SCRIPT.setResultType(Long.class);
        RELEASE_LOCK_SCRIPT.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]); " +
                "else return 0; end");
    }

    @Resource
    private IUserTableService userTableService;

    @Resource
    private ILectureTableService lectureTableService;

    @Resource
    private ILectureClassTableService lectureClassTableService;

    @Resource
    private IClassTableService classTableService;

    @Resource
    private IRegistrationTableService registrationTableService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistrationBO registerLecture(String userId, LectureRegistrationDTO registrationDTO) {
        // 1) 构造“讲座维度”分布式锁，避免同一讲座在瞬时高并发下被重复扣减库存。
        String lectureId = registrationDTO.getLectureId();
        String lockKey = RedisConstants.REGISTER_LOCK_KEY_PREFIX + lectureId;
        // 锁值使用随机token，释放时校验token可避免误删其他线程持有的锁。
        String lockValue = UUID.randomUUID().toString();

        // 2) Lua脚本尝试获取锁（含短暂重试）；获取失败直接快速失败，提示前端稍后重试。
        boolean locked = tryAcquireRegisterLock(lockKey, lockValue);
        if (!locked) {
            throw new BusinessException(429, "当前报名人数较多，请稍后重试");
        }

        try {
            // 3) 基础校验：用户必须存在。
            UserTable user = userTableService.getById(userId);
            if (user == null) {
                throw new BusinessException(404, "用户不存在");
            }

            // 4) 基础校验：讲座必须存在且可报名（已发布、在报名时间窗口内）。
            LectureTable lecture = lectureTableService.getById(lectureId);
            if (lecture == null) {
                throw new BusinessException(404, "讲座不存在");
            }

            if (!"published".equals(lecture.getStatus())) {
                throw new BusinessException(400, "仅可报名已发布讲座");
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.before(lecture.getRegistrationStartsTime())) {
                throw new BusinessException(400, "报名尚未开始");
            }
            if (now.after(lecture.getRegistrationEndsTime())) {
                throw new BusinessException(400, "报名已截止");
            }

            // 5) 幂等与重复报名处理：若已存在报名记录，区分“已取消”与“已报名”两类场景。
            RegistrationTable existed = registrationTableService.lambdaQuery()
                    .eq(RegistrationTable::getUserId, userId)
                    .eq(RegistrationTable::getLectureId, lectureId)
                    .one();

            if (existed != null) {
                // 5.1) 非取消状态视为重复报名，直接拒绝。
                if (!StrUtil.equals(existed.getStatus(), RegistrationStatusEnum.CANCELLED.getStatus())) {
                    throw new BusinessException(400, "您已报名该讲座，请勿重复报名");
                }

                // 5.2) 取消后恢复报名：先扣减名额，再恢复报名状态，避免超卖。
                decreaseLectureRemainingWithOptimisticLockOrThrow(lectureId);

                boolean resumed = registrationTableService.lambdaUpdate()
                        .eq(RegistrationTable::getId, existed.getId())
                        .set(RegistrationTable::getStatus, RegistrationStatusEnum.PENDING.getStatus())
                        .set(RegistrationTable::getRegistrationTime, now)
                        .set(RegistrationTable::getCheckInTime, null)
                        .update();
                if (!resumed) {
                    throw new BusinessException(500, "恢复报名失败");
                }

                // 返回最新报名数据（含恢复后的状态和时间）。
                RegistrationTable latest = registrationTableService.getById(existed.getId());
                return new RegistrationBO(latest);
            }

            // 6) 首次报名：先扣减名额（乐观锁），再创建报名记录。
            // 先扣库存可减少“先插入后回滚”带来的事务与日志开销。
            decreaseLectureRemainingWithOptimisticLockOrThrow(lectureId);

            RegistrationTable registration = new RegistrationTable();
            registration.setId(CommonUtil.generateUuidV7());
            registration.setUserId(userId);
            registration.setLectureId(lectureId);
            registration.setRegistrationTime(now);
            registration.setStatus(RegistrationStatusEnum.PENDING.getStatus());

            boolean saved = registrationTableService.save(registration);
            if (!saved) {
                throw new BusinessException(500, "讲座报名失败");
            }

            return new RegistrationBO(registration);
        } finally {
            // 7) 无论成功/失败都尝试释放锁，避免锁泄漏影响后续报名。
            releaseRegisterLockSafely(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistrationBO cancelLectureRegistration(String userId, LectureRegistrationDTO registrationDTO) {
        String lectureId = registrationDTO.getLectureId();
        String lockKey = RedisConstants.REGISTER_LOCK_KEY_PREFIX + lectureId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = tryAcquireRegisterLock(lockKey, lockValue);
        if (!locked) {
            throw new BusinessException(429, "当前操作人数较多，请稍后重试");
        }

        try {
            UserTable user = userTableService.getById(userId);
            if (user == null) {
                throw new BusinessException(404, "用户不存在");
            }

            LectureTable lecture = lectureTableService.getById(lectureId);
            if (lecture == null) {
                throw new BusinessException(404, "讲座不存在");
            }

            RegistrationTable registration = registrationTableService.lambdaQuery()
                    .eq(RegistrationTable::getUserId, userId)
                    .eq(RegistrationTable::getLectureId, lectureId)
                    .one();

            if (registration == null) {
                throw new BusinessException(404, "未找到报名记录");
            }

            if (StrUtil.equals(registration.getStatus(), RegistrationStatusEnum.CANCELLED.getStatus())) {
                throw new BusinessException(400, "该报名已取消，请勿重复取消");
            }

            boolean cancelled = registrationTableService.lambdaUpdate()
                    .eq(RegistrationTable::getId, registration.getId())
                    .set(RegistrationTable::getStatus, RegistrationStatusEnum.CANCELLED.getStatus())
                    .update();
            if (!cancelled) {
                throw new BusinessException(500, "取消报名失败");
            }

            increaseLectureRemaining(lectureId);

            RegistrationTable latest = registrationTableService.getById(registration.getId());
            return new RegistrationBO(latest);
        } finally {
            releaseRegisterLockSafely(lockKey, lockValue);
        }
    }

    @Override
    public Page<UserLectureAppointmentBO> getUserLectureAppointmentList(String userId, String status, Integer page, Integer size) {
        int pageNo = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 10 : size;

        var query = registrationTableService.lambdaQuery()
            .eq(RegistrationTable::getUserId, userId);

        if (StrUtil.isNotBlank(status)) {
            query.eq(RegistrationTable::getStatus, status);
        }

        Page<RegistrationTable> registrationPage = query
            .orderByDesc(RegistrationTable::getRegistrationTime)
            .orderByDesc(RegistrationTable::getCreateTime)
            .page(new Page<>(pageNo, pageSize));

        Page<UserLectureAppointmentBO> resultPage = new Page<>(
            registrationPage.getCurrent(),
            registrationPage.getSize(),
            registrationPage.getTotal());

        List<RegistrationTable> registrationRecords = registrationPage.getRecords();
        if (registrationRecords == null || registrationRecords.isEmpty()) {
            resultPage.setRecords(List.of());
            return resultPage;
        }

        List<String> lectureIds = registrationRecords.stream()
            .map(RegistrationTable::getLectureId)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();

        Map<String, LectureTable> lectureMap = lectureIds.isEmpty()
            ? Map.of()
            : lectureTableService.listByIds(lectureIds).stream()
                .collect(Collectors.toMap(LectureTable::getId, Function.identity(), (a, b) -> a));

        Map<String, String> lectureClassMap = lectureIds.isEmpty()
            ? Map.of()
            : lectureClassTableService.lambdaQuery()
                .in(LectureClassTable::getLectureId, lectureIds)
                .list()
                .stream()
                .collect(Collectors.toMap(LectureClassTable::getLectureId, LectureClassTable::getClassId, (a, b) -> a));

        Set<String> classIds = lectureClassMap.values().stream()
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());

        Map<String, ClassTable> classMap = classIds.isEmpty()
            ? Map.of()
            : classTableService.listByIds(classIds).stream()
                .collect(Collectors.toMap(ClassTable::getId, Function.identity(), (a, b) -> a));

        List<UserLectureAppointmentBO> records = registrationRecords.stream().map(registration -> {
            LectureTable lecture = lectureMap.get(registration.getLectureId());
            String classId = lectureClassMap.get(registration.getLectureId());
            ClassTable classTable = StrUtil.isBlank(classId) ? null : classMap.get(classId);
            String location = classTable == null ? null : classTable.getLocation();
            return new UserLectureAppointmentBO(registration, lecture, location);
        }).toList();

        resultPage.setRecords(records);
        return resultPage;
    }

    private boolean tryAcquireRegisterLock(String lockKey, String lockValue) {
        // 通过短重试平滑瞬时竞争，减少高峰期直接失败率。
        for (int i = 0; i < RedisConstants.REGISTER_LOCK_RETRY_TIMES; i++) {
            Long result = stringRedisTemplate.execute(
                    ACQUIRE_LOCK_SCRIPT,
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(RedisConstants.REGISTER_LOCK_EXPIRE_MILLIS));

            if (result != null && result == 1L) {
                return true;
            }

            // 仅在非最后一次重试时等待，最后一次重试后直接放弃，快速反馈。
            if (i < RedisConstants.REGISTER_LOCK_RETRY_TIMES - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(RedisConstants.REGISTER_LOCK_RETRY_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    // 恢复中断标记并尽快返回，避免吞掉中断信号。
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private void releaseRegisterLockSafely(String lockKey, String lockValue) {
        try {
            // 仅删除“自己持有”的锁，避免误删其他线程已续约/重入后的锁。
            stringRedisTemplate.execute(
                    RELEASE_LOCK_SCRIPT,
                    Collections.singletonList(lockKey),
                    lockValue);
        } catch (Exception ignored) {
            // 释放锁失败不影响主事务提交，依赖锁超时兜底。
        }
    }

    private void decreaseLectureRemainingWithOptimisticLockOrThrow(String lectureId) {
        // 乐观锁：SQL条件更新（remaining > 0）保证并发下库存不会被扣成负数。
        // 即使Redis锁失效或被绕过，该条件仍可作为最终一致性的兜底保护。
        boolean updated = lectureTableService.lambdaUpdate()
                .eq(LectureTable::getId, lectureId)
                .gt(LectureTable::getRemaining, 0)
                .setSql("remaining = remaining - 1")
                .update();

        if (!updated) {
            throw new BusinessException(400, "当前讲座名额已满");
        }
    }

    private void increaseLectureRemaining(String lectureId) {
        boolean updated = lectureTableService.lambdaUpdate()
                .eq(LectureTable::getId, lectureId)
                .setSql("remaining = remaining + 1")
                .update();

        if (!updated) {
            throw new BusinessException(500, "回补讲座名额失败");
        }
    }
}
