package com.im.imuserservice.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.im.common.dto.UserInfoDTO;
import com.im.imuserservice.dto.LoginDTO;
import com.im.imuserservice.dto.RegisterDTO;
import com.im.imuserservice.dto.UserDTO;
import com.im.imuserservice.entity.User;

import java.util.List;

public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录，返回JWT令牌
     */
    String login(LoginDTO loginDTO);

    /**
     * 根据用户ID查询用户信息（脱敏）
     */
    UserDTO getUserInfoById(Long userId);

    /**
     * 修改当前登录用户个人信息
     */
    void updateUserInfo(Long loginUserId, UserDTO userDTO);

    /**
     * 修改用户密码
     * @param loginUserId 当前登录人ID
     * @param oldPwd 原密码
     * @param newPwd 新密码
     */
    void updatePassword(Long loginUserId, String oldPwd, String newPwd);

    /**
     * 用户分页列表（后台管理）
     */
    IPage<UserDTO> getUserPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page,
                               String username,
                               Integer status);

    /**
     * 启用/禁用账号状态
     */
    void updateStatus(Long userId, Integer status);

    void logout(String token);

    /**
     * ⚡ 新增：根据一组用户 ID 集合批量查询用户信息（供内部 RPC 聚合调用）
     * @param userIds 用户 ID 集合
     * @return 状态正常的用户 DTO 列表
     */
    List<UserInfoDTO> listUserInfoByIds(java.util.List<Long> userIds);


    /**
     * 校验Token有效性，返回对应用户ID；无效返回null
     */
    Long validateTokenAndGetUserId(String token);

}