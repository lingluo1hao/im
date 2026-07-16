package com.im.groupservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_group")
public class ImGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupName;
    private Long ownerId;
    private String groupDesc;
    private String groupAvatar;
    private Integer isAllMute;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}