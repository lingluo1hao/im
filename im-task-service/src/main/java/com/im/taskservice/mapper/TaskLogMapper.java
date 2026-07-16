package com.im.taskservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.taskservice.entity.ImTaskLog;
import org.mapstruct.Mapper;

@Mapper
public interface TaskLogMapper extends BaseMapper<ImTaskLog> {}