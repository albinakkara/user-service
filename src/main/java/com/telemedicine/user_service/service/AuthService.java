package com.telemedicine.user_service.service;

import com.telemedicine.user_service.data.entity.User;
import com.telemedicine.user_service.data.repository.UserRepository;
import com.telemedicine.user_service.dto.LoginRequestDto;
import com.telemedicine.user_service.dto.RegisterRequestDto;
import com.telemedicine.user_service.dto.UserCreationRequest;
import com.telemedicine.user_service.util.feign.DoctorClient;
import com.telemedicine.user_service.util.feign.PatientClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final PatientClient patientClient;
    private final DoctorClient doctorClient;

    AuthService(UserRepository userRepo, PasswordEncoder encoder, PatientClient patientClient, DoctorClient doctorClient){
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.patientClient = patientClient;
        this.doctorClient = doctorClient;
    }

    public String register(RegisterRequestDto req) {

        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(req.getRole());

        userRepo.save(user);

        callRoleService(req);

        return "Registration successful!";
    }

    public User validateUser(LoginRequestDto req) {

        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    private void callRoleService(RegisterRequestDto registerRequestDto) {

        String role = registerRequestDto.getRole();
        UserCreationRequest body = new UserCreationRequest(registerRequestDto.getFirstName(), registerRequestDto.getLastName(), registerRequestDto.getEmail());

        switch (role.toUpperCase()) {
            case "PATIENT" -> patientClient.createPatientSkeleton(body);
            case "DOCTOR" -> doctorClient.createDoctorSkeleton(body);
            case "ADMIN" -> {
            }
        }
    }
}

