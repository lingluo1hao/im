package com.im.groupservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_group_member")
public class ImGroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private Integer role;
    private Integer isMuted;
    private LocalDateTime muteExpireTime;
    private LocalDateTime joinTime;
    private LocalDateTime lastMsgTime;
}