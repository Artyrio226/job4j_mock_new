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
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TelegramProfile;
import ru.checkdev.notification.service.TelegramProfileService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ForgetActionTest {

    @Mock
    private TelegramProfileService telegramProfileService;

    @Mock
    private TgAuthCallWebClint authCallWebClint;

    @InjectMocks
    private ForgetAction forgetAction;

    private TelegramProfile telegramProfile;
    private Message messageMock;
    private TgConfig tgConfig;

    @BeforeEach
    void setUp() {
        messageMock = new Message();
        messageMock.setChat(new Chat(12345L, "private"));
        telegramProfile = new TelegramProfile(0, "12345", "test@example.com", false);
        tgConfig = new TgConfig("tg/", 8);
    }

    @Test
    void whenHandleAndUserNoExistsThenReturnNotRegisteredMessage() {

        when(telegramProfileService.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.empty());

        SendMessage response = (SendMessage) forgetAction.handle(messageMock);

        assertThat(response.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(response.getText()).isEqualTo("Данный аккаунт Telegram не зарегистрирован");
    }

    @Test
    void whenHandleAndUserExistsThenReturnSuccessMessage() {

        when(telegramProfileService.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.of(telegramProfile));
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public int getId() {
                        return 1;
                    }
                }));

        SendMessage response = (SendMessage) forgetAction.handle(messageMock);

        assertThat(response.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(response.getText()).contains("Ваш Логин: test@example.com");
        assertThat(response.getText()).contains("Новый пароль: tg/");
    }

    @Test
    void whenHandleAndAuthServerNotAvailableThenReturnServiceUnavailableMessage() {

        when(telegramProfileService.findByChatId(messageMock.getChatId().toString())).thenReturn(Optional.of(telegramProfile));
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class))).thenThrow(new RuntimeException("WebClient error"));

        SendMessage response = (SendMessage) forgetAction.handle(messageMock);

        assertThat(response.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(response.getText()).contains("Сервис не доступен попробуйте позже");
    }

    @Test
    void whenHandleAndServerResponseHaveErrorThenReturnErrorMessage() {

        when(telegramProfileService.findByChatId("12345")).thenReturn(Optional.of(telegramProfile));
        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage response = (SendMessage) forgetAction.handle(messageMock);

        assertThat(response.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(response.getText()).isEqualTo("Ошибка восстановления пароля: обратитесь в поддержку ");
    }

    @Test
    public void whenCallbackThenInvokeHandle() {

        when(telegramProfileService.findByChatId(anyString())).thenReturn(Optional.empty());

        var actualAnswer = (SendMessage) forgetAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Данный аккаунт Telegram не зарегистрирован");
    }
}
