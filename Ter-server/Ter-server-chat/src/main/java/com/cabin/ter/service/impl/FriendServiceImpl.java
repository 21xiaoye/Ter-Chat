package com.cabin.ter.service.impl;

import com.cabin.ter.adapter.ChatAdapter;
import com.cabin.ter.adapter.MessageAdapter;
import com.cabin.ter.adapter.RoomAdapter;
import com.cabin.ter.cache.RoomFriendCache;
import com.cabin.ter.chat.domain.FriendApplyDomain;
import com.cabin.ter.admin.domain.UserDomain;
import com.cabin.ter.chat.domain.FriendRoomDomain;
import com.cabin.ter.chat.mapper.FriendApplyDomainMapper;
import com.cabin.ter.cache.UserInfoCache;
import com.cabin.ter.chat.mapper.FriendRoomDomainMapper;
import com.cabin.ter.constants.response.FriendApplyRecordInfoResp;
import com.cabin.ter.constants.response.FriendApplyResp;
import com.cabin.ter.listener.event.FriendApplyEvent;
import com.cabin.ter.service.ChatService;
import com.cabin.ter.service.FriendService;
import com.cabin.ter.service.RoomService;
import com.cabin.ter.util.AsserUtil;
import com.cabin.ter.constants.request.ApprovalFriendReq;
import com.cabin.ter.constants.request.FriendApplyReq;
import com.cabin.ter.constants.request.WhiteReq;
import com.cabin.ter.constants.response.FriendResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *     好友服务
 * </p>
 *
 * @author xiaoye
 * @date Created in 2024-05-27
 */
@Service
public class FriendServiceImpl implements FriendService {
    @Autowired
    private FriendApplyDomainMapper friendApplyDomainMapper;
    @Autowired
    private FriendRoomDomainMapper friendRoomDomainMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RoomService roomService;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private ChatService chatService;
    @Autowired
    private RoomFriendCache roomFriendCache;
    @Override
    public FriendApplyResp apply(Long userId, FriendApplyReq request) {
        FriendRoomDomain friendship = friendRoomDomainMapper.getFriendship(userId, request.getTargetId());
        // 创建好友申请记录
        FriendApplyDomain friendApplyDomain = RoomAdapter.buildFriendApplyDomain(userId, request);;
        // 在此之前已经添加过好友，但被删除,这里恢复房间状态就行
        if(Objects.nonNull(friendship)){
            if( Objects.equals(FriendRoomDomain.FRIENDSHIP_DELETE, friendship.getRoomStatus())){
                operateFriendStatus(userId, request.getTargetId(), FriendRoomDomain.FRIENDSHIP_RECOVER);
            }
            if(Objects.equals(FriendRoomDomain.FRIENDSHIP_BLOCK, friendship.getRoomStatus())) {
                return MessageAdapter.buildFriendApplyResp("你已被拉黑");
            }
            if(Objects.equals(FriendRoomDomain.FRIENDSHIP_RECOVER, friendship.getRoomStatus())){
                return MessageAdapter.buildFriendApplyResp("你们已经是好友了");
            }
        }else{
            friendApplyDomainMapper.saveFriendApplyRecord(friendApplyDomain);
            roomService.createFriend(userId, request.getTargetId(), request.getRemark());
        }
        UserDomain userInfo = userInfoCache.getUserInfo(userId);
        FriendApplyRecordInfoResp friendApplyResp = MessageAdapter.buildFriendApplyResp(
                userInfo,
                friendApplyDomain.getApplyId(),
                friendApplyDomain.getApplyStatus(),
                friendApplyDomain.getApplyMessage(),
                FriendApplyRecordInfoResp.TARGET_APPLY
        );
        applicationEventPublisher.publishEvent(new FriendApplyEvent(this,friendApplyResp, request.getTargetId()));
        return MessageAdapter.buildFriendApplyResp("好友申请发送成功");
    }

