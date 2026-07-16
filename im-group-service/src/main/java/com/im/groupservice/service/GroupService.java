package com.im.groupservice.service;


import com.im.groupservice.dto.CreateGroupReqDTO;
import com.im.groupservice.entity.ImGroup;

public interface GroupService {

    /**
     * 创建群组
     */
    Long createGroup(CreateGroupReqDTO dto);

    /**
     * 设置/解除全群禁言
     */
    void setAllMute(Long groupId, Integer isAllMute);

    /**
     * 解散群组
     */
    void dissolveGroup(Long groupId);

    /**
     * 根据ID查询群组信息
     */
    ImGroup getGroupById(Long groupId);
}