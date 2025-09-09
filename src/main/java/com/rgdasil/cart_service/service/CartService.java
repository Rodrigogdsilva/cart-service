package com.rgdasil.cart_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rgdasil.cart_service.domain.Cart;
import com.rgdasil.cart_service.domain.CartItem;
import com.rgdasil.cart_service.dto.AddItemRequest;
import com.rgdasil.cart_service.dto.ProductDTO;
import com.rgdasil.cart_service.exception.CartNotFoundException;
import com.rgdasil.cart_service.exception.ProductNotFoundException;
import com.rgdasil.cart_service.repository.CartRepository;

@Service
public class CartService {

	CartRepository cartRepository;
	ProductServiceClient productServiceClient;
	
	@Autowired
	public CartService(CartRepository cartRepository, ProductServiceClient productServiceClient) {
		this.cartRepository = cartRepository;
		this.productServiceClient = productServiceClient;
	}

	public Cart addItemToCart(String userId, AddItemRequest addItemRequest) {

		String productId = addItemRequest.getProductId();
		ProductDTO productDTO = productServiceClient.getProductById(productId)
				.orElseThrow(() -> new ProductNotFoundException("Product ID:" + productId + " not found."));
		
		Cart cart = cartRepository.findById(userId)
				.orElse(Cart.builder().userId(userId).build());

		CartItem existingItem = cart.getItems().get(productId);

		if (existingItem != null) {
			existingItem.setQuantity(existingItem.getQuantity() + addItemRequest.getQuantity());
			cart.getItems().put(productId, existingItem);
		} else {
			CartItem newItem = CartItem.builder()
					.productId(productId)
					.quantity(addItemRequest.getQuantity())
					.productName(productDTO.getName())
					.price(productDTO.getPrice())
					.build();
			cart.getItems().put(productId, newItem);
		}
		return cartRepository.save(cart);
	}
	
	public Cart getCart(String userId) {
		return cartRepository.findById(userId)
				.orElseThrow(() -> new CartNotFoundException("Cart ID: " + userId + " not found"));
	}
	
	public void deleteCart(String userId) {
		cartRepository.deleteById(userId);
	}
}
