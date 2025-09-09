package com.rgdasil.cart_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 8553155538498816627L;

	public ServiceUnavailableException(String message) {
		super(message);
	}

}
