package com.im.imuserservice.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.imuserservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}