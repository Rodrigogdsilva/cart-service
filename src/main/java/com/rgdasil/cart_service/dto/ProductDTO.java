package com.rgdasil.cart_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {

	private String name;
	private Double price;

}
