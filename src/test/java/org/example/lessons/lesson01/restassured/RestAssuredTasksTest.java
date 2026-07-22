package org.example.lessons.lesson01.restassured;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Урок 1. Практическое тестирование REST API с помощью REST Assured.
 *
 * API: https://restful-booker.herokuapp.com
 * Теория: docs/lessons/lesson-01/rest-assured.md
 *
 * У этого класса нет служебного родителя и готовых DTO. Всё, что нужно для
 * работы с API — общую спецификацию запроса, DTO запросов и ответов, получение
 * token и очистку тестовых данных — вы создаёте сами по ходу заданий 1–3 и
 * затем переиспользуете в заданиях 4–10. Так вы на практике проходите путь
 * автотестировщика: от первого запроса к незнакомому API до собственной
 * тестовой инфраструктуры.
 *
 * DTO оформляются как обычные классы с Lombok в пакетах
 * {@code dto.request} и {@code dto.response} — так же, как в реальном
 * проекте, а не как record внутри тестового класса.
 *
 * Решайте задания по порядку. Удаляйте @Disabled только у текущего задания.
 * Не создавайте вторую версию DTO или метода, если что-то похожее вы уже
 * написали в более раннем задании, — переиспользуйте написанное.
 */
class RestAssuredTasksTest {

    @Test
    @Disabled
    void task01_createAuthDtosAndReceiveToken() {
        // Задание 1. У Restful Booker нет отдельного эндпоинта для проверки
        // логина — token выдаётся через POST /auth. Создайте класс
        // dto.request.AuthRequest (поля username, password) и класс
        // dto.response.AuthResponse (поле token). Оба — обычные классы с
        // аннотациями Lombok @Data @NoArgsConstructor @AllArgsConstructor:
        // @Data добавляет геттеры/сеттеры и equals/hashCode, @NoArgsConstructor
        // нужен Jackson для десериализации ответа, а @AllArgsConstructor даёт
        // короткий конструктор для тела запроса.
        //
        // Соберите запрос самостоятельно:
        //
        // Преобразуйте ответ в AuthResponse и через AssertJ проверьте статус
        // 200, что Content-Type ответа содержит "application/json" и что
        // token не пустой.
        //
        // AuthRequest и AuthResponse понадобятся в задании 3, когда вы
        // напишете метод getToken() для защищённых запросов, — не удаляйте их.
    }

    @Test
    @Disabled
    void task02_buildBaseSpecAndListItemDto() {
        // Задание 2. В задании 1 вы указали baseUri один раз, а сейчас нужно
        // повторить его снова. Вместо повторения объявите общую спецификацию:
        //
        // private static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
        //         .setBaseUri("https://restful-booker.herokuapp.com")
        //         .addHeader("Accept", "application/json")
        //         .build();
        //
        // Заголовок Accept обязательно задайте строкой "application/json", а
        // не через .setAccept(ContentType.JSON): REST Assured может развернуть
        // ContentType.JSON в список MIME-типов, и Restful Booker в ответ на
        // это иногда отвечает статусом 418 вместо ожидаемого результата.
        //
        // Дальше создайте класс dto.response.BookingIdResponse (поле
        // bookingid, тот же набор аннотаций Lombok, что у AuthResponse) — так
        // выглядит один элемент ответа GET /booking: [{"bookingid": 1}, ...].
        // Отправьте GET /booking через .spec(BASE_SPEC), преобразуйте JSON-
        // массив в List<BookingIdResponse> с помощью TypeRef и проверьте
        // статус 200, что список не пуст и что bookingid каждого элемента
        // положителен через getBookingid() (AssertJ allSatisfy).
    }

