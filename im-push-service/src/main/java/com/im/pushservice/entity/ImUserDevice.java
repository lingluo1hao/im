package com.im.pushservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 用户设备实体
@Data
@TableName("im_user_device")
public class ImUserDevice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String deviceId;
    private Integer deviceType;
    private Integer pushChannel;
    private String deviceToken;
    private Integer isPushEnabled;
    private LocalDateTime lastOnlineTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}