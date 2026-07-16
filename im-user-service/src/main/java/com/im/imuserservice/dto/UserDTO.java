package com.im.imuserservice.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    /** 账号状态 0-禁用 1-正常 */
    private Integer status;
    private LocalDateTime createTime;
}
