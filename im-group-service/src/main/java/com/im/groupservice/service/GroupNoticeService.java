package com.im.groupservice.service;


import com.im.groupservice.dto.GroupNoticeReqDTO;
import com.im.groupservice.entity.ImGroupNotice;

import java.util.List;

public interface GroupNoticeService {

    /**
     * 发布群公告
     */
    Long publishNotice(GroupNoticeReqDTO dto);

    /**
     * 删除群公告
     */
    void deleteNotice(Long groupId, Long noticeId);

    /**
     * 查询群公告列表
     */
    List<ImGroupNotice> getNoticeList(Long groupId);
}