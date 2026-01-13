package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {

    @PositiveOrZero
    Integer id;

    @NotNull
    String name;

    @Size(min = 1, max = 200)
    String description;

    LocalDate releaseDate;

    @Positive
    int duration;

    Set<Integer> likes  = new HashSet<>();

    public void addLike(Integer userId) {
        likes.add(userId);
    }

    public void removeLike(Integer userId) {
        likes.remove(userId);
    }

    public int getLikesNumber() {
        return likes != null ? likes.size() : 0;
    }

}
