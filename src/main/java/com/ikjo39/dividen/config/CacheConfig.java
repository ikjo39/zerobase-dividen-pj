package com.ikjo39.dividen.config;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
public class CacheConfig {

	@Value("${spring.redis.host}")
	private String host;
	@Value("${spring.redis.port}")
	private int port;

	@Bean
	public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
		RedisCacheConfiguration conf = RedisCacheConfiguration.defaultCacheConfig()
			// Serialization 해야함
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new GenericJackson2JsonRedisSerializer()));
		return RedisCacheManager.RedisCacheManagerBuilder
			.fromConnectionFactory(redisConnectionFactory) // 아래 생성한 기능이 넘어옴
			.cacheDefaults(conf)
			.build();
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		// 클러스터는
//		RedisClusterConfiguration
		// 우리는 싱글임
		RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
		conf.setHostName(this.host);
		conf.setPort(this.port);
//		conf.setPassword();
		return new LettuceConnectionFactory(conf);
	}

	// 캐시에 적용하려면 캐시 매니저 빈을 추가 구성해야함
}
