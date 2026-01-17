package com.impact.notificationconsumer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.notificationconsumer.config.TestContainersConfig;
import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.repository.UserRepository;
import com.impact.notificationconsumer.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
public class UserControllerTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturn200WithUserList() throws Exception {
        mockMvc.perform(get("/users")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User list fetch success."))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.result", hasSize(3)));
    }

    @Test
    void shouldReturn200WhenValidUserId() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User detail fetch success."));
    }

    @Test
    void shouldReturn404StatusWhenUserIdNotExist() throws Exception {
        Long userId = 10L;
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found with id: " + userId));

    }

}
