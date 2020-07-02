package com.xx.sayHello.listener;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.xx.sayHello.thread.RequestProcessorThreadPool;

/**
 * 系统初始化监听器
 * @author Administrator
 *
 */
public class InitListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		// 初始化工作线程池和内存队列
		RequestProcessorThreadPool.init();
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
	}

}
