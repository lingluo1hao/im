package com.im.common.context;

import com.im.common.dto.UserInfoDTO;

public class UserContext {
    private static final ThreadLocal<UserInfoDTO> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(UserInfoDTO userInfo) {
        THREAD_LOCAL.set(userInfo);
    }

    public static UserInfoDTO get() {
        return THREAD_LOCAL.get();
    }

    public static Long getUserId() {
        UserInfoDTO user = THREAD_LOCAL.get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}