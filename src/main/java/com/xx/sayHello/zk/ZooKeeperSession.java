package com.xx.sayHello.zk;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class ZooKeeperSession {
	
    private ZooKeeper zookeeper;
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);

    private ZooKeeperSession() {
        String connectString = "192.168.1.5:2181,192.168.1.6:2181,192.168.1.7:2181";
        int sessionTimeout = 5000;
        try {
            // 异步连接，所以需要一个  org.apache.zookeeper.Watcher 来通知
            // 由于是异步，利用 CountDownLatch 来让构造函数等待
            zookeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
                Watcher.Event.KeeperState state = event.getState();
                System.out.println("watch event：" + state);
                if (state == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("zookeeper 已连接");
                    connectedSemaphore.countDown();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            connectedSemaphore.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("zookeeper 初始化成功");
    }

    /**
     * 获取分布式锁
     */
    public void acquireDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;
        byte[] data = "".getBytes();
        try {
            // 创建一个临时节点，后面两个参数一个安全策略，一个临时节点类型
            // EPHEMERAL：客户端被断开时，该节点自动被删除
            zookeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("获取锁成功 product[id=" + productId + "]");
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("锁已被获取product[id=" + productId + "] ");
            // 如果锁已经被创建，那么将异常
            // 循环等待锁的释放
            int count = 0;
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                    // 休眠 20 毫秒后再次尝试创建
                    zookeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e1) {
//                    e1.printStackTrace();
                    count++;
                    continue;
                }
                System.out.println("获取锁成功 product[id=" + productId + "] 尝试了 " + count + " 次.");
                break;
            }
        }
    }

    /**
     * 释放分布式锁
     */
    public void releaseDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;
        try {
            zookeeper.delete(path, -1);
            System.out.println("释放锁成功 product[id=" + productId + "] ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private static ZooKeeperSession instance = new ZooKeeperSession();

    public static ZooKeeperSession getInstance() {
        return instance;
    }

    public static void main(String[] args) throws InterruptedException {
    	
    	
    	Config config = new Config();
		config.useClusterServers()
		// cluster state scan interval in milliseconds
		.setScanInterval(200000)
		.addNodeAddress("redis://192.168.1.5:7001", "redis://192.168.1.7:7005")
		.addNodeAddress("redis://192.168.1.6:7003").setPassword("redis-pass");
		RedissonClient redisson = Redisson.create(config);
    	
    	
        ZooKeeperSession instance = ZooKeeperSession.getInstance();
        CountDownLatch downLatch = new CountDownLatch(2);
        IntStream.of(1, 2).forEach(i -> new Thread(() -> {
            instance.acquireDistributedLock(1L);
            
//        	RLock lock = redisson.getLock("productInventory_Lock_1");
//    		lock.lock(11,TimeUnit.SECONDS);
            
            System.out.println(Thread.currentThread().getName() + " 得到锁并休眠 10 秒");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            lock.unlock();
            
            instance.releaseDistributedLock(1L);
            System.out.println(Thread.currentThread().getName() + " 释放锁");
            downLatch.countDown();
        }).start());
        System.out.println("123");
        downLatch.await();
    }
}
