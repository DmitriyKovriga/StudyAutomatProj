package org.example.lessons.lesson01.restassured;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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
class RestAssuredTasksTest extends RestAssuredLessonSupport {

    @Test
    @Disabled
    void task01_createAuthDtosAndReceiveToken() {
        // Задание 1. По контракту POST /auth самостоятельно объявите внутри этого
        // класса два record: StudentAuthRequest(username, password) для запроса и
        // StudentAuthResponse(token) для ответа. Отправьте admin/password123 через
        // BASE_SPEC, преобразуйте ответ в свой DTO и проверьте статус, формат и token.
    }

    @Test
    @Disabled
    void task02_createListItemDtoAndReadIds() {
        // Задание 2. Ответ GET /booking имеет вид [{"bookingid": 1}, ...].
        // Самостоятельно объявите в этом классе record StudentBookingIdResponse
        // с подходящим компонентом. Получите список через TypeRef и проверьте
        // статус, JSON-формат, непустой список и положительный ID каждого элемента.
    }

    @Test
    @Disabled
    void task03_createNestedDtosAndBooking() {
        // Задание 3. По JSON из раздела теории самостоятельно объявите records
        // StudentBookingDates, StudentBookingRequest, StudentBookingResponse и
        // StudentCreateBookingResponse. Создайте уникальный request, отправьте
        // POST /booking, преобразуйте обёртку ответа, зарегистрируйте bookingid
        // для очистки и сравните вложенный booking с отправленными данными.
    }

    @Test
    @Disabled
    void task04_readCreatedBookingById() {
        BookingRequest expected = uniqueBooking("Read");
        int bookingId = createTrackedBooking(expected);

        // Задание 4. Самостоятельно соберите GET /booking/{id}, передав bookingId
        // через pathParam. Сохраните Response, преобразуйте его в BookingResponse
        // и проверьте статус 200 и полное совпадение ответа с expected.
    }

    @Test
    @Disabled
    void task05_filterBookingsByGuestName() {
        BookingRequest expected = uniqueBooking("Filter");
        int createdId = createTrackedBooking(expected);

        // Задание 5. Отправьте GET /booking с queryParam для firstname и lastname
        // из expected. Преобразуйте JSON-массив в List<BookingIdResponse> и
        // проверьте статус 200, а также наличие createdId среди полученных ID.
    }

    @Test
    @Disabled
    void task06_replaceBookingWithPut() {
        int bookingId = createTrackedBooking(uniqueBooking("BeforePut"));
        BookingRequest expected = uniqueBooking("AfterPut");

        // Задание 6. Получите token и отправьте PUT /booking/{id}: ID передайте
        // через pathParam, token через cookie, expected в JSON-теле. Проверьте
        // статус и тело PUT. Затем отдельным GET прочитайте запись и докажите,
        // что в системе действительно сохранён expected.
    }

    @Test
    @Disabled
    void task07_changeOneFieldWithPatch() {
        BookingRequest original = uniqueBooking("BeforePatch");
        int bookingId = createTrackedBooking(original);
        String newLastName = "Changed-" + UUID.randomUUID();

        // Задание 7. Создайте Map с новым lastname и отправьте его в JSON-теле
        // PATCH /booking/{id} с pathParam и token в cookie. Проверьте статус 200.
        // Затем выполните GET: lastname должен измениться, а firstname, totalprice
        // и bookingdates должны остаться такими же, как в original.
    }

    @Test
    @Disabled
    void task08_updateWithoutTokenIsRejected() {
        BookingRequest original = uniqueBooking("Protected");
        int bookingId = createTrackedBooking(original);
        BookingRequest forbiddenUpdate = uniqueBooking("MustNotBeSaved");

        // Задание 8. Отправьте PUT /booking/{id} с forbiddenUpdate, но намеренно
        // не передавайте token. Проверьте статус 403. Затем выполните обычный GET
        // и через рекурсивное сравнение докажите, что запись осталась original.
    }

    @Test
    @Disabled
    void task09_deleteBookingAndCheckThatItIsGone() {
        int bookingId = createTrackedBooking(uniqueBooking("Delete"));

        // Задание 9. Получите token и отправьте DELETE /booking/{id} с pathParam
        // и cookie. Проверьте статус 201 и уберите bookingId из списка очистки.
        // Затем выполните GET по тому же ID и проверьте статус 404.
    }

    @Test
    @Disabled
    void task10_completeBookingLifecycle() {
        BookingRequest initial = uniqueBooking("FinalCreate");
        BookingRequest updated = uniqueBooking("FinalUpdate");

        // Задание 10. Самостоятельно напишите полный сценарий POST → GET → PUT →
        // GET → DELETE → GET. Проверяйте статус и данные после каждого шага.
        // Сразу после POST добавьте ID в createdBookingIds, а после успешного
        // DELETE удалите его оттуда. Используйте DTO, token и AssertJ.
    }
}
