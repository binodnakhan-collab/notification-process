package com.impact.kafkagenerator.utils;

import java.util.List;

public class Constants {

    private Constants() {

    }

    public static final List<String> CONTENTS = List.of(
            "Welcome to our platform!",
            "New message from support.",
            "Account verification success.",
            "Discount offer on sale.",
            "Daily transaction report.",
            "Your subscription is expiring soon.",
            "Payment receipt."
    );

    public static final List<String> MESSAGE_TYPES = List.of(
            "EMAIL", "SMS", "PUSH_NOTIFICATION"
    );
}
