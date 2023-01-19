package com.cxb.storehelperserver.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.servlet.MultipartConfigElement;
import java.io.Serializable;

/**
 * desc: 系统配置
 * auth: cxb
 * date: 2022/11/29
 */
@Configuration
public class ApplicationConfig {
    @Value("${store-app.config.uploadpath}")
    private String uploadpath;

    /**
     * desc: 修改上传文件临时目录
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(uploadpath);
        return factory.createMultipartConfig();
    }

    /**
     * desc: 注入 redis template
     */
    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
        val redisTemplate = new RedisTemplate<String, Serializable>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
