package com.telemedicine.user_service.util.feign;

import com.telemedicine.user_service.dto.UserCreationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @PostMapping("/internal/patients")
    String createPatient(@RequestBody UserCreationRequest request);
}

