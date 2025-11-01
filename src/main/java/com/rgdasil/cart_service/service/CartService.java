package com.rgdasil.cart_service.service;

import com.rgdasil.cart_service.domain.Cart;
import com.rgdasil.cart_service.dto.AddItemRequest;

public interface CartService {
	
	public Cart addItemToCart(String userId, AddItemRequest addItemRequest);
	
	public Cart getCart(String userId);
	
	public void deleteCart(String userId);

}
