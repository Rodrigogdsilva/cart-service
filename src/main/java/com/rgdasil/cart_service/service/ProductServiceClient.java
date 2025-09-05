package com.rgdasil.cart_service.service;

import java.util.Optional;

import com.rgdasil.cart_service.dto.ProductDTO;

public interface ProductServiceClient {
	Optional<ProductDTO> getProductById(String productId);
}
