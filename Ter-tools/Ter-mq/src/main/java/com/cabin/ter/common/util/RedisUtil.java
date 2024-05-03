package com.cabin.ter.common.util;


import com.alibaba.fastjson.JSON;
import com.cabin.ter.common.constants.entity.ws.SendChannelInfo;
import com.cabin.ter.common.constants.enums.ClusterTopicEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     redis 发布者配置
 * </p>
 *
 * @author xiaoye
 * @date Created in 2024-05-02 21:57
 */
@Slf4j
@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void pushObj(SendChannelInfo userChannelInfo) {
        redisTemplate.opsForHash().put(ClusterTopicEnum.REDIS_USER_MESSAGE_PUSH.getMessage(),
                userChannelInfo.getChannelId(), JSON.toJSONString(userChannelInfo));
    }

    public List<SendChannelInfo> popList() {
        List<Object> values = redisTemplate.opsForHash().values(ClusterTopicEnum.REDIS_USER_MESSAGE_PUSH.getMessage());
        if (null == values) {
            return new ArrayList<>();
        }

        List<SendChannelInfo> userChannelInfoList = new ArrayList<>();

        for (Object strJson : values) {
            userChannelInfoList.add(JSON.parseObject(strJson.toString(), SendChannelInfo.class));
        }
        return userChannelInfoList;
    }

    public void remove(String channelId) {
        redisTemplate.opsForHash().delete(ClusterTopicEnum.REDIS_USER_MESSAGE_PUSH.getMessage(), channelId);
    }

    public void clear() {
        redisTemplate.delete(ClusterTopicEnum.REDIS_USER_MESSAGE_PUSH.getMessage());
    }


    public void push(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
}