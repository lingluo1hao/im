package com.im.friendservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.im.common.context.UserContext;
import com.im.common.dto.UserInfoDTO;
import com.im.common.result.R;
import com.im.friendservice.dto.FriendInfoRespDTO;
import com.im.friendservice.entity.FriendRelation;
import com.im.friendservice.feign.UserFeignClient;
import com.im.friendservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserFeignClient userFeignClient;

    /**
     * 查询当前用户的完整好友列表（强类型出参）
     */
    @GetMapping("/list")
    public R<List<FriendInfoRespDTO>> getFriendList() {
        Long userId = UserContext.getUserId();

        List<FriendRelation> relations = friendService.list(new LambdaQueryWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getStatus, 1));

        if (relations.isEmpty()) {
            return R.ok(Collections.emptyList());
        }

        List<Long> friendIds = relations.stream()
                .map(FriendRelation::getFriendId)
                .collect(Collectors.toList());

        Map<Long, UserInfoDTO> userMap = Collections.emptyMap();
        try {
            R<Map<Long, UserInfoDTO>> feignResult = userFeignClient.getUserInfoMapByIds(friendIds);
            if (feignResult != null && feignResult.getCode() == 200 && feignResult.getData() != null) {
                userMap = feignResult.getData();
            }
        } catch (Exception e) {
            // 容灾保持不变
        }

        // 🌟 强类型数据装配，彻底干掉冗余 Map
        List<FriendInfoRespDTO> resultList = new ArrayList<>();
        for (FriendRelation rel : relations) {
            FriendInfoRespDTO respDTO = new FriendInfoRespDTO();
            Long fId = rel.getFriendId();

            respDTO.setFriendId(fId);
            respDTO.setRemark(rel.getRemark());

            UserInfoDTO userInfo = userMap.get(fId);
            if (userInfo != null) {
                respDTO.setNickname(userInfo.getNickname());
                respDTO.setAvatar(userInfo.getAvatar());
                respDTO.setPhone(userInfo.getPhone());
            } else {
                respDTO.setNickname("未知用户");
                respDTO.setAvatar("");
                respDTO.setPhone("");
            }
            resultList.add(respDTO);
        }

        return R.ok(resultList);
    }

    @GetMapping("/ids")
    public R<Set<Long>> getFriendIds() {
        Long userId = UserContext.getUserId();
        return R.ok(friendService.getFriendIds(userId));
    }

    @DeleteMapping("/remove")
    public R<?> removeFriend(@RequestParam Long friendId) {
        friendService.removeFriend(friendId);
        return R.ok("好友删除成功");
    }

    @PutMapping("/remark")
    public R<?> updateRemark(@RequestParam Long friendId, @RequestParam String remark) {
        friendService.updateRemark(friendId, remark);
        return R.ok("备注修改成功");
    }
}
