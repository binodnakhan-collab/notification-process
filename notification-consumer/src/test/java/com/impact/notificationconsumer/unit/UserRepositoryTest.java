package com.impact.notificationconsumer.unit;

import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(20L)
                .fullName("Binod Nakhan")
                .country("Nepal")
                .address("Bhaktapur")
                .doe(LocalDateTime.now())
                .email("binod@yopmail.com")
                .notificationEventContext("{\"userId\": 123, \"message\": \"Welcome to our service!\", \"eventType\": \"USER_REGISTERED\"}")
                .build();
    }

    @Test
    void shouldSaveUserWhenUserRepositorySaveCalled() {
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        UserEntity savedUser = userRepository.save(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(20L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldReturnUserWhenFindById() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        Optional<UserEntity> foundUser = userRepository.findById(20L);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(20L);
        verify(userRepository, times(1)).findById(20L);
    }

    @Test
    void shouldReturnAllUserWhenDoFindAll() {
        UserEntity user1 = UserEntity.builder()
                .id(100L)
                .build();
        List<UserEntity> userList = Arrays.asList(user, user1);
        when(userRepository.findAll()).thenReturn(userList);
        List<UserEntity> users = userRepository.findAll();
        assertThat(users).hasSize(2);
        verify(userRepository,times(1)).findAll();
    }
}
