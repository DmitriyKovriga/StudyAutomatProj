package org.example.lessons.lesson01.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Урок 1. JUnit 5 на примерах, похожих на тестирование API.
 *
 * Теория: docs/lessons/lesson-01/junit.md
 * Удаляйте @Disabled только у текущего задания.
 */
class JUnitTasksTest {

    private static final TestBookingApi API = new TestBookingApi();

    private BookingRequest standardBooking;
    private final List<Integer> bookingIdsForCleanup = new ArrayList<>();

    // Задание 2. Сделайте этот метод частью жизненного цикла JUnit: он должен
    // автоматически выполняться перед каждым тестом. Тело метода уже готово.
    // Добавьте только нужную аннотацию, затем включите task02 и запустите его.
    void prepareBookingForTest() {
        standardBooking = new BookingRequest(
                "Student-" + UUID.randomUUID(),
                "JUnit",
                150);
    }

    // Задание 3. Сделайте этот метод частью жизненного цикла JUnit: он должен
    // автоматически выполняться после каждого теста, даже если тест упал.
    // Логика очистки уже готова. Добавьте только нужную аннотацию и включите task03.
    void removeCreatedBookings() {
        bookingIdsForCleanup.forEach(API::deleteBooking);
        bookingIdsForCleanup.clear();
    }

    @Test
    @Disabled
    void task01_checkSuccessfulAuthenticationResponse() {
        ApiResponse<AuthResponse> response = API.authenticate("admin", "password123");
        int statusCode = response.statusCode();
        AuthResponse body = response.body();
        String token = body.token();

        // Задание 1. В уже созданный assertAll добавьте три проверки JUnit: статус
        // равен 200, body не равен null, token не пустой. Данные уже получены выше:
        // в этом задании нужно написать только проверки и удалить @Disabled.
        assertAll("Успешная авторизация");
    }

    @Test
    @Disabled
    void task02_beforeEachPreparesFreshBooking() {
        assertNotNull(standardBooking, "standardBooking должен создаваться в @BeforeEach");

        ApiResponse<CreateBookingResponse> response = API.createBooking(standardBooking);
        int bookingId = response.body().bookingid();
        bookingIdsForCleanup.add(bookingId);

        assertAll("Создание записи с данными из @BeforeEach",
                () -> assertEquals(200, response.statusCode()),
                () -> assertTrue(bookingId > 0),
                () -> assertEquals(standardBooking.firstname(), response.body().booking().firstname()));
    }

    @Test
    @Disabled
    void task03_afterEachRemovesCreatedBookings() {
        assertNotNull(standardBooking, "Сначала выполните задание 2");

        ApiResponse<CreateBookingResponse> response = API.createBooking(standardBooking);
        int bookingId = response.body().bookingid();
        bookingIdsForCleanup.add(bookingId);

        assertTrue(API.bookingExists(bookingId));
    }

    // Задание 4. Превратите этот метод в параметризованный тест JUnit. Настройте
    // три запуска со значениями 0, -1 и -100 через источник простых int. Добавьте
    // шаблон имени «Некорректный bookingId: {0}». Тело теста менять не нужно.
    @Disabled
    void task04_valueSourceForInvalidIds(int bookingId) {
        ApiResponse<BookingResponse> response = API.getBooking(bookingId);

        assertEquals(400, response.statusCode());
    }

    // Задание 5. Превратите этот метод в параметризованный тест JUnit и передайте
    // через таблицу три набора: успешный вход admin/password123 с 200 и токеном;
    // admin/wrong-password с 401 без токена; wrong-user/password123 с 401 без
    // токена. Имя каждого запуска: «Пользователь {0}: ожидается статус {2}».
    // Тело метода уже полностью готово — добавьте только аннотации JUnit и данные.
    @Disabled
    void task05_csvSourceForAuthentication(
            String username,
            String password,
            int expectedStatus,
            boolean tokenExpected) {

        ApiResponse<AuthResponse> response = API.authenticate(username, password);
        boolean actualTokenPresent = response.body() != null
                && response.body().token() != null
                && !response.body().token().isBlank();

        assertAll("Результат авторизации",
                () -> assertEquals(expectedStatus, response.statusCode()),
                () -> assertEquals(tokenExpected, actualTokenPresent));
    }

