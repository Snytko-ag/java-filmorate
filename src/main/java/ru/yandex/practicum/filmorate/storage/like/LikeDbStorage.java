package ru.yandex.practicum.filmorate.storage.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class LikeDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final FilmMapper filmMapper;

    @Autowired
    public LikeDbStorage(JdbcTemplate jdbcTemplate, MpaService mpaService, FilmMapper filmMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.filmMapper = filmMapper;
    }

    public void like(Integer filmId, Integer userId) {
        log.debug("Добавление лайка: filmId={}, userId={}", filmId, userId);

        // Проверяем, не поставил ли уже лайк
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count > 0) {
            log.debug("Пользователь {} уже лайкнул фильм {}", userId, filmId);
            return;
        }

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }


    public void dislike(Integer filmId, Integer userId) {
        log.debug("Удаление лайка: filmId={}, userId={}", filmId, userId);

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, filmId, userId);

        if (rowsAffected == 0) {
            log.debug("Лайк не найден: filmId={}, userId={}", filmId, userId);

        } else {
            log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        }
    }

    public Set<Integer> getLikesByFilmId(Integer filmId) {
        log.debug("Получение лайков для фильма с ID={}", filmId);

        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";

        List<Integer> likes = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId
        );

        return new HashSet<>(likes);
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Получение {} популярных фильмов", count);

        String sql = "SELECT f.*, r.name as rating_name, r.description as rating_description, " +
                "COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, r.name, r.description " +
                "ORDER BY likes_count DESC, f.id " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, filmMapper, count);
    }
}
