package com.telemedicine.user_service.util.feign;

import com.telemedicine.user_service.dto.UserCreationRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @PostMapping("/internal/patients")
    String createPatientSkeleton(@Valid @RequestBody UserCreationRequest request);
}

