package com.bird.sso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 11:24
 */
@Configuration
public class RedisConfig {

    /**
     * redis模板
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
       /* ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);*/

        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * redis模板
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public StringRedisTemplate redisTemplateWithJdk(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
        /**
         * SpringBoot扩展了ClassLoader，进行分离打包的时候，使用到JdkSerializationRedisSerializer的地方
         * 会因为ClassLoader的不同导致加载不到Class
         * 指定使用项目的ClassLoader
         *
         * JdkSerializationRedisSerializer默认使用{@link sun.misc.Launcher.AppClassLoader}
         * SpringBoot默认使用{@link org.springframework.boot.loader.LaunchedURLClassLoader}
         */
        ClassLoader classLoader = this.getClass().getClassLoader();
        stringRedisTemplate.setValueSerializer(new JdkSerializationRedisSerializer(classLoader));
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }
}
