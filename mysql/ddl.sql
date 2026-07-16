CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(50) NOT NULL COMMENT '账号',
                            `password` varchar(100) NOT NULL COMMENT '加密密码',
                            `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
                            `phone` varchar(20) DEFAULT '' COMMENT '手机号',
                            `email` varchar(100) DEFAULT '' COMMENT '邮箱',
                            `avatar` varchar(255) DEFAULT '' COMMENT '头像',
                            `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态 0禁用 1正常',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY uk_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `im_friend_relation` (
                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                      `user_id` bigint NOT NULL COMMENT '用户ID',
                                      `friend_id` bigint NOT NULL COMMENT '好友ID',
                                      `remark` varchar(50) DEFAULT '' COMMENT '好友备注',
                                      `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1正常 2已删除',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_user_friend` (`user_id`,`friend_id`),
                                      KEY `idx_user_id` (`user_id`)
) COMMENT='好友关系表';

CREATE TABLE `im_friend_apply` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `from_user_id` bigint NOT NULL COMMENT '申请人ID',
                                   `to_user_id` bigint NOT NULL COMMENT '被申请人ID',
                                   `apply_msg` varchar(100) DEFAULT '' COMMENT '申请留言',
                                   `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待处理 1已同意 2已拒绝 3已过期',
                                   `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_from_to` (`from_user_id`,`to_user_id`,`status`),
                                   KEY `idx_to_user` (`to_user_id`,`status`)
) COMMENT='好友申请表';


CREATE TABLE `im_friend_blacklist` (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `user_id` bigint NOT NULL COMMENT '用户ID',
                                       `black_user_id` bigint NOT NULL COMMENT '被拉黑用户ID',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_user_black` (`user_id`,`black_user_id`),
                                       KEY `idx_user_id` (`user_id`)
) COMMENT='好友黑名单表';












-- 1. 群组主表
CREATE TABLE `im_group` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '群组ID',
                            `group_name` varchar(50) NOT NULL COMMENT '群名称',
                            `owner_id` bigint NOT NULL COMMENT '群主ID',
                            `group_desc` varchar(200) DEFAULT '' COMMENT '群简介',
                            `group_avatar` varchar(255) DEFAULT '' COMMENT '群头像',
                            `is_all_mute` tinyint NOT NULL DEFAULT '0' COMMENT '全群禁言：0否 1是',
                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1正常 2已解散',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            KEY `idx_owner_id` (`owner_id`)
) COMMENT='群组主表';

-- 2. 群成员表
CREATE TABLE `im_group_member` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `group_id` bigint NOT NULL COMMENT '群组ID',
                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                   `role` tinyint NOT NULL DEFAULT '3' COMMENT '角色：1群主 2管理员 3普通成员',
                                   `is_muted` tinyint NOT NULL DEFAULT '0' COMMENT '单人禁言：0否 1是',
                                   `mute_expire_time` datetime DEFAULT NULL COMMENT '禁言到期时间',
                                   `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                   `last_msg_time` datetime DEFAULT NULL COMMENT '最后发言时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
                                   KEY `idx_group_id` (`group_id`),
                                   KEY `idx_user_id` (`user_id`)
) COMMENT='群成员表';

-- 3. 群公告表
CREATE TABLE `im_group_notice` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `group_id` bigint NOT NULL COMMENT '群组ID',
                                   `publisher_id` bigint NOT NULL COMMENT '发布人ID',
                                   `content` varchar(500) NOT NULL COMMENT '公告内容',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   KEY `idx_group_id` (`group_id`)
) COMMENT='群公告表';



-- 1. 消息主表：全量消息持久化
CREATE TABLE `im_message` (
                              `id` bigint NOT NULL COMMENT '消息ID（雪花算法，全局时序递增）',
                              `conversation_id` varchar(64) NOT NULL COMMENT '会话ID：单聊=双方ID拼接，群聊=群ID',
                              `conversation_type` tinyint NOT NULL COMMENT '会话类型：1单聊 2群聊',
                              `sender_id` bigint NOT NULL COMMENT '发送方用户ID',
                              `target_id` bigint NOT NULL COMMENT '接收目标：单聊=用户ID，群聊=群ID',
                              `msg_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型：1文本 2图片 3语音 4系统消息',
                              `content` text NOT NULL COMMENT '消息内容',
                              `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1正常 2已撤回',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_conversation_time` (`conversation_id`,`create_time`),
                              KEY `idx_sender_id` (`sender_id`),
                              KEY `idx_target_id` (`target_id`)
) COMMENT='消息主表';

