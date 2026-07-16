package com.im.friendservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("im_friend_apply")
public class FriendApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String applyMsg; // 精准适配字段：apply_msg
    private Integer status;  // 状态：0待处理 1已同意 2已拒绝 3已过期
    private LocalDateTime handleTime; // 处理时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
