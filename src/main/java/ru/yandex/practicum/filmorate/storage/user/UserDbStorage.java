package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Component("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage{

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;


    @Override
    public User createUser(User user) {
        log.debug("createUser({})", user);

        // Валидация уникальности email и login
        validateUserUniqueness(user);

        // Вставляем пользователя и получаем сгенерированный ID
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) " +
                            "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        Integer generatedId = keyHolder.getKey().intValue();

        // Получаем полную информацию о пользователе
        User createdUser = getUserById(generatedId);

        log.trace("Пользователь {} успешно добавлен в базу данных", createdUser);
        return createdUser;
    }

    @Override
    public User updateUser(User user) {
        log.debug("updateUser({})", user);

        // Проверяем, что пользователь существует
        getUserById(user.getId());

        // Валидация уникальности email и login (исключая текущего пользователя)
        validateUserUniquenessOnUpdate(user);

        // Обновляем данные пользователя
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (rowsAffected == 0) {
            throw new NotFoundException(format("Пользователь с id=%d не найден", user.getId()));
        }

        // Получаем обновленного пользователя
        User updatedUser = getUserById(user.getId());

        log.trace("Пользователь {} успешно обновлен", updatedUser);
        return updatedUser;
    }

    @Override
    public void deleteUsers() {
        log.info("Удаление всех пользователей");

        // Сначала удаляем связи дружбы
        try {
            jdbcTemplate.update("DELETE FROM friends");
            log.debug("Удалены все связи дружбы");
        } catch (DataAccessException e) {
            log.debug("Таблица friends не существует или пуста");
        }

        // Удаляем всех пользователей
        int usersDeleted = jdbcTemplate.update("DELETE FROM users");

        log.info("Все пользователи удалены. Всего: {}", usersDeleted);

    }

    @Override
    public User getUserById(Integer id) {
        log.debug("getUserById({})", id);

        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userMapper, id);

            // Загружаем друзей пользователя
            loadUserFriends(user);

            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public Collection<User> findAll() {
        log.debug("findAll users");

        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = jdbcTemplate.query(sql, userMapper);

        // Загружаем друзей для каждого пользователя
        for (User user : users) {
            loadUserFriends(user);
        }

        log.trace("Найдено пользователей: {}", users.size());
        return users;
    }

    // Дополнительные методы для работы с друзьями


    // Дополнительные методы
    private void validateUserUniqueness(User user) {
        // Проверка уникальности email
        String emailCheckSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer emailCount = jdbcTemplate.queryForObject(emailCheckSql, Integer.class, user.getEmail());

        if (emailCount != null && emailCount > 0) {
            throw new ValidationException(format("Пользователь с email %s уже существует", user.getEmail()));
        }

        // Проверка уникальности login
        String loginCheckSql = "SELECT COUNT(*) FROM users WHERE login = ?";
        Integer loginCount = jdbcTemplate.queryForObject(loginCheckSql, Integer.class, user.getLogin());

        if (loginCount > 0) {
            throw new ValidationException(format("Пользователь с login %s уже существует", user.getLogin()));
        }
    }

    private void validateUserUniquenessOnUpdate(User user) {
        // Проверка уникальности email (исключая текущего пользователя)
        String emailCheckSql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        Integer emailCount = jdbcTemplate.queryForObject(
                emailCheckSql, Integer.class, user.getEmail(), user.getId()
        );

        if (emailCount > 0) {
            throw new ValidationException(format("Пользователь с email %s уже существует", user.getEmail()));
        }

        // Проверка уникальности login (исключая текущего пользователя)
        String loginCheckSql = "SELECT COUNT(*) FROM users WHERE login = ? AND id != ?";
        Integer loginCount = jdbcTemplate.queryForObject(
                loginCheckSql, Integer.class, user.getLogin(), user.getId()
        );

        if (loginCount > 0) {
            throw new ValidationException(format("Пользователь с login %s уже существует", user.getLogin()));
        }
    }

    private void loadUserFriends(User user) {
        // Загружаем ID всех друзей (и подтвержденных, и неподтвержденных)
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";

        List<Integer> friendIds = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("friend_id"),
                user.getId()
        );

        user.setFriends(new HashSet<>(friendIds));
    }
}
