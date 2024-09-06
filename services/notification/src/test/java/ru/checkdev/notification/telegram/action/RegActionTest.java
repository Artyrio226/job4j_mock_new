package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegActionTest {

    @InjectMocks
    private RegAction regAction;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClint;
    @Mock
    Message messageMock;

    @BeforeEach
    void setUp() {
        messageMock = new Message();
        messageMock.setChat(new Chat(123L, "test"));
        messageMock.setText("testName testPass");
        messageMock.setFrom(new User(123L, "test", true));
    }

    @Test
    void whenHandleAndUserNoExistsThenGetRegisterMessage() {

        SendMessage actualAnswer = (SendMessage) regAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Введите email для регистрации:");
    }

    @Test
    void whenCallbackAndInvalidEmailThenGetInvalidEmailMessage() {
        messageMock.setText("invalid_email");

        var actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Email: invalid_email не корректный." + System.lineSeparator()
                          + "Попробуйте снова." + System.lineSeparator() + "/new");
    }

    @Test
    void whenCallbackAndAuthServiceErrorThenGetAuthServiceErrorMessage() {

        messageMock.setText("test@example.com");

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(new RuntimeException("Auth service error"));

        var actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен попробуйте позже."
                                                    + System.lineSeparator() + "/start");
    }

    @Test
    void whenCallbackAndServerResponseHaveErrorObjectThenGetErrorMessage() {

        messageMock.setText("test@example.com");

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Ошибка регистрации: Error Info");
    }

    @Test
    void whenCallbackAndServerResponseHaveNoErrorThenGetSubscriptionCompletedMessage() {

        messageMock.setText("test@example.com");

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public int getId() {
                        return 1;
                    }
                }));

        SendMessage actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Вы зарегистрированы: ")
                .contains("Логин: test@example.com")
                .contains("Пароль: tg/");
    }
}