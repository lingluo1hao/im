package com.im.pushservice.dto;

import lombok.Data;

import java.util.List;

// 批量推送入参
@Data
public class BatchPushDTO {
    private List<Long> userIds;
    private String title;
    private String content;
}