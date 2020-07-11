//package com.xx.sayHello.zk;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.IntStream;
//
//import org.apache.zookeeper.CreateMode;
//import org.apache.zookeeper.KeeperException;
//import org.apache.zookeeper.WatchedEvent;
//import org.apache.zookeeper.Watcher;
//import org.apache.zookeeper.ZooDefs;
//import org.apache.zookeeper.ZooKeeper;
//import org.apache.zookeeper.data.Stat;
//
//public class ZooKeeperDistributedLock implements Watcher {
//
//	private ZooKeeper zk;
//	private String locksRoot = "/locks";
//	private String productId;
//	private String waitNode;
//	private String lockNode;
//	private CountDownLatch latch;
//	private CountDownLatch connectedLatch = new CountDownLatch(1);
//	private int sessionTimeout = 300;
//
//	public ZooKeeperDistributedLock(String productId) {
//		this.productId = productId;
//		try {
//			String address = "192.168.1.5:2181,192.168.1.6:2181,192.168.1.7:2181";
//			zk = new ZooKeeper(address, sessionTimeout, this);
//			connectedLatch.await();
//		} catch (IOException e) {
//			throw new LockException(e);
//		} catch (InterruptedException e) {
//			throw new LockException(e);
//		}
//	}
//
//	public void process(WatchedEvent event) {
//		if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
//			connectedLatch.countDown();
//			return;
//		}
//
//		if (this.latch != null) {
//			this.latch.countDown();
//		}
//	}
//
//	public void acquireDistributedLock() {
//		try {
//			if (this.tryLock()) {
//				return;
//			} else {
//				waitForLock(waitNode, sessionTimeout);
//			}
//		} catch (KeeperException e) {
//			throw new LockException(e);
//		} catch (InterruptedException e) {
//			throw new LockException(e);
//		}
//	}
//
//	public boolean tryLock() {
//		try {
//			// 传入进去的locksRoot + “/” + productId
//			// 假设productId代表了一个商品id，比如说1
//			// locksRoot = locks
//			// /locks/10000000000，/locks/10000000001，/locks/10000000002
//			lockNode = zk.create(locksRoot + "/" + productId, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
//
//			// 看看刚创建的节点是不是最小的节点,获取根节点下的所有临时顺序节点，不设置监视器
//
//			// locks：10000000000，10000000001，10000000002
//			List<String> locks = zk.getChildren(locksRoot, false);
//			//对根节点下的所有临时顺序节点进行从小到大排序
//			Collections.sort(locks);
//
//			if(lockNode.equals(locksRoot+"/"+ locks.get(0))){
//				//如果是最小的节点,则表示取得锁
//				return true;
//			}
//
//			//如果不是最小的节点，找到比自己小1的节点
//			int previousLockIndex = -1;
//			for(int i = 0; i < locks.size(); i++) {
//				if(lockNode.equals(locksRoot + "/" + locks.get(i))) {
//					previousLockIndex = i - 1;
//					break;
//				}
//			}
//
//			this.waitNode = locks.get(previousLockIndex);
//		} catch (KeeperException e) {
//			throw new LockException(e);
//		} catch (InterruptedException e) {
//			throw new LockException(e);
//		}
//		return false;
//	}
//
//	private boolean waitForLock(String waitNode, long waitTime) throws InterruptedException, KeeperException {
//		Stat stat = zk.exists(locksRoot + "/" + waitNode, true);
//		if (stat != null) {
//			this.latch = new CountDownLatch(1);
//			this.latch.await(waitTime, TimeUnit.MILLISECONDS);
//			this.latch = null;
//		}
//		return true;
//	}
//
//	public void unlock() {
//		try {
//			// 删除/locks/10000000000节点
//			// 删除/locks/10000000001节点
//			System.out.println("unlock " + lockNode);
//			zk.delete(lockNode, -1);
//			lockNode = null;
//			zk.close();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (KeeperException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public class LockException extends RuntimeException {
//		private static final long serialVersionUID = 1L;
//
//		public LockException(String e) {
//			super(e);
//		}
//
//		public LockException(Exception e) {
//			super(e);
//		}
//	}
//
//	private static ZooKeeperDistributedLock instance = new ZooKeeperDistributedLock("12");
//	public static ZooKeeperDistributedLock getInstance() {
//		return instance;
//	}
//
//
//	public static void main(String[] args) throws InterruptedException {
//		ZooKeeperDistributedLock instance = ZooKeeperDistributedLock.getInstance();
//		CountDownLatch downLatch = new CountDownLatch(2);
//		IntStream.of(1, 2).forEach(i -> new Thread(() -> {
//			instance.acquireDistributedLock();
//			System.out.println(Thread.currentThread().getName() + " 得到锁并休眠 10 秒");
//			try {
//				TimeUnit.SECONDS.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			instance.unlock();
//			System.out.println(Thread.currentThread().getName() + " 释放锁");
//			downLatch.countDown();
//		}).start());
//		downLatch.await();
//	}
//}