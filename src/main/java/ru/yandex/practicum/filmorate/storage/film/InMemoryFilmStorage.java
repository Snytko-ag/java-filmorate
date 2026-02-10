package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

import static java.lang.String.format;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
    }

    @Override
    public Film createFilm(Film film) {
        // Логируем добавление фильма
        log.info("Adding a new film with description {}", film.getDescription());
        validate(film);

        // Гарантируем, что likes не null
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        film.setId(getNextId());
        // сохраняем новый фильм в памяти приложения
        films.put(film.getId(), film);
        // Логируем успешное сохранение фильма
        log.info("New film added successfully with id={}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            log.info("Updating film with id={}", film.getId());
            validate(film);

            // Гарантируем, что likes не null при обновлении
            if (film.getLikes() == null) {
                film.setLikes(new HashSet<>());
            }

            films.put(film.getId(), film);
            log.info("Updated film with id={}", film.getId());
            return film;
        } else {
            throw new NotFoundException(format("Фильм с id=%d не найден", film.getId()));
        }
    }

    @Override
    public void deleteFilms() {
        films.clear();
        log.info("Movie storage is empty now");
    }

    @Override
    public Film deleteFilmsById(Integer id) {
        if (id == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!films.containsKey(id)) {
            throw new NotFoundException(format("Фильм с id=%d не найден", id));
        }
        return films.remove(id);
    }

    @Override
    public Film getFilmById(Integer id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException(format("Фильм с id=%d не найден", id));
        }
        return films.get(id);
    }

    @Override
    public List<Film> findAll() {
        log.info("There are '{}' movies in a library now", films.size());
        return new ArrayList<>(films.values());
    }

    private void validate(Film film) {
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
