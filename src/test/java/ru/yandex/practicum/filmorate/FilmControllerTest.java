
package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmControllerTest {

    private FilmController controller;

    private final FilmStorage storage = new InMemoryFilmStorage();
    private final UserStorage userStorage = new InMemoryUserStorage();
    private final UserService userService = new UserService(userStorage);
    private final FilmService service = new FilmService(storage, userService);


    @BeforeEach
    void setUp() {
        this.controller = new FilmController(storage, service); // Создаем реальный экземпляр контроллера
    }

    @Test
    public void testCreateEmptyTitle() throws Exception {
        Film film = new Film();
        film.setDescription("Valid name");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        boolean except = false;

        try {
            controller.create(film); // Пробуем создать фильм без названия
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");

    }

    @Test
    public void testCreateTooLongDescription() throws Exception {
        Film film = new Film();
        String longDesc = "a".repeat(201);
        film.setName("Valid description");
        film.setDescription(longDesc);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        boolean except = false;

        try {
            controller.create(film); // Пробуем создать фильм с длинным описанием
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");
    }

    @Test
    public void testCreateInvalidReleaseDate() throws Exception {
        Film film = new Film();
        film.setName("Valid ReleaseDate");
        film.setDescription("Valid ReleaseDate");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Дата до минимальной разрешенной
        film.setDuration(120);

        boolean except = false;

        try {
            controller.create(film); // Пробуем создать фильм с не верной датой
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");
    }

    @Test
    public void testCreateNegativeDuration() throws Exception {
        Film film = new Film();
        film.setName("Valid NegativeDuration");
        film.setDescription("Valid NegativeDuration");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);

        boolean except = false;

        try {
            controller.create(film); // Пробуем создать фильм с отрицательной продолжительностью
        } catch (ValidationException e) { // Ожидаем исключение ValidationException
            except = true;
        }

        assertTrue(except, "Исключение не возникло, хотя ожидалось ValidationException.");

    }

    @Test
    public void testCreateSuccess() throws Exception {
        Film film = new Film();
        film.setName("Create Success");
        film.setDescription("Create Success");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        assertEquals(Integer.valueOf(1), controller.create(film).getId()); // Проверка, что присвоился ID
    }

    @Test
    public void testUpdateFilm() throws  Exception {
        Film film = new Film();
        film.setName("Create Success");
        film.setDescription("Create Success");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        controller.create(film);

        Film filmNew = new Film();
        filmNew.setId(1);
        filmNew.setName("Create Success new");
        filmNew.setDescription("Create Success new");
        filmNew.setReleaseDate(LocalDate.of(2025, 1, 1));
        filmNew.setDuration(125);

        Film filmUpdate = controller.update(filmNew);

        assertEquals("Create Success new", filmUpdate.getName());
        assertEquals("Create Success new", filmUpdate.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), filmUpdate.getReleaseDate());
        assertEquals(Integer.valueOf(125), filmUpdate.getDuration());

    }

    @Test
    void likeAMovie_whenUserLikesFilm_thenFilmContainsUserLike() {
        User user = new User(1, "test@ya.ru", "test", "Ivan",
                LocalDate.of(2008, 8, 25), new HashSet<>());
        userStorage.createUser(user);
        Film film = new Film(1, "Film", "FilmTest",
                LocalDate.of(2026, 1, 2), 120, new HashSet<>());
        controller.create(film);
        controller.likeAMovie(film.getId(), user.getId());

        Assertions.assertTrue(film.getLikesNumber() != 0);
    }

    @Test
    void getPopularMovies_shouldReturnMostLikedFilmsFirst() {
        User user = new User(1, "test@ya.ru", "test", "Ivan",
                LocalDate.of(2008, 8, 25), new HashSet<>());
        userStorage.createUser(user);
        Film film = new Film(1, "Film", "FilmTest",
                LocalDate.of(2026, 1, 2), 120, new HashSet<>());
        controller.create(film);
        controller.likeAMovie(film.getId(), user.getId());
        List<Film> popularMoviesList = service.getPopularMovies(1);

        Assertions.assertEquals(1, popularMoviesList.size());
    }
}


