package com.cabin.ter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * <p>
 *     redis 序列化 、监听
 * </p>
 *
 * @author xiaoye
 * @date Created in 2024-05-02 22:04
 */
@Configuration
public class RedisConfig{
    /**
     * redis 序列化配置
     * @param redisConnectionFactory
     * @return
     */
    @Bean("myRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);

        MyRedisSerializerCustomized jsonRedisSerializer = new MyRedisSerializerCustomized();

        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());

        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    /**
     * 序列化工具
     */
    private static class MyRedisSerializerCustomized extends GenericJackson2JsonRedisSerializer {
        /**
         * 序列化
         * @param source
         * @return
         * @throws SerializationException
         */
        @Override
        public byte[] serialize(Object source) throws SerializationException {
            if (Objects.nonNull(source)) {
                if (source instanceof String || source instanceof Character) {
                    return source.toString().getBytes();
                }
            }
            return super.serialize(source);
        }
        /**
         * 反序列化
         * @param source
         * @param type
         * @return
         * @param <T>
         * @throws SerializationException
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T> T deserialize(byte[] source,@NonNull Class<T> type) throws SerializationException {
            Assert.notNull(type,
                    "Deserialization type must not be null! Please provide Object.class to make use of Jackson2 default typing.");
            if (source == null || source.length == 0) {
                return null;
            }

            if (type.isAssignableFrom(String.class) || type.isAssignableFrom(Character.class)) {
                return (T) new String(source);
            }
            return super.deserialize(source, type);
        }
    }
}