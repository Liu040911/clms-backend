package com.clms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clms.entity.bo.HotLectureBO;
import com.clms.entity.po.LectureTable;

@Mapper
public interface LectureTableMapper extends BaseMapper<LectureTable> {

	List<HotLectureBO> selectHotLectureList(@Param("tagId") String tagId, @Param("limit") Integer limit);

}
