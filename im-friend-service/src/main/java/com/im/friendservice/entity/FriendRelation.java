package com.im.friendservice.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("im_friend_relation")
public class FriendRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long friendId;
    private String remark;
    private Integer status; // 状态：1正常 2已删除
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
