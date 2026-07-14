package org.example.lessons.lesson01.restassured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Урок 1 / REST Assured.
 *
 * API: https://restful-booker.herokuapp.com
 * Теория: docs/lessons/lesson-01/rest-assured.md
 *
 * Удаляйте @Disabled только у текущего задания. Тесты обращаются во внешний сервис.
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
        if (createdBookingIds.isEmpty()) {
            return;
        }

        try {
            String token = getToken();
            for (int id : createdBookingIds) {
                given()
                        .spec(BASE_SPEC)
                        .cookie("token", token)
                        .when()
                        .delete("/booking/{id}", id);
            }
        } catch (RuntimeException cleanupError) {
            System.err.println("Cleanup failed: " + cleanupError.getMessage());
        }
    }

    @Test
    @Disabled("Задание 1: реализуйте health check")
    void task01_healthCheck() {
        // TODO: GET /ping, проверьте status 201 и body "Created".
        failUntilImplemented();
    }

    @Test
    @Disabled("Задание 2: проверьте список booking id")
    void task02_getBookingIds() {
        // TODO: GET /booking.
        // Проверки: status 200, JSON-массив не пуст, каждый bookingid > 0.
        failUntilImplemented();
    }

    @Test
    @Disabled("Задание 3: примените queryParam firstname")
    void task03_filterBookingsByFirstName() {
        BookingRequest booking = uniqueBooking("Filter");
        int createdId = createTrackedBooking(booking);

        // TODO: GET /booking?firstname=<booking.firstname()> и сохраните Response.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertThat(ids).contains(createdId);
    }

    @Test
    @Disabled("Задание 4: создайте booking body через Map")
    void task04_createBookingFromMap() {
        String firstName = "Map-" + UUID.randomUUID();
        Map<String, Object> dates = Map.of(
                "checkin", "2026-08-01",
                "checkout", "2026-08-05");
        Map<String, Object> body = Map.of(
                "firstname", firstName,
                "lastname", "Student",
                "totalprice", 250,
                "depositpaid", true,
                "bookingdates", dates,
                "additionalneeds", "Breakfast");

        // TODO: POST /booking с Content-Type JSON и body. Сохраните Response.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("bookingid")).isPositive();
        assertThat(response.jsonPath().getString("booking.firstname")).isEqualTo(firstName);

        // TODO: добавьте созданный bookingid в createdBookingIds для cleanup.
    }

    @Test
    @Disabled("Задание 5: извлеките id и выполните последующий GET")
    void task05_extractIdAndGetCreatedBooking() {
        BookingRequest expected = uniqueBooking("Extract");

        // TODO: самостоятельно создайте booking, извлеките bookingid и выполните GET /booking/{id}.
        int bookingId = 0;
        Response getResponse = null;

        assertThat(bookingId).isPositive();
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.jsonPath().getString("firstname")).isEqualTo(expected.firstname());
        // TODO: зарегистрируйте id в createdBookingIds.
    }

    @Test
    @Disabled("Задание 6: десериализуйте JSON в BookingResponse DTO")
    void task06_deserializeResponseToDto() {
        BookingRequest expected = uniqueBooking("Dto");
        int bookingId = createTrackedBooking(expected);

        // TODO: GET /booking/{id}, затем response.as(BookingResponse.class).
        BookingResponse actual = null;

        assertThat(actual).isNotNull();
        assertThat(actual.firstname()).isEqualTo(expected.firstname());
        assertThat(actual.bookingdates()).isEqualTo(expected.bookingdates());
        assertThat(actual.totalprice()).isEqualTo(expected.totalprice());
    }

    @Test
    @Disabled("Задание 7: создайте RequestSpecification")
    void task07_requestSpecification() {
        // TODO: через RequestSpecBuilder задайте baseUri, Accept JSON и логирование при ошибке.
        RequestSpecification specification = null;

        Response response = given()
                .spec(specification)
                .when()
                .get("/booking");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("bookingid")).isNotNull();
    }

    @Test
    @Disabled("Задание 8: создайте ResponseSpecification")
    void task08_responseSpecification() {
        // TODO: через ResponseSpecBuilder ожидайте status 200 и Content-Type JSON.
        ResponseSpecification okJson = null;

        given()
                .spec(BASE_SPEC)
                .when()
                .get("/booking")
                .then()
                .spec(okJson)
                .body("bookingid", everyItem(greaterThan(0)));
    }

    @Test
    @Disabled("Задание 9: получите token и проверьте плохие credentials")
    void task09_authentication() {
        // TODO: POST /auth с admin/password123 и извлеките token.
        String token = null;

        assertThat(token).isNotBlank();

        // TODO: повторите POST /auth с неверным password и сохраните Response.
        Response invalidAuth = null;

        assertThat(invalidAuth).isNotNull();
        assertThat(invalidAuth.statusCode()).isEqualTo(200);
        assertThat(invalidAuth.jsonPath().getString("reason")).isEqualTo("Bad credentials");
    }

    @Test
    @Disabled("Задание 10: выполните полное обновление PUT")
    void task10_fullUpdateWithPut() {
        int bookingId = createTrackedBooking(uniqueBooking("BeforePut"));
        BookingRequest updated = uniqueBooking("AfterPut");

        // TODO: PUT /booking/{id} с cookie token и updated body. Сохраните Response.
        Response updateResponse = null;

        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.statusCode()).isEqualTo(200);

        BookingResponse saved = getBooking(bookingId).as(BookingResponse.class);
        assertThat(saved.firstname()).isEqualTo(updated.firstname());
        assertThat(saved.lastname()).isEqualTo(updated.lastname());
        assertThat(saved.totalprice()).isEqualTo(updated.totalprice());
    }

    @Test
    @Disabled("Задание 11: выполните частичное обновление PATCH")
    void task11_partialUpdateKeepsOtherFields() {
        BookingRequest original = uniqueBooking("BeforePatch");
        int bookingId = createTrackedBooking(original);
        String changedLastName = "Patched-" + UUID.randomUUID();

        // TODO: PATCH /booking/{id} с body Map.of("lastname", changedLastName) и cookie token.
        Response patchResponse = null;

        assertThat(patchResponse).isNotNull();
        assertThat(patchResponse.statusCode()).isEqualTo(200);

        BookingResponse saved = getBooking(bookingId).as(BookingResponse.class);
        assertThat(saved.lastname()).isEqualTo(changedLastName);
        assertThat(saved.firstname()).isEqualTo(original.firstname());
        assertThat(saved.totalprice()).isEqualTo(original.totalprice());
    }

    @Test
    @Disabled("Задание 12: удалите запись и подтвердите 404")
    void task12_deleteAndVerify() {
        int bookingId = createTrackedBooking(uniqueBooking("Delete"));

        // TODO: DELETE /booking/{id} с cookie token, ожидайте status 201.
        Response deleteResponse = null;

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.statusCode()).isEqualTo(201);
        assertThat(getBooking(bookingId).statusCode()).isEqualTo(404);

        createdBookingIds.remove(bookingId);
    }

    @Test
    @Disabled("Задание 13: негативный GET с логированием при ошибке")
    void task13_unknownBookingReturns404() {
        int unknownId = Integer.MAX_VALUE;

        // TODO: GET /booking/{id}; добавьте log().ifValidationFails() для request и response.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    @Disabled("Задание 14: примените JSON Schema validator")
    void task14_jsonSchemaValidation() {
        int bookingId = createTrackedBooking(uniqueBooking("Schema"));

        // TODO: GET /booking/{id}, status 200 и matchesJsonSchemaInClasspath(...).
        // Schema уже лежит в schemas/lesson01/booking-schema.json.
        failUntilImplemented();
    }

    @Test
    @Disabled("Задание 15: реализуйте независимый CRUD-сценарий")
    void task15_completeCrudScenarioWithFinallyCleanup() {
        Integer bookingId = null;
        try {
            // TODO 1: создайте booking и извлеките id.
            // TODO 2: получите его GET-запросом и сравните ключевые поля.
            // TODO 3: измените additionalneeds через PATCH.
            // TODO 4: повторным GET подтвердите изменение.
            // Не используйте createTrackedBooking: cleanup этого задания должен быть в finally.
            assertThat(bookingId).isPositive();
        } finally {
            // TODO 5: если bookingId != null, удалите запись с токеном.
        }
    }

    private int createTrackedBooking(BookingRequest request) {
        int id = given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .extract()
                .path("bookingid");
        createdBookingIds.add(id);
        return id;
    }

    private Response getBooking(int id) {
        return given()
                .spec(BASE_SPEC)
                .pathParam("id", id)
                .when()
                .get("/booking/{id}");
    }

    private String getToken() {
        return given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(Map.of("username", "admin", "password", "password123"))
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
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

    private static void failUntilImplemented() {
        throw new AssertionError("Замените эту строку реализацией задания");
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
}
