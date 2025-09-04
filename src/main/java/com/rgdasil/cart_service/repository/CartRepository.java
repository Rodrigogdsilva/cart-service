package com.rgdasil.cart_service.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rgdasil.cart_service.domain.Cart;

@Repository
public interface CartRepository extends CrudRepository<Cart, String> {

}
