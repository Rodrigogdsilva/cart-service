package com.rgdasil.cart_service.service;

import com.rgdasil.cart_service.dto.ProductDTO;
import com.rgdasil.cart_service.exception.ServiceUnavailableException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class RestProductServiceClient implements ProductServiceClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;
    private final String internalApiKey;
    private static final Logger log = LoggerFactory.getLogger(RestProductServiceClient.class);


    @Autowired
    public RestProductServiceClient(RestTemplate restTemplate,
                                    @Value("${service.product.url}") String productServiceUrl,
                                    @Value("${service.internal.api-key}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    public Optional<ProductDTO> getProductById(String productId) {
        String url = UriComponentsBuilder.fromHttpUrl(productServiceUrl)
                                         .path("/{id}")
                                         .buildAndExpand(productId)
                                         .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Calling Product Service: GET {}", url);

        try {
            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ProductDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Product found: {}", response.getBody().getId());
                return Optional.of(response.getBody());
            } else {
                 log.warn("Received non-OK status or empty body from Product Service: {}", response.getStatusCode());
                return Optional.empty();
            }

        } catch (HttpClientErrorException.NotFound notFoundEx) {
            log.warn("Product not found (404) for ID: {}", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling Product Service for ID {}: {}", productId, e.getMessage());
            throw e;
        }
    }

    // Método Fallback
    public Optional<ProductDTO> getProductByIdFallback(String productId, Throwable t) {
        log.error("Circuit breaker fallback for getProductById triggered for ID {}: {}", productId, t.getMessage());
        throw new ServiceUnavailableException("Product service is currently unavailable. Please try again later.");
    }
}