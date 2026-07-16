package com.im.pushservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.pushservice.entity.ImPushRecord;
import org.mapstruct.Mapper;

@Mapper
public interface PushRecordMapper extends BaseMapper<ImPushRecord> {}