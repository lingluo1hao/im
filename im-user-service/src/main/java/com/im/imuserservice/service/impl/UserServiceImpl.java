package com.im.imuserservice.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.common.config.JwtProperties;
import com.im.common.dto.UserInfoDTO;
import com.im.common.exception.BusinessException;
import com.im.imuserservice.convert.UserConvert;
import com.im.imuserservice.dto.LoginDTO;
import com.im.imuserservice.dto.RegisterDTO;
import com.im.imuserservice.dto.UserDTO;
import com.im.imuserservice.entity.User;
import com.im.imuserservice.mapper.UserMapper;
import com.im.imuserservice.service.UserService;
import com.im.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(400, "用户名已被注册");
        }
        // 2. 密码加密
        String encryptPwd = passwordEncoder.encode(registerDTO.getPassword());
        // 3. 组装用户数据入库，默认状态1正常
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPwd);
        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        baseMapper.insert(user);
    }

    @Override
    public String login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = baseMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(400, "用户名不存在");
        }
        // 2. 校验账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(400, "账号已被禁用，无法登录");
        }
        // 3. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(400, "密码错误");
        }

        // 4. 组装用户核心信息
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setPhone(user.getPhone());
        userInfo.setStatus(user.getStatus());

        // 5. 生成携带用户信息的 JWT
        String token = jwtUtil.generateToken(userInfo);

        // 6. Token 存入 Redis，设置与 JWT 一致的过期时间
        // Key 设计：auth:token:{token值}，Value 存储用户信息 JSON
        try {
            String userInfoJson = objectMapper.writeValueAsString(userInfo);
            redisTemplate.opsForValue().set(
                    "auth:token:" + token,
                    userInfoJson,
                    jwtProperties.getExpire(),
                    TimeUnit.MILLISECONDS
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "登录凭证生成失败");
        }

        return token;
    }

    @Override
    public UserDTO getUserInfoById(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 脱敏，不返回密码
        return UserConvert.INSTANCE.toDTO(user);
    }

    @Override
    public void updateUserInfo(Long loginUserId, UserDTO userDTO) {
        User user = baseMapper.selectById(loginUserId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 仅更新允许修改的字段
        User update = new User();
        update.setId(loginUserId);
        update.setNickname(userDTO.getNickname());
        update.setPhone(userDTO.getPhone());
        update.setEmail(userDTO.getEmail());
        update.setAvatar(userDTO.getAvatar());
        baseMapper.updateById(update);
    }

    @Override
    public void updatePassword(Long loginUserId, String oldPwd, String newPwd) {
        User user = baseMapper.selectById(loginUserId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 校验原密码
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BusinessException(400, "原密码输入错误");
        }
        // 加密新密码更新
        User update = new User();
        update.setId(loginUserId);
        update.setPassword(passwordEncoder.encode(newPwd));
        baseMapper.updateById(update);
    }

    @Override
    public IPage<UserDTO> getUserPage(Page<User> page, String username, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        // 分页查询原始用户数据
        IPage<User> userPage = baseMapper.selectPage(page, wrapper);
        // 转换为DTO分页返回（脱敏去除password）
        return userPage.convert(UserConvert.INSTANCE::toDTO);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "状态参数非法，仅支持0禁用/1启用");
        }
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        User update = new User();
        update.setId(userId);
        update.setStatus(status);
        baseMapper.updateById(update);
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("auth:token:" + token);
    }

    @Override
    public List<UserInfoDTO> listUserInfoByIds(List<Long> userIds) {
        // 1. 防御性空校验：避免直接执行 SQL 触发 MyBatis-Plus 语法异常
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 物理批量拉取：利用主键 IN 索引，性能达到极致 (SELECT * FROM sys_user WHERE id IN (...))
        // 注意：由于你的实体类是 User（或者是 SysUser），这里直接使用当前 Service 对应的泛型实例
        List<User> userList = this.listByIds(userIds);

        if (userList == null || userList.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 内存流极速脱敏清洗与对象转换
        return userList.stream()
                .filter(user -> user.getStatus() != null && user.getStatus() == 1) // 🌟 核心过滤：只保留账号状态为 1（正常）的用户
                .map(user -> {
                    UserInfoDTO dto = new UserInfoDTO();
                    dto.setId(user.getId());
                    dto.setNickname(user.getNickname());
                    dto.setAvatar(user.getAvatar());
                    dto.setPhone(user.getPhone());
                    // 🔒 绝对不返回 password、email 等敏感字段，实现服务间的高级数据脱敏
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long validateTokenAndGetUserId(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }

        // 示例：Redis存储Token的场景，key前缀为 login:token:
        String userIdStr = redisTemplate.opsForValue().get("login:token:" + token);
        if (StrUtil.isBlank(userIdStr)) {
            return null;
        }
        return Long.parseLong(userIdStr);

        // JWT方案可替换为：
        // try {
        //     Claims claims = JwtUtil.parseToken(token);
        //     return claims.get("userId", Long.class);
        // } catch (Exception e) {
        //     return null;
        // }
    }

}