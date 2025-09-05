package com.rgdasil.cart_service.exception;

public class CartNotFoundException extends RuntimeException{

	private static final long serialVersionUID = -7237913647494984753L;
	
	public CartNotFoundException(String message) {
		super(message);
	}

}