-- 2. 用户会话表：每个用户的会话列表，冗余未读数与最后消息
CREATE TABLE `im_conversation` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                   `conversation_id` varchar(64) NOT NULL COMMENT '会话ID',
                                   `conversation_type` tinyint NOT NULL COMMENT '会话类型：1单聊 2群聊',
                                   `target_id` bigint NOT NULL COMMENT '对方ID：单聊=用户ID，群聊=群ID',
                                   `last_msg_id` bigint DEFAULT NULL COMMENT '最后一条消息ID',
                                   `last_msg_content` varchar(500) DEFAULT '' COMMENT '最后一条消息预览',
                                   `unread_count` int NOT NULL DEFAULT '0' COMMENT '未读消息数',
                                   `last_msg_time` datetime DEFAULT NULL COMMENT '最后消息时间',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_user_conversation` (`user_id`,`conversation_id`),
                                   KEY `idx_user_time` (`user_id`,`last_msg_time`)
) COMMENT='用户会话表';

-- 3. 会话已读位置表：记录每个用户在会话中的最后已读消息ID
CREATE TABLE `im_conversation_read` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `conversation_id` varchar(64) NOT NULL COMMENT '会话ID',
                                        `user_id` bigint NOT NULL COMMENT '用户ID',
                                        `last_read_msg_id` bigint NOT NULL DEFAULT '0' COMMENT '最后已读消息ID',
                                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uk_conversation_user` (`conversation_id`,`user_id`)
) COMMENT='会话已读位置表';





-- 1. 用户设备绑定表：存储用户终端设备信息与推送凭证
CREATE TABLE `im_user_device` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `device_id` varchar(128) NOT NULL COMMENT '设备唯一标识',
                                  `device_type` tinyint NOT NULL COMMENT '设备类型：1安卓 2iOS 3Web 4PC',
                                  `push_channel` tinyint NOT NULL COMMENT '推送通道：1极光 2个推 3华为 4小米 5苹果APNs',
                                  `device_token` varchar(255) NOT NULL COMMENT '厂商推送令牌',
                                  `is_push_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否开启推送：0关闭 1开启',
                                  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_user_device` (`user_id`,`device_id`),
                                  KEY `idx_device_token` (`device_token`),
                                  KEY `idx_user_id` (`user_id`)
) COMMENT='用户设备绑定表';

-- 2. 推送任务表：全员/批量推送任务，异步执行
CREATE TABLE `im_push_task` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `task_title` varchar(128) NOT NULL COMMENT '推送标题',
                                `task_content` varchar(500) NOT NULL COMMENT '推送内容',
                                `push_type` tinyint NOT NULL COMMENT '推送类型：1单用户 2批量 3全员',
                                `target_ids` text COMMENT '目标用户ID集合，逗号分隔；全员推送为空',
                                `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待执行 1执行中 2已完成 3失败',
                                `total_count` int NOT NULL DEFAULT '0' COMMENT '目标总数',
                                `success_count` int NOT NULL DEFAULT '0' COMMENT '成功数',
                                `fail_count` int NOT NULL DEFAULT '0' COMMENT '失败数',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `finish_time` datetime DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `idx_status` (`status`)
) COMMENT='推送任务表';

-- 3. 推送记录表：单条推送明细，用于幂等与回溯
CREATE TABLE `im_push_record` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `task_id` bigint DEFAULT NULL COMMENT '所属任务ID，单推可为空',
                                  `user_id` bigint NOT NULL COMMENT '目标用户ID',
                                  `device_token` varchar(255) DEFAULT NULL,
                                  `push_channel` tinyint NOT NULL,
                                  `title` varchar(128) DEFAULT NULL,
                                  `content` varchar(500) NOT NULL,
                                  `biz_msg_id` bigint DEFAULT NULL COMMENT '业务消息ID，用于幂等',
                                  `status` tinyint NOT NULL COMMENT '状态：1推送中 2成功 3失败',
                                  `fail_reason` varchar(255) DEFAULT NULL,
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_biz_msg_user` (`biz_msg_id`,`user_id`),
                                  KEY `idx_user_id` (`user_id`),
                                  KEY `idx_task_id` (`task_id`)
) COMMENT='推送记录表';


CREATE TABLE `im_task_log` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `task_code` varchar(64) NOT NULL COMMENT '任务编码',
                               `task_name` varchar(128) NOT NULL COMMENT '任务名称',
                               `status` tinyint NOT NULL COMMENT '执行状态：1执行中 2成功 3失败',
                               `cost_time` bigint DEFAULT NULL COMMENT '耗时（毫秒）',
                               `fail_reason` text COMMENT '失败原因',
                               `start_time` datetime NOT NULL COMMENT '开始时间',
                               `end_time` datetime DEFAULT NULL COMMENT '结束时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_task_code` (`task_code`),
                               KEY `idx_start_time` (`start_time`)
) COMMENT='定时任务执行日志表';