package com.rgdasil.cart_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.rgdasil.cart_service.domain.Cart;
import com.rgdasil.cart_service.dto.AddItemRequest;
import com.rgdasil.cart_service.dto.ProductDTO;
import com.rgdasil.cart_service.repository.CartRepository;
import com.rgdasil.cart_service.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class CartServiceApplicationTests {

	// Inicia um contêiner Redis real para este teste
	@Container
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(6379);

	// Inicia um servidor WireMock (simulador de API) numa porta dinâmica
	@RegisterExtension
	static WireMockExtension wireMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort())
			.build();

	// Intercepta as propriedades da aplicação e substitui pelas URLs dos contêineres
	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
		registry.add("service.product.url", wireMockServer::baseUrl);
	}

	@Autowired
	private CartService cartService;

	@Autowired
	private CartRepository cartRepository;

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	private final String mockUserId = "test-user-123";
	private final String mockProductId = "prod-abc-789";

	@BeforeEach
	void setUp() {
		// Limpa o WireMock e o Redis antes de cada teste
		wireMockServer.resetAll();
		cartRepository.deleteAll();
	}

	@Test
	void contextLoads() {
		assertNotNull(cartService);
		assertNotNull(cartRepository);
	}

	@Test
	void shouldAddItemToCart_WhenProductServiceIsAvailable() throws JsonProcessingException {
		// --- ARRANGE ---
		// 1. Preparar a resposta JSON do produto (simulando o product-service)
		ProductDTO mockProduct = ProductDTO.builder()
				.id(mockProductId)
				.name("Produto Teste via WireMock")
				.description("Descrição")
				.price(150.0)
				.stock(10)
				.createdAt(OffsetDateTime.now())
				.updatedAt(OffsetDateTime.now())
				.build();
		String mockProductJson = objectMapper.writeValueAsString(mockProduct);

		// 2. Configurar o WireMock para responder à chamada GET
		wireMockServer.stubFor(get(urlEqualTo("/" + mockProductId))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(mockProductJson)));

		// 3. Preparar o pedido do carrinho
		AddItemRequest addItemRequest = new AddItemRequest();
		addItemRequest.setProductId(mockProductId);
		addItemRequest.setQuantity(2);

		// --- ACT ---
		// Chama o método de serviço real.
		// O RestProductServiceClient fará uma chamada HTTP real ao WireMock.
		Cart resultCart = cartService.addItemToCart(mockUserId, addItemRequest);

		// --- ASSERT ---
		// 1. Verificar se o WireMock foi chamado corretamente
		wireMockServer.verify(1, getRequestedFor(urlEqualTo("/" + mockProductId)));

		// 2. Verificar se o carrinho foi salvo corretamente no Redis
		assertNotNull(resultCart);
		assertEquals(1, resultCart.getItems().size());
		assertEquals("Produto Teste via WireMock", resultCart.getItems().get(mockProductId).getProductName());

		// 3. Verificar diretamente no repositório Redis
		Optional<Cart> savedCart = cartRepository.findById(mockUserId);
		assertTrue(savedCart.isPresent());
		assertEquals(2, savedCart.get().getItems().get(mockProductId).getQuantity());
	}
	
	@Test
	void shouldThrowProductNotFound_WhenProductServiceReturns404() {
		// --- ARRANGE ---
		// 1. Configurar o WireMock para responder com 404 Not Found
		wireMockServer.stubFor(get(urlEqualTo("/" + mockProductId))
				.willReturn(aResponse()
						.withStatus(404)));

		// 2. Preparar o pedido
		AddItemRequest addItemRequest = new AddItemRequest();
		addItemRequest.setProductId(mockProductId);
		addItemRequest.setQuantity(1);

		// --- ACT & ASSERT ---
		// Verifica se o serviço lança a exceção correta
		// quando o cliente HTTP recebe um 404
		assertThrows(com.rgdasil.cart_service.exception.ProductNotFoundException.class, () -> {
			cartService.addItemToCart(mockUserId, addItemRequest);
		});
		
		// Verifica se o repositório NUNCA foi chamado para salvar
		assertEquals(0, cartRepository.count());
	}
}