package ru.checkdev.notification.telegram.config;

import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.PersonDTO;

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing TgConfig;
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 06.10.2023
 */
class TgConfigTest {
    private final String prefix = "pr/";
    private final int passSize = 10;
    private final TgConfig tgConfig = new TgConfig(prefix, passSize);

    @Test
    void whenIsEmailThenReturnTrue() {
        var email = "mail@mail.ru";
        var actual = tgConfig.isEmail(email);
        assertThat(actual).isTrue();
    }

    @Test
    void whenIsEmailThenReturnFalse() {
        var email = "mail.ru";
        var actual = tgConfig.isEmail(email);
        assertThat(actual).isFalse();
    }

    @Test
    void whenGetPasswordThenLengthPassSize() {
        var pass = tgConfig.getPassword();
        assertThat(pass).hasSize(passSize);
    }

    @Test
    void whenGetPasswordThenStartWishPrefix() {
        var pass = tgConfig.getPassword();
        assertThat(pass).startsWith(prefix);
    }

    @Test
    void whenGetObjectToMapThenReturnObjectMap() {
        var personDto = PersonDTO.builder()
                .email("email")
                .password("pass")
                .privacy(true)
                .created(Calendar.getInstance())
                .build();
        var map = tgConfig.getObjectToMap(personDto);
        assertThat(map)
                .containsEntry("email", personDto.getEmail())
                .containsEntry("password", personDto.getPassword());
        assertThat(String.valueOf(map.get("privacy"))).isEqualTo(String.valueOf(true));
    }
}