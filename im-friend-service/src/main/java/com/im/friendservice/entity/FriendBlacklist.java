package com.im.friendservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("im_friend_blacklist")
public class FriendBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long blackUserId;
    private LocalDateTime createTime;
}
