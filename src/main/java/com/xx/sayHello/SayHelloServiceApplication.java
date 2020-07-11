package com.xx.sayHello;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.xx.sayHello.listener.InitListener;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@SpringBootApplication
@EnableEurekaClient
@RestController
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker

//@EnableAutoConfiguration
//@ComponentScan
@MapperScan("com.xx.sayHello.mapper")
public class SayHelloServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SayHelloServiceApplication.class, args); 
	}
	
	
    @Bean
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource() {
        return new DataSource();
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }
 
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
    
	@Value("${server.port}")
	private String port;
	
//	@Value("${jedisCluster.nodesString}")
//	private String jedisClusterNodesString;
//    @Bean
//	public JedisCluster JedisClusterFactory() {
//    	System.out.println("jedisClusterNodesString--"+jedisClusterNodesString);
//    	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
//    	
//    	if(jedisClusterNodesString.length() > 0){
//    		String [] jedisClusterNodesList = jedisClusterNodesString.split(",");
//    		for (String hostAndPort : jedisClusterNodesList) {
//    			jedisClusterNodes.add(new HostAndPort(hostAndPort.split(":")[0],
//    					Integer.parseInt(hostAndPort.split(":")[1])));
//    		}
//    	}
////		jedisClusterNodes.add(new HostAndPort("192.168.1.5", 7001));
////		jedisClusterNodes.add(new HostAndPort("192.168.1.5", 7002));
////		jedisClusterNodes.add(new HostAndPort("192.168.1.6", 7003));
////		jedisClusterNodes.add(new HostAndPort("192.168.1.6", 7004));
////		jedisClusterNodes.add(new HostAndPort("192.168.1.7", 7005));
////		jedisClusterNodes.add(new HostAndPort("192.168.1.7", 7006));
//		JedisCluster  jedisCluster = new  JedisCluster(jedisClusterNodes,150,5000,1,"redis-pass" ,new GenericObjectPoolConfig());
//		//下面错误示范
////		JedisCluster jedisCluster = new new JedisCluster(jedisClusterNodes,1000, 3000,12,genericObjectPoolConfig);
////		jedisCluster.auth("redis-pass");
//		return jedisCluster;
//	}

    
    
    /**
     * 注册监听器
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    public ServletListenerRegistrationBean servletListenerRegistrationBean() {
    	ServletListenerRegistrationBean servletListenerRegistrationBean = 
    			new ServletListenerRegistrationBean();
    	servletListenerRegistrationBean.setListener(new InitListener());  
    	return servletListenerRegistrationBean;
    }
	
	@RequestMapping("/sayHello")
	@HystrixCommand(fallbackMethod = "sayHelloFallback")
	public String sayHello(String name) {
		return "hello, " + name + " from port: " + port;
	}
	
	public String sayHelloFallback(String name) {
		return "error, " + name;
	}
	
}
