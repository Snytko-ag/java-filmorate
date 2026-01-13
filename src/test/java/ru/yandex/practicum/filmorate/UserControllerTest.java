
package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest {

    private UserController controller;
    private final InMemoryUserStorage storage = new InMemoryUserStorage();
    private UserService service = new UserService(storage);

    @BeforeEach
    void setUp() {
        this.controller = new UserController(storage, service); // Создаем реальный экземпляр контроллера
    }

    @Test
    public void testCreateEmptyEmail() throws Exception {
        User user = new User();
        user.setLogin("valid_login");
        user.setName("valid_name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        boolean except = false;

        try {
            controller.create(user); // Пробуем создать пользователя без Email
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");
    }

    @Test
    public void testCreateInvalidEmailFormat() throws Exception {
        User user = new User();
        user.setEmail("invalid_email");
        user.setLogin("valid_login");
        user.setName("valid_name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        boolean except = false;

        try {
            controller.create(user); // Пробуем создать пользователя с неверным форматом Email
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");
    }

    @Test
    public void testCreateSpaceInLogin() throws Exception {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("login login");
        user.setName("valid_name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        boolean except = false;

        try {
            controller.create(user); // Пробуем создать пользователя с неверным логином
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");
    }

    @Test
    public void testCreateFutureBirthDate() throws Exception {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("valid_login");
        user.setName("valid_name");
        user.setBirthday(LocalDate.now().plusYears(1)); // Будущая дата рождения

        boolean except = false;

        try {
            controller.create(user); // Пробуем создать пользователя с датой рождения из будущего
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");
    }

    @Test
    public void testCreateSuccess() throws Exception {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("valid_login");
        user.setName("valid_name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertEquals(Integer.valueOf(1), controller.create(user).getId()); // Проверка, что присвоился ID
    }

    @Test
    public void testUpdateUser() throws  Exception {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("valid_login");
        user.setName("valid_name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        controller.create(user);

        User userNew = new User();
        userNew.setId(1);
        userNew.setEmail("valid2@email.com");
        userNew.setLogin("valid_login_login");
        userNew.setName("valid_name_name");
        userNew.setBirthday(LocalDate.of(1995, 1, 1));

        User userUpdate = controller.update(userNew);

        assertEquals("valid2@email.com", userUpdate.getEmail());
        assertEquals("valid_login_login", userUpdate.getLogin());
        assertEquals("valid_name_name", userUpdate.getName());
        assertEquals(LocalDate.of(1995, 1, 1), userUpdate.getBirthday());

    }

    @Test
    void addFriend_shouldAddFriendToOtherUsersSet() {
        User user = new User(1, "valid@email.com", "login", "Ivan",
                LocalDate.of(1980, 1, 1), new HashSet<>());
        User emptyNameUser = new User(2, "valid2@email.com", "login2", "Petr",
                LocalDate.of(1985, 1, 1), new HashSet<>());
        controller.create(user);
        controller.create(emptyNameUser);
        controller.addFriend(user.getId(), emptyNameUser.getId());

        Assertions.assertTrue(user.getFriendsQuantity() != 0);
        Assertions.assertTrue(emptyNameUser.getFriendsQuantity() != 0);
    }


}


