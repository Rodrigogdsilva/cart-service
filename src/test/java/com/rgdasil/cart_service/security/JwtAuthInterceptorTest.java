package com.rgdasil.cart_service.security;

import com.rgdasil.cart_service.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;

@ExtendWith(MockitoExtension.class)
public class JwtAuthInterceptorTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;
    
    @Mock
    private PrintWriter printWriter; 

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(jwtAuthInterceptor, "authServiceUrl", "http://fake-auth-service/auth/validate");
        ReflectionTestUtils.setField(jwtAuthInterceptor, "internalApiKey", "fake-api-key");
    }

    @Test
    void whenTokenIsValid_thenPreHandleReturnsTrue() throws Exception {
        // --- ARRANGE ---
        String validToken = "valid-jwt-token";
        String userId = "user-abc-123";

        // Prepara uma resposta de sucesso do serviço de autenticação
        AuthResponse authResponse = new AuthResponse();
        authResponse.setValid(true);
        authResponse.setUserId(userId);
        ResponseEntity<AuthResponse> successResponseEntity = new ResponseEntity<>(authResponse, HttpStatus.OK);

        // Define o comportamento do mock do request
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Define o comportamento do mock do RestTemplate:
        // "QUANDO o método exchange for chamado com a URL, método POST, qualquer HttpEntity
        // e esperando um AuthResponse.class, ENTÃO retorne a nossa resposta de sucesso."
        when(restTemplate.exchange(
                eq("http://fake-auth-service/auth/validate"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(AuthResponse.class)
        )).thenReturn(successResponseEntity);

        // --- ACT ---
        boolean result = jwtAuthInterceptor.preHandle(request, response, handler);

        // --- ASSERT ---
        assertTrue(result, "O interceptor deveria retornar true para um token válido.");

        // Verifica se o userId foi corretamente adicionado como um atributo no request.
        verify(request, times(1)).setAttribute("userId", userId);
    }

    @Test
    void whenAuthorizationHeaderIsMissing_thenPreHandleReturnsFalse() throws Exception {
       
    	// --- ARRANGE ---
    	when(response.getWriter()).thenReturn(printWriter);
    	
        // Simula uma requisição sem o header de autorização.
        when(request.getHeader("Authorization")).thenReturn(null);

        // --- ACT ---
        boolean result = jwtAuthInterceptor.preHandle(request, response, handler);

        // --- ASSERT ---
        assertFalse(result, "O interceptor deveria retornar false se o header de autorização estiver ausente.");

        // Verifica se o status da resposta HTTP foi definido como 401 Unauthorized.
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void whenAuthorizationHeaderIsMalformed_thenPreHandleReturnsFalse() throws Exception {
        
    	// --- ARRANGE ---
    	when(response.getWriter()).thenReturn(printWriter);
        // Simula um header malformado (sem "Bearer ").
        when(request.getHeader("Authorization")).thenReturn("InvalidTokenFormat");

        // --- ACT ---
        boolean result = jwtAuthInterceptor.preHandle(request, response, handler);

        // --- ASSERT ---
        assertFalse(result, "O interceptor deveria retornar false para um header malformado.");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    @Test
    void whenAuthServiceReturnsInvalid_thenPreHandleReturnsFalse() throws Exception {
    	
    	// --- ARRANGE ---
    	when(response.getWriter()).thenReturn(printWriter);
        String invalidToken = "invalid-jwt-token";
        
        // Prepara uma resposta de falha do auth-service
        AuthResponse authResponse = new AuthResponse();
        authResponse.setValid(false);
        ResponseEntity<AuthResponse> failureResponseEntity = new ResponseEntity<>(authResponse, HttpStatus.OK);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(AuthResponse.class)))
            .thenReturn(failureResponseEntity);

        // --- ACT ---
        boolean result = jwtAuthInterceptor.preHandle(request, response, handler);

        // --- ASSERT ---
        assertFalse(result, "O interceptor deveria retornar false se o auth-service invalidar o token.");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Garante que o atributo userId NÃO foi adicionado ao request
        verify(request, never()).setAttribute(eq("userId"), any());
    }

    @Test
    void whenAuthServiceCallFails_thenPreHandleReturnsFalse() throws Exception {
        
    	// --- ARRANGE ---
    	when(response.getWriter()).thenReturn(printWriter);
        String token = "any-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Simula uma exceção na chamada ao auth-service (ex: serviço fora do ar).
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(AuthResponse.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        // --- ACT ---
        boolean result = jwtAuthInterceptor.preHandle(request, response, handler);

        // --- ASSERT ---
        assertFalse(result, "O interceptor deveria retornar false se a chamada ao auth-service falhar.");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}