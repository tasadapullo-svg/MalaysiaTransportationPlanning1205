package com.utm.traffic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置最佳实践
 * 包含：序列化优化、连接池配置、Template标准化
 */
@Configuration
public class RedisConfig {

    // 1. 配置RedisTemplate（核心序列化+连接）
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 绑定连接工厂（Lettuce/Jedis由配置文件决定）
        template.setConnectionFactory(connectionFactory);

        // 通用Jackson2序列化器（支持复杂对象+类型信息）
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // Key/HashKey：String序列化（避免乱码）
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value/HashValue：Jackson2序列化（替代JDK默认序列化）
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 初始化参数（Spring会自动调用，可省略，但显式调用更清晰）
        template.afterPropertiesSet();
        return template;
    }

    // 2. 可选：配置Lettuce连接池（若用Jedis则替换为JedisConnectionFactory）
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 实际项目中建议通过application.yml配置，这里是硬编码示例
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
        // factory.setPassword("your-redis-password"); // 若有密码
        factory.afterPropertiesSet();
        return factory;
    }
}