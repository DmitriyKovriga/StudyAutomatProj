package org.example.lessons.lesson01.restassured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Урок 1. Практическое тестирование REST API с помощью REST Assured.
 *
 * API: https://restful-booker.herokuapp.com
 * Теория: docs/lessons/lesson-01/rest-assured.md
 *
 * Решайте задания по порядку. Удаляйте @Disabled только у текущего теста.
 */
class RestAssuredTasksTest {

    private static final String BASE_URL = "https://restful-booker.herokuapp.com";
    private static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .setAccept(ContentType.JSON)
            .build();

    private final Set<Integer> createdBookingIds = new LinkedHashSet<>();

    @AfterEach
    void removeCreatedBookings() {
        for (int bookingId : createdBookingIds) {
            deleteBookingIfPresent(bookingId);
        }
        createdBookingIds.clear();
    }

    @Test
    @Disabled
    void task01_receiveAuthenticationToken() {
        AuthRequest credentials = new AuthRequest("admin", "password123");

        // Задание 1. Отправьте credentials в теле POST /auth. Сохраните полный ответ
        // в response, а JSON из ответа преобразуйте в объект AuthResponse.
        Response response = null;
        AuthResponse actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).contains("application/json");
        assertThat(actual).isNotNull();
        assertThat(actual.token()).isNotBlank();
    }

    @Test
    @Disabled
    void task02_receiveBookingIdsAsDtoList() {
        // Задание 2. Отправьте GET /booking. Сохраните полный ответ в response,
        // а JSON-массив преобразуйте в список объектов BookingIdResponse.
        Response response = null;
        List<BookingIdResponse> bookings = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).contains("application/json");
        assertThat(bookings)
                .isNotEmpty()
                .allSatisfy(booking -> assertThat(booking.bookingid()).isPositive());
    }

    @Test
    @Disabled
    void task03_createBookingFromDto() {
        BookingRequest expected = uniqueBooking("Create");

        // Задание 3. Отправьте expected в теле POST /booking. Преобразуйте JSON-ответ
        // в CreateBookingResponse и добавьте полученный bookingid в createdBookingIds.
        Response response = null;
        CreateBookingResponse actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).isNotNull();
        assertThat(actual.bookingid()).isPositive();
        assertThat(actual.booking()).usingRecursiveComparison().isEqualTo(expected);
        assertThat(createdBookingIds).contains(actual.bookingid());
    }

    @Test
    @Disabled
    void task04_readCreatedBookingById() {
        BookingRequest expected = uniqueBooking("Read");
        int bookingId = createTrackedBooking(expected);

        // Задание 4. Передайте bookingId как параметр пути в GET /booking/{id}.
        // Сохраните полный ответ и преобразуйте его JSON в BookingResponse.
        Response response = null;
        BookingResponse actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @Disabled
    void task05_filterBookingsByGuestName() {
        BookingRequest expected = uniqueBooking("Filter");
        int createdId = createTrackedBooking(expected);

        // Задание 5. Передайте firstname и lastname как параметры запроса в GET /booking.
        // Преобразуйте полученный JSON-массив в список BookingIdResponse.
        Response response = null;
        List<BookingIdResponse> actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).extracting(BookingIdResponse::bookingid).contains(createdId);
    }

    @Test
    @Disabled
    void task06_replaceBookingWithPut() {
        int bookingId = createTrackedBooking(uniqueBooking("BeforePut"));
        BookingRequest expected = uniqueBooking("AfterPut");
        String token = getToken();

        // Задание 6. Отправьте PUT /booking/{id}: bookingId передайте в путь,
        // token — в cookie, expected — в JSON-тело. Затем отдельным GET прочитайте запись.
        Response updateResponse = null;
        BookingResponse updateBody = null;
        BookingResponse saved = null;

        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        assertThat(updateBody).usingRecursiveComparison().isEqualTo(expected);
        assertThat(saved).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @Disabled
    void task07_changeOneFieldWithPatch() {
        BookingRequest original = uniqueBooking("BeforePatch");
        int bookingId = createTrackedBooking(original);
        String newLastName = "Changed-" + UUID.randomUUID();
        Map<String, Object> changes = Map.of("lastname", newLastName);

        // Задание 7. Отправьте changes в PATCH /booking/{id} с token в cookie.
        // После PATCH отдельным GET получите фактически сохранённую запись в saved.
        Response patchResponse = null;
        BookingResponse saved = null;

        assertThat(patchResponse).isNotNull();
        assertThat(patchResponse.statusCode()).isEqualTo(200);
        assertThat(saved).isNotNull();
        assertThat(saved.lastname()).isEqualTo(newLastName);
        assertThat(saved.firstname()).isEqualTo(original.firstname());
        assertThat(saved.totalprice()).isEqualTo(original.totalprice());
        assertThat(saved.bookingdates()).isEqualTo(original.bookingdates());
    }

    @Test
    @Disabled
    void task08_updateWithoutTokenIsRejected() {
        BookingRequest original = uniqueBooking("Protected");
        int bookingId = createTrackedBooking(original);
        BookingRequest forbiddenUpdate = uniqueBooking("MustNotBeSaved");

        // Задание 8. Отправьте PUT /booking/{id} без token. Сохраните ответ сервера,
        // затем отдельным GET получите запись и убедитесь, что она не изменилась.
        Response deniedResponse = null;
        BookingResponse saved = null;

        assertThat(deniedResponse).isNotNull();
        assertThat(deniedResponse.statusCode()).isEqualTo(403);
        assertThat(saved).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @Disabled
    void task09_deleteBookingAndCheckThatItIsGone() {
        int bookingId = createTrackedBooking(uniqueBooking("Delete"));

        // Задание 9. Отправьте DELETE /booking/{id} с token в cookie. После успешного
        // удаления уберите ID из createdBookingIds и выполните контрольный GET по тому же ID.
        Response deleteResponse = null;
        Response getAfterDelete = null;

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.statusCode()).isEqualTo(201);
        assertThat(createdBookingIds).doesNotContain(bookingId);
        assertThat(getAfterDelete).isNotNull();
        assertThat(getAfterDelete.statusCode()).isEqualTo(404);
    }

    @Test
    @Disabled
    void task10_completeBookingLifecycle() {
        BookingRequest initial = uniqueBooking("FinalCreate");
        BookingRequest updated = uniqueBooking("FinalUpdate");
        Integer bookingId = null;

        try {
            // Задание 10. Самостоятельно соберите один рабочий сценарий:
            // POST → GET → PUT → GET → DELETE → GET. Заполните переменные ниже.
            Response createResponse = null;
            CreateBookingResponse created = null;
            BookingResponse afterCreate = null;
            Response updateResponse = null;
            BookingResponse afterUpdate = null;
            Response deleteResponse = null;
            Response afterDelete = null;

            assertThat(createResponse).isNotNull();
            assertThat(createResponse.statusCode()).isEqualTo(200);
            assertThat(created).isNotNull();
            assertThat(bookingId).isEqualTo(created.bookingid()).isPositive();
            assertThat(afterCreate).usingRecursiveComparison().isEqualTo(initial);
            assertThat(updateResponse).isNotNull();
            assertThat(updateResponse.statusCode()).isEqualTo(200);
            assertThat(afterUpdate).usingRecursiveComparison().isEqualTo(updated);
            assertThat(deleteResponse).isNotNull();
            assertThat(deleteResponse.statusCode()).isEqualTo(201);
            assertThat(afterDelete).isNotNull();
            assertThat(afterDelete.statusCode()).isEqualTo(404);
        } finally {
            if (bookingId != null) {
                deleteBookingIfPresent(bookingId);
            }
        }
    }

    private Response createBooking(BookingRequest request) {
        return given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/booking");
    }

    private int createTrackedBooking(BookingRequest request) {
        Response response = createBooking(request);
        assertThat(response.statusCode()).as("Статус подготовки тестовых данных").isEqualTo(200);
        int bookingId = response.as(CreateBookingResponse.class).bookingid();
        createdBookingIds.add(bookingId);
        return bookingId;
    }

    private Response getBooking(int bookingId) {
        return given()
                .spec(BASE_SPEC)
                .pathParam("id", bookingId)
                .when()
                .get("/booking/{id}");
    }

    private String getToken() {
        Response response = given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(new AuthRequest("admin", "password123"))
                .when()
                .post("/auth");
        assertThat(response.statusCode()).as("Статус подготовки авторизации").isEqualTo(200);
        return response.as(AuthResponse.class).token();
    }

    private void deleteBookingIfPresent(int bookingId) {
        try {
            given()
                    .spec(BASE_SPEC)
                    .cookie("token", getToken())
                    .pathParam("id", bookingId)
                    .when()
                    .delete("/booking/{id}");
        } catch (RuntimeException cleanupError) {
            System.err.println("Не удалось удалить booking " + bookingId + ": "
                    + cleanupError.getMessage());
        }
    }

    private BookingRequest uniqueBooking(String prefix) {
        return new BookingRequest(
                prefix + "-" + UUID.randomUUID(),
                "Student",
                150,
                true,
                new BookingDates("2026-08-01", "2026-08-05"),
                "Breakfast");
    }

    public record BookingIdResponse(int bookingid) {
    }

    public record AuthRequest(String username, String password) {
    }

    public record AuthResponse(String token) {
    }

    public record BookingDates(String checkin, String checkout) {
    }

    public record BookingRequest(
            String firstname,
            String lastname,
            int totalprice,
            boolean depositpaid,
            BookingDates bookingdates,
            String additionalneeds) {
    }

    public record BookingResponse(
            String firstname,
            String lastname,
            int totalprice,
            boolean depositpaid,
            BookingDates bookingdates,
            String additionalneeds) {
    }

    public record CreateBookingResponse(int bookingid, BookingResponse booking) {
    }
}
