package com.rgdasil.cart_service.dto;

import lombok.Data;

@Data
public class AuthResponse {
	
	private boolean isValid;
	private String userId;
	private String email;
	
}
