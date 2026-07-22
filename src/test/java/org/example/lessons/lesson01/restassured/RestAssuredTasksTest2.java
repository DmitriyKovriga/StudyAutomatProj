package org.example.lessons.lesson01.restassured;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Урок 1, часть 2. Гибкие DTO и динамический JSON в API-тестах.
 *
 * Теория: docs/lessons/lesson-01/rest-assured.md
 * Решайте задания по порядку: некоторые следующие модели используют предыдущие.
 */
class RestAssuredTasksTest2 extends RestAssuredDtoFlexibilitySupport {

    @Test
    @Disabled
    void task01_emptyNullAndMissingFieldInOneRequestDto() {
        // Задание 1. Объявите record FlexibleBookingRequest(firstname, lastname,
        // additionalneeds) и создайте один корректный объект. Через valueToTree
        // получите три независимых ObjectNode: с additionalneeds="", с явным null
        // и без поля additionalneeds. AssertJ должен доказать различие трёх JSON.
    }

    @Test
    @Disabled
    void task02_optionalObjectAndPresenceOfField() {
        // Задание 2. Объявите StudentProfile(city) и StudentUserResponse(id, name,
        // profile). Преобразуйте ответы USER_WITH_NULL_PROFILE_JSON и
        // USER_WITHOUT_PROFILE_JSON в DTO: в обоих profile будет null. Затем
        // прочитайте их как JsonNode и отличите явный null от отсутствующего поля.
    }

    @Test
    @Disabled
    void task03_ignoreNewUnknownResponseField() {
        // Задание 3. Добавьте к StudentUserResponse настройку Jackson, позволяющую
        // игнорировать незнакомые поля. Преобразуйте USER_WITH_EXTRA_FIELD_JSON в
        // тот же DTO и проверьте id, name и profile.city; traceId в DTO не добавляйте.
    }

    @Test
    @Disabled
    void task04_genericEnvelopeWithDifferentDataTypes() {
        // Задание 4. Объявите ApiError(code, message), BookingSummary(bookingid,
        // firstname) и generic-record ApiEnvelope<T>(status, data, error).
        // Преобразуйте два BOOKING_*_ENVELOPE_JSON через TypeRef: в первом data —
        // BookingSummary, во втором List<BookingSummary>. Проверьте обе структуры.
    }

    @Test
    @Disabled
    void task05_polymorphicResponseListWithDiscriminator() {
        // Задание 5. Создайте интерфейс ApiEvent с @JsonTypeInfo по полю type и
        // @JsonSubTypes для CreatedEvent, PriceChangedEvent и DeletedEvent.
        // Преобразуйте EVENTS_JSON в List<ApiEvent> и проверьте тип и важные поля
        // каждого элемента. Сопоставления type описаны в теории.
    }

    @Test
    @Disabled
    void task06_polymorphicRequestList() throws Exception {
        // Задание 6. Создайте ApiCommand с типами CreateBookingCommand и
        // CancelBookingCommand, используя discriminator type. Соберите List из
        // двух разных команд и сериализуйте его writerFor(TypeReference), чтобы
        // JSON каждого элемента содержал type и собственные поля команды.
    }

    @Test
    @Disabled
    void task07_heterogeneousArrayWithoutDiscriminator() throws Exception {
        // Задание 7. В MIXED_ITEMS_JSON нет общего DTO и discriminator. Прочитайте
        // массив как JsonNode, пройдите по элементам и через isObject, isTextual,
        // isNull и isInt докажите наличие booking, heartbeat, warning, null и 42.
        // Не создавайте общий DTO с набором всех возможных полей.
    }

    @Test
    @Disabled
    void task08_combinedTypedAndDynamicRequest() throws Exception {
        // Задание 8. Объявите BatchRequest(requestId, List<ApiCommand>, metadata),
        // где metadata имеет тип Map<String, JsonNode>. Создайте batch с двумя
        // разными командами из задания 6 и динамическими metadata. Сериализуйте,
        // проверьте type команд, затем в копии ObjectNode сделайте одно metadata
        // явным null, другое удалите и докажите различие через has и isNull.
    }
}
