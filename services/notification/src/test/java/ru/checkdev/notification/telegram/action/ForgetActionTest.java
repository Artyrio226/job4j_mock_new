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
public class ForgetActionTest {

    @Mock
    private TgAuthCallWebClint tgAuthCallWebClint;

    @InjectMocks
    private ForgetAction forgetAction;

    private Message messageMock;

    @BeforeEach
    void setUp() {
        messageMock = new Message();
        messageMock.setChat(new Chat(123L, "test"));
        messageMock.setText("testName testPass");
        messageMock.setFrom(new User(123L, "test", true));
    }

    @Test
    void whenHandleAndUserNoExistsThenReturnNotRegisteredMessage() {

        when(tgAuthCallWebClint.doGet(any())).thenReturn(Mono.empty());

        var actualAnswer = (SendMessage) forgetAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }

    @Test
    void whenHandleAndUserExistsThenReturnSuccessMessage() {

        var testPerson = PersonDTO.builder()
                .username("test")
                .email("test@mail.ru")
                .password("123")
                .userChatId(111L)
                .build();

        when(tgAuthCallWebClint.doGet(any())).thenReturn(Mono.just(testPerson));

        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public int getId() {
                        return 1;
                    }
                }));

        SendMessage response = (SendMessage) forgetAction.handle(messageMock);

        assertThat(response.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(response.getText()).contains("Ваш Логин: test@mail.ru");
        assertThat(response.getText()).contains("Новый пароль: tg/");
    }

    @Test
    void whenHandleAndAuthServerNotAvailableThenReturnServiceUnavailableMessage() {

        SendMessage response = (SendMessage) forgetAction.handle(messageMock);

        assertThat(response.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(response.getText()).contains("Сервис авторизации не доступен попробуйте позже"
                                                + System.lineSeparator() + "/start");
    }

    @Test
    void whenHandleAndServerResponseHaveErrorThenReturnErrorMessage() {
        var testPerson = PersonDTO.builder()
                .username("test")
                .email("test@mail.ru")
                .password("123")
                .userChatId(111L)
                .build();

        when(tgAuthCallWebClint.doGet(any(String.class))).thenReturn(Mono.just(testPerson));
        when(tgAuthCallWebClint.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Пароль введен не верно.";
                    }
                }));

        SendMessage response = (SendMessage) forgetAction.handle(messageMock);

        assertThat(response.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(response.getText()).isEqualTo("Ошибка восстановления пароля: Пароль введен не верно.");
    }

    @Test
    public void whenCallbackThenInvokeHandle() {
        when(tgAuthCallWebClint.doGet(any())).thenReturn(Mono.empty());

        var actualAnswer = (SendMessage) forgetAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }
}
