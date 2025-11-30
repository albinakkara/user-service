package com.telemedicine.user_service.data.repository;

import com.telemedicine.user_service.data.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserRepositoryTest {

    @Test
    void findByEmail_returnsEmptyOptionalByDefault() {
        UserRepository repo = Mockito.mock(UserRepository.class);

        when(repo.findByEmail("test@example.com")).thenReturn(Optional.empty());

        Optional<User> result = repo.findByEmail("test@example.com");

        assertTrue(result.isEmpty());
    }
}
