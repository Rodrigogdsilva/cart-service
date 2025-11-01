package com.rgdasil.cart_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rgdasil.cart_service.domain.Cart;
import com.rgdasil.cart_service.dto.AddItemRequest;
import com.rgdasil.cart_service.service.RestCartService;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private RestCartService cartService;

	@RequestMapping(value = "/test")
	public String test() {
		return "Cart Service is up!";
	}

	@PostMapping
	public ResponseEntity<Cart> addItemToCart(@RequestAttribute(value = "userId") String userId,
			@RequestBody AddItemRequest addItemRequest) {

		return ResponseEntity.ok(cartService.addItemToCart(userId, addItemRequest));
	}

	@GetMapping
	public ResponseEntity<Cart> getCart(@RequestAttribute(value = "userId") String userId) {
		return ResponseEntity.ok(cartService.getCart(userId));
	}

	@DeleteMapping
	public ResponseEntity<Cart> deleteCart(@RequestAttribute(value = "userId") String userId) {
		cartService.deleteCart(userId);
		return ResponseEntity.noContent().build();
	}
}
