package com.im.friendservice.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.im.common.context.UserContext;
import com.im.common.result.R;
import com.im.friendservice.entity.FriendBlacklist;
import com.im.friendservice.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friend/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    /**
     * 拉黑用户（自动解除双向好友关系，清理缓存）
     */
    @PostMapping("/add")
    public R<?> addBlacklist(@RequestParam Long blackUserId) {
        blacklistService.addBlacklist(blackUserId);
        return R.ok("已将对方加入黑名单并解除好友关系");
    }

    /**
     * 移出黑名单
     */
    @DeleteMapping("/remove")
    public R<?> removeBlacklist(@RequestParam Long blackUserId) {
        blacklistService.removeBlacklist(blackUserId);
        return R.ok("已将对方移出黑名单");
    }

    /**
     * 查询当前登录用户的全部黑名单用户ID列表
     */
    @GetMapping("/ids")
    public R<List<Long>> getBlacklistIds() {
        Long userId = UserContext.getUserId();
        List<FriendBlacklist> list = blacklistService.list(new LambdaQueryWrapper<FriendBlacklist>()
                .eq(FriendBlacklist::getUserId, userId));

        List<Long> blackIds = list.stream()
                .map(FriendBlacklist::getBlackUserId)
                .collect(Collectors.toList());
        return R.ok(blackIds);
    }
}
