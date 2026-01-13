package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public void like(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        userService.getUserStorage().getUserById(userId);
        if (film == null) {
            throw new NotFoundException(format("Пользователь с id=%d не найден", filmId));
        }
        film.addLike(userId);
        log.info("'{}' liked a movie '{}'", userId, filmId);
    }

    public void dislike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        userService.getUserStorage().getUserById(userId);
        if (film == null) {
            throw new NotFoundException(format("Пользователь с id=%d не найден", filmId));
        }
        film.removeLike(userId);
        log.info("'{}' disliked a movie '{}'", userId, filmId);
    }

    public List<Film> getPopularMovies(int count) {
        log.info("Attempt to get the most liked movies list");
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikesNumber).reversed())
                .limit(count).collect(Collectors.toList());
    }
}
