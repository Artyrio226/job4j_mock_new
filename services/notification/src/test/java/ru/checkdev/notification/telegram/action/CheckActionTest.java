package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.checkdev.notification.domain.TelegramProfile;
import ru.checkdev.notification.service.TelegramProfileService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckActionTest {

    @Mock
    private TelegramProfileService telegramProfileService;
    @InjectMocks
    private CheckAction checkAction;
    @Mock
    Message messageMock;
    private TelegramProfile telegramProfile;

    @BeforeEach
    public void setUp() {
        telegramProfile = new TelegramProfile(0, "12345", "test@example.com", false);
        when(messageMock.getChatId()).thenReturn(123L);
    }

    @Test
    public void whenHandleAndUserNoExistsThenUserNotRegistered() {

        when(telegramProfileService.findByChatId(anyString())).thenReturn(Optional.empty());

        var actualAnswer = (SendMessage) checkAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }

    @Test
    public void whenHandleAndUserExistsThenUserRegistered() {
        User user = new User();
        String firstName = "John";
        user.setFirstName(firstName);

        when(messageMock.getFrom()).thenReturn(user);
        when(telegramProfileService.findByChatId(anyString())).thenReturn(Optional.of(telegramProfile));

        var actualAnswer = (SendMessage) checkAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Ваше имя: " + firstName + System.lineSeparator() + "Ваша почта: test@example.com");
    }

    @Test
    public void whenCallbackThenInvokeHandle() {

        when(telegramProfileService.findByChatId(anyString())).thenReturn(Optional.empty());

        var actualAnswer = (SendMessage) checkAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }
}
