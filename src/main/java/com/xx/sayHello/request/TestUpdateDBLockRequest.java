package com.xx.sayHello.request;


import com.xx.sayHello.model.ProductInventory;
import com.xx.sayHello.service.ProductInventoryService;

/**
 * 比如说一个商品发生了交易，那么就要修改这个商品对应的库存
 * 
 * 此时就会发送请求过来，要求修改库存，那么这个可能就是所谓的data update request，数据更新请求
 * 
 * （1）删除缓存
 * （2）更新数据库
 * 
 * @author Administrator
 *
 */
public class TestUpdateDBLockRequest implements Request {

	/**
	 * 商品库存
	 */
	private ProductInventory productInventory;
	/**
	 * 商品库存Service
	 */
	private ProductInventoryService productInventoryService;
	
	public TestUpdateDBLockRequest(ProductInventory productInventory,
			ProductInventoryService productInventoryService) {
		this.productInventory = productInventory;
		this.productInventoryService = productInventoryService;
	}
	
	public void process() {
		System.out.println("===========日志===========: 数据库更新请求开始执行，商品id=" + productInventory.getProductId() + ", 商品库存数量=" + productInventory.getInventoryCnt());  
		// 删除redis中的缓存
		productInventoryService.removeProductInventoryCache(productInventory);
		// 修改数据库中的库存
		productInventoryService.testUpdateDBLock(productInventory);  
	}
	
	/**
	 * 获取商品id
	 */
	public Integer getProductId() {
		return productInventory.getProductId();
	}

	public boolean isForceRefresh() {
		return false;
	}
	
}
