package com.impact.notificationconsumer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.notificationconsumer.config.TestContainersConfig;
import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.exception.CustomException;
import com.impact.notificationconsumer.payload.request.PaginationRequest;
import com.impact.notificationconsumer.payload.response.DataPaginationResponse;
import com.impact.notificationconsumer.payload.response.GlobalResponse;
import com.impact.notificationconsumer.payload.response.UserResponse;
import com.impact.notificationconsumer.repository.UserRepository;
import com.impact.notificationconsumer.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Import(TestContainersConfig.class)
public class UserServiceTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity testUser1;
    private UserEntity testUser2;
    private UserEntity testUser3;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser1 = new UserEntity();
        testUser1.setFullName("John Doe");
        testUser1.setEmail("john.doe@example.com");
        testUser1.setAddress("Bhaktapur");
        testUser1.setDoe(LocalDateTime.now());
        testUser1.setCountry("Nepal");
        testUser1.setNotificationEventContext("{\"userId\": 500, \"content\": \"Welcome to our service!\", \"messageType\": \"EMAIL\"}");

        testUser2 = new UserEntity();
        testUser2.setFullName("Jane Smith");
        testUser2.setEmail("jane.smith@example.com");
        testUser2.setAddress("Kathmandu");
        testUser2.setDoe(LocalDateTime.now());
        testUser2.setCountry("Nepal");
        testUser2.setNotificationEventContext("{\"userId\": 501, \"content\": \"Offer!\", \"messageType\": \"SMS\"}");

        testUser3 = new UserEntity();
        testUser3.setFullName("Binod Nakhan");
        testUser3.setEmail("binod@yopmail.com");
        testUser3.setAddress("Lalitpur");
        testUser3.setDoe(LocalDateTime.now());
        testUser3.setCountry("Nepal");
        testUser3.setNotificationEventContext("{\"userId\": 502, \"content\": \"Sales offer!\", \"messageType\": \"SMS\"}");

        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
    }

    @Test
    void shouldReturnAllUsersWithFirstPage() {
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setPageNo(0);
        paginationRequest.setPageSize(2);

        GlobalResponse response = userService.getAllUsers(paginationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("User list fetch success.");
        assertThat(response.isSuccess()).isTrue();
        DataPaginationResponse<UserResponse> paginationResponse = (DataPaginationResponse<UserResponse>) response.getData();

        assertThat(paginationResponse.totalElements()).isGreaterThanOrEqualTo(2);
        assertThat(paginationResponse.result().size()).isLessThanOrEqualTo(paginationRequest.getPageSize());

    }

    @Test
    void shouldReturnEmptyListWhenPageNoAndExceed() {
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setPageNo(10);
        paginationRequest.setPageSize(2);

        GlobalResponse response = userService.getAllUsers(paginationRequest);
        assertThat(response).isNotNull();
        DataPaginationResponse<UserResponse> paginationResponse = (DataPaginationResponse<UserResponse>) response.getData();
        assertThat(paginationResponse.totalElements()).isEqualTo(0);
        assertThat(paginationResponse.result().isEmpty());
    }

    @Test
    void shouldReturnUserDetailWhenValidUserId() {
        GlobalResponse response = userService.getUserById(testUser3.getId());
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("User detail fetch success.");
        assertThat(response.isSuccess()).isTrue();
        UserResponse userResponse = (UserResponse) response.getData();
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(testUser3.getId());
        assertThat(userResponse.getFullName()).isEqualTo("Binod Nakhan");
        assertThat(userResponse.getEmail()).isEqualTo("binod@yopmail.com");
    }

    @Test
    void shouldThrowCustomExceptionWhenUserNotFound() {
        Long userId = 100L;
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("User not found with id: " + userId);
    }

    @Test
    void shouldReturn404NotFoundWhenUserIdNotExist() {
        Long userId = 100L;
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("User not found with id: 100")
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }


}
