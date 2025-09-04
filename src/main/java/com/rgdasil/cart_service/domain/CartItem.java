package com.rgdasil.cart_service.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem implements Serializable {
	
	private static final long serialVersionUID = 132099127463836025L;
	
	private String productId;
	private String productName;
	private Integer quantity;
	private Double price;

}
