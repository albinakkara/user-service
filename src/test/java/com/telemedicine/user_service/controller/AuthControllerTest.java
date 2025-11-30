package com.telemedicine.user_service.controller;

import com.telemedicine.user_service.data.entity.User;
import com.telemedicine.user_service.dto.LoginRequestDto;
import com.telemedicine.user_service.dto.RegisterRequestDto;
import com.telemedicine.user_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_returnsOkWithMessage() {
        RegisterRequestDto req = new RegisterRequestDto();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("password123");
        req.setRole("PATIENT");

        when(authService.register(any(RegisterRequestDto.class)))
                .thenReturn("User registered successfully!");

        // Controller method signature: ResponseEntity<?>
        ResponseEntity<?> response = authController.register(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        String body = (String) response.getBody();
        assertEquals("User registered successfully!", body);
    }

    @Test
    void login_returnsEmailAndRole() {
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("john@example.com");
        req.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setRole("PATIENT");

        when(authService.validateUser(any(LoginRequestDto.class))).thenReturn(user);

        // login() also returns ResponseEntity<?>
        ResponseEntity<?> response = authController.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();

        assertEquals("john@example.com", body.get("email"));
        assertEquals("PATIENT", body.get("role"));
    }
}
