package com.im.friendservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFriendReqDTO {
    @NotNull(message = "被申请人ID不能为空")
    private Long toUserId;

    private String applyMsg; // 申请留言，对应表里的 apply_msg
}
