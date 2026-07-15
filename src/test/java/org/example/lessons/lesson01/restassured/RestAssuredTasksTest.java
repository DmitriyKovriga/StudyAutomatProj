package org.example.lessons.lesson01.restassured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.ErrorLoggingFilter;
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
            .addFilter(new ErrorLoggingFilter())
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
        // TODO task01: Выполните GET /ping, сохраните результат в response и оставьте
        //              проверки status, content type и текстового body через AssertJ.
        Response response = null;

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.contentType()).startsWith("text/plain");
        assertThat(response.asString()).isEqualTo("Created");
    }

    @Test
    @Disabled
    void task02_bookingListAsDtoList() {
        // TODO task02: Выполните GET /booking, сохраните response и десериализуйте
        //              JSON-массив в bookings через TypeRef<List<BookingIdResponse>>.
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

        // TODO task03: Отправьте expected в POST /booking, сохраните Response в response,
        //              проверьте status 200, десериализуйте body в actual и зарегистрируйте ID.
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

        // TODO task04: Выполните GET /booking/{id} через pathParam, сохраните response,
        //              проверьте status 200 и десериализуйте body в actual BookingResponse.
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

        // TODO task05: В GET /booking передайте firstname и lastname через queryParam,
        //              проверьте status 200 и десериализуйте массив в actual DTO-список.
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

        // TODO task06: Выполните GET /booking/{id}, включив request/response logging только
        //              при провале validation, и сохраните ответ в response.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.asString()).containsIgnoringCase("not found");
    }

    // БЛОК 3. Общая HTTP-конфигурация и аутентификация.

    @Test
    @Disabled
    void task07_buildAndUseRequestSpecification() {
        // TODO task07: Создайте через RequestSpecBuilder specification с BASE_URL,
        //              Accept JSON и ErrorLoggingFilter, затем примените её к GET /booking.
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

        // TODO task08: Отправьте credentials в POST /auth, сохраните response,
        //              проверьте status 200 и десериализуйте JSON в actual AuthResponse.
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
        // TODO task09: Отправьте AuthRequest с параметрами метода в POST /auth,
        //              проверьте status 200 и десериализуйте body в actual AuthErrorResponse.
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

        // TODO task10: Выполните авторизованный PUT /booking/{id} с updated, проверьте
        //              status 200, затем отдельным GET десериализуйте состояние в saved.
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

        // TODO task11: Выполните авторизованный PATCH /booking/{id} с patch, проверьте
        //              status 200, затем отдельным GET десериализуйте состояние в saved.
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

        // TODO task12: Выполните PUT /booking/{id} без token, сохраните deniedResponse,
        //              затем отдельным GET десериализуйте текущее состояние в saved.
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

        // TODO task13: Выполните авторизованный DELETE /booking/{id}, проверьте status 201,
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

        // TODO task14: Создайте две записи, прочитайте обе как BookingResponse и заполните
        //              подготовленные переменные так, чтобы доказать независимость данных.
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
            // TODO task15: Одним сценарием выполните POST и получите DTO с ID; GET и сравнение
            //              DTO; PATCH additionalneeds; GET changed/preserved; DELETE; GET 404.
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
