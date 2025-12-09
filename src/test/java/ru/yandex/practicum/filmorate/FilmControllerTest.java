package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        this.controller = new FilmController(); // Создаем реальный экземпляр контроллера
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
}
