package com.im.common.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserInfoDTO implements Serializable {
    private Long id;
    private String username;
    private String nickname;
    private Integer status;
    private String phone;
    private String avatar;
}