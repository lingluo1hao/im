package com.im.groupservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.im.groupservice.dto.MuteUserReqDTO;
import com.im.groupservice.entity.ImGroupMember;

public interface GroupMemberService extends IService<ImGroupMember> {

    /**
     * 校验是否为群主
     */
    void checkGroupOwner(Long groupId, Long userId);

    /**
     * 校验是否为管理员及以上（群主+管理员）
     */
    void checkGroupAdmin(Long groupId, Long userId);

    /**
     * 获取群成员信息，不在群内则抛出异常
     */
    ImGroupMember getMember(Long groupId, Long userId);

    /**
     * 单人禁言 / 解除禁言
     */
    void muteUser(MuteUserReqDTO dto);

    /**
     * 设置 / 取消管理员
     */
    void setAdmin(Long groupId, Long userId, Integer role);

    /**
     * 移除群成员
     */
    void removeMember(Long groupId, Long userId);
}