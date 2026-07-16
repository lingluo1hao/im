package com.im.friendservice.dto;

import lombok.Data;

@Data
public class FriendInfoRespDTO {
    private Long friendId;
    private String remark;
    private String nickname;
    private String avatar;
    private String phone;
}
