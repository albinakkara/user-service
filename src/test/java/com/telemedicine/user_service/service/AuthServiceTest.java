package com.telemedicine.user_service.service;

import com.telemedicine.user_service.data.entity.User;
import com.telemedicine.user_service.data.repository.UserRepository;
import com.telemedicine.user_service.dto.LoginRequestDto;
import com.telemedicine.user_service.dto.RegisterRequestDto;
import com.telemedicine.user_service.dto.UserCreationRequest;
import com.telemedicine.user_service.util.feign.DoctorClient;
import com.telemedicine.user_service.util.feign.PatientClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private PatientClient patientClient;

    @Mock
    private DoctorClient doctorClient;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("PATIENT");
    }

    @Test
    void register_newPatient_savesUserAndCallsPatientClient() {
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("password123")).thenReturn("encoded-pass");

        String result = authService.register(registerRequest);

        assertEquals("Registration successful!", result);

        // User saved with encoded password and correct role
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("john@example.com", saved.getEmail());
        assertEquals("encoded-pass", saved.getPassword());
        assertEquals("PATIENT", saved.getRole());

        // Patient skeleton created, doctor client NOT called
        verify(patientClient, times(1)).createPatientSkeleton(any(UserCreationRequest.class));
        verify(doctorClient, never()).createDoctorSkeleton(any(UserCreationRequest.class));
    }

    @Test
    void register_newDoctor_callsDoctorClient() {
        RegisterRequestDto doctorReq = new RegisterRequestDto();
        doctorReq.setFirstName("Doc");
        doctorReq.setLastName("Tor");
        doctorReq.setEmail("doc@example.com");
        doctorReq.setPassword("docpass");
        doctorReq.setRole("DOCTOR");

        when(userRepo.findByEmail("doc@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("docpass")).thenReturn("encoded-doc");

        String result = authService.register(doctorReq);

        assertEquals("Registration successful!", result);
        verify(doctorClient, times(1)).createDoctorSkeleton(any(UserCreationRequest.class));
        verify(patientClient, never()).createPatientSkeleton(any(UserCreationRequest.class));
    }

    @Test
    void register_existingUser_throwsRuntimeException() {
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void validateUser_success_returnsUser() {
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("john@example.com");
        login.setPassword("plain-pass");

        User stored = new User();
        stored.setId(1L);
        stored.setEmail("john@example.com");
        stored.setPassword("encoded-pass");
        stored.setRole("PATIENT");

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(stored));
        when(encoder.matches("plain-pass", "encoded-pass")).thenReturn(true);

        User result = authService.validateUser(login);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("PATIENT", result.getRole());
    }

    @Test
    void validateUser_userNotFound_throwsRuntimeException() {
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("missing@example.com");
        login.setPassword("pass");

        when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.validateUser(login));
    }

    @Test
    void validateUser_invalidPassword_throwsRuntimeException() {
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("john@example.com");
        login.setPassword("wrong-pass");

        User stored = new User();
        stored.setEmail("john@example.com");
        stored.setPassword("encoded-pass");

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(stored));
        when(encoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.validateUser(login));
    }
}
