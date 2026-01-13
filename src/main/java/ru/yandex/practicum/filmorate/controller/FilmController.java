package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {

    private static final String FILM_ID_PATH = "/{id}";
    private static final String LIKE_PATH = FILM_ID_PATH + "/like/{userId}";
    private static final String POPULAR_PATH = "/popular";

    @Autowired
    private final FilmStorage filmStorage;
    @Autowired
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmStorage.createFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    @GetMapping(FILM_ID_PATH)
    public Film getFilmById(@PathVariable Integer id) {
        return filmStorage.getFilmById(id);
    }

    @GetMapping(POPULAR_PATH)
    public List<Film> getPopularMovies(@RequestParam(defaultValue = "10") Integer count) {
        return filmService.getPopularMovies(count);
    }

    @PutMapping(LIKE_PATH)
    public void likeAMovie(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.like(id, userId);
    }

    @DeleteMapping(LIKE_PATH)
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.dislike(id, userId);
    }
}
