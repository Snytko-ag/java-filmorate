package ru.yandex.practicum.filmorate.storage.friend;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Component
@Slf4j
public class FriendDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Autowired
    public FriendDbStorage(JdbcTemplate jdbcTemplate,
                           @Qualifier("UserDbStorage") UserStorage userStorage,
                           UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    public void addFriend(Integer userId, Integer friendId) {
        log.debug("addFriend: userId={}, friendId={}", userId, friendId);

        // Проверяем существование пользователей
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        // Проверяем, не является ли это самодружбой
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        // Проверяем, не являются ли уже друзьями
        String checkSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count > 0) {
            throw new ValidationException("Пользователь уже в друзьях");
        }

        // Проверяем обратную связь (есть ли запись от friendId к userId)
        String reverseCheckSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer reverseCount = jdbcTemplate.queryForObject(reverseCheckSql, Integer.class, friendId, userId);

        // Определяем статус (безопасная проверка)
        boolean status = (reverseCount > 0);

        // Добавляем запись о дружбе
        String insertSql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, userId, friendId, status);

        // Если есть обратная связь, обновляем и её статус на true
        if (status) {
            String updateSql = "UPDATE friends SET status = TRUE WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(updateSql, friendId, userId);
        }

        log.info("Пользователь {} добавил в друзья пользователя {} (статус: {})",
                userId, friendId, status ? "подтверждено" : "ожидание");
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.debug("removeFriend: userId={}, friendId={}", userId, friendId);

        // Проверяем, не является ли это самодружбой
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить себя из друзей");
        }

        // Проверяем существование пользователей
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        // Удаляем связь дружбы
        String deleteSql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        int rowsAffected = jdbcTemplate.update(deleteSql, userId, friendId);

        // Получаем статус обратной связи
        Boolean reverseStatus = false;
        try {
            String getStatusSql = "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?";
            reverseStatus = jdbcTemplate.queryForObject(getStatusSql, Boolean.class, friendId, userId);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Обратной связи не найдено");
        }

        // Если была взаимная дружба (статус true), меняем статус обратной связи на false
        if (reverseStatus) {
            String updateSql = "UPDATE friends SET status = FALSE WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(updateSql, friendId, userId);
            log.debug("Обновлен статус обратной дружбы на false");
        }

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        log.debug("getFriends({})", userId);

        // Проверяем существование пользователя
        userStorage.getUserById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.id";

        List<User> friends = jdbcTemplate.query(sql, userMapper, userId);

        log.trace("Найдено друзей у пользователя {}: {}", userId, friends.size());
        return friends;
    }

    public List<User> getCommonFriends(Integer userId1, Integer userId2) {
        log.debug("getCommonFriends({}, {})", userId1, userId2);

        // Проверяем существование пользователей
        userStorage.getUserById(userId1);
        userStorage.getUserById(userId2);

        String sql = "SELECT DISTINCT u.* FROM users u " +
                "JOIN friends f1 ON u.id = f1.friend_id " +
                "JOIN friends f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "ORDER BY u.id";

        List<User> commonFriends = jdbcTemplate.query(sql, userMapper, userId1, userId2);

        log.trace("Общих друзей у пользователей {} и {}: {}",
                userId1, userId2, commonFriends.size());
        return commonFriends;
    }
}
