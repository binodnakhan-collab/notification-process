package com.impact.notificationconsumer.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.external.UserExternalCommunication;
import com.impact.notificationconsumer.payload.request.NotificationEvent;
import com.impact.notificationconsumer.payload.response.ExternalUserResponse;
import com.impact.notificationconsumer.repository.UserRepository;
import com.impact.notificationconsumer.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationEventProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserExternalCommunication userExternalCommunication;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private String validJsonMessage;
    private NotificationEvent validNotificationEvent;
    private ExternalUserResponse validUserResponse;

    @BeforeEach
    void setUp() {
        validJsonMessage = "{\"userId\": 511, \"content\": \"Welcome to our service!\", \"messageType\": \"EMAIL\"}";

        validNotificationEvent = new NotificationEvent();
        validNotificationEvent.setUserId("511");
        validNotificationEvent.setContent("Welcome to our service!");
        validNotificationEvent.setMessageType("EMAIL");

        validUserResponse = new ExternalUserResponse();
        validUserResponse.setUserId(511L);
        validUserResponse.setFirstName("Binod");
        validUserResponse.setLastName("Nakhan");
        validUserResponse.setEmail("binod@yopmail.com");
        validUserResponse.setCountry("Nepal");
        validUserResponse.setAddress("Bhaktapur");
        validUserResponse.setPhoneNumber("9766919210");
        validUserResponse.setCity("Bhaktapur");
        validUserResponse.setZipCode("44800");
        validUserResponse.setState("Bagmati");
    }


    @Test
    void shouldProcessEventWithValidMessageAndSaveUser() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        userService.processNotificationEvent(validJsonMessage);

        verify(objectMapper, times(1)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(1)).getUserDetail(511L);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void shouldProcessEventWithInvalidJsonAndShouldNotSaveUser() throws JsonProcessingException {
        String invalidJsonMessage = "{json string}";
        when(objectMapper.readValue(invalidJsonMessage, NotificationEvent.class)).thenThrow(new JsonProcessingException("Invalid Json") {
        });

        userService.processNotificationEvent(invalidJsonMessage);

        verify(objectMapper, times(1)).readValue(invalidJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, never()).getUserDetail(anyLong());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldNotSaveUserWhenExternalApiWhenExternalServiceDown() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L))
                .thenThrow(new WebClientRequestException(
                        new ConnectException("Connection refused."),
                        HttpMethod.GET,
                        URI.create("http://localhost:7200/api/user/metadata/511"),
                        HttpHeaders.EMPTY
                ));
        assertThrows(WebClientRequestException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });

        verify(objectMapper, times(1)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(1)).getUserDetail(511L);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldNotSaveUserWhenUserIdNotFoundInExternalApi() throws JsonProcessingException {
        NotificationEvent notFoundEvent = new NotificationEvent();
        notFoundEvent.setUserId("512");
        notFoundEvent.setContent("Welcome to our service!");
        notFoundEvent.setMessageType("EMAIL");

        String notFoundMessage = "{\"userId\": 512, \"content\": \"Welcome!\", \"messageType\": \"EMAIL\"}";
        when(objectMapper.readValue(notFoundMessage, NotificationEvent.class)).thenReturn(notFoundEvent);
        when(userExternalCommunication.getUserDetail(512L)).thenReturn(null);

        userService.processNotificationEvent(notFoundMessage);

        verify(objectMapper, times(1)).readValue(notFoundMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(1)).getUserDetail(512L);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldNotSaveUserWhenDuplicateUserIdFoundInMessage() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.existsById(511L)).thenReturn(false)
                .thenReturn(true);
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        userService.processNotificationEvent(validJsonMessage);
        userService.processNotificationEvent(validJsonMessage);
        verify(objectMapper, times(2)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(2)).getUserDetail(511L);
        verify(userRepository, times(2)).existsById(511L);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void shouldNotSaveUserWhenExternalApiReturn404Error() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L))
                .thenThrow(WebClientResponseException.create(
                        404,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        "User not found".getBytes(),
                        null
                ));

        assertThrows(WebClientResponseException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });

        verify(objectMapper, times(1)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(1)).getUserDetail(511L);
        verify(userRepository, never()).existsById(anyLong());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldNotSaveUserWhenExternalApiTimesOut() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L))
                .thenThrow(new WebClientRequestException(
                        new TimeoutException("Read timeout"),
                        HttpMethod.GET,
                        URI.create("http://localhost:7200/api/user/metadata/511"),
                        HttpHeaders.EMPTY
                ));

        assertThrows(WebClientRequestException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });

        verify(objectMapper, times(1)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(1)).getUserDetail(511L);
        verify(userRepository, never()).existsById(anyLong());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldRetryAndSucceedWhenExternalApiFailsFirstTimeThenSucceeds() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L))
                .thenThrow(WebClientResponseException.create(
                        503,
                        "Service Unavailable",
                        HttpHeaders.EMPTY,
                        "Temporary error".getBytes(),
                        null
                ))
                .thenReturn(validUserResponse);
        when(userRepository.existsById(511L))
                .thenReturn(false);
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(WebClientResponseException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });
        userService.processNotificationEvent(validJsonMessage);
        verify(objectMapper, times(2)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(2)).getUserDetail(511L);
        verify(userRepository, times(1)).existsById(511L);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void shouldExternalApiFailWhenMaxRetries() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L))
                .thenThrow(WebClientResponseException.create(
                        503,
                        "Service Unavailable",
                        HttpHeaders.EMPTY,
                        "Service unavailable".getBytes(),
                        null
                ));

        assertThrows(WebClientResponseException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });
        assertThrows(WebClientResponseException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });
        assertThrows(WebClientResponseException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });

        verify(objectMapper, times(3)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(3)).getUserDetail(511L);
        verify(userRepository, never()).existsById(anyLong());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldMapFieldFromExternalResponseToUser() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.existsById(511L)).thenReturn(false);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        userService.processNotificationEvent(validJsonMessage);
        verify(userRepository, times(1)).save(userCaptor.capture());
        UserEntity user = userCaptor.getValue();
        assertNotNull(user);
        assertEquals("Binod Nakhan", user.getFullName());
        assertEquals("binod@yopmail.com",  user.getEmail());
        assertEquals(validJsonMessage, user.getNotificationEventContext());
    }

    @Test
    void shouldConcatFirstnameAndLastNameFromExternalApiToUserFullName() throws JsonProcessingException {
        ExternalUserResponse userWithNames = new ExternalUserResponse();
        userWithNames.setUserId(511L);
        userWithNames.setFirstName("Binod");
        userWithNames.setLastName("Nakhan");

        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(userWithNames);
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        userService.processNotificationEvent(validJsonMessage);
        verify(userRepository, times(1)).save(userCaptor.capture());
        UserEntity user = userCaptor.getValue();
        assertEquals("Binod Nakhan", user.getFullName());
    }

    @Test
    void shouldSetDoeToCurrentTimestamp() throws JsonProcessingException {
        LocalDateTime beforeExecution = LocalDateTime.now().minusSeconds(1);
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.existsById(511L)).thenReturn(false);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        userService.processNotificationEvent(validJsonMessage);
        LocalDateTime afterExecution = LocalDateTime.now().plusSeconds(1);

        verify(userRepository, times(1)).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getDoe());
        assertTrue(savedUser.getDoe().isAfter(beforeExecution) || savedUser.getDoe().isEqual(beforeExecution));
        assertTrue(savedUser.getDoe().isBefore(afterExecution) || savedUser.getDoe().isEqual(afterExecution));
    }

    @Test
    void shouldHandleMissingFirstNameByCreatingPartialFullName() throws JsonProcessingException {
        ExternalUserResponse userWithMissingFirstName = new ExternalUserResponse();
        userWithMissingFirstName.setUserId(511L);
        userWithMissingFirstName.setFirstName(null);
        userWithMissingFirstName.setLastName("Nakhan");

        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(userWithMissingFirstName);
        when(userRepository.existsById(511L)).thenReturn(false);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        userService.processNotificationEvent(validJsonMessage);
        verify(userRepository, times(1)).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertEquals("null Nakhan", savedUser.getFullName());
    }

    @Test
    void shouldHandleMissingCountryByStoringNull() throws JsonProcessingException {
        ExternalUserResponse userWithMissingCountry = new ExternalUserResponse();
        userWithMissingCountry.setUserId(511L);
        userWithMissingCountry.setFirstName("Binod");
        userWithMissingCountry.setLastName("Nakhan");
        userWithMissingCountry.setEmail("binod@yopmail.com");
        userWithMissingCountry.setState(null);

        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(userWithMissingCountry);
        when(userRepository.existsById(511L)).thenReturn(false);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        userService.processNotificationEvent(validJsonMessage);

        verify(userRepository, times(1)).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertNull(savedUser.getCountry());
    }

    @Test
    void shouldSuccessfullySaveUserToDatabase() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.existsById(511L)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity user = invocation.getArgument(0);
                    user.setFullName("Binod Nakhan");
                    user.setAddress("Sudal");
                    user.setCountry("Nepal");
                    user.setDoe(LocalDateTime.now());
                    user.setEmail("binod@yopmail.com");
                    user.setNotificationEventContext(validJsonMessage);
                    return user;
                });
        userService.processNotificationEvent(validJsonMessage);

        verify(userRepository, times(1)).save(any(UserEntity.class));

    }

    @Test
    void shouldNotSaveUserWhenDuplicateUserIdFoundInDatabase() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.existsById(511L)).thenReturn(true);

        userService.processNotificationEvent(validJsonMessage);
        verify(userRepository, times(1)).existsById(511L);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenNonNullConstrainViolation() throws JsonProcessingException {
        ExternalUserResponse userWithNullEmail = new ExternalUserResponse();
        userWithNullEmail.setUserId(511L);
        userWithNullEmail.setFirstName("Binod");
        userWithNullEmail.setLastName("Nakhan");
        userWithNullEmail.setEmail(null);

        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(userWithNullEmail);
        when(userRepository.existsById(511L)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "not-null property references a null or transient value : " +
                                "com.impact.notificationconsumer.entity.UserEntity.email"
                ));
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });
        verify(objectMapper, times(1)).readValue(validJsonMessage, NotificationEvent.class);
        verify(userExternalCommunication, times(1)).getUserDetail(511L);
        verify(userRepository, times(1)).existsById(511L);
    }

    @Test
    void shouldThrowExceptionWhenDatabaseConnectionFails() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.existsById(511L))
                .thenThrow(new CannotCreateTransactionException(
                        "Database connection failed."
                ));
        assertThrows(CannotCreateTransactionException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });
        verify(userRepository, times(1)).existsById(511L);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldHandleOptimisticLockingException() throws JsonProcessingException {
        when(objectMapper.readValue(validJsonMessage, NotificationEvent.class)).thenReturn(validNotificationEvent);
        when(userExternalCommunication.getUserDetail(511L)).thenReturn(validUserResponse);
        when(userRepository.existsById(511L)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class)))
                .thenThrow(new OptimisticLockingFailureException(
                        "Row was updated or deleted by another transaction"
                ));
        assertThrows(OptimisticLockingFailureException.class, () -> {
            userService.processNotificationEvent(validJsonMessage);
        });
        verify(userRepository, times(1)).existsById(511L);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}
