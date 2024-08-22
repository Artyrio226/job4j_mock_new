package ru.checkdev.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramProfileTest {

    private TelegramProfile telegramProfile;

    @BeforeEach
    public void setUp() {
        telegramProfile = TelegramProfile.builder()
                .id(0)
                .chatId("12345")
                .email("email")
                .subscribed(true)
                .build();
    }

    @Test
    void testGetId() {
        assertEquals(0, telegramProfile.getId());
    }

    @Test
    void testGetEmail() {
        assertEquals("email", telegramProfile.getEmail());
    }

    @Test
    void testGetChatId() {
        telegramProfile.setChatId("54321");
        assertEquals("54321", telegramProfile.getChatId());
    }

    @Test
    void testIsSubscribed() {
        telegramProfile.setSubscribed(true);
        assertTrue(telegramProfile.isSubscribed());
    }
}