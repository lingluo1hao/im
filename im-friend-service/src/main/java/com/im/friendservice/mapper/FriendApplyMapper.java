package com.im.friendservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.im.friendservice.entity.FriendApply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendApplyMapper extends BaseMapper<FriendApply> {
    // 继承 BaseMapper 后，自动拥有针对 im_friend_apply 表的全套单表 CRUD 能力
}
