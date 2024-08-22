package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TelegramProfile;
import ru.checkdev.notification.service.TelegramProfileService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribeActionTest {

    @Mock
    private TelegramProfileService tgUserServiceMock;
    @Mock
    private TgAuthCallWebClint authCallWebClint;

    private TelegramProfile telegramProfile;
    private SubscribeAction regAction;
    private Message messageMock;

    @BeforeEach
    void setUp() {
        regAction = new SubscribeAction(tgUserServiceMock, authCallWebClint);
        messageMock = new Message();
        messageMock.setChat(new Chat(1L, "private"));
        telegramProfile = new TelegramProfile(0, "12345", "test@mail", false);
    }

    @Test
    void whenHandleAndUserNotExistsThenGetNotExistsMessage() {

        when(tgUserServiceMock.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.empty());

        SendMessage actualAnswer = (SendMessage) regAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы" + System.lineSeparator() + "/start");
    }

    @Test
    void whenHandleAndUserExistsThenGetEnterPasswordMessage() {

        when(tgUserServiceMock.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.of(telegramProfile));

        SendMessage actualAnswer = (SendMessage) regAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Введите пароль для подтверждения:");
    }

    @Test
    void whenCallbackAndAuthServerNotAvailableThenExceptionLogAndServiceNotAvailableMessage() {

        when(tgUserServiceMock.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.of(telegramProfile));
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(new RuntimeException("Auth service error"));

        var actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен попробуйте позже"
                                                    + System.lineSeparator() + "/start");
    }

    @Test
    void whenCallbackAndServerResponseHaveErrorObjectThenGetErrorMessage() {

        when(tgUserServiceMock.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.of(telegramProfile));
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Пароль введен не верно.";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Ошибка: Пароль введен не верно.");
    }

    @Test
    void whenCallbackAndServerResponseHaveNoErrorThenGetSubscriptionCompletedMessage() {

        when(tgUserServiceMock.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.of(telegramProfile));
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getOk() {
                        return "ok";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Подписка оформлена");
    }
}