package com.im.groupservice.dto;

import lombok.Data;

@Data
public class CreateGroupReqDTO {

    /**
     * 群名称（必填）
     */
    private String groupName;

    /**
     * 群简介（选填）
     */
    private String groupDesc;

    /**
     * 群头像地址（选填）
     */
    private String groupAvatar;
}