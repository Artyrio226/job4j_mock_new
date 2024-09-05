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
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckActionTest {

    @Mock
    private TgAuthCallWebClint tgAuthCallWebClint;

    @Mock
    private TgConfig tgConfig;
    @InjectMocks
    private CheckAction checkAction;
    @Mock
    Message messageMock;

    @BeforeEach
    public void setUp() {
        messageMock = new Message();
        messageMock.setChat(new Chat(123L, "test"));
        messageMock.setText("testName testPass");
        messageMock.setFrom(new User(123L, "test", true));
    }

    @Test
    public void whenHandleAndUserNoExistsThenUserNotRegistered() {

        when(tgAuthCallWebClint.doGet(any())).thenReturn(Mono.empty());

        var actualAnswer = (SendMessage) checkAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }

    @Test
    public void whenCallbackThenInvokeHandle() {

        when(tgAuthCallWebClint.doGet(any())).thenReturn(Mono.empty());

        var actualAnswer = (SendMessage) checkAction.callback(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Вы не зарегистрированы в системе.");
    }

    @Test
    public void whenHandleThenServiceUnavailable() {
        when(tgAuthCallWebClint.doGet(any())).thenThrow(new RuntimeException("Сервис не доступен попробуйте позже."));

        var actualAnswer = (SendMessage) checkAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен попробуйте позже"
                                                    + System.lineSeparator() + "/start");
    }

    @Test
    public void whenHandleAndUserExistsThenUserRegistered() {

        var testPerson = PersonDTO.builder()
                .username("test")
                .email("test@mail.ru")
                .password("123")
                .userChatId(111L)
                .build();

        when(tgAuthCallWebClint.doGet(any())).thenReturn(Mono.just(testPerson));

        var actualAnswer = (SendMessage) checkAction.handle(messageMock);

        assertThat(actualAnswer.getChatId()).isEqualTo(messageMock.getChatId().toString());
        assertThat(actualAnswer.getText())
                .contains("Ваше имя: " + testPerson.getUsername() + System.lineSeparator()
                          + "Ваша почта: " + testPerson.getEmail());
    }
}
