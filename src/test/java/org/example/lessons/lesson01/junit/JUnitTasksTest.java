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

    // Задание 2. Добавьте к этому методу подходящую аннотацию JUnit. Перед каждым
    // тестом создавайте новый standardBooking с уникальным firstname.
    void prepareBookingForTest() {
    }

    // Задание 3. Добавьте к этому методу подходящую аннотацию JUnit. После каждого
    // теста удаляйте из API все ID из bookingIdsForCleanup, затем очищайте список.
    void removeCreatedBookings() {
    }

    @Test
    @Disabled
    void task01_checkSuccessfulAuthenticationResponse() {
        ApiResponse<AuthResponse> response = API.authenticate("admin", "password123");

        // Задание 1. Получите из response фактический статус и токен. Подготовленные
        // проверки должны подтвердить оба свойства одного успешного ответа.
        int actualStatus = 0;
        String actualToken = null;

        assertAll("Успешная авторизация",
                () -> assertEquals(200, actualStatus),
                () -> assertNotNull(actualToken),
                () -> assertFalse(actualToken.isBlank()));
    }

    @Test
    @Disabled
    void task02_beforeEachPreparesFreshBooking() {
        // Задание 2 находится над методом prepareBookingForTest. В этом тесте не нужно
        // создавать standardBooking вручную: его должен подготовить JUnit перед запуском.
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
        // Задание 3 находится над методом removeCreatedBookings. Созданная здесь запись
        // должна существовать во время теста и удаляться автоматически после него.
        assertNotNull(standardBooking, "Сначала выполните задание 2");

        ApiResponse<CreateBookingResponse> response = API.createBooking(standardBooking);
        int bookingId = response.body().bookingid();
        bookingIdsForCleanup.add(bookingId);

        assertTrue(API.bookingExists(bookingId));
    }

    @ParameterizedTest(name = "Некорректный bookingId: {0}")
    @ValueSource(ints = {0, -1, -100})
    @Disabled
    void task04_valueSourceForInvalidIds(int bookingId) {
        ApiResponse<BookingResponse> response = API.getBooking(bookingId);

        // Задание 4. Получите фактический HTTP-статус из response. JUnit запустит
        // одну и ту же проверку отдельно для каждого ID из ValueSource.
        int actualStatus = 0;

        assertEquals(400, actualStatus);
    }

    @ParameterizedTest(name = "Пользователь {0}: ожидается статус {2}")
    @CsvSource({
            "admin, password123, 200, true",
            "admin, wrong-password, 401, false",
            "wrong-user, password123, 401, false"
    })
    @Disabled
    void task05_csvSourceForAuthentication(
            String username,
            String password,
            int expectedStatus,
            boolean tokenExpected) {

        // Задание 5. Вызовите API с username и password из текущей строки CsvSource.
        // Определите фактический статус и наличие непустого токена в ответе.
        ApiResponse<AuthResponse> response = null;
        int actualStatus = 0;
        boolean actualTokenPresent = false;

        assertAll("Результат авторизации",
                () -> assertEquals(expectedStatus, actualStatus),
                () -> assertEquals(tokenExpected, actualTokenPresent));
    }

    @ParameterizedTest(name = "Некорректное имя гостя: [{0}]")
    @NullSource
    @EmptySource
    @ValueSource(strings = {" "})
    @Disabled
    void task06_nullAndEmptySourcesForRequiredField(String firstname) {
        BookingRequest request = new BookingRequest(firstname, "Student", 150);

        // Задание 6. Отправьте request в API и получите фактический статус.
        // Один метод должен проверить null, пустую строку и строку из пробела.
        ApiResponse<CreateBookingResponse> response = null;
        int actualStatus = 0;

        assertEquals(400, actualStatus);
    }

    @Test
    @Disabled
    void task07_readableNameAndSmokeTag() {
        // Задание 7. Добавьте к тесту отображаемое имя «API доступен» и тег smoke.
        // Затем вызовите API.healthCheck() и получите фактический статус ответа.
        ApiResponse<Void> response = null;
        int actualStatus = 0;

        assertEquals(200, actualStatus);
    }

    @Test
    @Disabled
    void task08_completeCreateAndReadScenario() {
        assertNotNull(standardBooking, "Сначала выполните задание 2");

        // Задание 8. Создайте запись, сохраните её ID для очистки и прочитайте запись
        // по ID. Заполните фактический статус создания, ID и прочитанный объект.
        int createStatus = 0;
        int bookingId = 0;
        BookingResponse saved = null;

        assertAll("Создание и чтение записи",
                () -> assertEquals(200, createStatus),
                () -> assertTrue(bookingId > 0),
                () -> assertNotNull(saved),
                () -> assertEquals(standardBooking.firstname(), saved.firstname()),
                () -> assertEquals(standardBooking.lastname(), saved.lastname()),
                () -> assertEquals(standardBooking.totalprice(), saved.totalprice()));
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
