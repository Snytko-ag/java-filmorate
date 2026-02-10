package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;


@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FriendDbStorage friendDbStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, FriendDbStorage friendDbStorage) {
        this.userStorage = userStorage;
        this.friendDbStorage = friendDbStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья!");
        }
        friendDbStorage.addFriend(userId, friendId);
        log.info("'{}' added '{}' to a friend list", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей!");
        }
        friendDbStorage.removeFriend(userId, friendId);
        log.info("'{}' removed '{}' from friends list", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        List<User> friends = new ArrayList<>();
        if (userId != null) {
            friends = friendDbStorage.getFriends(userId);
        }
        return friends;
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        log.info("'{}' requested common friends' list with user '{}'", userId, friendId);


        return friendDbStorage.getCommonFriends(userId, friendId);
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }




}
