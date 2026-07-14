# REST Assured: теория и 15 заданий

Практика использует [Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html) — учебный CRUD API бронирований с аутентификацией. Сервис общий для всех пользователей и периодически сбрасывает данные, поэтому нельзя полагаться на заранее известный booking id.

## 1. Что проверяет API-тест

Хороший API-тест проверяет контракт на нескольких уровнях:

- HTTP status code;
- заголовки и content type;
- структуру JSON/XML;
- типы и значения ключевых полей;
- бизнес-инварианты;
- побочный эффект: созданный объект читается, обновление сохранено, удалённый объект недоступен;
- корректное поведение при ошибочных данных и без авторизации.

Проверка только `statusCode(200)` обычно недостаточна.

## 2. Базовый синтаксис

```java
given()
    .baseUri("https://restful-booker.herokuapp.com")
    .contentType(ContentType.JSON)
    .body(requestBody)
.when()
    .post("/booking")
.then()
    .statusCode(200)
    .contentType(ContentType.JSON)
    .body("booking.firstname", equalTo("Ivan"));
```

- `given()` — подготовка запроса;
- `when()` — HTTP-метод и endpoint;
- `then()` — валидация ответа.

Статические импорты обычно берут из `io.restassured.RestAssured.*` и `org.hamcrest.Matchers.*`.

## 3. Параметры и заголовки

```java
given()
    .pathParam("id", bookingId)
    .queryParam("firstname", "Ivan")
    .header("Accept", "application/json")
.when()
    .get("/booking/{id}");
```

- `pathParam` подставляется в путь;
- `queryParam` кодируется как query string;
- `header` передаёт заголовок;
- `cookie("token", token)` удобен для Restful Booker.

Не собирайте URL конкатенацией, если можно использовать path/query params.

## 4. RequestSpecification и ResponseSpecification

Общие настройки не следует копировать в каждый тест:

```java
RequestSpecification requestSpec = new RequestSpecBuilder()
    .setBaseUri(BASE_URL)
    .setContentType(ContentType.JSON)
    .addFilter(new ErrorLoggingFilter())
    .build();

ResponseSpecification okJson = new ResponseSpecBuilder()
    .expectStatusCode(200)
    .expectContentType(ContentType.JSON)
    .build();
```

Specification должна содержать технические настройки, общие для группы запросов. Не прячьте в ней ожидаемые бизнес-значения конкретного теста.

## 5. Работа с body

### Строка

```java
.body("{\"username\":\"admin\",\"password\":\"password123\"}")
```

Подходит для короткого примера, но неудобна для сложных данных.

### Map

```java
Map<String, Object> body = Map.of(
    "firstname", "Ivan",
    "totalprice", 150
);
```

### DTO/record

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

DTO даёт типобезопасность, переиспользование и удобную сериализацию через Jackson.

## 6. Извлечение ответа

```java
Response response = given().spec(requestSpec)
    .when().get("/booking/{id}", id);

int status = response.statusCode();
String firstName = response.jsonPath().getString("firstname");
int bookingId = response.then()
    .statusCode(200)
    .extract().path("bookingid");

Booking booking = response.as(Booking.class);
```

Выбирайте один подход осознанно:

- inline Hamcrest body checks — компактный контракт;
- `extract().path(...)` — нужно одно значение для следующего шага;
- `Response` — нужны status, headers и body;
- десериализация DTO — много связанных проверок и дальнейшая работа с объектом.

## 7. GPath/JsonPath

Примеры выражений:

```java
body("bookingid", everyItem(greaterThan(0)));
body("booking.findAll { it.totalprice > 100 }.firstname", hasItem("Ivan"));
```

Не делайте выражение настолько сложным, что его невозможно быстро прочитать. Для сложной бизнес-проверки извлеките DTO/коллекцию и примените AssertJ.

## 8. Аутентификация Restful Booker

Токен создаётся запросом:

