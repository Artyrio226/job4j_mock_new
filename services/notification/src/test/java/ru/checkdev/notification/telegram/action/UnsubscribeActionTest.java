package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TelegramProfile;
import ru.checkdev.notification.service.TelegramProfileService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnsubscribeActionTest {
    @Mock
    private TelegramProfileService telegramProfileService;
    private TelegramProfile telegramProfile;
    @InjectMocks
    private UnsubscribeAction unsubscribeAction;
    @Mock
    private Message messageMock;

    @BeforeEach
    void setUp() {
        telegramProfile = new TelegramProfile(0, "12345", "test@mail", false);
    }

    @Test
    void whenHandleUnsubscribeSuccess() {
        long chatId = 123456789L;

        when(messageMock.getChatId()).thenReturn(chatId);
        when(telegramProfileService.findByChatId(anyString())).thenReturn(Optional.of(telegramProfile));

        var actualAnswer = (SendMessage) unsubscribeAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Подписка отменена.");
    }

    @Test
    void whenHandleAndUserNotRegisteredThenGetNotRegisteredMessage() {
        long chatId = 123456789L;

        when(messageMock.getChatId()).thenReturn(chatId);
        when(telegramProfileService.findByChatId(anyString())).thenReturn(Optional.empty());

        var actualAnswer = (SendMessage) unsubscribeAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }

    @Test
    public void whenCallbackThenInvokeHandle() {

        when(telegramProfileService.findByChatId(anyString())).thenReturn(Optional.empty());

        var actualAnswer = (SendMessage) unsubscribeAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }
}