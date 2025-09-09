package com.rgdasil.cart_service.service;

import com.rgdasil.cart_service.domain.Cart;
import com.rgdasil.cart_service.domain.CartItem;
import com.rgdasil.cart_service.dto.AddItemRequest;
import com.rgdasil.cart_service.dto.ProductDTO;
import com.rgdasil.cart_service.exception.ProductNotFoundException;
import com.rgdasil.cart_service.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    // @Mock: Cria uma implementação falsa (mock) das dependências.
    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    // @InjectMocks: Cria uma instância real do CartService e injeta
    @InjectMocks
    private CartService cartService;

    private String userId;
    private String productId;

    @BeforeEach
    void setUp() {
        userId = "user-test-123";
        productId = "prod-test-abc";
    }

    @Test
    void whenAddItemToNewCart_thenCreatesCartAndAddsItem() {
        // --- ARRANGE ---
        AddItemRequest request = new AddItemRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        ProductDTO product = ProductDTO.builder().name("Produto Teste").price(10.0).build();
        
        // Comportamento dos mocks:
        // 1. Quando o client de produto for chamado, retorne o produto de teste.
        when(productServiceClient.getProductById(productId)).thenReturn(Optional.of(product));
        // 2. Quando o repositório procurar um carrinho, retorne vazio (carrinho novo).
        when(cartRepository.findById(userId)).thenReturn(Optional.empty());
        // 3. Quando o repositório salvar QUALQUER carrinho, apenas retorne o mesmo carrinho.
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- ACT ---
        Cart resultCart = cartService.addItemToCart(userId, request);

        // --- ASSERT ---
        assertNotNull(resultCart);
        assertEquals(userId, resultCart.getUserId());
        assertEquals(1, resultCart.getItems().size());
        
        CartItem item = resultCart.getItems().get(productId);
        assertEquals("Produto Teste", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(10.0, item.getPrice());

        // Verifica se o método save foi chamado exatamente 1 vez.
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void whenAddItemToExistingCart_thenUpdatesQuantity() {
        // --- ARRANGE ---
        AddItemRequest request = new AddItemRequest();
        request.setProductId(productId);
        request.setQuantity(1);

        ProductDTO product = ProductDTO.builder().name("Produto Teste").price(10.0).build();

        // Cria um carrinho que já existe no "banco de dados"
        Cart existingCart = Cart.builder().userId(userId).build();
        existingCart.getItems().put(productId, CartItem.builder().productId(productId).quantity(2).build());

        when(productServiceClient.getProductById(productId)).thenReturn(Optional.of(product));
        // Agora, o repositório retorna o carrinho existente
        when(cartRepository.findById(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- ACT ---
        Cart resultCart = cartService.addItemToCart(userId, request);

        // --- ASSERT ---
        // A quantidade inicial era 2, adicionamos 1, o total deve ser 3.
        assertEquals(3, resultCart.getItems().get(productId).getQuantity());
    }
    
    @Test
    void whenProductNotFound_thenThrowsException() {
        // --- ARRANGE ---
        AddItemRequest request = new AddItemRequest();
        request.setProductId("produto-inexistente");
        
        // O client de produto retorna vazio.
        when(productServiceClient.getProductById("produto-inexistente")).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // Verifica se uma exceção do tipo ProductNotFoundException é lançada
        // ao executar o método.
        assertThrows(ProductNotFoundException.class, () -> {
            cartService.addItemToCart(userId, request);
        });

        // Garante que o método save NUNCA foi chamado.
        verify(cartRepository, never()).save(any(Cart.class));
    }
}