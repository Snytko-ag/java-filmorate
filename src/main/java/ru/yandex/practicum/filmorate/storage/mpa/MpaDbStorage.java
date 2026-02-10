package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaMapper;

import java.util.List;

@Component
@Slf4j
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaMapper mpaMapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate, MpaMapper mpaMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaMapper = mpaMapper;
    }

    public List<Mpa> getAllMpa() {
        log.debug("Получение всех рейтингов MPA");

        String sql = "SELECT id, name FROM ratings_mpa ORDER BY id";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaMapper);

        log.trace("Найдено рейтингов MPA: {}", mpaList.size());
        return mpaList;
    }

    public Mpa getMpaById(Integer mpaId) {
        log.debug("Получение MPA по ID: {}", mpaId);

        String sql = "SELECT id, name FROM ratings_mpa WHERE id = ?";

        try {
            Mpa mpa = jdbcTemplate.queryForObject(sql, mpaMapper, mpaId);
            log.trace("Найден рейтинг MPA: id={}, name={}", mpa.getId(), mpa.getName());
            return mpa;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Рейтинг MPA с ID={} не найден", mpaId);
            throw new NotFoundException("Рейтинг с ID=" + mpaId + " не найден");
        }
    }
}
