package com.rgdasil.cart_service;

import com.rgdasil.cart_service.repository.CartRepository;
import com.rgdasil.cart_service.service.ProductServiceClient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CartServiceApplicationTests {

    // Mock para o repositório do Redis
    @MockBean
    private CartRepository cartRepository;

    // Mock para o cliente do serviço de produtos
    @MockBean
    private ProductServiceClient productServiceClient;

    @Test
    void contextLoads() {
    }

}