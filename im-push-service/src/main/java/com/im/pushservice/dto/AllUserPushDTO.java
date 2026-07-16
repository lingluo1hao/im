package com.im.pushservice.dto;

import lombok.Data;

// 全员推送入参
@Data
public class AllUserPushDTO {
    private String title;
    private String content;
}