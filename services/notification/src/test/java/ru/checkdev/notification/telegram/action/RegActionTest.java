package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
class RegActionTest {
    private final String siteUrl = "site.com";

    @Mock
    Message messageMock;

    private RegAction regAction;
    @Mock
    private TelegramProfileService tgUserService;
    @Mock
    private TgAuthCallWebClint authCallWebClint;

    @BeforeEach
    void setUp() {
        regAction = new RegAction(authCallWebClint, tgUserService, siteUrl);
        when(messageMock.getChatId()).thenReturn(123L);
    }

    @Test
    void whenHandleAndUserNoExistsThenGetRegisterMessage() {

        SendMessage actualAnswer = (SendMessage) regAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Введите email для регистрации:");
    }

    @Test
    void whenCallbackAndUserExistsThenGetAlreadyRegisteredMessage() {

        when(messageMock.getText()).thenReturn("test@example.com");
        when(tgUserService.findByEmail("test@example.com")).thenReturn(Optional.of(new TelegramProfile()));

        var actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Вы уже зарегистрированы в системе.")
                .contains("Чтобы узнать регистрационные данные используйте /check");
    }

    @Test
    void whenCallbackAndInvalidEmailThenGetInvalidEmailMessage() {

        when(messageMock.getText()).thenReturn("invalid_email");

        var actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Email: invalid_email не корректный." + System.lineSeparator()
                          + "попробуйте снова." + System.lineSeparator() + "/new");
    }

    @Test
    void whenCallbackAndAuthServiceErrorThenGetAuthServiceErrorMessage() {

        when(messageMock.getText()).thenReturn("test@example.com");
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(new RuntimeException("Auth service error"));

        var actualAnswer = (SendMessage) regAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен попробуйте позже"
                                                    + System.lineSeparator() + "/start");
    }

    @Test
    void whenCallbackAndServerResponseHaveErrorObjectThenGetErrorMessage() {

        when(messageMock.getText()).thenReturn("test@example.com");
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
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

        when(messageMock.getText()).thenReturn("test@example.com");
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
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
                .contains("Пароль: tg/")
                .contains("site.com");
    }
}