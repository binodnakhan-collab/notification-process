package com.impact.kafkagenerator.utils;

import java.util.Random;

import static com.impact.kafkagenerator.utils.Constants.CONTENTS;
import static com.impact.kafkagenerator.utils.Constants.MESSAGE_TYPES;

public class Helper {

    private Helper() {
    }

    private static final Random random = new Random();

    public static String generateUserId() {
        int userIdNum = random.nextInt(1000) + 1;
        return String.valueOf(userIdNum);
    }

    public static String getRandomMessageType() {
        return MESSAGE_TYPES.get(random.nextInt(MESSAGE_TYPES.size()));
    }

    public static String getRandomContent() {
        return CONTENTS.get(random.nextInt(CONTENTS.size()));
    }
}
