package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.addFriend(friendId);
        friend.addFriend(userId);
        log.info("'{}' added '{}' to a friend list", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        log.info("'{}' removed '{}' from friends list", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        User user = userStorage.getUserById(userId);
        Set<Integer> friends = user.getFriends();

        return friends.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        log.info("'{}' requested common friends' list with user '{}'", userId, friendId);

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        Set<Integer> userFriends = user.getFriends();
        Set<Integer> friendFriends = friend.getFriends();

        return userFriends.stream()
                .filter(friendFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }


}
