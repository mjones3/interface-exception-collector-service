package com.arcone.biopro.distribution.partnerorderproviderservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveKeyCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Value("${spring.data.redis.host}")
    private  String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        log.info("Connecting into Redis Server {} , {}" , host , port);
        return new LettuceConnectionFactory(host,port);
    }

    @Bean
    public ReactiveKeyCommands keyCommands(ReactiveRedisConnectionFactory
                                               reactiveRedisConnectionFactory) {
        return reactiveRedisConnectionFactory.getReactiveConnection().keyCommands();
    }
    @Bean
    public ReactiveStringCommands stringCommands(ReactiveRedisConnectionFactory
                                                     reactiveRedisConnectionFactory) {
        return reactiveRedisConnectionFactory.getReactiveConnection().stringCommands();
    }
}
