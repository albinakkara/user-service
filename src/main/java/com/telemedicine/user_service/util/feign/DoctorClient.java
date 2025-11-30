package com.telemedicine.user_service.util.feign;

import com.telemedicine.user_service.dto.UserCreationRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "doctor-service")
public interface DoctorClient {

    @PostMapping("/internal/doctors")
    public ResponseEntity<Boolean> createDoctorSkeleton(@Valid @RequestBody UserCreationRequest request);

}

//public interface PatientClient {
//
//    @PostMapping("/internal/patients")
//    String createPatient(@RequestBody UserCreationRequest request);
