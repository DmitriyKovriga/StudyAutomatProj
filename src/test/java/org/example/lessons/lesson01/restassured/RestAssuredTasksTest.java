package org.example.lessons.lesson01.restassured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Урок 1 / практический REST API testing: REST Assured + DTO + AssertJ + JUnit.
 *
 * API: https://restful-booker.herokuapp.com
 * Теория: docs/lessons/lesson-01/rest-assured.md
 *
 * Выполняйте задачи по порядку и удаляйте @Disabled только у текущей задачи.
 * Каждый тест создаёт собственные данные и не зависит от порядка запуска.
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

    // БЛОК 1. Два эталона: HTTP response и JSON-массив как Java DTO.

    @Test
    @Disabled
    void task01_healthCheckAsResponseAndAssertJ() {
        // TODO task01 (печатка: лист 1): Выполните GET /ping через BASE_SPEC и сохраните
        //              результат в response; готовые AssertJ-проверки оставьте без изменений.
        Response response = null;

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.contentType()).startsWith("text/plain");
        assertThat(response.asString()).isEqualTo("Created");
    }

    @Test
    @Disabled
    void task02_bookingListAsDtoList() {
        // TODO task02 (печатка: лист 2): Выполните GET /booking, сохраните response
        //              и десериализуйте JSON-массив в bookings через TypeRef.
        Response response = null;
        List<BookingIdResponse> bookings = null;

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).contains("application/json");
        assertThat(bookings)
                .isNotEmpty()
                .allSatisfy(booking -> assertThat(booking.bookingid()).isPositive());
    }

    // БЛОК 2. Ежедневная основа: request DTO, response DTO и read-back.

    @Test
    @Disabled
    void task03_createBookingFromDto() {
        BookingRequest expected = uniqueBooking("Create");

        // TODO task03 (печатка: лист 3): Отправьте expected в POST /booking, сохраните
        //              response, десериализуйте body в actual и добавьте ID в tracker.
        Response response = null;
        CreateBookingResponse actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).isNotNull();
        assertThat(actual.bookingid()).isPositive();
        assertThat(actual.booking()).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @Disabled
    void task04_createThenReadSavedBooking() {
        BookingRequest expected = uniqueBooking("ReadBack");
        int bookingId = createTrackedBooking(expected);

        // TODO task04 (печатка: лист 4): Выполните GET /booking/{id} через pathParam,
        //              сохраните response и десериализуйте body в actual BookingResponse.
        Response response = null;
        BookingResponse actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @Disabled
    void task05_filterBookingsAndDeserializeList() {
        BookingRequest expected = uniqueBooking("Filter");
        int createdId = createTrackedBooking(expected);

        // TODO task05 (печатка: лист 4): Передайте firstname и lastname через queryParam,
        //              выполните GET /booking и десериализуйте response в actual DTO-список.
        Response response = null;
        List<BookingIdResponse> actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).extracting(BookingIdResponse::bookingid).contains(createdId);
    }

    @Test
    @Disabled
    void task06_notFoundResponseWithFailureLogging() {
        int unknownId = Integer.MAX_VALUE;

        // TODO task06 (печатка: лист 5): Выполните GET /booking/{id}, включите request log
        //              при validation failure, сохраните response и провалидируйте 404 ниже.
        Response response = null;

        assertThat(response).isNotNull();
        response.then().log().ifValidationFails().statusCode(404);
        assertThat(response.asString()).containsIgnoringCase("not found");
    }

    // БЛОК 3. Общая HTTP-конфигурация и аутентификация.

    @Test
    @Disabled
    void task07_buildAndUseRequestSpecification() {
        // TODO task07 (печатка: лист 5): Создайте через RequestSpecBuilder specification
        //              с BASE_URL и Accept JSON, затем примените её к GET /booking.
        RequestSpecification specification = null;
        Response response = null;

        assertThat(specification).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.as(new TypeRef<List<BookingIdResponse>>() {
        })).isNotNull();
    }

    @Test
    @Disabled
    void task08_authenticateAndDeserializeToken() {
        AuthRequest credentials = new AuthRequest("admin", "password123");

        // TODO task08 (печатка: лист 6): Отправьте credentials в POST /auth,
        //              сохраните response и десериализуйте JSON в actual AuthResponse.
        Response response = null;
        AuthResponse actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).isNotNull();
        assertThat(actual.token()).isNotBlank();
    }

    @ParameterizedTest(name = "invalid auth: username={0}, password={1}")
    @CsvSource({
            "admin, wrong-password",
            "wrong-user, password123",
            "wrong-user, wrong-password"
    })
    @Disabled
    void task09_invalidAuthenticationAsDataTable(String username, String password) {
        // TODO task09 (печатка: лист 6): Отправьте AuthRequest с параметрами метода
        //              в POST /auth и десериализуйте body в actual AuthErrorResponse.
        Response response = null;
        AuthErrorResponse actual = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(actual).isNotNull();
        assertThat(actual.reason()).isEqualTo("Bad credentials");
    }

    // БЛОК 4. Изменения проверяются отдельным чтением сохранённого состояния.

    @Test
    @Disabled
    void task10_replaceBookingWithPutAndReadBack() {
        int bookingId = createTrackedBooking(uniqueBooking("BeforePut"));
        BookingRequest updated = uniqueBooking("AfterPut");

        // TODO task10 (печатка: лист 7): Выполните PUT /booking/{id} с token и updated,
        //              затем отдельным GET десериализуйте сохранённое состояние в saved.
        Response updateResponse = null;
        BookingResponse saved = null;

        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        assertThat(saved).usingRecursiveComparison().isEqualTo(updated);
    }

    @Test
    @Disabled
    void task11_patchOneFieldAndCheckPreservedFields() {
        BookingRequest original = uniqueBooking("BeforePatch");
        int bookingId = createTrackedBooking(original);
        String changedLastName = "Patched-" + UUID.randomUUID();
        Map<String, Object> patch = Map.of("lastname", changedLastName);

        // TODO task11 (печатка: лист 7): Выполните PATCH /booking/{id} с token и patch,
        //              затем отдельным GET десериализуйте сохранённое состояние в saved.
        Response patchResponse = null;
        BookingResponse saved = null;

        assertThat(patchResponse).isNotNull();
        assertThat(patchResponse.statusCode()).isEqualTo(200);
        assertThat(saved).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(saved.lastname()).as("changed lastname").isEqualTo(changedLastName);
            softly.assertThat(saved.firstname()).as("preserved firstname")
                    .isEqualTo(original.firstname());
            softly.assertThat(saved.totalprice()).as("preserved totalprice")
                    .isEqualTo(original.totalprice());
            softly.assertThat(saved.bookingdates()).as("preserved bookingdates")
                    .isEqualTo(original.bookingdates());
        });
    }

    @Test
    @Disabled
    void task12_rejectedUpdateDoesNotChangeState() {
        BookingRequest original = uniqueBooking("Protected");
        int bookingId = createTrackedBooking(original);
        BookingRequest forbiddenUpdate = uniqueBooking("MustNotBeSaved");

        // TODO task12 (печатка: лист 8): Выполните PUT /booking/{id} без token,
        //              сохраните deniedResponse, затем отдельным GET получите saved.
        Response deniedResponse = null;
        BookingResponse saved = null;

        assertThat(deniedResponse).isNotNull();
        assertThat(deniedResponse.statusCode()).isEqualTo(403);
        assertThat(saved).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @Disabled
    void task13_deleteBookingAndConfirmItIsGone() {
        int bookingId = createTrackedBooking(uniqueBooking("Delete"));

        // TODO task13 (печатка: лист 8): Выполните DELETE /booking/{id} с token,
        //              уберите ID из tracker и отдельным GET сохраните getAfterDelete.
        Response deleteResponse = null;
        Response getAfterDelete = null;

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.statusCode()).isEqualTo(201);
        assertThat(getAfterDelete).isNotNull();
        assertThat(getAfterDelete.statusCode()).isEqualTo(404);
    }

    // БЛОК 5. Устойчивый suite и итоговый рабочий сценарий.

    @Test
    @Disabled
    void task14_twoIndependentResourcesDoNotMix() {
        BookingRequest firstExpected = uniqueBooking("First");
        BookingRequest secondExpected = uniqueBooking("Second");

        // TODO task14 (печатка: лист 9): Создайте две записи, прочитайте обе как DTO
        //              и заполните переменные так, чтобы доказать независимость данных.
        int firstId = 0;
        int secondId = 0;
        BookingResponse firstActual = null;
        BookingResponse secondActual = null;

        assertThat(firstId).isPositive().isNotEqualTo(secondId);
        assertThat(secondId).isPositive();
        assertThat(firstActual).usingRecursiveComparison().isEqualTo(firstExpected);
        assertThat(secondActual).usingRecursiveComparison().isEqualTo(secondExpected);
    }

    @Test
    @Disabled
    void task15_finalIndependentCrudScenario() {
        BookingRequest initial = uniqueBooking("Final");
        Integer bookingId = null;
        try {
            // TODO task15 (печатка: листы 9–10): Выполните POST и сохраните ID в bookingId;
            //              затем GET, PATCH+GET, DELETE и финальный GET 404; cleanup уже в finally.
            failUntilImplemented();
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
        assertThat(response.statusCode()).as("fixture creation status").isEqualTo(200);
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
        assertThat(response.statusCode()).as("fixture authentication status").isEqualTo(200);
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
            System.err.println("Cleanup failed for booking " + bookingId + ": "
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

    private static void failUntilImplemented() {
        throw new AssertionError("Замените эту строку реализацией задания");
    }

    public record BookingIdResponse(int bookingid) {
    }

    public record AuthRequest(String username, String password) {
    }

    public record AuthResponse(String token) {
    }

    public record AuthErrorResponse(String reason) {
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
