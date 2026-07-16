package com.im.friendservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.im.friendservice.entity.FriendRelation;
import java.util.Set;

public interface FriendService extends IService<FriendRelation> {

    /**
     * 获取用户的所有好友ID列表（走旁路缓存）
     */
    Set<Long> getFriendIds(Long userId);

    /**
     * 双向删除好友关系（逻辑删除，状态置为2）
     */
    void removeFriend(Long friendId);

    /**
     * 修改好友备注
     */
    void updateRemark(Long friendId, String remark);
}
