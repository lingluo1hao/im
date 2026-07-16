package com.im.groupservice.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MuteUserReqDTO {

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 被禁言/解除禁言的目标用户ID
     */
    private Long userId;

    /**
     * 禁言状态：0-解除禁言，1-开启禁言
     */
    private Integer isMuted;

    /**
     * 禁言到期时间；永久禁言可传null，需业务层自行判断
     */
    private LocalDateTime muteExpireTime;
}