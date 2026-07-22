package org.example.lessons.lesson01.restassured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/** Служебная инфраструктура урока. В заданиях изменять её не нужно. */
abstract class RestAssuredLessonSupport {

    protected static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
            .setBaseUri("https://restful-booker.herokuapp.com")
            .setAccept("application/json")
            .build();

    protected final Set<Integer> createdBookingIds = new LinkedHashSet<>();

    @AfterEach
    void removeCreatedBookings() {
        createdBookingIds.forEach(this::deleteBookingIfPresent);
        createdBookingIds.clear();
    }

    protected int createTrackedBooking(BookingRequest request) {
        Response response = given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/booking");

        assertThat(response.statusCode())
                .as("Статус подготовки тестовых данных")
                .isEqualTo(200);

        int bookingId = response.as(CreateBookingResponse.class).bookingid();
        createdBookingIds.add(bookingId);
        return bookingId;
    }

    protected String getToken() {
        Response response = given()
                .spec(BASE_SPEC)
                .contentType(ContentType.JSON)
                .body(new AuthRequest("admin", "password123"))
                .when()
                .post("/auth");

        assertThat(response.statusCode())
                .as("Статус подготовки авторизации")
                .isEqualTo(200);

        return response.as(AuthResponse.class).token();
    }

    protected BookingRequest uniqueBooking(String prefix) {
        return new BookingRequest(
                prefix + "-" + UUID.randomUUID(),
                "Student",
                150,
                true,
                new BookingDates("2026-08-01", "2026-08-05"),
                "Breakfast");
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

    protected record BookingIdResponse(int bookingid) {
    }

    protected record AuthRequest(String username, String password) {
    }

    protected record AuthResponse(String token) {
    }

    protected record BookingDates(String checkin, String checkout) {
    }

    protected record BookingRequest(
            String firstname,
            String lastname,
            int totalprice,
            boolean depositpaid,
            BookingDates bookingdates,
            String additionalneeds) {
    }

    protected record BookingResponse(
            String firstname,
            String lastname,
            int totalprice,
            boolean depositpaid,
            BookingDates bookingdates,
            String additionalneeds) {
    }

    protected record CreateBookingResponse(int bookingid, BookingResponse booking) {
    }
}
