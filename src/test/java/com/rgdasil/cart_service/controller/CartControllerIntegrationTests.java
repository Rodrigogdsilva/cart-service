package com.rgdasil.cart_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgdasil.cart_service.domain.Cart;
import com.rgdasil.cart_service.domain.CartItem;
import com.rgdasil.cart_service.dto.AddItemRequest;
import com.rgdasil.cart_service.security.JwtAuthInterceptor;
import com.rgdasil.cart_service.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    // Utilitário para converter objetos Java para JSON e vice-versa.
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtAuthInterceptor jwtAuthInterceptor;

    private String mockUserId;
    private Cart mockCart;

    @BeforeEach
    void setUp() throws Exception {

        mockUserId = "user-123";

        // Prepara um carrinho de exemplo que será retornado pelo nosso CartService mockado.
        mockCart = Cart.builder()
                .userId(mockUserId)
                .items(Map.of("prod-abc",
                        CartItem.builder()
                                .productId("prod-abc")
                                .productName("Produto Teste")
                                .quantity(1)
                                .price(99.99)
                                .build()))
                .build();

        // Configura o comportamento do interceptor mockado.
        // Quando o método preHandle do interceptor for chamado com QUALQUER
        // request/response, retorne 'true' para permitir que a requisição prossiga."
        when(jwtAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void shouldAddItemToCartAndReturnCart() throws Exception {
        // --- ARRANGE ---
        AddItemRequest requestBody = new AddItemRequest();
        requestBody.setProductId("prod-abc");
        requestBody.setQuantity(1);

        // Define o comportamento do mock: "Quando o método addItemToCart do serviço
        // for chamado com o userId 'user-123' e o corpo da requisição,
        // então retorne o nosso carrinho mockado."
        when(cartService.addItemToCart(eq(mockUserId), any(AddItemRequest.class))).thenReturn(mockCart);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/cart") // Faz um POST para /cart
                        .contentType(MediaType.APPLICATION_JSON) // Define o tipo de conteúdo
                        .content(objectMapper.writeValueAsString(requestBody)) // Converte o corpo da requisição para JSON
                        .requestAttr("userId", mockUserId)) // **IMPORTANTE**: Simula o atributo que o interceptor adicionaria
                .andExpect(status().isOk()) // Espera que o status da resposta seja 200 OK
                .andExpect(jsonPath("$.userId").value(mockUserId)) // Verifica se o JSON de resposta tem o campo userId correto
                .andExpect(jsonPath("$.items['prod-abc'].productName").value("Produto Teste")); // Verifica um campo dentro do item
    }

    @Test
    void shouldGetCartSuccessfully() throws Exception {
        // --- ARRANGE ---
        // Define o comportamento: "Quando getCart for chamado com o userId, retorne o mockCart."
        when(cartService.getCart(mockUserId)).thenReturn(mockCart);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/cart") // Faz um GET para /cart
                        .requestAttr("userId", mockUserId)) // Simula o userId
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.userId").value(mockUserId))
                .andExpect(jsonPath("$.itemCount").value(1)); // Verifica o total de itens
    }

    @Test
    void shouldDeleteCartAndReturnNoContent() throws Exception {
        // --- ARRANGE ---
        // O método deleteCart não retorna nada (void).
        // Usamos doNothing() para configurar o mock para um método void.
        doNothing().when(cartService).deleteCart(mockUserId);

        // --- ACT & ASSERT ---
        mockMvc.perform(delete("/cart") // Faz um DELETE para /cart
                        .requestAttr("userId", mockUserId)) // Simula o userId
                .andExpect(status().isNoContent()); // Espera o status 204 No Content
    }
}