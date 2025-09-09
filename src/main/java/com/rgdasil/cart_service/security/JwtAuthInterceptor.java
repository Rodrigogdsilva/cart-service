package com.rgdasil.cart_service.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.rgdasil.cart_service.dto.AuthRequest;
import com.rgdasil.cart_service.dto.AuthResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

	@Value("${service.auth.url}")
	private String authServiceUrl;

	@Value("${service.internal.api-key}")
	private String internalApiKey;

	private final RestTemplate restTemplate;

	private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

	public JwtAuthInterceptor(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Authorization header is missing or invalid");
			return false;
		}

		String token = authHeader.substring(7);

		// Prepara a chamada para o auth-service (Go)
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("X-Internal-Api-Key", internalApiKey);

		AuthRequest authRequest = new AuthRequest(token);
		HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest, headers);

		try {
			// Executa a chamada
			ResponseEntity<AuthResponse> authResponseEntity = restTemplate.exchange(authServiceUrl, HttpMethod.POST,
					entity, AuthResponse.class);

			AuthResponse authResponse = authResponseEntity.getBody();

			if (authResponseEntity.getStatusCode().is2xxSuccessful() && authResponse != null
					&& authResponse.isValid()) {
				log.info("Token valid for userId: {}", authResponse.getUserId());
				
				// Se o token for válido, anexa o userId à requisição para o controller usar
				request.setAttribute("userId", authResponse.getUserId());
				return true; // Permite que a requisição continue
			}
		} catch (Exception e) {
			log.error("Error validating token: " + e.getMessage());
		}

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write("Invalid token");
		return false;
	}
}