    // Задание 6. Превратите этот метод в параметризованный тест JUnit и настройте
    // отдельные запуски для null, пустой строки и строки из одного пробела. Имя
    // запуска должно быть «Некорректное имя гостя: [{0}]». Тело не меняйте.
    @Disabled
    void task06_nullAndEmptySourcesForRequiredField(String firstname) {
        BookingRequest request = new BookingRequest(firstname, "Student", 150);
        ApiResponse<CreateBookingResponse> response = API.createBooking(request);

        assertEquals(400, response.statusCode());
    }

    // Задание 7. Добавьте к методу две аннотации JUnit: отображаемое имя
    // «API доступен» для отчёта и тег smoke для выборочного запуска. Вызов API
    // и проверка уже написаны, поэтому больше ничего в теле менять не нужно.
    @Test
    @Disabled
    void task07_readableNameAndSmokeTag() {
        ApiResponse<Void> response = API.healthCheck();

        assertEquals(200, response.statusCode());
    }

    @Test
    @Disabled
    void task08_completeCreateAndReadScenario() {
        assertNotNull(standardBooking, "Сначала выполните задание 2");

        ApiResponse<CreateBookingResponse> createResponse = API.createBooking(standardBooking);
        int bookingId = createResponse.body().bookingid();
        bookingIdsForCleanup.add(bookingId);
        ApiResponse<BookingResponse> readResponse = API.getBooking(bookingId);
        BookingResponse saved = readResponse.body();

        // Задание 8. Соберите в assertAll проверки одного сценария: статус создания
        // равен 200, ID положительный, статус чтения равен 200, сохранённый объект
        // не null, а его firstname, lastname и totalprice равны standardBooking.
        // Все действия и фактические значения готовы — напишите только проверки.
        assertAll("Создание и чтение записи");
    }

    private record ApiResponse<T>(int statusCode, T body) {
    }

    private record AuthResponse(String token) {
    }

    private record BookingRequest(String firstname, String lastname, int totalprice) {
    }

    private record BookingResponse(String firstname, String lastname, int totalprice) {
    }

    private record CreateBookingResponse(int bookingid, BookingResponse booking) {
    }

    /**
     * Небольшая имитация внешнего API. Она нужна только для заданий JUnit:
     * здесь мы изучаем запуск тестов, подготовку данных и параметры без зависимости от сети.
     */
    private static final class TestBookingApi {
        private final AtomicInteger idSequence = new AtomicInteger(1000);
        private final Map<Integer, BookingResponse> bookings = new ConcurrentHashMap<>();

        ApiResponse<Void> healthCheck() {
            return new ApiResponse<>(200, null);
        }

        ApiResponse<AuthResponse> authenticate(String username, String password) {
            if ("admin".equals(username) && "password123".equals(password)) {
                return new ApiResponse<>(200, new AuthResponse("token-" + UUID.randomUUID()));
            }
            return new ApiResponse<>(401, null);
        }

        ApiResponse<CreateBookingResponse> createBooking(BookingRequest request) {
            if (request == null || request.firstname() == null || request.firstname().isBlank()) {
                return new ApiResponse<>(400, null);
            }

            int bookingId = idSequence.incrementAndGet();
            BookingResponse saved = new BookingResponse(
                    request.firstname(),
                    request.lastname(),
                    request.totalprice());
            bookings.put(bookingId, saved);
            return new ApiResponse<>(200, new CreateBookingResponse(bookingId, saved));
        }

        ApiResponse<BookingResponse> getBooking(int bookingId) {
            if (bookingId <= 0) {
                return new ApiResponse<>(400, null);
            }

            BookingResponse booking = bookings.get(bookingId);
            if (booking == null) {
                return new ApiResponse<>(404, null);
            }
            return new ApiResponse<>(200, booking);
        }

        ApiResponse<Void> deleteBooking(int bookingId) {
            return bookings.remove(bookingId) == null
                    ? new ApiResponse<>(404, null)
                    : new ApiResponse<>(201, null);
        }

        boolean bookingExists(int bookingId) {
            return bookings.containsKey(bookingId);
        }
    }
}
