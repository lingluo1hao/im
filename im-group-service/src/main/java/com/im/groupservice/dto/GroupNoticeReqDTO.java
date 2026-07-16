package com.im.groupservice.dto;


import lombok.Data;

@Data
public class GroupNoticeReqDTO {

    private Long groupId;

    private String content;
}