    @Override
    public List<FriendApplyRecordInfoResp> getFriendApplyRecord(Long userId) {
        List<FriendApplyDomain> friendApplyDomainList = friendApplyDomainMapper.getFriendApplyRecord(userId);
        return friendApplyDomainList.stream()
                .map(friendApply -> {
                    // true 其它人发给用户的好友申请 false用户发出的好友申请
                    boolean equals = Objects.equals(friendApply.getTargetId(), userId);
                    Long friendId =  equals ? friendApply.getUserId() : friendApply.getTargetId();
                    UserDomain userInfo = userInfoCache.getUserInfo(friendId);
                    return MessageAdapter.buildFriendApplyResp(
                            userInfo,
                            friendApply.getApplyId(),
                            friendApply.getApplyStatus(),
                            friendApply.getApplyMessage(),
                            equals ? FriendApplyRecordInfoResp.TARGET_APPLY : FriendApplyRecordInfoResp.USER_APPLY
                    );
                })
                .collect(Collectors.toList());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeFriendApply(ApprovalFriendReq approvalFriendReq,Long userId) {
        // 删除好友申请记录
        if(Objects.equals(approvalFriendReq.getApplyStatus(), FriendApplyDomain.DELETE_APPLY)){
            friendApplyDomainMapper.deleteFriendApplyRecord(approvalFriendReq.getApplyId(), approvalFriendReq.getApplyStatus(), System.currentTimeMillis());
            return;
        }
        // 审批好友申请拒绝或者同意
        friendApplyDomainMapper.approvalFriendApplyRecord(approvalFriendReq.getApplyId(), approvalFriendReq.getApplyStatus(), System.currentTimeMillis());
        // 同意好友创建房间
        if(Objects.equals(approvalFriendReq.getApplyStatus(), FriendApplyDomain.AGREE_APPLY)){
            // 已经添加过好友，但是删除直接恢复好友关系
            FriendRoomDomain friendDomain = friendRoomDomainMapper.getFriendship(userId, approvalFriendReq.getTargetId());
            if(Objects.nonNull(friendDomain) && friendDomain.getRoomStatus().equals(FriendRoomDomain.FRIENDSHIP_DELETE)){
                operateFriendStatus(friendDomain.getUserId(), friendDomain.getFriendId(), FriendRoomDomain.FRIENDSHIP_RECOVER);
            }else{
                // 创建好友房间
                friendDomain = roomService.createFriend(userId, approvalFriendReq.getTargetId(),approvalFriendReq.getRemark());
                AsserUtil.isEmpty(friendDomain, "房间创建失败");
            }
            chatService.sendMsg(MessageAdapter.buildAgreeMsg(friendDomain.getRoomId()),userId);
        }
    }
    @Override
    public List<FriendResp> getFriendPage(Long userId){

        List<FriendRoomDomain> friendPage = roomFriendCache.getUserFriendInfoSet(userId);
        // 获取好友信息
        Map<Long, UserDomain> friendInfo = getFriendInfo(friendPage);

        return friendPage.stream().map(friendRoomDomain -> {
            UserDomain userDomain = friendInfo.get(friendRoomDomain.getFriendId());
            FriendResp friendResp = ChatAdapter.buildFriendResp(userDomain, friendRoomDomain.getRoomName());
            if(userInfoCache.isOnline(friendResp.getUserId())){
                friendResp.setLineStatus(FriendResp.ONLINE);
            }else{
                friendResp.setLineStatus(FriendResp.NO_ONLINE);
            }
            return friendResp;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FriendResp> getBlockFriendPage(Long userId) {
        // 获取所有好友
        List<FriendRoomDomain> friendPage = friendRoomDomainMapper.getFriendPage(userId, FriendRoomDomain.FRIENDSHIP_BLOCK);
        Map<Long, UserDomain> friendInfo = getFriendInfo(friendPage);
        return friendPage.stream().map(friendRoomDomain -> {
            UserDomain userDomain = friendInfo.get(friendRoomDomain.getFriendId());
            return ChatAdapter.buildFriendResp(userDomain, friendRoomDomain.getRoomName());
        }).collect(Collectors.toList());
    }

    @Override
    public void pullBackWhitePage(Long userId,WhiteReq whiteReq) {
        friendRoomDomainMapper.pullBackWhitePage(userId, whiteReq.getFriendList());
    }

    @Override
    public void operateFriendStatus(Long userId, Long targetId, Integer friendStatus) {
        friendRoomDomainMapper.operateFriendship(userId, targetId,friendStatus);
    }

    /**
     * 根据给定的好友列表，返回好友信息
     *
     * @param friendPage    好友列表
     * @return  好友信息
     */
    private Map<Long, UserDomain> getFriendInfo(List<FriendRoomDomain> friendPage){
        List<Long> friendIds = friendPage
                .stream().map(FriendRoomDomain::getFriendId)
                .toList();
        return userInfoCache.getBatch(friendIds);
    }

}
