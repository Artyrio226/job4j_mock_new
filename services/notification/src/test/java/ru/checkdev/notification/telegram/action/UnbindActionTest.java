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
class UnbindActionTest {
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClint;

    @InjectMocks
    private UnbindAction unbindAction;
    @Mock
    private Message messageMock;

    @BeforeEach
    void setUp() {
        messageMock = new Message();
        messageMock.setChat(new Chat(123L, "test"));
        messageMock.setText("testName testPass");
        messageMock.setFrom(new User(123L, "test", true));
    }

    @Test
    public void testHandle() {
        var actualAnswer = (SendMessage) unbindAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Введите логин и пароль через пробел для отвязки аккаунта telegram от платформы CheckDev");
    }

    @Test
    void whenCallbackAndAuthServiceErrorThenGetAuthServiceErrorMessage() {

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(new RuntimeException("Auth service error"));

        var actualAnswer = (SendMessage) unbindAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен попробуйте позже."
                                                    + System.lineSeparator() + "/start");
    }

    @Test
    void whenCallbackAndServerResponseHaveErrorObjectThenGetErrorMessage() {

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) unbindAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Ошибка отвязки: Error Info");
    }

    @Test
    void whenHandleUnbindSuccess() {

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getOk() {
                        return "ok";
                    }
                }));

        var actualAnswer = (SendMessage) unbindAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Аккаунт telegram отвязан от платформы CheckDev.");
    }

    @Test
    public void whenCallbackThenInvalidFormat() {
        messageMock.setText("test");

        var actualAnswer = (SendMessage) unbindAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Неправильный формат ввода.");
    }

}