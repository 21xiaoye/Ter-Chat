package com.cabin.ter.listener;

import com.cabin.ter.adapter.MQMessageBuilderAdapter;
import com.cabin.ter.adapter.MemberAdapter;
import com.cabin.ter.adapter.MessageAdapter;
import com.cabin.ter.admin.domain.UserDomain;
import com.cabin.ter.cache.GroupMemberCache;
import com.cabin.ter.cache.UserInfoCache;
import com.cabin.ter.chat.domain.GroupMemberDomain;
import com.cabin.ter.chat.domain.GroupRoomDomain;
import com.cabin.ter.config.ThreadPoolConfig;
import com.cabin.ter.constants.TopicConstant;
import com.cabin.ter.constants.response.WSBaseResp;
import com.cabin.ter.listener.event.GroupMemberAddEvent;
import com.cabin.ter.service.ChatService;
import com.cabin.ter.template.RocketMQEnhanceTemplate;
import com.cabin.ter.constants.request.ChatMessageReq;
import com.cabin.ter.constants.response.WSMemberChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaoye
 * @date Created in 2024-05-31 20:02
 */
@Slf4j
@Component
public class GroupMemberAddListener {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private RocketMQEnhanceTemplate rocketMQEnhanceTemplate;
    @Async(value = ThreadPoolConfig.WS_EXECUTOR)
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event){
        // 获取成员信息
        List<GroupMemberDomain> memberDomainList = event.getMemberDomainList();
        // 获取群组信息
        GroupRoomDomain groupRoomDomain = event.getGroupRoomDomain();
        // 获取用户userId
        Long inviteUid = event.getInviteUid();
        // 查询用户信息
        UserDomain userDomain = userInfoCache.get(inviteUid);
        // 拿到群组成员uid列表
        List<Long> uidList = memberDomainList.stream().map(GroupMemberDomain::getUserId).collect(Collectors.toList());
        Map<Long, UserDomain> userDomainMap = userInfoCache.getBatch(uidList);
        ChatMessageReq chatMessageReq = MessageAdapter.buildGroupAddMessage(groupRoomDomain, userDomain, userDomainMap);
        chatService.sendMsg(chatMessageReq, inviteUid);
    }

    @Async(value = ThreadPoolConfig.WS_EXECUTOR)
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event){
        List<GroupMemberDomain> memberDomainList = event.getMemberDomainList();
        GroupRoomDomain groupRoomDomain = event.getGroupRoomDomain();

        List<Long> uidList = memberDomainList.stream().map(GroupMemberDomain::getUserId).collect(Collectors.toList());
        Map<Long, UserDomain> userDomainMap = userInfoCache.getBatch(uidList);

        List<UserDomain> userDomainList = userDomainMap.values().stream().toList();

        userDomainList.forEach(user -> {
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberAddWS(groupRoomDomain.getRoomId(), user);
            rocketMQEnhanceTemplate.sendSecureMsg(TopicConstant.CHAT_MESSAGE_SEND_TOPIC, MQMessageBuilderAdapter.buildChatMessageDTO(ws, Collections.singletonList(user.getUserId())));
        });
        //移除缓存
        groupMemberCache.evictMemberUidList(groupRoomDomain.getRoomId());
    }

}