    @Test
    @Disabled
    void task03_createBookingDtosWithCleanup() {
        // Задание 3. Здесь вы один раз строите DTO и инфраструктуру для всей
        // работы с booking, которые затем переиспользуете в заданиях 4–10.
        //
        // 1) По JSON-контракту POST /booking из теории создайте классы (тот же
        //    Lombok-набор @Data @NoArgsConstructor @AllArgsConstructor):
        //    dto.request.BookingDates(checkin, checkout);
        //    dto.request.BookingRequest(firstname, lastname, totalprice,
        //            depositpaid, bookingdates, additionalneeds);
        //    dto.response.BookingResponse — та же форма, что BookingRequest,
        //    но для ответа (bookingdates может ссылаться на dto.request.BookingDates —
        //    структура одна и та же для запроса и ответа);
        //    dto.response.CreateBookingResponse(bookingid, BookingResponse booking) —
        //    обёртка ответа POST /booking.
        //
        // 2) Добавьте в класс поле для очистки:
        //    private final Set<Integer> createdBookingIds = new HashSet<>();
        //
        // 3) Добавьте приватный метод получения token, который переиспользует
        //    DTO из задания 1:
        //    private static String getToken() { ... POST /auth ... return response.as(AuthResponse.class).getToken(); }
        //
        // 4) Добавьте метод с аннотацией @AfterEach, который (если
        //    createdBookingIds не пуст) получает token через getToken() и
        //    отправляет DELETE /booking/{id} с cookie "token" для каждого
        //    оставшегося id, затем очищает набор. Так каждый тест удаляет за
        //    собой данные, даже если сам не дошёл до DELETE.
        //
        // 5) В самом тесте создайте уникальный BookingRequest, например с
        //    firstname = "Create-" + UUID.randomUUID(), отправьте POST /booking
        //    через BASE_SPEC, преобразуйте ответ в CreateBookingResponse,
        //    добавьте bookingid в createdBookingIds и сравните вложенный
        //    booking с отправленным BookingRequest.
    }

    @Test
    @Disabled
    void task04_readCreatedBookingById() {
        // Задание 4. Создайте и зарегистрируйте (как в задании 3) уникальный
        // booking. Затем самостоятельно соберите GET /booking/{id}, передав
        // id через pathParam. Преобразуйте ответ в BookingResponse и через
        // usingRecursiveComparison() сравните его с отправленным BookingRequest.
    }

    @Test
    @Disabled
    void task05_filterBookingsByGuestName() {
        // Задание 5. Создайте и зарегистрируйте уникальный booking, как в
        // задании 4. Отправьте GET /booking с двумя queryParam — firstname и
        // lastname вашего booking. Преобразуйте JSON-массив в
        // List<BookingIdResponse> (тип из задания 2) и через AssertJ
        // extracting проверьте, что среди полученных id есть id вашего booking.
    }

    @Test
    @Disabled
    void task06_replaceBookingWithPut() {
        // Задание 6. Создайте и зарегистрируйте booking. Получите token через
        // getToken() (задание 3) и отправьте PUT /booking/{id}: id — через
        // pathParam, token — через cookie "token", новый BookingRequest — в
        // JSON-теле. Преобразуйте ответ PUT в BookingResponse и проверьте его.
        // Затем отдельным GET (как в задании 4) убедитесь, что сервер
        // действительно сохранил новые данные, а не просто ответил статусом 200.
    }

    @Test
    @Disabled
    void task07_changeOneFieldWithPatch() {
        // Задание 7. Создайте и зарегистрируйте booking. Соберите
        // Map<String, Object> changes = Map.of("lastname", "Changed-" + UUID.randomUUID());
        // Отправьте PATCH /booking/{id} с pathParam, token в cookie и changes
        // в JSON-теле. Проверьте статус 200. Затем выполните GET и докажите,
        // что lastname изменился, а firstname, totalprice и bookingdates
        // остались такими же, как в исходном booking.
    }

    @Test
    @Disabled
    void task08_updateWithoutTokenIsRejected() {
        // Задание 8. Создайте и зарегистрируйте booking. Отправьте PUT
        // /booking/{id} с новым BookingRequest, но намеренно не передавайте
        // cookie "token". Проверьте статус 403. Затем выполните обычный GET и
        // через рекурсивное сравнение докажите, что сохранённые данные
        // остались исходным booking, а не тем, что вы пытались записать.
    }

    @Test
    @Disabled
    void task09_deleteBookingAndCheckThatItIsGone() {
        // Задание 9. Создайте и зарегистрируйте booking. Получите token и
        // отправьте DELETE /booking/{id} с pathParam и cookie "token".
        // Проверьте статус 201 и уберите id из createdBookingIds — запись уже
        // удалена вами, повторный DELETE в @AfterEach не нужен. Затем
        // выполните GET по тому же id и проверьте статус 404.
    }

    @Test
    @Disabled
    void task10_completeBookingLifecycle() {
        // Задание 10. Самостоятельно постройте сценарий из уже знакомых
        // частей: создайте booking и зарегистрируйте id → проверьте его
        // GET-ом → обновите через PUT с token → проверьте обновление GET-ом →
        // удалите через DELETE с token и уберите id из createdBookingIds →
        // финальным GET проверьте статус 404. Используйте только написанные
        // вами ранее DTO и методы getToken() / BASE_SPEC — не создавайте их
        // копий с другими именами.
    }
}
