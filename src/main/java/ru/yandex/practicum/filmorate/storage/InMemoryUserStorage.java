package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users;

    public InMemoryUserStorage() {
        users = new HashMap<>();
    }

    @Override
    public User createUser(User user) {

        //Логируем добавление нового пользователя
        log.info("Adding a new user with email {}", user.getEmail());
        validate(user);

        // Гарантируем, что friends не null при обновлении
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        user.setId(getNextId());
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        // Логируем успешное добавление пользователя
        log.info("New user added successfully with id={}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            // Логируем попытку обновить пользователя
            log.info("Updating user with id={}", user.getId());
            validate(user);
            // Гарантируем, что friends не null при обновлении
            if (user.getFriends() == null) {
                user.setFriends(new HashSet<>());
            }
            users.put(user.getId(), user);
            // Логируем успешное обновление пользователя
            log.info("Updated user with id={}", user.getId());
            return user;
        } else {
            throw new NotFoundException(format("Пользователь с id=%d не найден", user.getId()));
        }
    }

    @Override
    public void deleteUsers() {
        users.clear();
        log.info("User storage is empty now");
    }

    @Override
    public User getUserById(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(format("Пользователь с id=%d не найден", id));
        }
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    private void validate(User user) {
        // проверяем выполнение необходимых условий
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email не может быть пустым и должен содержать символ @");
        }

        boolean existsEmail = users.values().stream()
                .anyMatch(e -> e.getEmail().equals(user.getEmail()));

        if (existsEmail) {
            throw new ValidationException("Этот Email уже используется");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return currentMaxId + 1;
    }


}
