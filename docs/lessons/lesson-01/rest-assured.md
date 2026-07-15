# REST Assured: практический DTO-first маршрут

Цель курса — писать обычные поддерживаемые API-тесты, а не изучать все возможности DSL.

## Основной стиль

```java
Response response = given()
        .spec(BASE_SPEC)
        .contentType(ContentType.JSON)
        .body(requestDto)
    .when()
        .post("/booking");

assertThat(response.statusCode()).isEqualTo(200);
CreateBookingResponse actual = response.as(CreateBookingResponse.class);
assertThat(actual.booking()).usingRecursiveComparison().isEqualTo(requestDto);
```

Распределение ответственности:

- REST Assured собирает и отправляет HTTP request, возвращает `Response`;
- Jackson превращает DTO в JSON и JSON обратно в DTO;
- AssertJ проверяет status, headers и Java-объекты;
- JUnit запускает сценарии, даёт lifecycle и parameterization.

Вложенные `body("path", matcher)` и JsonPath не запрещены. Они полезны для единичного
значения, но не являются основой курса: связанный JSON читаем как DTO.

## Навигация

| Задачи | Рабочий навык |
|---|---|
| 01–02 | `Response`, plain text, JSON-массив → `List<DTO>` |
| 03–05 | POST/GET/filter, request и response DTO, read-back |
| 06–07 | негативный ответ, failure logging, `RequestSpecification` |
| 08–09 | token DTO и параметризованный negative auth |
| 10–13 | PUT, PATCH, auth rejection, DELETE и проверка state |
| 14–15 | независимые ресурсы, cleanup, полный CRUD-flow |

## Response и DTO

Один объект:

```java
BookingResponse booking = response.as(BookingResponse.class);
```

Generic collection:

```java
List<BookingIdResponse> bookings = response.as(new TypeRef<>() {});
```

`TypeRef` сохраняет generic type, который потерялся бы у `List.class`.

## Parameters

```java
.pathParam("id", bookingId)
.get("/booking/{id}")       // конкретный resource

.queryParam("firstname", name)
.get("/booking")            // фильтрация collection
```

## POST и read-back

```java
Response create = given().spec(BASE_SPEC)
        .contentType(ContentType.JSON)
        .body(expected)
        .post("/booking");
CreateBookingResponse created = create.as(CreateBookingResponse.class);

Response read = getBooking(created.bookingid());
BookingResponse saved = read.as(BookingResponse.class);
assertThat(saved).usingRecursiveComparison().isEqualTo(expected);
```

POST response не доказывает сохранение. После mutation делаем отдельный GET.

## RequestSpecification

```java
RequestSpecification spec = new RequestSpecBuilder()
        .setBaseUri(BASE_URL)
        .setAccept(ContentType.JSON)
        .addFilter(new ErrorLoggingFilter())
        .build();
```

В spec храним общую HTTP-конфигурацию. Request body, test data и business assertions
остаются в тесте.

## Auth

```java
AuthResponse auth = response.as(AuthResponse.class);

given().spec(BASE_SPEC)
    .cookie("token", auth.token())
    .body(updateDto)
    .put("/booking/{id}");
```

Негативные credentials удобно подавать через `@ParameterizedTest` и `@CsvSource`,
если для всех наборов действует один контракт.

## PUT, PATCH, DELETE

- PUT отправляет полный DTO; GET после него должен совпасть с updated DTO.
- PATCH отправляет маленькую `Map` только с изменяемыми полями; GET проверяет changed
  и preserved fields.
- отказ без token проверяется не только status 403: следующий GET должен показать,
  что исходный объект не изменился.
- DELETE подтверждается отдельным GET со status 404.

## Независимость

Каждый тест создаёт свои unique data, сразу регистрирует ID для cleanup, не использует
static mutable ID и проходит отдельно. `@AfterEach` очищает созданные resources даже
после assertion failure.

## Рабочий алгоритм

1. Прочитать method/path/auth/request/response contract.
2. Подготовить unique request DTO.
3. Выполнить request и сохранить `Response`.
4. AssertJ-проверкой проверить точный status и нужные headers.
5. Десериализовать JSON в response DTO.
6. Проверить DTO относительно request DTO.
7. После mutation выполнить read-back.
8. Добавить важный negative case и проверить неизменность state.
9. Зарегистрировать cleanup и failure logging.
10. После появления повторений вынести HTTP-механику в API client/specification.
