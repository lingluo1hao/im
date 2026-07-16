# IM-Server 千万级分布式即时通讯系统
技术栈：SpringBoot 3.5.15 | JDK 21 | Netty | Protobuf | Spring Cloud 微服务

## 项目简介
面向千万级在线用户的分布式IM平台
- 支持单聊、群聊、消息撤回、离线消息、在线状态同步
- TCP长连接通讯，采用Protobuf二进制协议
- 用户登录、RBAC权限控制、多层限流熔断
- 微服务拆分，支持水平扩容，消息异步可靠投递

![系统架构图](./doc/img/arch-overview.png)

## 技术栈清单
|分类|组件|版本|
|----|----|----|
|核心框架|Spring Boot|3.5.15|
|运行环境|OpenJDK|21|
|长连接通讯|Netty|4.2.x|
|序列化协议|Protobuf|3.25.x|
|微服务框架|Spring Cloud|2024.0.x|
|API网关|Spring Cloud Gateway|配套版本|
|注册配置中心|Nacos|2.3.2|
|消息队列|RocketMQ|5.3.x|
|缓存|Redis|7.2.x|
|数据库|MySQL|8.4 LTS|
|限流熔断组件|Sentinel|适配SpringBoot3版本|

## 微服务模块划分

## 流量分层模型
1. **HTTP业务流量**
客户端 → Gateway → 后端微服务（登录、好友、群组操作）
2. **TCP长连接流量**
客户端 → 四层负载均衡 → Netty集群
> TCP流量不经过Gateway，避免海量长连接造成网关性能瓶颈

## 通讯协议规范
TCP报文结构：`4字节总长度 + 2字节指令码 + Protobuf消息体`
- 4字节长度：用于解决TCP粘包、拆包问题
- 2字节指令码：区分不同消息类型
- Protobuf二进制：业务消息实体

核心指令定义：
- CMD_AUTH：长连接登录鉴权
- CMD_HEARTBEAT：客户端心跳保活
- CMD_CHAT_SINGLE：单聊消息
- CMD_CHAT_GROUP：群聊消息
- CMD_MSG_RECALL：消息撤回指令
- CMD_ACK：消息接收回执

## 消息完整流转流程
客户端发送消息
→ Netty服务权限校验、流量限流
→ 生成全局唯一MsgId（雪花算法）
→ 投递RocketMQ异步队列
→ 消费服务持久化消息至MySQL
→ 查询Redis路由表定位接收用户所在Netty节点
→ 在线用户实时推送 / 离线消息缓存，等待上线补发

## 本地开发环境部署指南
虚拟机系统：Rocky Linux 10.2 Minimal ISO
虚拟化软件：VMware Workstation 17
虚拟机硬件配置：4核CPU / 8G内存 / 40G SATA SSD / 桥接模式静态IP
预装中间件：Nacos、MySQL8.4、Redis7、RocketMQ5.x

> VMware安装Rocky Linux10关键配置
> 新建虚拟机系统选择 `RHEL9 64位`，固件设置为BIOS，磁盘类型选择SATA

## 服务启动顺序
1. 启动中间件：Nacos → MySQL → Redis → RocketMQ
2. 启动网关服务 im-gateway
3. 启动基础业务服务：user、auth、friend、group
4. 启动核心服务：im-netty-server、im-message-service、im-push-service

## 常见踩坑记录
1. VMware报错 `Unable to find the VMX binary`
解决方案：创建虚拟机选择RHEL9 64位，固件切换为BIOS模式
2. Rocky Linux安装界面识别不到硬盘
解决方案：磁盘类型选择SATA，禁止使用NVMe磁盘
3. Netty开发注意事项
TCP通道建立后，首条消息强制为鉴权包；必须添加长度域解码器处理TCP粘包拆包

## 开发迭代规划
1. 基础工程搭建、Nacos集成、Gateway网关、登录鉴权体系
2. Netty基础服务开发、Protobuf编解码、心跳保活机制
3. 实现单聊、群聊基础消息投递逻辑
4. 接入RocketMQ，实现异步消息、离线消息补发
5. 多层限流开发、Netty集群会话路由改造
6. 性能压测、MySQL分库分表落地优化

## License
MIT
