package com.im.messageservice.util;

// 会话ID生成工具：单聊保证双方ID一致
public class ConversationIdUtil {
    /**
     * 生成单聊会话ID：小ID在前，大ID在后，保证双向同一个会话
     */
    public static String buildPrivateId(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    /**
     * 生成群聊会话ID：直接用群ID
     */
    public static String buildGroupId(Long groupId) {
        return "g_" + groupId;
    }
}