package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User {

    @PositiveOrZero
    Integer id;

    @Email
    String email;

    @NotNull
    String login;

    String name;

    @PastOrPresent
    LocalDate birthday;

    Set<Integer> friends  = new HashSet<>();

    public User(String mail, String flyingDragon, String andrew, LocalDate of) {
    }

    public void addFriend(Integer id) {
        friends.add(id);
    }

    public void removeFriend(Integer id) {
        friends.remove(id);
    }

    public int getFriendsQuantity() {
        return friends.size();
    }
}
