package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureCache
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final FilmService filmService;
    private final UserService userService;
    private User firstUser;
    private User secondUser;
    private User thirdUser;
    private Film firstFilm;
    private Film secondFilm;
    private Film thirdFilm;

    @BeforeEach
    public void beforeEach() {
        firstUser = User.builder()
                .name("Алексей Петров")
                .login("alex_petrov")
                .email("alex@mail.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();

        secondUser = User.builder()
                .name("Мария Сидорова")
                .login("maria_sidorova")
                .email("maria@mail.com")
                .birthday(LocalDate.of(1990, 8, 22))
                .build();

        thirdUser = User.builder()
                .name("Иван Иванов")
                .login("ivan_ivanov")
                .email("ivan@mail.com")
                .birthday(LocalDate.of(1985, 3, 10))
                .build();

        firstFilm = Film.builder()
                .name("Интерстеллар")
                .description("Фантастический фильм Кристофера Нолана о космическом путешествии" +
                        " для спасения человечества от экологической катастрофы.")
                .releaseDate(LocalDate.of(2014, 10, 26))
                .duration(169)
                .build();
        firstFilm.setMpa(new Mpa(1, "G"));
        firstFilm.setLikes(new HashSet<>());
        firstFilm.setGenres(new HashSet<>(Arrays.asList(
                new Genre(5, "Фантастика"),
                new Genre(2, "Драма")
        )));

        secondFilm = Film.builder()
                .name("Зеленая книга")
                .description("Драмеди о путешествии афроамериканского музыканта и его" +
                        " водителя-итальянца по южным штатам США в 1960-х годах.")
                .releaseDate(LocalDate.of(2018, 11, 16))
                .duration(130)
                .build();
        secondFilm.setMpa(new Mpa(3, "PG-13"));
        secondFilm.setLikes(new HashSet<>());
        secondFilm.setGenres(new HashSet<>(Arrays.asList(
                new Genre(2, "Драма"),
                new Genre(1, "Комедия")
        )));

        thirdFilm = Film.builder()
                .name("Побег из Шоушенка")
                .description("Драма о банкире Энди Дюфрейне, осужденном за убийство жены" +
                        " и ее любовника и приговоренном к пожизненному заключению в тюрьме Шоушенк.")
                .releaseDate(LocalDate.of(1994, 9, 23))
                .duration(142)
                .build();
        thirdFilm.setMpa(new Mpa(4, "R"));
        thirdFilm.setLikes(new HashSet<>());
        thirdFilm.setGenres(new HashSet<>(Arrays.asList(
                new Genre(2, "Драма")
        )));
    }

    @Test
    public void testCreateUserAndGetUserById() {
        firstUser = userStorage.createUser(firstUser);
        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(firstUser.getId()));
        assertThat(userOptional)
                .hasValueSatisfying(user ->
                        assertThat(user)
                                .hasFieldOrPropertyWithValue("id", firstUser.getId())
                                .hasFieldOrPropertyWithValue("name", "Алексей Петров"));
    }

    @Test
    public void testGetUsers() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        Collection<User> listUsers = userStorage.findAll();
        assertThat(listUsers).contains(firstUser);
        assertThat(listUsers).contains(secondUser);
    }

    @Test
    public void testUpdateUser() {
        firstUser = userStorage.createUser(firstUser);
        User updateUser = User.builder()
                .id(firstUser.getId())
                .name("Алексей Обновленный")
                .login("alex_petrov")
                .email("alex@mail.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();
        Optional<User> testUpdateUser = Optional.ofNullable(userStorage.updateUser(updateUser));
        assertThat(testUpdateUser)
                .hasValueSatisfying(user -> assertThat(user)
                        .hasFieldOrPropertyWithValue("name", "Алексей Обновленный")
                );
    }

    @Test
    public void testCreateFilmAndGetFilmById() {
        firstFilm = filmStorage.createFilm(firstFilm);
        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(firstFilm.getId()));
        assertThat(filmOptional)
                .hasValueSatisfying(film -> assertThat(film)
                        .hasFieldOrPropertyWithValue("id", firstFilm.getId())
                        .hasFieldOrPropertyWithValue("name", "Интерстеллар")
                );
    }

    @Test
    public void testGetFilms() {
        firstFilm = filmStorage.createFilm(firstFilm);
        secondFilm = filmStorage.createFilm(secondFilm);
        thirdFilm = filmStorage.createFilm(thirdFilm);
        List<Film> listFilms = filmStorage.findAll();
        assertThat(listFilms).contains(firstFilm);
        assertThat(listFilms).contains(secondFilm);
        assertThat(listFilms).contains(thirdFilm);
    }

    @Test
    public void testUpdateFilm() {
        firstFilm = filmStorage.createFilm(firstFilm);
        Film updateFilm = Film.builder()
                .id(firstFilm.getId())
                .name("Обновленное название")
                .description("Обновленное описание фильма")
                .releaseDate(LocalDate.of(2014, 10, 26))
                .duration(169)
                .build();
        updateFilm.setMpa(new Mpa(1, "G"));
        Optional<Film> testUpdateFilm = Optional.ofNullable(filmStorage.updateFilm(updateFilm));
        assertThat(testUpdateFilm)
                .hasValueSatisfying(film ->
                        assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Обновленное название")
                                .hasFieldOrPropertyWithValue("description", "Обновленное описание фильма")
                );
    }

    @Test
    public void deleteFilm() {
        firstFilm = filmStorage.createFilm(firstFilm);
        secondFilm = filmStorage.createFilm(secondFilm);
        filmStorage.deleteFilmsById(firstFilm.getId());
        List<Film> listFilms = filmStorage.findAll();
        assertThat(listFilms).hasSize(1);
        assertThat(Optional.of(listFilms.get(0)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Зеленая книга"));
    }

    @Test
    public void testAddLike() {
        firstUser = userStorage.createUser(firstUser);
        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.like(firstFilm.getId(), firstUser.getId());
        firstFilm = filmStorage.getFilmById(firstFilm.getId());
        assertThat(firstFilm.getLikes()).hasSize(1);
        assertThat(firstFilm.getLikes()).contains(firstUser.getId());
    }

    @Test
    public void testDeleteLike() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.like(firstFilm.getId(), firstUser.getId());
        filmService.like(firstFilm.getId(), secondUser.getId());
        filmService.dislike(firstFilm.getId(), firstUser.getId());
        firstFilm = filmStorage.getFilmById(firstFilm.getId());
        assertThat(firstFilm.getLikes()).hasSize(1);
        assertThat(firstFilm.getLikes()).contains(secondUser.getId());
    }

    @Test
    public void testGetPopularFilms() {

        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);

        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.like(firstFilm.getId(), firstUser.getId());

        secondFilm = filmStorage.createFilm(secondFilm);
        filmService.like(secondFilm.getId(), firstUser.getId());
        filmService.like(secondFilm.getId(), secondUser.getId());
        filmService.like(secondFilm.getId(), thirdUser.getId());

        thirdFilm = filmStorage.createFilm(thirdFilm);
        filmService.like(thirdFilm.getId(), firstUser.getId());
        filmService.like(thirdFilm.getId(), secondUser.getId());

        List<Film> listFilms = filmService.getPopularMovies(5);

        assertThat(listFilms).hasSize(3);

        assertThat(Optional.of(listFilms.get(0)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Зеленая книга"));

        assertThat(Optional.of(listFilms.get(1)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Побег из Шоушенка"));

        assertThat(Optional.of(listFilms.get(2)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Интерстеллар"));
    }

    @Test
    public void testAddFriend() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        assertThat(userService.getFriends(firstUser.getId())).hasSize(1);
        assertThat(userService.getFriends(firstUser.getId())).contains(secondUser);
    }

    @Test
    public void testGetFriends() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        assertThat(userService.getFriends(firstUser.getId())).hasSize(2);
        assertThat(userService.getFriends(firstUser.getId())).contains(secondUser, thirdUser);
    }

}