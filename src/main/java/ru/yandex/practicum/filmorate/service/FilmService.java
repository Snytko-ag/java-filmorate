package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;


import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final LikeDbStorage likeDbStorage;
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       LikeDbStorage likeDbStorage,
                       GenreDbStorage genreDbStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.likeDbStorage = likeDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public void like(Integer filmId, Integer userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);

        // 1. Проверяем существование пользователя
        userService.getUserStorage().getUserById(userId);

        // 2. Проверяем существование фильма
        filmStorage.getFilmById(filmId);

        // 3. Используем LikeDbStorage для сохранения в БД
        likeDbStorage.like(filmId, userId);

        log.info("Пользователь '{}' поставил лайк фильму '{}'", userId, filmId);
    }


    public void dislike(Integer filmId, Integer userId) {
        log.debug("Пользователь {} удаляет лайк у фильма {}", userId, filmId);

        // Проверяем существование пользователя
        userService.getUserStorage().getUserById(userId);

        // Проверяем существование фильма
        filmStorage.getFilmById(filmId);

        // Используем LikeDbStorage для удаления из БД
        likeDbStorage.dislike(filmId, userId);

        log.info("Пользователь '{}' удалил лайк у фильма '{}'", userId, filmId);
    }

    public List<Film> getPopularMovies(int count) {
        log.info("Получение {} популярных фильмов", count);

        // Если у вас есть метод getPopularFilms в LikeDbStorage
        List<Film> films = likeDbStorage.getPopularFilms(count);

        // Загружаем жанры для каждого фильма
        films.forEach(film -> {
            Set<Genre> genres = genreDbStorage.getGenresByFilmId(film.getId());
            film.setGenres(genres);

            // Загружаем лайки
            Set<Integer> likes = likeDbStorage.getLikesByFilmId(film.getId());
            film.setLikes(likes);
        });

        return films;
    }
}
