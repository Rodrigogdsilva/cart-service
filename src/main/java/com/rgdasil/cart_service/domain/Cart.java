package com.rgdasil.cart_service.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Data;

@RedisHash("carts")
@Data
@Builder
public class Cart {
	
	@Id
	private String userId;
	
	private Map<String, CartItem> items = new HashMap<>();
	
	@TimeToLive(unit = TimeUnit.DAYS)
	private Long expiration = 7L;
	
	
	public Double getTotalPrice() {
		return items.values().stream()
				.mapToDouble(item -> item.getPrice() * item.getQuantity())
				.sum();
	}
	
	public Integer getItemCount() {
		return items.values().stream()
				.mapToInt(CartItem::getQuantity)
				.sum();
	}
}
