package com.rgdasil.cart_service.service;

import java.util.Optional;

import com.github.javafaker.Faker;
import com.rgdasil.cart_service.dto.ProductDTO;

public class FakeProductServiceClient implements ProductServiceClient {

	@Override
	public Optional<ProductDTO> getProductById(String productId) {
		
		if("1".equals(productId)) {
			
			return Optional.ofNullable(ProductDTO.builder()
					.name(Faker.instance().commerce().productName())
					.price(Faker.instance().number().randomDouble(2, 1, 1000))
					.build());
		}
		
		return Optional.empty();
	}
}
