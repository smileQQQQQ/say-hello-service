package com.xx.sayHello.service.impl;


import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xx.sayHello.dao.RedisDAO;
import com.xx.sayHello.mapper.ProductInventoryMapper;
import com.xx.sayHello.model.ProductInventory;
import com.xx.sayHello.service.ProductInventoryService;
import com.xx.sayHello.zk.ZooKeeperSession;

/**
 * 商品库存Service实现类
 * @author Administrator
 *
 */
@Service("productInventoryService")  
public class ProductInventoryServiceImpl implements ProductInventoryService {

	@Resource
	private ProductInventoryMapper productInventoryMapper;
	@Resource
	private RedisDAO redisDAO;

	@Autowired
	public RedissonClient redissonClient;

	public void updateProductInventory(ProductInventory productInventory) {
		productInventoryMapper.updateProductInventory(productInventory); 
		System.out.println("===========日志===========: 已修改数据库中的库存，商品id=" + productInventory.getProductId() + ", 商品库存数量=" + productInventory.getInventoryCnt());
	}

	
	/**
	 * 测试分布式并发下，修改mysql同一数据的情况
	 * @param productInventory
	 */
	public void testUpdateDBLock(ProductInventory productInventory) {
//		//获取锁zk
//		 ZooKeeperSession zks = ZooKeeperSession.getInstance();
//       zks.acquireDistributedLock(productInventory.getProductId().longValue());
		//只测试productInventory_Lock_1
		RLock lock = redissonClient.getLock("productInventory_Lock_1");
		
		Boolean cacheRes = true;
		try {
			//第一个参数30s=表示尝试获取分布式锁，并且最大的等待获取锁的时间为30s
			//第二个参数10s=表示上锁之后，10s内操作完毕将自动释放锁
//			cacheRes = lock.tryLock(30,10,TimeUnit.SECONDS);
//			System.out.println("获取锁"+lock.getName() +",获取状态："+cacheRes);
			if(cacheRes){
				//测试并发
				ProductInventory productInventoryRedis = this.findProductInventory(productInventory.getProductId());
				Long inventoryCnt= productInventoryRedis.getInventoryCnt();
				productInventory.setInventoryCnt(inventoryCnt+1L);

				productInventoryMapper.updateProductInventory(productInventory); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			//释放锁
//			zks.releaseDistributedLock(productInventory.getProductId().longValue());
			lock.unlock();
		}
		System.out.println("===========日志===========: 已修改数据库中的库存，商品id=" + productInventory.getProductId() + ", 商品库存数量=" + productInventory.getInventoryCnt());
	}

	
	public void removeProductInventoryCache(ProductInventory productInventory) {
		String key = "product:inventory:" + productInventory.getProductId();
		redisDAO.delete(key);
		System.out.println("===========日志===========: 已删除redis中的缓存，key=" + key); 
	}

	/**
	 * 根据商品id查询商品库存
	 * @param productId 商品id 
	 * @return 商品库存
	 */
	public ProductInventory findProductInventory(Integer productId) {
		return productInventoryMapper.findProductInventory(productId);
	}

	/**
	 * 设置商品库存的缓存
	 * @param productInventory 商品库存
	 */
	public void setProductInventoryCache(ProductInventory productInventory) {
		String key = "product:inventory:" + productInventory.getProductId();

		////		//测试并发
		//		ProductInventory productInventoryRedis = this.findProductInventory(productInventory.getProductId());
		//		Long inventoryCnt= productInventoryRedis.getInventoryCnt();
		//		productInventory.setInventoryCnt(inventoryCnt+1L);

		redisDAO.set(key, String.valueOf(productInventory.getInventoryCnt()));
		System.out.println("===========日志===========: 已更新商品库存的缓存，商品id=" + productInventory.getProductId() + ", 商品库存数量=" + productInventory.getInventoryCnt() + ", key=" + key);  
	}

	/**
	 * 获取商品库存的缓存
	 * @param productId
	 * @return
	 */
	public ProductInventory getProductInventoryCache(Integer productId) {
		Long inventoryCnt = 0L;

		String key = "product:inventory:" + productId;
		String result = redisDAO.get(key);

		if(result != null && !"".equals(result)) {
			try {
				inventoryCnt = Long.valueOf(result);
				return new ProductInventory(productId, inventoryCnt);
			} catch (Exception e) {
				e.printStackTrace(); 
			}
		}
		return null;
	}
}
