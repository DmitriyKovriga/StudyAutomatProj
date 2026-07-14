# REST API testing: цельный рабочий маршрут с REST Assured

Практика использует [Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html) — общий учебный CRUD API. Его [официальная Postman-документация](https://www.postman.com/automation-in-testing/restful-booker-collections/documentation/55eh7vh/restful-booker) предупреждает, что данные общие и периодически сбрасываются. Поэтому надёжный тест не берёт случайный готовый `bookingid`, а создаёт собственную запись и очищает её.

Цель урока — не запомнить максимум методов REST Assured. После практики вы должны уметь самостоятельно:

1. прочитать контракт endpoint;
2. подготовить контролируемые тестовые данные;
3. отправить запрос;
4. проверить HTTP-контракт и бизнес-данные;
5. подтвердить сохранённое состояние отдельным запросом;
6. диагностировать падение;
7. оставить окружение чистым.

## 1. Главная модель API-теста

Весь курс собирается вокруг одного цикла:

```text
КОНТРАКТ
   ↓
СОБСТВЕННЫЕ ДАННЫЕ
   ↓
REQUEST → RESPONSE
   ↓
TRANSPORT → STRUCTURE → BUSINESS DATA → SAVED STATE
   ↓
CLEANUP
   ↓
ДИАГНОСТИКА ПРИ ПАДЕНИИ
```

Если этот цикл понятен, конкретный инструмент можно заменить: REST Assured, curl, Postman или другой HTTP-клиент выполняют ту же работу.

### Приоритет P0 — без этого нельзя считать тест рабочим

- понимать method, path, параметры, headers и body;
- проверять ожидаемый status, а не просто «любой 2xx»;
- проверять ключевые бизнес-значения;
- после POST/PUT/PATCH подтверждать состояние отдельным GET;
- создавать собственные данные;
- выполнять cleanup даже после падения;
- иметь request/response для диагностики.

### Приоритет P1 — ежедневная работа

- DTO и сериализация Jackson;
- извлечение id и связанных значений;
- `RequestSpecification` и `ResponseSpecification`;
- JsonPath для точечных значений;
- AssertJ для связанных полей DTO;
- JUnit parameterized tests для таблиц однотипных случаев;
- JSON Schema как дополнительный структурный слой.

### Приоритет P2 — изучать после устойчивой базы

- собственные filters и extensions;
- сложные GPath-выражения;
- кастомные assertion-классы;
- параллельное выполнение и продвинутая конфигурация HTTP-клиента;
- интеграция секретов, retries и отчётности в CI.

P2 не делает слабый тест хорошим. Сначала P0.

## 2. Сначала прочитайте контракт endpoint

Перед кодом ответьте письменно на семь вопросов:

| Вопрос | Пример для создания booking |
|---|---|
| HTTP method? | `POST` |
| Path? | `/booking` |
| Path/query params? | нет |
| Нужна авторизация? | нет |
| Request headers/body? | `Content-Type: application/json`, booking JSON |
| Ожидаемый response? | status `200`, JSON с `bookingid` и `booking` |
| Как проверить эффект? | `GET /booking/{bookingid}` |

Основные endpoints Restful Booker:

| Method | Path | Назначение | Auth |
|---|---|---|---|
| GET | `/ping` | health check, status 201 | нет |
| GET | `/booking` | список id и фильтрация | нет |
| GET | `/booking/{id}` | получить booking | нет |
| POST | `/booking` | создать booking | нет |
| POST | `/auth` | получить token | credentials в body |
| PUT | `/booking/{id}` | полная замена | token/basic auth |
| PATCH | `/booking/{id}` | частичное изменение | token/basic auth |
| DELETE | `/booking/{id}` | удалить | token/basic auth |

## 3. Request: из чего реально состоит запрос

```java
given()
    .baseUri("https://restful-booker.herokuapp.com")
    .accept(ContentType.JSON)
    .contentType(ContentType.JSON)
    .pathParam("id", bookingId)
    .queryParam("firstname", "Ivan")
    .cookie("token", token)
    .body(requestBody)
.when()
    .put("/booking/{id}");
```

Читайте сверху вниз:

- `baseUri` — сервер;
- `accept` — какой response умеет принять клиент;
- `contentType` — формат отправляемого body;
- `pathParam` — часть пути;
- `queryParam` — фильтр/настройка запроса;
- `cookie`/`auth` — полномочия;
- `body` — данные команды;
- `put` — действие и endpoint.

Не собирайте URL строковой конкатенацией:

```java
// хуже
.get("/booking/" + bookingId)

// лучше
.pathParam("id", bookingId)
.get("/booking/{id}")
```

Во втором варианте структура endpoint видна, а кодирование параметра выполняет библиотека.

## 4. given → when → then — это не магия

```java
given()  // подготовить request
.when()  // выполнить HTTP action
.then(); // проверить response
```

Минимальный health check:

```java
given()
    .baseUri(BASE_URL)
.when()
    .get("/ping")
.then()
    .statusCode(201)
    .contentType(ContentType.TEXT)
    .body(equalTo("Created"));
```

Это уже полный маленький тест, потому что он проверяет:

1. правильный endpoint отвечает;
2. status соответствует контракту;
3. representation имеет ожидаемый тип;
4. body имеет ожидаемое значение.

## 5. Response проверяется слоями, а не случайным набором assertions

Используйте лестницу проверок.

### Слой 1. Transport

```java
.statusCode(200)
.contentType(ContentType.JSON)
```

Проверяйте конкретный status из контракта. `200`, `201`, `204` и `404` означают разное.

### Слой 2. Structure

```java
.body("bookingid", greaterThan(0))
.body("booking.firstname", notNullValue())
```

Или JSON Schema. Schema отвечает на вопрос «форма и типы допустимы?», но не знает, правильная ли фамилия сохранена.

### Слой 3. Business data

```java
.body("booking.firstname", equalTo(expected.firstname()))
.body("booking.totalprice", equalTo(expected.totalprice()))
```

### Слой 4. Saved state

Response от PUT/PATCH может выглядеть правильно, даже если данные не сохранились. Поэтому mutation подтверждается отдельным GET.

### Слой 5. Unchanged state

После PATCH проверьте не только изменённое поле, но и важные поля, которые обязаны сохраниться.

## 6. Собственные данные важнее красивого assertion

Публичный сервис общий. Тест, который обновляет первый id из `/booking`, может изменить чужую запись или упасть после сброса базы.

Надёжная схема:

```text
создать уникальный booking
        ↓
сохранить bookingid
        ↓
работать только с этим bookingid
        ↓
удалить его в @AfterEach/finally
```

Уникальность:

```java
String firstName = "ApiTest-" + UUID.randomUUID();
```

Случайное значение должно попадать в отчёт при падении. Иначе тест невозможно воспроизвести.

## 7. Map, JSON string или DTO

### Map — увидеть фактическую структуру JSON

```java
Map<String, Object> body = Map.of(
    "firstname", "Ivan",
    "totalprice", 150,
    "bookingdates", Map.of(
        "checkin", "2026-08-01",
        "checkout", "2026-08-05")
);
```

Map полезен в первых задачах и для маленького PATCH. Минусы: опечатки в ключах и слабая типобезопасность.

### DTO/record — основной рабочий вариант

```java
record BookingDates(String checkin, String checkout) {}

record BookingRequest(
    String firstname,
    String lastname,
    int totalprice,
    boolean depositpaid,
    BookingDates bookingdates,
    String additionalneeds
) {}
```

```java
given()
    .contentType(ContentType.JSON)
    .body(bookingRequest); // Jackson сериализует DTO в JSON
```

DTO делает контракт видимым в Java, помогает IDE и упрощает сравнение expected/actual.

### JSON string

Подходит для очень короткого негативного payload или точной проверки сырого формата. Большие JSON-строки в Java трудно читать и поддерживать.

## 8. Четыре способа работать с response — выбирайте по задаче

### 1. Inline Hamcrest

```java
response.then()
    .statusCode(200)
    .body("firstname", equalTo(expected.firstname()))
    .body("bookingdates.checkin", equalTo(expected.bookingdates().checkin()));
```

Используйте для 1–4 простых полей, когда проверка остаётся читаемой.

### 2. Извлечь одно значение

```java
int bookingId = response.then()
    .statusCode(200)
    .extract()
    .path("bookingid");
```

Используйте, если значение нужно следующему запросу.

### 3. JsonPath

```java
String firstName = response.jsonPath().getString("firstname");
int price = response.jsonPath().getInt("totalprice");
```

Используйте для нескольких отдельных значений. Не превращайте JsonPath в скрытый язык программирования.

### 4. DTO + AssertJ

```java
BookingResponse actual = response.as(BookingResponse.class);

assertThat(actual)
    .usingRecursiveComparison()
    .isEqualTo(expected);
```

Используйте, когда проверяется связанный объект целиком или результат нужен дальше.

Правило выбора:

```text
1 поле → extract/path
несколько простых полей → inline body или JsonPath
связанный объект → DTO + AssertJ
форма/типы → schema (дополнительно)
```

## 9. Полный create → read сценарий

```java
BookingRequest expected = uniqueBooking();

int bookingId = given()
    .spec(BASE_SPEC)
    .contentType(ContentType.JSON)
    .body(expected)
.when()
    .post("/booking")
.then()
    .statusCode(200)
    .extract()
    .path("bookingid");

BookingResponse actual = given()
    .spec(BASE_SPEC)
    .pathParam("id", bookingId)
.when()
    .get("/booking/{id}")
.then()
    .statusCode(200)
    .extract()
    .as(BookingResponse.class);

assertThat(actual)
    .usingRecursiveComparison()
    .isEqualTo(expected);
```

POST отвечает за создание. GET доказывает сохранение. AssertJ сравнивает связанный DTO. Это одна цельная проверка, а не три несвязанные технологии.

## 10. Specifications: убираем шум, не смысл

```java
RequestSpecification baseSpec = new RequestSpecBuilder()
    .setBaseUri(BASE_URL)
    .setAccept(ContentType.JSON)
    .addFilter(new ErrorLoggingFilter())
    .build();
```

В specification уместны:

- base URI;
- общие безопасные headers;
- accept/content type для группы запросов;
- timeout/config;
- условное логирование;
- техническая авторизация конкретного клиента.

Не прячьте туда:

- firstname конкретного теста;
- ожидаемый status, если endpoint имеет разные результаты;
- бизнес-assertions;
- случайно изменяемое глобальное состояние.

Response specification:

```java
ResponseSpecification okJson = new ResponseSpecBuilder()
    .expectStatusCode(200)
    .expectContentType(ContentType.JSON)
    .build();
```

После `.spec(okJson)` тест всё равно проверяет бизнес-данные.

## 11. Auth — часть контракта, а не служебная мелочь

Получение token:

```java
String token = given()
    .spec(BASE_SPEC)
    .contentType(ContentType.JSON)
    .body(new AuthRequest("admin", "password123"))
.when()
    .post("/auth")
.then()
    .statusCode(200)
    .extract()
    .path("token");
```

Использование:

```java
.cookie("token", token)
```

Обязательно проверяйте и отказ без авторизации. Иначе suite доказывает только happy path и не проверяет защиту endpoint.

В рабочем проекте token, password и Authorization нельзя печатать в лог или Allure без маскирования.

## 12. PUT, PATCH и DELETE проверяют состояние

### PUT

PUT передаёт полное новое представление. После запроса:

1. status ответа;
2. body ответа;
3. отдельный GET;
4. полное сравнение saved DTO с updated DTO.

### PATCH

PATCH меняет часть объекта. После запроса:

1. изменённое поле имеет новое значение;
2. критичные остальные поля не изменились.

Для связанных проверок удобно:

```java
SoftAssertions.assertSoftly(softly -> {
    softly.assertThat(saved.lastname()).isEqualTo(changedLastName);
    softly.assertThat(saved.firstname()).isEqualTo(original.firstname());
    softly.assertThat(saved.bookingdates()).isEqualTo(original.bookingdates());
});
```

### DELETE

Ответ DELETE недостаточен. Подтвердите отдельным GET, что ресурс недоступен.

## 13. JUnit усиливает REST-сценарии

JUnit отвечает за выполнение и организацию:

- `@Test` — самостоятельный сценарий;
- `@ParameterizedTest` — одна логика для таблицы входов;
- `@BeforeEach`/`@AfterEach` — lifecycle и cleanup;
- `@Disabled` — учебный переключатель;
- tags/display names — наборы и отчётность.

Пример таблицы неправильных credentials:

```java
@ParameterizedTest
@CsvSource({
    "admin, wrong-password",
    "wrong-user, password123"
})
void invalidAuth(String username, String password) {
    // один контракт, разные входные данные
}
```

Не создавайте цепочку зависимых методов `testCreate → testUpdate → testDelete`. Один полный CRUD-flow может быть цепочкой внутри одного теста, но отдельные `@Test` должны быть независимы.

## 14. AssertJ усиливает проверку данных

REST Assured/Hamcrest особенно удобен рядом с HTTP response:

```java
response.then().statusCode(200).contentType(ContentType.JSON);
```

AssertJ особенно удобен после извлечения Java-объекта:

```java
assertThat(actualBooking)
    .usingRecursiveComparison()
    .isEqualTo(expectedBooking);
```

Разделение ответственности:

```text
REST Assured → отправка request и transport response
Hamcrest     → компактные inline body checks
Jackson      → JSON ↔ DTO
AssertJ      → связанные проверки Java-объектов
JUnit        → lifecycle, parameterization, запуск и отчёт
```

## 15. JSON Schema: полезный, но не главный слой

```java
.body(matchesJsonSchemaInClasspath(
    "schemas/lesson01/booking-schema.json"));
```

Schema ловит:

- отсутствующее обязательное поле;
- неправильный JSON type;
- неправильную вложенность;
- часть форматных ошибок.

Schema не поймает:

- чужой firstname;
- неправильную сумму, если она остаётся integer;
- несохранённый PATCH;
- отсутствие авторизационной проверки.

Поэтому schema всегда дополняется бизнес-assertions.

## 16. Диагностика: хороший тест помогает найти причину

```java
given()
    .log().ifValidationFails()
.when()
    .get("/booking/{id}", id)
.then()
    .log().ifValidationFails();
```

При падении нужно видеть:

1. environment/base URL;
2. method и path;
3. безопасные headers и request body;
4. response status, headers и body;
5. созданные id/уникальные данные;
6. expected и actual;
7. на каком шаге упал flow.

Не включайте глобальный `.log().all()` бездумно: шум мешает, а секреты могут попасть в CI-логи.

Классификация причины:

| Категория | Пример |
|---|---|
| Product defect | PATCH изменил лишнее поле |
| Test defect | перепутан JsonPath или expected |
| Test data defect | используется чужой/удалённый id |
| Environment defect | DNS, timeout, сервис недоступен |
| Contract mismatch | документация и реализация расходятся |

## 17. Cleanup и независимость

`@AfterEach` подходит для всех id, зарегистрированных тестом. `try/finally` особенно полезен для длинного финального flow.

```java
Integer bookingId = null;
try {
    bookingId = createBooking();
    // проверки
} finally {
    if (bookingId != null) {
        deleteBookingIfPresent(bookingId);
    }
}
```

Cleanup:

- выполняется после упавшего assertion;
- удаляет только данные текущего теста;
- не скрывает исходную ошибку;
- терпимо относится к уже удалённому ресурсу;
- не использует случайный id из общего списка.

## 18. Как самостоятельно тестировать новый endpoint

Используйте этот алгоритм на работе:

1. Прочитать контракт и выписать method/path/auth/request/response.
2. Определить happy path и главные риски.
3. Решить, откуда берутся независимые данные.
4. Сделать один запрос вручную и изучить реальный response.
5. Автоматизировать happy path с transport + business assertions.
6. Для mutation добавить read-back.
7. Добавить самые ценные негативные сценарии.
8. Добавить cleanup и условное логирование.
9. Убрать техническое дублирование в specs/client/fixtures.
10. Проверить, что каждый тест запускается отдельно и параллельно не портит чужие данные.

## 19. Маршрут заданий

Задания находятся в `RestAssuredTasksTest.java` и выполняются последовательно.

### Блок 1 — HTTP-контракт

1. Полный health check.
2. Transport и shape списка booking id.
3. Извлечение существующего id и GET по path parameter.
4. Неизвестный id и диагностическое логирование.

### Блок 2 — собственные данные

5. POST из Map.
6. POST из DTO и десериализация wrapper-response.
7. POST → GET → recursive comparison.
8. Фильтрация только собственной записи.

### Блок 3 — выбор способа проверки body

9. Небольшой inline-контракт Hamcrest.
10. JsonPath для значений, нужных следующим шагам.
11. JSON Schema плюс бизнес-assertion.

### Блок 4 — переиспользование без потери смысла

12. RequestSpecification.
13. ResponseSpecification.

### Блок 5 — auth и изменение состояния

14. Получение token.
15. Неверные credentials как JUnit data table.
16. PUT и полное read-back сравнение.
17. PATCH и проверка изменённого/неизменившегося через AssertJ SoftAssertions.
18. Отказ без авторизации.
19. DELETE и подтверждение состояния через GET.

### Блок 6 — самостоятельность

20. Две независимые записи без смешивания данных.
21. Финальный boss: независимый CRUD-flow с REST Assured, DTO, AssertJ, JUnit lifecycle и cleanup.

## 20. Критерий готовности

Вы готовы применять это на работе, если без копирования можете объяснить и собрать тест, который:

- создаёт контролируемые данные;
- отправляет request с правильными method/path/params/headers/body/auth;
- проверяет конкретный HTTP-контракт;
- выбирает подходящий способ проверки body;
- подтверждает state transition отдельным GET;
- проверяет значимый негативный сценарий;
- очищает данные после любого исхода;
- при падении оставляет достаточно информации для локализации причины.

Это важнее знания редкого метода REST Assured наизусть.
