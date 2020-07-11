package com.xx.sayHello.jedisCluster;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@Configuration
@PropertySource("classpath:application.yml")
public class JedisClusterInit {

	@Value("${jedisCluster.nodesString}")
	private String jedisClusterNodesString;
	@Value("${jedisCluster.pass}")
	private String pass;
    @Bean
	public JedisCluster JedisClusterFactory() {
    	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
    	if(jedisClusterNodesString.length() > 0){
    		String [] jedisClusterNodesList = jedisClusterNodesString.split(",");
    		for (String hostAndPort : jedisClusterNodesList) {
    			jedisClusterNodes.add(new HostAndPort(hostAndPort.split(":")[0],
    					Integer.parseInt(hostAndPort.split(":")[1])));
    		}
    	}
//		jedisClusterNodes.add(new HostAndPort("192.168.1.5", 7001));
//		jedisClusterNodes.add(new HostAndPort("192.168.1.5", 7002));
//		jedisClusterNodes.add(new HostAndPort("192.168.1.6", 7003));
//		jedisClusterNodes.add(new HostAndPort("192.168.1.6", 7004));
//		jedisClusterNodes.add(new HostAndPort("192.168.1.7", 7005));
//		jedisClusterNodes.add(new HostAndPort("192.168.1.7", 7006));
		JedisCluster  jedisCluster = new  JedisCluster(jedisClusterNodes,150,5000,1,pass ,new GenericObjectPoolConfig());
		//下面错误示范
//		JedisCluster jedisCluster = new new JedisCluster(jedisClusterNodes,1000, 3000,12,genericObjectPoolConfig);
//		jedisCluster.auth("redis-pass");
		return jedisCluster;
	}

}
