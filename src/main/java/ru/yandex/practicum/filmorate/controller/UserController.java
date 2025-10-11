package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {

        try {

            //Логируем добавление нового пользователя
            log.info("Adding a new user with email {}", user.getEmail());

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

            user.setId(getNextId());
            // сохраняем нового пользователя в памяти приложения
            users.put(user.getId(), user);

            // Логируем успешное добавление пользователя
            log.info("New user added successfully with id={}", user.getId());

            return user;
        } catch (ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User update(@RequestBody User newUser) {

        try {

            // проверяем необходимые условия
            if (newUser.getId() == 0) {
                throw new NotFoundException("Id должен быть указан");
            }

            if (!users.containsKey(newUser.getId())) {
                throw new NotFoundException(format("Пользователь с id=%d не найден", newUser.getId()));
            }

            // Логируем попытку обновить пользователя
            log.info("Updating user with id={}", newUser.getId());

            User oldUser = users.get(newUser.getId());

            if (newUser.getEmail() != null) {
                boolean existsEmail = users.values().stream()
                        .anyMatch(e -> e.getEmail().equals(newUser.getEmail()));

                if (existsEmail) {
                    throw new ValidationException("Этот Email уже используется");
                }

                if (!newUser.getEmail().contains("@")) {
                    throw new ValidationException("Email должен содержать символ @");
                }

                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getLogin() != null) {
                if (newUser.getLogin().contains(" ")) {
                    throw new ValidationException("Логин не должен содержать пробелы");
                }
                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getBirthday() != null) {
                if (newUser.getBirthday().isAfter(LocalDate.now())) {
                    throw new ValidationException("Дата рождения не может быть в будущем.");
                }
                oldUser.setBirthday(newUser.getBirthday());
            }

            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }

            // Логируем успешное обновление пользователя
            log.info("Updated user with id={}", newUser.getId());

            return oldUser;


        } catch (NotFoundException | ValidationException e) {
            // Логируем причину ошибки
            log.error(e.getMessage());
            throw e;
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
