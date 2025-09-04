package com.rgdasil.cart_service.dto;

import lombok.Data;

@Data
public class AddItemRequest {

	private String productId;
	private Integer quantity;
	
}
