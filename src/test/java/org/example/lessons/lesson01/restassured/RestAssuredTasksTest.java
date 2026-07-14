package org.example.lessons.lesson01.restassured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
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
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Урок 1 / REST API testing with REST Assured, JUnit and AssertJ.
 *
 * API: https://restful-booker.herokuapp.com
 * Теория: docs/lessons/lesson-01/rest-assured.md
 *
 * Выполняйте блоки по порядку. Удаляйте @Disabled только у текущего задания.
 * Сервис общий и периодически сбрасывает данные, поэтому тесты создают собственные записи.
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

    // БЛОК 1. Понимаем HTTP-контракт и читаем существующие данные.

    @Test
    @Disabled
    void task01_healthCheckAsCompleteHttpCheck() {
        // TODO: Выполните GET /ping через given/when/then и проверьте status 201,
        //       Content-Type text/plain и точное тело "Created".
        failUntilImplemented();
    }

    @Test
    @Disabled
    void task02_bookingListTransportAndShape() {
        // TODO: Выполните GET /booking и проверьте status 200, Content-Type JSON,
        //       непустой массив и положительный bookingid у каждого элемента.
        failUntilImplemented();
    }

    @Test
    @Disabled
    void task03_extractExistingIdAndReadBooking() {
        // TODO: Получите список /booking, извлеките первый bookingid, затем выполните
        //       GET /booking/{id} через pathParam и сохраните оба результата.
        int bookingId = 0;
        Response bookingResponse = null;

        assertThat(bookingId).isPositive();
        assertThat(bookingResponse).isNotNull();
        assertThat(bookingResponse.statusCode()).isEqualTo(200);
        assertThat(bookingResponse.contentType()).contains("application/json");
        assertThat(bookingResponse.jsonPath().getString("firstname")).isNotBlank();
        assertThat(bookingResponse.jsonPath().getString("bookingdates.checkin")).isNotBlank();
    }

    @Test
    @Disabled
    void task04_unknownBookingWithUsefulDiagnostics() {
        int unknownId = Integer.MAX_VALUE;

        // TODO: Выполните GET /booking/{id} для unknownId, включите логирование request и response
        //       только при провале проверки и сохраните ответ в response.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(404);
    }

    // БЛОК 2. Создаём свои данные и проверяем не только ответ, но и состояние сервера.

    @Test
    @Disabled
    void task05_createFromMapAndValidateResponse() {
        String firstName = "Map-" + UUID.randomUUID();
        Map<String, Object> body = Map.of(
                "firstname", firstName,
                "lastname", "Student",
                "totalprice", 250,
                "depositpaid", true,
                "bookingdates", Map.of("checkin", "2026-08-01", "checkout", "2026-08-05"),
                "additionalneeds", "Breakfast");

        // TODO: Выполните POST /booking с Content-Type JSON и body, проверьте transport-контракт,
        //       извлеките bookingid в createdId и зарегистрируйте его через track(createdId).
        Response response = null;
        int createdId = 0;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).contains("application/json");
        assertThat(createdId).isPositive();
        assertThat(response.jsonPath().getString("booking.firstname")).isEqualTo(firstName);
    }

    @Test
    @Disabled
    void task06_createFromDtoAndDeserializeWrapper() {
        BookingRequest expected = uniqueBooking("DtoCreate");

        // TODO: Отправьте expected в POST /booking, десериализуйте весь ответ
        //       в CreateBookingResponse actual и зарегистрируйте actual.bookingid() через track.
        CreateBookingResponse actual = null;

        assertThat(actual).isNotNull();
        assertThat(actual.bookingid()).isPositive();
        assertThat(actual.booking())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @Disabled
    void task07_createReadAndComparePersistedDto() {
        BookingRequest expected = uniqueBooking("Persisted");
        int bookingId = createTrackedBooking(expected);

        // TODO: Выполните GET /booking/{id}, проверьте status 200, десериализуйте body
        //       в BookingResponse actual и сравните его с expected через recursive comparison.
        BookingResponse actual = null;

        assertThat(actual).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @Disabled
    void task08_filterOnlyOwnBookingByName() {
        BookingRequest expected = uniqueBooking("Filter");
        int createdId = createTrackedBooking(expected);

        // TODO: Выполните GET /booking с queryParam firstname и lastname из expected,
        //       извлеките все bookingid в ids и проверьте, что среди них есть createdId.
        Response response = null;
        List<Integer> ids = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(ids).contains(createdId);
    }

    // БЛОК 3. Выбираем подходящий способ проверки body.

    @Test
    @Disabled
    void task09_inlineHamcrestForSmallBodyContract() {
        BookingRequest expected = uniqueBooking("Hamcrest");
        int bookingId = createTrackedBooking(expected);

        // TODO: Выполните GET /booking/{id} и одной then-цепочкой проверьте status 200,
        //       firstname, totalprice, bookingdates.checkin и непустой additionalneeds.
        failUntilImplemented();
    }

    @Test
    @Disabled
    void task10_jsonPathForValuesNeededByTest() {
        BookingRequest expected = uniqueBooking("JsonPath");
        int bookingId = createTrackedBooking(expected);

        // TODO: Выполните GET /booking/{id}, через JsonPath извлеките firstname,
        //       totalprice и bookingdates.checkout в подготовленные переменные.
        Response response = null;
        String firstName = null;
        int totalPrice = 0;
        String checkout = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(firstName).isEqualTo(expected.firstname());
        assertThat(totalPrice).isEqualTo(expected.totalprice());
        assertThat(checkout).isEqualTo(expected.bookingdates().checkout());
    }

    @Test
    @Disabled
    void task11_schemaPlusBusinessAssertions() {
        BookingRequest expected = uniqueBooking("Schema");
        int bookingId = createTrackedBooking(expected);

        // TODO: Выполните GET /booking/{id}, проверьте status 200, JSON Schema
        //       schemas/lesson01/booking-schema.json и отдельно firstname из expected.
        failUntilImplemented();
    }

    // БЛОК 4. Убираем техническое дублирование, не пряча смысл теста.

    @Test
    @Disabled
    void task12_buildReusableRequestSpecification() {
        // TODO: Через RequestSpecBuilder создайте specification с BASE_URL, Accept JSON
        //       и ErrorLoggingFilter, затем примените её к GET /booking.
        RequestSpecification specification = null;
        Response response = null;

        assertThat(specification).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("bookingid")).isNotNull();
    }

    @Test
    @Disabled
    void task13_buildReusableResponseSpecification() {
        // TODO: Через ResponseSpecBuilder создайте okJson, ожидающий status 200 и Content-Type JSON,
        //       примените его к GET /booking и дополнительно проверьте положительные bookingid.
        ResponseSpecification okJson = null;

        given()
                .spec(BASE_SPEC)
                .when()
                .get("/booking")
                .then()
                .spec(okJson)
                .body("bookingid", everyItem(greaterThan(0)));
    }

    // БЛОК 5. Аутентификация и проверка реального изменения состояния.

    @Test
    @Disabled
    void task14_createAndUseAuthenticationToken() {
        // TODO: Выполните POST /auth с AuthRequest("admin", "password123"), проверьте status 200
        //       и извлеките token в переменную token.
        String token = null;

        assertThat(token).isNotBlank();
    }

    @ParameterizedTest(name = "invalid auth: username={0}, password={1}")
    @CsvSource({
            "admin, wrong-password",
            "wrong-user, password123",
            "wrong-user, wrong-password"
    })
    @Disabled
    void task15_invalidAuthenticationAsJUnitDataTable(String username, String password) {
        // TODO: Отправьте username/password в POST /auth, сохраните Response и проверьте
        //       status 200 и reason == "Bad credentials".
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("reason")).isEqualTo("Bad credentials");
    }

    @Test
    @Disabled
    void task16_putMeansFullReplacementAndRequiresReadBack() {
        int bookingId = createTrackedBooking(uniqueBooking("BeforePut"));
        BookingRequest updated = uniqueBooking("AfterPut");

        // TODO: Получите token, выполните PUT /booking/{id} с updated, проверьте ответ,
        //       затем отдельным GET прочитайте saved и сравните весь DTO с updated.
        Response updateResponse = null;
        BookingResponse saved = null;

        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        assertThat(saved).isNotNull();
        assertThat(saved)
                .usingRecursiveComparison()
                .isEqualTo(updated);
    }

    @Test
    @Disabled
    void task17_patchChangesOneFieldAndPreservesTheRest() {
        BookingRequest original = uniqueBooking("BeforePatch");
        int bookingId = createTrackedBooking(original);
        String changedLastName = "Patched-" + UUID.randomUUID();

        // TODO: Выполните авторизованный PATCH /booking/{id} только с lastname,
        //       затем отдельным GET десериализуйте сохранённый BookingResponse в saved.
        Response patchResponse = null;
        BookingResponse saved = null;

        assertThat(patchResponse).isNotNull();
        assertThat(patchResponse.statusCode()).isEqualTo(200);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(saved).as("saved booking").isNotNull();
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
    void task18_updateWithoutAuthenticationIsRejected() {
        int bookingId = createTrackedBooking(uniqueBooking("Unauthorized"));
        BookingRequest update = uniqueBooking("MustNotBeSaved");

        // TODO: Выполните PUT /booking/{id} с update, но без token/basic auth,
        //       и сохраните ответ в response для проверки отказа 403.
        Response response = null;

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    @Disabled
    void task19_deleteAndConfirmResourceState() {
        int bookingId = createTrackedBooking(uniqueBooking("Delete"));

        // TODO: Выполните авторизованный DELETE /booking/{id}, проверьте status 201,
        //       удалите id из createdBookingIds и отдельным GET подтвердите status 404.
        Response deleteResponse = null;
        Response getAfterDelete = null;

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.statusCode()).isEqualTo(201);
        assertThat(getAfterDelete).isNotNull();
        assertThat(getAfterDelete.statusCode()).isEqualTo(404);
    }

    // БЛОК 6. Надёжность suite и финальный самостоятельный сценарий.

    @Test
    @Disabled
    void task20_twoTestsWorthOfDataMustNotMix() {
        BookingRequest firstExpected = uniqueBooking("First");
        BookingRequest secondExpected = uniqueBooking("Second");

        // TODO: Создайте и зарегистрируйте две записи, прочитайте обе через GET,
        //       сохраните id и DTO в подготовленные переменные и докажите их независимость.
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
    void task21_finalBossIndependentCrudScenario() {
        BookingRequest initial = uniqueBooking("FinalBoss");
        Integer bookingId = null;
        try {
            // TODO: Одним независимым сценарием выполните POST и извлеките id; GET и сравнение DTO
            //       через AssertJ; PATCH additionalneeds; повторный GET; авторизованный DELETE;
            //       финальный GET со status 404. Используйте BASE_SPEC, DTO и проверки transport/body/state.
            failUntilImplemented();
        } finally {
            if (bookingId != null) {
                deleteBookingIfPresent(bookingId);
            }
        }
    }

    private int createTrackedBooking(BookingRequest request) {
        int bookingId = given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .extract()
                .path("bookingid");
        track(bookingId);
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
        return given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(new AuthRequest("admin", "password123"))
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }

    private void track(int bookingId) {
        createdBookingIds.add(bookingId);
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

    public record AuthRequest(String username, String password) {
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
