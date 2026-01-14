package com.impact.kafkagenerator.payload;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent {

    private String userId;
    private String messageType;
    private String content;
}
