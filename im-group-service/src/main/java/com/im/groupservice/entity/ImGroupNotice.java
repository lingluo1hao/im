package com.im.groupservice.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_group_notice")
public class ImGroupNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 公告内容
     */
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}