package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {

        try {

            // Логируем добавление фильма
            log.info("Adding a new film with description {}", film.getDescription());

            // проверяем выполнение необходимых условий
            if (film.getName() == null || film.getName().isBlank()) {
                throw new ValidationException("Название фильма не может быть пустым");
            }

            if (film.getDescription().length() > 200) {
                throw new ValidationException("Описание превышает 200 символов");
            }
            LocalDate minAllowedDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(minAllowedDate)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
            }

            if (film.getDuration() < 0) {
                throw new ValidationException("Продолжительность фильма не должно быть отрицательным числом");
            }

            film.setId(getNextId());
            // сохраняем новый фильм в памяти приложения
            films.put(film.getId(), film);

            // Логируем успешное сохранение фильма
            log.info("New film added successfully with id={}", film.getId());

            return film;
        } catch (ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {

        try {

            // проверяем необходимые условия
            if (newFilm.getId() == 0) {
                throw new NotFoundException("Id фильма должен быть указан");
            }

            log.info("Updating film with id={}", newFilm.getId());

            if (!films.containsKey(newFilm.getId())) {
                throw new NotFoundException(format("Фильм с id=%d не найден", newFilm.getId()));
            }

            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getDescription() != null) {

                if (newFilm.getDescription().length() > 200) {
                    throw new ValidationException("Описание превышает 200 символов");
                }

                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null) {

                LocalDate minAllowedDate = LocalDate.of(1895, 12, 28);

                if (newFilm.getReleaseDate().isBefore(minAllowedDate)) {
                    throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
                }

                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() != 0) {

                if (newFilm.getDuration() < 0) {
                    throw new ValidationException("Продолжительность фильма не должно быть отрицательным числом");
                }

                oldFilm.setDuration(newFilm.getDuration());
            }

            if (newFilm.getName() != null) {

                oldFilm.setName(newFilm.getName());
            }

            log.info("Updated film with id={}", newFilm.getId());

            return oldFilm;


        } catch (NotFoundException | ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return currentMaxId + 1;
    }

}
