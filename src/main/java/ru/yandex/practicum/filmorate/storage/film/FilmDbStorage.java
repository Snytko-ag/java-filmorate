package ru.yandex.practicum.filmorate.storage.film;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

import static java.lang.String.format;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;
    private final GenreService genreService;
    private final GenreDbStorage genreDbStorage;
    private final MpaService mpaService;
    private final LikeDbStorage likeDbStorage;

    @Override
    public Film createFilm(Film film) {
        log.debug("createFilm({})", film);


        validateFilm(film);

        // Проверяем, что рейтинг MPA существует
        Integer mpaId = film.getMpa().getId();
        Mpa mpa = mpaService.getMpaById(mpaId);
        film.setMpa(mpa);

        // Вставляем фильм и получаем сгенерированный ID
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        int generatedId;
        try {
            generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить ID созданного фильма", e);
        }

        // Получаем полную информацию о фильме
        String sql = "SELECT f.*, r.name as rating_name " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id " +
                "WHERE f.id = ?";

        Film createdFilm = jdbcTemplate.queryForObject(
                sql,
                filmMapper,
                generatedId
        );

        // Сохраняем жанры, если они есть
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Проверяем существование всех жанров
            Set<Genre> fullGenres = new LinkedHashSet<>();
            for (Genre genre : film.getGenres()) {
                Genre fullGenre = genreService.getGenreById(genre.getId());
                fullGenres.add(fullGenre);
            }

            genreDbStorage.saveFilmGenres(generatedId, film.getGenres());

            createdFilm.setGenres(fullGenres);
        } else {
            createdFilm.setGenres(new HashSet<>());
        }

        // Загружаем лайки (будет пустой набор, потому что только создали)
        createdFilm.setLikes(new HashSet<>());

        log.trace("Фильм {} успешно добавлен в базу данных", createdFilm);
        return createdFilm;
    }



    @Override
    public void deleteFilms() {
        log.info("Удаление всех фильмов из базы данных");
        jdbcTemplate.update("DELETE FROM films");
        log.info("Все фильмы успешно удалены");

    }

    @Override
    public Film deleteFilmsById(Integer id) {
        Film film = getFilmById(id);
        log.debug("Удаление фильма с id={}", id);

        // Проверяем, существует ли фильм
        String checkSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);

        if (count == 0) {
            throw new NotFoundException(format("Фильм с id=%d не найден", id));
        }

        String deleteSql = "DELETE FROM films WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(deleteSql, id);

        log.info("Фильм с id={} успешно удален. Удалено строк: {}", id, rowsAffected);
        return film;
    }



    @Override
    public Film getFilmById(Integer id) {
        log.debug("Получение фильма по ID: {}", id);
        String sql = "SELECT f.*, r.name as rating_name " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id " +
                "WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmMapper, id);

            // Загружаем жанры через GenreDbStorage
            Set<Genre> genres = genreDbStorage.getGenresByFilmId(id);
            film.setGenres(genres);

            // Загружаем лайки через LikeDbStorage
            Set<Integer> likes = likeDbStorage.getLikesByFilmId(id);
            film.setLikes(likes);

            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(format("Фильм с id=%d не найден", id));
        }
    }

    @Override
    public List<Film> findAll() {
        log.debug("Получение всех фильмов");
        String sql = "SELECT f.*, r.name as rating_name " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id " +
                "ORDER BY f.id";

        List<Film> films = jdbcTemplate.query(sql, filmMapper);

        // Загружаем жанры и лайки для каждого фильма
        films.forEach(film -> {
            Set<Genre> genres = genreDbStorage.getGenresByFilmId(film.getId());
            film.setGenres(genres);

            Set<Integer> likes = likeDbStorage.getLikesByFilmId(film.getId());
            film.setLikes(likes);
        });

        return films;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("Обновление фильма: {}", film);
        // Проверяем, что фильм существует
        getFilmById(film.getId());

        // Проверяем, что рейтинг MPA существует
        Integer mpaId = film.getMpa().getId();
        Mpa mpa = mpaService.getMpaById(mpaId);
        film.setMpa(mpa);

        String sql = "UPDATE films SET name = ?, description = ?, " +
                "release_date = ?, duration = ?, rating_id = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        // Получаем обновленный фильм
        String selectSql = "SELECT f.*, r.name as rating_name, r.description as rating_description " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id " +
                "WHERE f.id = ?";

        Film updatedFilm = jdbcTemplate.queryForObject(selectSql, filmMapper, film.getId());

        // Обновляем жанры
        Set<Genre> fullGenres = new HashSet<>();
        if (film.getGenres() != null) {

            // Получаем полную информацию о жанрах
            for (Genre genre : film.getGenres()) {
                Genre fullGenre = genreService.getGenreById(genre.getId());
                fullGenres.add(fullGenre);
            }
        }

        genreDbStorage.updateFilmGenres(film.getId(), film.getGenres());
        updatedFilm.setGenres(fullGenres);

        // Загружаем лайки
        Set<Integer> likes = likeDbStorage.getLikesByFilmId(film.getId());
        updatedFilm.setLikes(likes);

        return updatedFilm;
    }

    private void validateFilm(Film film) {
        // Проверка названия
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        // Проверка описания
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        }

        // Проверка даты релиза
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза обязательна");
        }

        // Проверка что дата не раньше 28 декабря 1895 года
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Проверка продолжительности
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        // Проверка MPA
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("Рейтинг MPA обязателен");
        }
    }
}
