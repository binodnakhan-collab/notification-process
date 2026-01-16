package com.impact.notificationconsumer.unit;

import com.impact.notificationconsumer.controller.UserController;
import com.impact.notificationconsumer.payload.request.PaginationRequest;
import com.impact.notificationconsumer.payload.response.GlobalResponse;
import com.impact.notificationconsumer.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("User controller tests")
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private GlobalResponse mockGlobalResponse;

    @BeforeEach
    void setUp() {

        // mock user data
        Map<String, Object> mockUserData = getDataMap();

        // setup pagination response mock
        Map<String, Object> listData = new HashMap<>();
        listData.put("totalElements", 1);
        listData.put("result", List.of(mockUserData));

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        mockGlobalResponse = new GlobalResponse("Data fetch success.", listData);
    }

    @NonNull
    private static Map<String, Object> getDataMap() {
        Map<String, Object> mockUserData = new HashMap<>();
        mockUserData.put("id", 1L);
        mockUserData.put("email", "binod@yopmail.com");
        mockUserData.put("fullName", "Binod Nakhan");
        mockUserData.put("address", "Bhaktapur");
        mockUserData.put("country", "Nepal");
        mockUserData.put("notificationEventContext",
                "{\"userId\": 123, \"message\": \"Welcome to our service!\", \"eventType\": \"USER_REGISTERED\"}");
        mockUserData.put("doe", "2026-01-01T05:45:00.02");
        return mockUserData;
    }

    @Test
    void shouldReturnSuccessResponse() throws Exception {
        when(userService.getAllUsers(any(PaginationRequest.class))).thenReturn(mockGlobalResponse);
        mockMvc.perform(get("/users")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
        verify(userService, times(1)).getAllUsers(any(PaginationRequest.class));
    }

    @Test
    void shouldVerifyValidPaginationParam() throws Exception {
        when(userService.getAllUsers(any(PaginationRequest.class))).thenReturn(mockGlobalResponse);
        ArgumentCaptor<PaginationRequest> captor = ArgumentCaptor.forClass(PaginationRequest.class);

        mockMvc.perform(get("/users")
                        .param("pageNo", "2")
                        .param("pageSize", "20")
                        .param("sortBy", "email")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());
        verify(userService).getAllUsers(captor.capture());
        PaginationRequest captureRequest = captor.getValue();
        assertThat(captureRequest.getPageNo()).isEqualTo(2);
        assertThat(captureRequest.getPageSize()).isEqualTo(20);
        assertThat(captureRequest.getSortBy()).isEqualTo("email");
        assertThat(captureRequest.getSortDirection()).isEqualTo("DESC");
    }


}
