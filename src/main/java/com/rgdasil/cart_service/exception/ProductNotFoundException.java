package com.rgdasil.cart_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException{

	private static final long serialVersionUID = 4453486573191883481L;

	public ProductNotFoundException(String message) {
		super(message);
	}

}
