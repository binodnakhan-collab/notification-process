package com.impact.notificationconsumer.unit;

import com.impact.notificationconsumer.controller.UserController;
import com.impact.notificationconsumer.exception.CustomException;
import com.impact.notificationconsumer.exception.GlobalExceptionHandler;
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
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private GlobalResponse mockUserListResponse;
    private GlobalResponse mockUserDetailResponse;

    @BeforeEach
    void setUp() {

        //Mock user data
        Map<String, Object> mockUserData = getMockUserData();

        // setup pagination response mock
        Map<String, Object> listData = new HashMap<>();
        listData.put("totalElements", 1);
        listData.put("result", List.of(mockUserData));

        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new GlobalExceptionHandler()).build();
        mockUserListResponse = new GlobalResponse("User list fetch success.", listData);
        mockUserDetailResponse = new GlobalResponse("User detail fetch success.", mockUserData);
    }

    @NonNull
    private static Map<String, Object> getMockUserData() {
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
    void shouldReturnUserListSuccessResponse() throws Exception {
        when(userService.getAllUsers(any(PaginationRequest.class))).thenReturn(mockUserListResponse);
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
        when(userService.getAllUsers(any(PaginationRequest.class))).thenReturn(mockUserListResponse);
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

    @Test
    void shouldReturnEmptyArrayWhenUserListIsEmpty() throws Exception {
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("totalElements", 0);
        emptyData.put("result", Collections.emptyList());

        GlobalResponse emptyResponse = new GlobalResponse("User list fetch success.", emptyData);
        when(userService.getAllUsers(any(PaginationRequest.class))).thenReturn(emptyResponse);
        mockMvc.perform(get("/users")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.result").isEmpty());

    }

    @Test
        /*
         * here missing sortBy and sortDirection
         * */
    void shouldReturnBadRequestWhenMissingQueryParamsAndNeverCallService() throws Exception {
        mockMvc.perform(get("/users")
                        .param("pageNo", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
        verify(userService, never()).getAllUsers(any(PaginationRequest.class));

    }

    @Test
    void shouldReturnUserDetailSuccess() throws Exception {
        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(mockUserDetailResponse);
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void shouldCallServiceWithCorrectUserId() throws Exception {
        Long userId = 10L;
        when(userService.getUserById(userId)).thenReturn(mockUserDetailResponse);
        mockMvc.perform(get("/users/{id}", userId)).andExpect(status().isOk());
        verify(userService, times(1)).getUserById(eq(10L));
        verify(userService, never()).getUserById(1L);
    }

    @Test
    void shouldReturnBadRequestWhenPathVariableIsInvalid() throws Exception {
        mockMvc.perform(get("/users/{id}", "test"))
                .andExpect(status().isBadRequest());
        verify(userService, never()).getAllUsers(any(PaginationRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenUserIdNotFound() throws Exception {
        Long nonExistUserId = 100L;
        when(userService.getUserById(nonExistUserId)).thenThrow(new CustomException("User not found with id: ", HttpStatus.NOT_FOUND, nonExistUserId));
        mockMvc.perform(get("/users/{id}", nonExistUserId))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).getUserById(nonExistUserId);

    }
}
