package com.xx.sayHello.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonInit {
	@Bean
	public static RedissonClient getInstance(){
		Config config = new Config();
		
		config.useClusterServers()
		// cluster state scan interval in milliseconds
		.setScanInterval(200000)
		.addNodeAddress("redis://192.168.1.5:7001", "redis://192.168.1.7:7005")
		.addNodeAddress("redis://192.168.1.6:7003").setPassword("redis-pass");
		RedissonClient redisson = Redisson.create(config);
		return redisson;
	}
}
