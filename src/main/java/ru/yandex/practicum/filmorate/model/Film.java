package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.LinkedHashSet;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Film {

    @PositiveOrZero
    Integer id;

    @NotBlank
    String name;
    @Size(min = 1, max = 200)
    String description;
    LocalDate releaseDate;
    @Positive
    int duration;
    @NotNull
    Mpa mpa;
    Set<Genre> genres = new LinkedHashSet<>();
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
