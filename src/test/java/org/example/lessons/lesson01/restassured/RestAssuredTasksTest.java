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
    @Disabled
    void task01_healthCheck() {
        // TODO: GET /ping, проверьте status 201 и body "Created".
        failUntilImplemented();
    }

    @Test
    @Disabled
    void task02_getBookingIds() {
        // TODO: Выполните GET /booking и проверьте status 200, непустой JSON-массив
        //       и положительный bookingid у каждого элемента.
        failUntilImplemented();
    }

    @Test
    @Disabled
    void task03_filterBookingsByFirstName() {
        BookingRequest booking = uniqueBooking("Filter");
        int createdId = createTrackedBooking(booking);

        // TODO: Выполните GET /booking с queryParam firstname, равным booking.firstname(),
        //       и сохраните результат в response для подготовленных проверок.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertThat(ids).contains(createdId);
    }

    @Test
    @Disabled
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

        // TODO: Выполните POST /booking с Content-Type JSON и подготовленным body, сохраните Response,
        //       затем добавьте созданный bookingid в createdBookingIds для cleanup.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("bookingid")).isPositive();
        assertThat(response.jsonPath().getString("booking.firstname")).isEqualTo(firstName);

    }

    @Test
    @Disabled
    void task05_extractIdAndGetCreatedBooking() {
        BookingRequest expected = uniqueBooking("Extract");

        // TODO: Самостоятельно создайте booking, извлеките bookingid, зарегистрируйте его
        //       в createdBookingIds, выполните GET /booking/{id} и сохраните результаты
        //       в подготовленные переменные.
        int bookingId = 0;
        Response getResponse = null;

        assertThat(bookingId).isPositive();
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.jsonPath().getString("firstname")).isEqualTo(expected.firstname());
    }

    @Test
    @Disabled
    void task06_deserializeResponseToDto() {
        BookingRequest expected = uniqueBooking("Dto");
        int bookingId = createTrackedBooking(expected);

        // TODO: Выполните GET /booking/{id}, десериализуйте ответ через
        //       response.as(BookingResponse.class) и сохраните DTO в actual
        //       для подготовленных проверок.
        BookingResponse actual = null;

        assertThat(actual).isNotNull();
        assertThat(actual.firstname()).isEqualTo(expected.firstname());
        assertThat(actual.bookingdates()).isEqualTo(expected.bookingdates());
        assertThat(actual.totalprice()).isEqualTo(expected.totalprice());
    }

    @Test
    @Disabled
    void task07_requestSpecification() {
        // TODO: Через RequestSpecBuilder создайте specification с baseUri, Accept JSON
        //       и логированием запроса при ошибке, чтобы подготовленный GET /booking
        //       прошёл проверки.
        RequestSpecification specification = null;

        Response response = given()
                .spec(specification)
                .when()
                .get("/booking");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("bookingid")).isNotNull();
    }

    @Test
    @Disabled
    void task08_responseSpecification() {
        // TODO: Через ResponseSpecBuilder создайте okJson, который ожидает status 200
        //       и Content-Type JSON, и используйте его в подготовленном запросе.
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
    @Disabled
    void task09_authentication() {
        // TODO: Выполните POST /auth с admin/password123 и извлеките token, затем повторите запрос
        //       с неверным password и сохраните Response в invalidAuth для подготовленных проверок.
        String token = null;

        assertThat(token).isNotBlank();

        Response invalidAuth = null;

        assertThat(invalidAuth).isNotNull();
        assertThat(invalidAuth.statusCode()).isEqualTo(200);
        assertThat(invalidAuth.jsonPath().getString("reason")).isEqualTo("Bad credentials");
    }

    @Test
    @Disabled
    void task10_fullUpdateWithPut() {
        int bookingId = createTrackedBooking(uniqueBooking("BeforePut"));
        BookingRequest updated = uniqueBooking("AfterPut");

        // TODO: Выполните PUT /booking/{id} с cookie token и телом updated, сохранив результат
        //       в updateResponse для проверки ответа и сохранённого состояния.
        Response updateResponse = null;

        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.statusCode()).isEqualTo(200);

        BookingResponse saved = getBooking(bookingId).as(BookingResponse.class);
        assertThat(saved.firstname()).isEqualTo(updated.firstname());
        assertThat(saved.lastname()).isEqualTo(updated.lastname());
        assertThat(saved.totalprice()).isEqualTo(updated.totalprice());
    }

    @Test
    @Disabled
    void task11_partialUpdateKeepsOtherFields() {
        BookingRequest original = uniqueBooking("BeforePatch");
        int bookingId = createTrackedBooking(original);
        String changedLastName = "Patched-" + UUID.randomUUID();

        // TODO: Выполните PATCH /booking/{id} с cookie token и body
        //       Map.of("lastname", changedLastName), сохранив результат в patchResponse
        //       для проверки изменённого и неизменившихся полей.
        Response patchResponse = null;

        assertThat(patchResponse).isNotNull();
        assertThat(patchResponse.statusCode()).isEqualTo(200);

        BookingResponse saved = getBooking(bookingId).as(BookingResponse.class);
        assertThat(saved.lastname()).isEqualTo(changedLastName);
        assertThat(saved.firstname()).isEqualTo(original.firstname());
        assertThat(saved.totalprice()).isEqualTo(original.totalprice());
    }

    @Test
    @Disabled
    void task12_deleteAndVerify() {
        int bookingId = createTrackedBooking(uniqueBooking("Delete"));

        // TODO: Выполните DELETE /booking/{id} с cookie token, сохраните ответ в deleteResponse,
        //       проверьте status 201 и последующий GET со status 404.
        Response deleteResponse = null;

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.statusCode()).isEqualTo(201);
        assertThat(getBooking(bookingId).statusCode()).isEqualTo(404);

        createdBookingIds.remove(bookingId);
    }

    @Test
    @Disabled
    void task13_unknownBookingReturns404() {
        int unknownId = Integer.MAX_VALUE;

        // TODO: Выполните GET /booking/{id} для unknownId, добавьте log().ifValidationFails()
        //       для request и response и сохраните результат в response для проверки status 404.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    @Disabled
    void task14_jsonSchemaValidation() {
        int bookingId = createTrackedBooking(uniqueBooking("Schema"));

        // TODO: Выполните GET /booking/{id}, проверьте status 200 и соответствие схеме через
        //       matchesJsonSchemaInClasspath("schemas/lesson01/booking-schema.json").
        failUntilImplemented();
    }

    @Test
    @Disabled
    void task15_completeCrudScenarioWithFinallyCleanup() {
        Integer bookingId = null;
        try {
            // TODO: Не используя createTrackedBooking, создайте booking и извлеките id,
            //       получите запись через GET и сравните ключевые поля, измените additionalneeds
            //       через PATCH, подтвердите изменение повторным GET, а в finally
            //       при ненулевом bookingId удалите запись с токеном.
            assertThat(bookingId).isPositive();
        } finally {
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
