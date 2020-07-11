package com.xx.sayHello.redisson;

import java.util.HashSet;
import java.util.Set;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import redis.clients.jedis.HostAndPort;

@Configuration
@PropertySource("classpath:application.yml")
public class RedissonInit {

	@Value("${jedisCluster.nodesString}")
	private String nodesString;
	@Value("${jedisCluster.pass}")
	private String pass;
	@Bean
	public  RedissonClient getInstance(){
		System.out.println("jedisClusterNodesString"+nodesString);
		
		Config config = new Config();
		if(nodesString.length() > 0){
    		String [] jedisClusterNodesList = nodesString.split(",");
    		for (int i = 0 ; jedisClusterNodesList.length > i; i++) {
    			jedisClusterNodesList[i] =  "redis://"+jedisClusterNodesList[i];
			}
    		for (String ip : jedisClusterNodesList) {
    			System.out.println("ip"+ip);
    		}
    		
    		config.useClusterServers().addNodeAddress(jedisClusterNodesList);
    	}
		config.useClusterServers()
		.setScanInterval(200000)
//		.addNodeAddress("redis://192.168.1.5:7001", "redis://192.168.1.7:7005")
//		.addNodeAddress("redis://192.168.1.6:7003")
		.setPassword(pass);
		RedissonClient redisson = Redisson.create(config);
		return redisson;
	}
}
