package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        java.sql.Date sqlDate = rs.getDate("release_date");
        if (sqlDate != null) {
            film.setReleaseDate(sqlDate.toLocalDate());
        }

        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("rating_id"));

        // Пытаемся получить имя рейтинга, если оно есть в запросе
        try {
            mpa.setName(rs.getString("rating_name"));
        } catch (SQLException e) {
            // Игнорируем, если колонки нет
        }

        film.setMpa(mpa);

        // Инициализируем пустые коллекции
        film.setGenres(new HashSet<>());
        film.setLikes(new HashSet<>());

        return film;
    }
}