package com.say.hello;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LockTest {


	public static void main(String[] args) throws InterruptedException {
		 concurrenceTest();
	}
	
	 public static void concurrenceTest() {
	        /**
	         * 模拟高并发情况代码
	         */
	        final AtomicInteger atomicInteger = new AtomicInteger(0);
	        final CountDownLatch countDownLatch = new CountDownLatch(100); // 相当于计数器，当所有都准备好了，再一起执行，模仿多并发，保证并发量
	        final CountDownLatch countDownLatch2 = new CountDownLatch(100); // 保证所有线程执行完了再打印atomicInteger的值
	        ExecutorService executorService = Executors.newFixedThreadPool(10);
	        Count count = new Count();
	        try {
	            for (int i = 0; i < 100; i++) {
	                executorService.submit(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                            countDownLatch.await(); //一直阻塞当前线程，直到计时器的值为0,保证同时并发
	                        } catch (InterruptedException e) {
	                            e.printStackTrace();
	                        }
	                        //每个线程增加1000次，每次加1
	                        for (int j = 0; j < 10; j++) {
	                            atomicInteger.incrementAndGet();
	                            count.addCout();
	                            HttpClient4.doGet("http://localhost:8764/testUpdateDBLock?productId=1&inventoryCnt="+atomicInteger);
	                        }
	                        countDownLatch2.countDown();
	                    }
	                });
	                countDownLatch.countDown();
	            }
//	        	String result = HttpClient4.doGet("http://localhost:8764/getProductInventory?productId=1");
//	        	System.out.println(result);
	            countDownLatch2.await();// 保证所有线程执行完
	            System.out.println(atomicInteger);
	            System.out.println( count.getI());
	            executorService.shutdown();
	        }catch (Exception e){
	            e.printStackTrace();
	        }
	    }

	
}
