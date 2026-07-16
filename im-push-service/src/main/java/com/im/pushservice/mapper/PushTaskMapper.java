package com.im.pushservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.pushservice.entity.ImPushTask;
import org.mapstruct.Mapper;

@Mapper
public interface PushTaskMapper extends BaseMapper<ImPushTask> {}