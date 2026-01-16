package com.impact.notificationconsumer.payload.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String address;
    private String country;
    private String notificationEventContext;
    private LocalDateTime doe;
}
