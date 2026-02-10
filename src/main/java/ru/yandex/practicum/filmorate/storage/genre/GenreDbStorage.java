package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreMapper genreMapper;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreMapper genreMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreMapper = genreMapper;
    }

    public List<Genre> getGenres() {
        log.debug("Получение всех жанров");

        String sql = "SELECT id, name FROM genres ORDER BY id";
        List<Genre> genres = jdbcTemplate.query(sql, genreMapper);

        log.trace("Найдено жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(Integer genreId) {
        log.debug("Получение жанра по ID: {}", genreId);

        String sql = "SELECT id, name FROM genres WHERE id = ?";

        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreMapper, genreId);
            log.trace("Найден жанр: id={}, name={}", genre.getId(), genre.getName());
            return genre;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Жанр с ID={} не найден", genreId);
            throw new NotFoundException("Жанр с ID=" + genreId + " не найден");
        }
    }

    // Сохраняем жанры фильма
    public void saveFilmGenres(Integer filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            log.debug("Нет жанров для сохранения для фильма с ID={}", filmId);
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        // Убираем дубликаты жанров
        Set<Integer> uniqueGenreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        for (Integer genreId : uniqueGenreIds) {
            jdbcTemplate.update(sql, filmId, genreId);
        }

        log.debug("Сохранено {} жанров для фильма с ID={}", uniqueGenreIds.size(), filmId);
    }

    // Обновляем жанры фильма
    public void updateFilmGenres(Integer filmId, Set<Genre> genres) {
        log.debug("Обновление жанров для фильма с ID={}", filmId);

        // Удаляем все старые жанры
        deleteFilmGenres(filmId);

        // Добавляем новые, если они есть
        if (genres != null && !genres.isEmpty()) {
            saveFilmGenres(filmId, genres);
        }

        log.debug("Жанры фильма с ID={} обновлены", filmId);
    }

    // Удаляем жанры фильма
    public void deleteFilmGenres(Integer filmId) {
        log.debug("Удаление жанров для фильма с ID={}", filmId);

        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, filmId);

        log.debug("Удалено {} жанров для фильма с ID={}", rowsAffected, filmId);
    }

    public Set<Genre> getGenresByFilmId(Integer filmId) {
        log.debug("Получение жанров для фильма с ID={}", filmId);

        String sql = "SELECT g.id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id ASC";

        List<Genre> genres = jdbcTemplate.query(sql, genreMapper, filmId);

        return new LinkedHashSet<>(genres);
    }

}
