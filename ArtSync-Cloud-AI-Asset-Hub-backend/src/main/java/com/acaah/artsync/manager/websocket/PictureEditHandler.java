package com.acaah.artsync.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.acaah.artsync.manager.websocket.disruptor.PictureEditEventProducer;
import com.acaah.artsync.manager.websocket.model.PictureEditActionEnum;
import com.acaah.artsync.manager.websocket.model.PictureEditMessageTypeEnum;
import com.acaah.artsync.manager.websocket.model.PictureEditRequestMessage;
import com.acaah.artsync.manager.websocket.model.PictureEditResponseMessage;
import com.acaah.artsync.model.entity.User;
import com.acaah.artsync.service.UserService;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片编辑 WebSocket 处理器
 */
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();


    @Resource
    private UserService userService;


    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);

        // 构造响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s加入编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        // 广播给同一张图片的用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 收到前端发送的消息，根据消息类别处理消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        // 获取消息内容，将 JSON 转换为 PictureEditRequestMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        // 从 Session 属性中获取到公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 根据消息类型处理消息（生产消息到 Disruptor 环形队列中）
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);
    }


/**
 * 处理用户进入图片编辑状态的方法
 * @param pictureEditRequestMessage 图片编辑请求消息
 * @param session WebSocket会话
 * @param user 当前用户
 * @param pictureId 图片ID
 * @throws Exception 可能抛出的异常
 */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        // 没有用户正在编辑该图片，才能进入编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            // 设置当前用户为编辑用户
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("%s开始编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


/**
 * 处理图片编辑动作消息的方法
 * @param pictureEditRequestMessage 图片编辑请求消息对象
 * @param session WebSocket会话对象
 * @param user 用户对象
 * @param pictureId 图片ID
 * @throws Exception 可能抛出的异常
 */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
    // 获取当前图片的编辑者ID
        Long editingUserId = pictureEditingUsers.get(pictureId);
    // 获取编辑动作类型
        String editAction = pictureEditRequestMessage.getEditAction();
    // 将编辑动作字符串转换为对应的枚举值
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
    // 如果动作枚举为空，直接返回
        if (actionEnum == null) {
            return;
        }
        // 确认是当前编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
        // 创建编辑响应消息对象
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("%s执行%s", user.getUserName(), actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 广播给除了当前客户端之外的其他用户，否则会造成重复编辑
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }


    /**
     * 处理用户退出图片编辑的消息
     * @param pictureEditRequestMessage 图片编辑请求消息
     * @param session WebSocket会话
     * @param user 当前用户信息
     * @param pictureId 图片ID
     * @throws Exception 可能抛出的异常
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        // 获取当前正在编辑该图片的用户ID
        Long editingUserId = pictureEditingUsers.get(pictureId);
        // 检查当前用户是否是正在编辑该图片的用户
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 移除当前用户的编辑状态
            pictureEditingUsers.remove(pictureId);
            // 构造响应，发送退出编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            // 设置消息类型为退出编辑
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            // 设置退出编辑的消息内容
            String message = String.format("%s退出编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            // 设置用户信息
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 向所有正在查看该图片的用户广播退出编辑的消息
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


/**
 * 重写WebSocket连接关闭后的处理方法
 * 当WebSocket连接关闭时，此方法会被调用
 *
 * @param session WebSocket会话对象，包含了连接的相关信息
 * @param status 关闭状态，包含了连接关闭的原因和状态码
 * @throws Exception 可能抛出的异常
 */

    @Override
    // 调用父类的afterConnectionClosed方法，确保父类的处理逻辑能够执行
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        // 从会话中获取属性，包括图片ID和用户信息
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User user = (User) attributes.get("user");
        // 移除当前用户的编辑状态，处理退出编辑的消息
        handleExitEditMessage(null, session, user, pictureId);

        // 删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }

        // 响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }




    /**
 * 向指定图片的所有连接会话广播编辑消息
 * @param pictureId 图片ID，用于定位相关的WebSocket会话
 * @param pictureEditResponseMessage 要广播的图片编辑响应消息
 * @param excludeSession 不需要广播的WebSocket会话（排除自己）
 * @throws Exception 可能抛出的异常
 */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
    // 获取与该图片关联的所有WebSocket会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
    // 如果会话集合不为空
        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper 用于对象序列化
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
        // 添加Long类型的序列化器，将Long转为String
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }


    // 全部广播
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }


}