```http
POST /auth
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

Для `PUT`, `PATCH`, `DELETE` передайте cookie:

```java
.cookie("token", token)
```

Токен нельзя выводить в общие логи реального проекта. В учебном API он временный, но привычку скрывать credentials нужно формировать сразу.

## 9. Основные endpoints

| Метод | Путь | Назначение |
|---|---|---|
| GET | `/ping` | health check, ожидается 201 |
| POST | `/auth` | получить token |
| GET | `/booking` | список booking id, поддерживает фильтры |
| GET | `/booking/{id}` | получить бронирование |
| POST | `/booking` | создать бронирование |
| PUT | `/booking/{id}` | полностью заменить бронирование |
| PATCH | `/booking/{id}` | частично обновить бронирование |
| DELETE | `/booking/{id}` | удалить бронирование |

Документация и примеры также доступны в [официальной Postman-коллекции Restful Booker](https://www.postman.com/automation-in-testing/restful-booker-collections/collection/55eh7vh/restful-booker).

## 10. Проверка JSON Schema

```java
given().spec(requestSpec)
.when().get("/booking/{id}", id)
.then()
    .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"));
```

Schema проверяет форму и типы, но не заменяет бизнес-проверки. JSON с неправильной фамилией может полностью соответствовать schema.

## 11. Логирование и диагностика

```java
given()
    .log().ifValidationFails()
.when()
    .get("/booking")
.then()
    .log().ifValidationFails();
```

Не включайте `.log().all()` глобально в большом suite: логи станут шумными и могут раскрыть токены/персональные данные. Для диагностики полезны `ErrorLoggingFilter`, `ResponseLoggingFilter` по условию и собственный correlation id.

## 12. Cleanup и независимость

Shared API означает, что чужие данные могут исчезнуть. Надёжный сценарий:

1. Создать собственное бронирование.
2. Извлечь его id.
3. Выполнить проверяемое действие.
4. В `finally` удалить созданные данные, если они ещё существуют.

Cleanup не должен скрывать первоначальное падение теста. Не обновляйте и не удаляйте случайную запись из `GET /booking`.

## 13. Негативные проверки

Проверяйте:

- неизвестный id;
- отсутствие/невалидную авторизацию;
- отсутствующее обязательное поле;
- неверный тип;
- некорректный content type;
- граничные значения.

Учебный Restful Booker намеренно имеет особенности и дефекты. Если фактическое поведение расходится с документацией, сохраните request/response и сформулируйте defect, а не подгоняйте assertion без объяснения.

## 14. Уровень middle+: архитектура API-тестов

- Разделяйте transport layer, модели данных, builders/fixtures и сами тесты.
- Не превращайте endpoint client в набор assertions: client выполняет запрос, тест определяет ожидание.
- Переиспользуйте specifications, но не создавайте один глобальный изменяемый объект на все случаи.
- Проверяйте итоговое состояние отдельным GET после mutation.
- Не связывайте разные `@Test` в цепочку create → update → delete. Один end-to-end тест может быть цепочкой внутри одного метода.
- Указывайте разумные connect/read timeout в рабочих проектах.
- Отличайте retry инфраструктурной операции от маскировки продуктового дефекта.
- В CI прикладывайте request/response к Allure только при необходимости и очищайте секреты.

## Задания

Все заготовки находятся в `RestAssuredTasksTest.java`.

1. Health check `/ping`.
2. Получение списка booking id и проверка структуры.
3. Фильтрация списка по firstname на собственных данных.
4. Создание бронирования из `Map`.
5. Извлечение id и последующий GET.
6. Десериализация ответа в DTO.
7. Создание и применение `RequestSpecification`.
8. Создание и применение `ResponseSpecification`.
9. Получение auth token и негативная проверка credentials.
10. Полное обновление через PUT и проверка сохранённого состояния.
11. Частичное обновление через PATCH и проверка неизменившихся полей.
12. DELETE и подтверждение, что GET возвращает 404.
13. Негативный GET неизвестного id с диагностическим логированием.
14. Проверка ответа по JSON Schema.
15. Независимый CRUD-сценарий с cleanup в `finally`.

Внешний сервис может быть временно недоступен. Ошибка сети не означает, что assertion написан неверно; сохраните stack trace и повторите позже, но не добавляйте бесконечный retry.
