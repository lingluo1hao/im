package com.im.friendservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.friendservice.entity.FriendRelation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendRelationMapper extends BaseMapper<FriendRelation> {
    // 继承 BaseMapper 后，自动拥有针对 im_friend_relation 表的全套单表 CRUD 能力
}
