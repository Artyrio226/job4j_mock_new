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
class BindActionTest {
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClint;
    @InjectMocks
    private BindAction bindAction;
    private Message messageMock;

    @BeforeEach
    void setUp() {
        messageMock = new Message();
        messageMock.setChat(new Chat(123L, "test"));
        messageMock.setText("testName testPass");
        messageMock.setFrom(new User(123L, "test", true));
    }

    @Test
    void whenHandleAndUserExistsThenGetEnterPasswordMessage() {
        SendMessage actualAnswer = (SendMessage) bindAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains(
                "Введите логин и пароль через пробел для привязки аккаунта telegram к платформе CheckDev");
    }

    @Test
    void whenCallbackAndAuthServerNotAvailableThenExceptionLogAndServiceNotAvailableMessage() {

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(new RuntimeException("Auth service error"));

        var actualAnswer = (SendMessage) bindAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен попробуйте позже"
                                                    + System.lineSeparator() + "/start");
    }

    @Test
    void whenCallbackAndServerResponseHaveErrorObjectThenGetErrorMessage() {

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Пароль введен не верно.";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) bindAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Ошибка привязки: Пароль введен не верно.");
    }

    @Test
    void whenCallbackAndServerResponseHaveNoErrorThenGetSubscriptionCompletedMessage() {

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getOk() {
                        return "ok";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) bindAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Аккаунт telegram привязан к платформе CheckDev");
    }
}