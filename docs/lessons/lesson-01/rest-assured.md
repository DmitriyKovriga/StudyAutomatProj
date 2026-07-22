# REST Assured: самостоятельное написание рабочих API-тестов

Материал связан с `RestAssuredTasksTest.java` и `RestAssuredTasksTest2.java`.
Первая часть учит с нуля создавать DTO, спецификацию запроса, авторизацию и
очистку тестовых данных, а затем писать на этой основе обычные API-тесты.
Вторая часть учит работать с неполными, динамическими и полиморфными JSON-
контрактами.

В файле заданий намеренно нет ни одной готовой строчки кода — только условие в
комментарии. Никакого отдельного «служебного» класса с уже написанными DTO,
спецификацией или методом получения token тоже нет: вы создаёте всё это сами
в заданиях 1–3 и затем переиспользуете в заданиях 4–10. Это осознанное решение:
единственный надёжный способ научиться самостоятельно тестировать чужой API —
самостоятельно пройти путь от первого запроса к незнакомому эндпоинту до
собственной маленькой тестовой инфраструктуры.

DTO в этом уроке — обычные Java-классы с аннотациями Lombok, а не `record`.
Это осознанный выбор в пользу того, что чаще встречается в реальных проектах:
`record` появился только в Java 16, и множество рабочих кодовых баз до сих пор
пишут DTO как класс с полями, геттерами/сеттерами и конструкторами — часто с
помощью Lombok, чтобы не писать этот код руками.

## Перед первым запросом: как самостоятельно создать DTO

DTO не появляется автоматически. Сначала автотестировщик смотрит на JSON-контракт,
затем описывает ту же структуру Java-классом.

### Шаг 0. Lombok в двух словах

В `pom.xml` этого проекта Lombok уже подключён. Три аннотации, которые нужны
почти всегда:

| Аннотация | Что генерирует |
|---|---|
| `@Data` | геттеры и сеттеры для всех полей, `toString()`, `equals()` и `hashCode()` по всем полям |
| `@NoArgsConstructor` | пустой конструктор `AuthRequest()` |
| `@AllArgsConstructor` | конструктор со всеми полями `AuthRequest(String username, String password)` |

Стандартный набор для DTO — все три сразу:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String username;
    private String password;
}
```

`@NoArgsConstructor` важен для DTO **ответа**: Jackson создаёт объект пустым
конструктором и затем заполняет поля через сеттеры, которые дал `@Data`. Без
пустого конструктора десериализация упадёт с ошибкой. `@AllArgsConstructor`
нужен для DTO **запроса**, чтобы в тесте можно было написать один короткий
конструктор вместо `new AuthRequest(); request.setUsername(...); request.setPassword(...);`.
Проще включать оба конструктора в любой DTO, а не выбирать один — это ничего
не стоит и снимает вопрос, какой из двух конструкторов понадобится дальше.

Если IntelliJ подчёркивает `@Data` как ошибку или геттеры/сеттеры не находятся
автодополнением, проверьте, что в IDE установлен и включён плагин Lombok
(`Settings → Plugins → Lombok`) и что включена annotation processing
(`Settings → Build, Execution, Deployment → Compiler → Annotation Processors`).

### Шаг 1. Определите форму JSON

Запрос авторизации Restful Booker:

```json
{
  "username": "admin",
  "password": "password123"
}
```

Ответ:

```json
{
  "token": "abc123"
}
```

Это два разных контракта, поэтому создаются два DTO — и, как в реальном
проекте, они лежат в разных пакетах: `dto.request` для тела запроса,
`dto.response` для тела ответа.

```text
package dto.request;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String username;
    private String password;
}
```

```text
package dto.response;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
```

Правило чтения простое:

```text
имя JSON-поля → имя приватного поля класса
тип JSON      → Java-тип поля
```

### Шаг 2. Создайте DTO запроса

`@AllArgsConstructor` даёт готовый конструктор. Поэтому тело запроса создаётся так:

```text
AuthRequest request = new AuthRequest(
        "admin",
        "password123"
);
```

После `.body(request)` Jackson вызовет геттеры `getUsername()` и `getPassword()`,
которые сгенерировал `@Data`, и соберёт JSON.

### Шаг 3. Создайте DTO ответа

После получения `Response` Jackson выполняет обратное преобразование:

```text
AuthResponse actual = response.as(AuthResponse.class);
```

Теперь token читается типобезопасно через геттер:

```text
actual.getToken()
```

DTO полезнее ручного `path("token")`, когда ответ используется дальше, содержит
несколько полей или должен сравниваться как единый объект.

### Как выбрать Java-тип поля

| JSON | Обычный Java-тип |
|---|---|
| строка | `String` |
| целое число | `int` или `Integer` |
| дробное число | `BigDecimal` или `double` |
| `true` / `false` | `boolean` или `Boolean` |
| объект | отдельный DTO-класс |
| массив объектов | `List<DtoType>` |
| произвольная структура | `JsonNode` |

Для строго обязательного числового поля часто подходит `int`. Если важно отличить
реальный `0` от отсутствующего значения, нужен `Integer`: пропущенное поле сможет
стать `null`. То же различие существует между `boolean` и `Boolean`.

### Вложенный объект требует вложенного DTO

JSON booking содержит объект `bookingdates`:

```json
{
  "firstname": "Anna",
  "lastname": "Student",
  "totalprice": 150,
  "depositpaid": true,
  "bookingdates": {
    "checkin": "2026-08-01",
    "checkout": "2026-08-05"
  },
  "additionalneeds": "Breakfast"
}
```

Сначала моделируется вложенная часть:

```text
package dto.request;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDates {
    private String checkin;
    private String checkout;
}
```

Затем основной класс с полем этого типа:

```text
package dto.request;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String firstname;
    private String lastname;
    private int totalprice;
    private boolean depositpaid;
    private BookingDates bookingdates;
    private String additionalneeds;
}
```

Ответ `POST /booking` является обёрткой:

```json
{
  "bookingid": 101,
  "booking": {
    "firstname": "Anna",
    "lastname": "Student",
    "totalprice": 150,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2026-08-01",
      "checkout": "2026-08-05"
    },
    "additionalneeds": "Breakfast"
  }
}
```

Для него нужны DTO сохранённой записи и DTO-обёртка:

```text
package dto.response;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String firstname;
    private String lastname;
    private int totalprice;
    private boolean depositpaid;
    private BookingDates bookingdates;
    private String additionalneeds;
}
```

```text
package dto.response;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingResponse {
    private int bookingid;
    private BookingResponse booking;
}
```

`BookingResponse` использует `bookingdates` типа `dto.request.BookingDates` —
структура дат одна и та же для запроса и ответа, поэтому нет смысла заводить
второй одинаковый класс. Импорт между `dto.request` и `dto.response` в обе
стороны — нормальная практика, если сущность действительно общая.

Request и Response могут выглядеть одинаково, но это разные роли. В реальном API
ответ со временем может получить серверные поля, которых нет в запросе (например,
`createdAt` или `updatedBy`). Поэтому раздельные типы обычно проще поддерживать,
даже если сегодня их поля совпадают.

### DTO элемента списка

`GET /booking` возвращает не полные booking, а короткие элементы:

```json
[
  {"bookingid": 101},
  {"bookingid": 102}
]
```

DTO описывает один элемент массива:

```text
package dto.response;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingIdResponse {
    private int bookingid;
}
```

А весь ответ имеет тип `List<BookingIdResponse>`. Не создавайте DTO с полями
полного booking, если фактический элемент JSON содержит только `bookingid`.

### Если имена JSON и Java отличаются

При JSON-поле `first_name` и Java-поле `firstName` аннотация ставится прямо на поле:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    @JsonProperty("first_name")
    private String firstName;
}
```

В Restful Booker названия совпадают, поэтому в этом уроке `@JsonProperty` не нужен.

## 1. Какая общая картина должна быть в голове

В одном API-тесте участвуют четыре инструмента:

```text
JUnit запускает тестовый метод
        ↓
REST Assured собирает и отправляет HTTP-запрос
        ↓
Jackson превращает JSON-ответ в Java-объект
        ↓
AssertJ сравнивает фактический результат с ожидаемым
```

На уровне кода это выглядит примерно так:

```text
@Test
void userCanBeCreated() {
    UserRequest expected = new UserRequest("Ivan");

    Response response = given()
            // здесь описывается запрос
            .when()
            .post("/users");

    UserResponse actual = response.as(UserResponse.class);

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(actual.getName()).isEqualTo(expected.getName());
}
```

Запомните границы ответственности:

- `given()...post(...)` — REST Assured;
- `response.as(...)` — REST Assured вызывает Jackson;
- `assertThat(...)` — AssertJ;
- `@Test` и запуск метода — JUnit.

## 2. Сначала определяем HTTP-контракт

До написания Java-кода нужно понять контракт метода API:

```text
HTTP-метод + путь + параметры + авторизация + тело → статус + тело ответа
```

Пример:

```text
POST /users
Content-Type: application/json

{"name":"Ivan"}

→ 201
→ {"id":15,"name":"Ivan"}
```

Если непонятно, какой путь, статус или JSON ожидается, сначала откройте документацию
API. REST Assured не угадывает контракт — он только отправляет описанный вами запрос.

### Основные HTTP-методы

| Метод | Обычный смысл |
|---|---|
| `GET` | получить данные |
| `POST` | создать запись или выполнить действие |
| `PUT` | полностью заменить запись |
| `PATCH` | изменить отдельные поля |
| `DELETE` | удалить запись |

Статусы зависят от конкретного API. В Restful Booker создание booking возвращает
`200`, а удаление — `201`. Да, это нетипично и легко перепутать — именно поэтому
в задании 9 стоит специальная проверка статуса `201` у `DELETE`. В другом проекте
те же операции могут вернуть `201` и `204`. Не переносите статусы одного API на
другой без проверки документации.

## 3. Как самостоятельно собрать запрос

Базовая цепочка REST Assured:

```text
Response response = given()
        .spec(BASE_SPEC)
        // настройки конкретного запроса
        .when()
        .get("/users");
```

Читайте сверху вниз:

1. `given()` — начинаем собирать запрос.
2. `.spec(BASE_SPEC)` — подключаем общие настройки сервера.
3. Между `spec` и `when` добавляем параметры, cookie, заголовки и тело.
4. `.when()` — закончили описывать запрос.
5. `.get(...)`, `.post(...)` и другие методы — отправляем запрос.
6. Полученный полный ответ сохраняем в `Response`.

`when()` не отправляет запрос сам по себе. Запрос отправляется последним методом:
`get`, `post`, `put`, `patch` или `delete`.

### `BASE_SPEC` — ваша собственная общая спецификация

`RequestSpecification` — это набор настроек, общих для всех запросов к одному
серверу. В задании 1 вы укажете `baseUri` прямо в цепочке запроса — это нормально
для одного теста, но неудобно повторять в каждом задании. Поэтому в задании 2 вы
один раз создадите статическое поле:

```text
private static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
        .setBaseUri("https://restful-booker.herokuapp.com")
        .addHeader("Accept", "application/json")
        .build();
```

и дальше будете подключать его через `.spec(BASE_SPEC)` вместо повторения адреса.

Обратите внимание на заголовок `Accept`. Он задан точной строкой `"application/json"`.
Не заменяйте его на `.setAccept(ContentType.JSON)`: REST Assured может развернуть
такое значение в список из нескольких MIME-типов, а Restful Booker в ответ на
такой заголовок иногда отвечает статусом `418` вместо ожидаемого результата. Это
частный, но реальный пример того, что у конкретного API могут быть особенности,
не описанные явно в документации, и их приходится обнаруживать на практике.

`BASE_SPEC` не содержит HTTP-метод, путь, тело, параметры пути, параметры запроса
или token. Эти части вы добавляете в каждом задании самостоятельно.

## 4. Тело запроса и `Content-Type`

Для `POST`, `PUT` и `PATCH` часто требуется JSON-тело:

```text
UserRequest request = new UserRequest("Ivan", true);

Response response = given()
        .spec(BASE_SPEC)
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/users");
```

Здесь происходят две связанные вещи:

- `contentType(ContentType.JSON)` сообщает серверу формат отправляемого тела;
- `body(request)` передаёт Java-объект, который Jackson превратит в JSON, вызвав
  его геттеры.

`Accept` и `Content-Type` — не одно и то же:

```text
Accept       → какой формат ответа хотим получить
Content-Type → какой формат тела отправляем
```

Для обычного `GET` тело и `Content-Type` чаще всего не нужны.

## 5. Что находится в `Response`

`Response` хранит полный ответ сервера:

```text
int statusCode = response.statusCode();
String contentType = response.contentType();
String rawJson = response.asString();
```

Проверки технической части:

```text
assertThat(response.statusCode()).isEqualTo(200);
assertThat(response.contentType()).contains("application/json");
```

`response.asString()` полезен при отладке, но рабочий тест не должен разбирать JSON
ручным поиском строк. Для данных ответа используйте DTO.

## 6. DTO и Jackson

DTO описывает структуру JSON в виде Java-класса:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private int id;
    private String name;
    private boolean active;
}
```

Если сервер вернул один JSON-объект:

```json
{"id":15,"name":"Ivan","active":true}
```

его можно преобразовать так:

```text
UserResponse actual = response.as(UserResponse.class);
```

Направления преобразования:

```text
Java DTO → JSON → сервер     сериализация (Jackson читает геттеры)
сервер → JSON → Java DTO     десериализация (Jackson создаёт объект пустым
                              конструктором и вызывает сеттеры)
```

Имена полей и их типы в DTO должны соответствовать JSON. Вложенный JSON-объект
требует вложенного DTO, а JSON-массив нельзя преобразовать в один объект.

Кстати, именно поэтому `@Data` удобен для AssertJ: он генерирует `equals()` и
`hashCode()` по всем полям. Без него `assertThat(actual).isEqualTo(expected)`
сравнил бы объекты по ссылке (как обычный `Object.equals()`) и почти всегда
падал бы, даже если содержимое совпадает. В этом уроке для DTO с вложенными
объектами всё равно рекомендуется `usingRecursiveComparison()` (раздел 9) — она
не зависит от того, сгенерирован ли `equals()`, и даёт понятное сообщение о
том, какое именно поле отличается.

### Объект-обёртка

Ответ создания может содержать ID и созданный объект:

```json
{
  "id": 15,
  "user": {
    "name": "Ivan"
  }
}
```

Ему соответствуют два DTO:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponse {
    private int id;
    private UserResponse user;
}
```

После десериализации доступны обе части через геттеры:

```text
CreateUserResponse created = response.as(CreateUserResponse.class);
int id = created.getId();
UserResponse user = created.getUser();
```

### JSON-массив и `TypeRef`

Если сервер возвращает массив:

```json
[
  {"id": 15},
  {"id": 16}
]
```

нужен список DTO:

```text
List<UserIdResponse> users = response.as(
        new TypeRef<List<UserIdResponse>>() {
        }
);
```

`TypeRef` сохраняет информацию о том, что это не просто `List`, а список именно
`UserIdResponse`.

## 7. Параметр пути и параметр запроса

Эти два механизма часто путают.

### `pathParam`: конкретный ресурс

Документация показывает путь `/users/{id}`. Значение вставляется внутрь пути:

```text
Response response = given()
        .spec(BASE_SPEC)
        .pathParam("id", userId)
        .when()
        .get("/users/{id}");
```

Имя `"id"` должно совпадать с `{id}` в строке пути.

### `queryParam`: фильтр или настройка

Параметр запроса добавляется после `?`:

```text
Response response = given()
        .spec(BASE_SPEC)
        .queryParam("role", "admin")
        .queryParam("active", true)
        .when()
        .get("/users");
```

Фактический адрес будет похож на:

```text
/users?role=admin&active=true
```

Короткое правило:

```text
/users/{id}       → pathParam выбирает одну запись
/users?role=admin → queryParam фильтрует список
```

## 8. Авторизация Restful Booker

Защищённые `PUT`, `PATCH` и `DELETE` требуют token.

### Получение token

В `POST /auth` отправляются логин и пароль. JSON-ответ содержит поле `token`.
Вы уже прошли этот запрос вручную в задании 1 с DTO `AuthRequest`/`AuthResponse`.
В задании 3 тот же запрос оформляется как переиспользуемый метод:

```text
private static String getToken() {
    Response response = given()
            .spec(BASE_SPEC)
            .contentType(ContentType.JSON)
            .body(new AuthRequest("admin", "password123"))
            .when()
            .post("/auth");

    return response.as(AuthResponse.class).getToken();
}
```

Начиная с задания 3, вызывайте `getToken()` везде, где нужен token, вместо того
чтобы повторять запрос авторизации внутри каждого теста.

### Передача token

Restful Booker ожидает token в cookie:

```text
given()
        .spec(BASE_SPEC)
        .cookie("token", token)
        // остальные части запроса
```

Это особенность контракта данного API. В другом проекте может использоваться
`Authorization: Bearer ...`.

## 9. Как строить проверки через AssertJ

Хороший API-тест обычно проверяет несколько уровней.

### Технический результат

```text
assertThat(response.statusCode()).isEqualTo(200);
assertThat(response.contentType()).contains("application/json");
```

### Важные данные

```text
assertThat(actual.getId()).isPositive();
assertThat(actual.getName()).isEqualTo(expected.getName());
```

### Полное сравнение DTO

Если ожидаемый и фактический объекты имеют одинаковые по смыслу поля:

```text
assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(expected);
```

Рекурсивное сравнение проверяет и вложенные объекты. Это удобно для booking с
вложенным `bookingdates`.

### Проверка ID в списке DTO

```text
assertThat(users)
        .extracting(BookingIdResponse::getBookingid)
        .contains(createdId);
```

`extracting` превращает список объектов в список выбранных значений, после чего
можно проверить нужный ID. Ссылка на метод `BookingIdResponse::getBookingid` —
это геттер, который сгенерировал `@Data`.

## 10. Почему после изменения нужен контрольный GET

Ответ `200` от `POST`, `PUT` или `PATCH` ещё не доказывает, что сервер правильно
сохранил данные. Рабочий сценарий часто проверяет состояние отдельным GET:

```text
отправить изменение
→ проверить статус операции
→ выполнить GET по ID
→ десериализовать сохранённую запись
→ сравнить её с ожидаемыми данными
```

Для `DELETE` логика похожа:

```text
DELETE по ID → успешный статус → GET по тому же ID → статус 404
```

Так тест проверяет не только ответ одной ручки, но и реальное состояние системы.
Это одна из главных привычек, которая отличает middle-тестировщика от junior:
junior обычно останавливается на проверке статуса, middle проверяет фактический
результат операции.

## 11. POST, PUT и PATCH на практике

### POST

Создаёт запись. После ответа обычно нужно:

1. проверить статус;
2. десериализовать объект-обёртку;
3. проверить положительный ID;
4. сравнить созданные данные с отправленными;
5. зарегистрировать ID для очистки.

### PUT

Полностью заменяет запись. Обычно отправляется полный DTO:

```text
.contentType(ContentType.JSON)
.body(fullUpdatedDto)
.put("/users/{id}")
```

После PUT выполните GET и сравните сохранённый объект с полным ожидаемым DTO.

### PATCH

Меняет только переданные поля. Для небольшого тела удобно создать `Map`:

```text
Map<String, Object> changes = Map.of("active", false);
```

После PATCH проверяйте и изменённое поле, и несколько важных неизменённых полей.
Иначе тест не заметит, что сервер случайно затёр остальные данные.

## 12. Независимые данные и очистка

Публичный Restful Booker используют одновременно разные люди. Нельзя выбирать
случайную чужую запись и ожидать в ней конкретные данные.

Надёжный тест:

1. создаёт собственную уникальную запись (например, с `firstname` вида
   `"Create-" + UUID.randomUUID()`, чтобы не столкнуться с чужими данными);
2. получает её ID;
3. выполняет сценарий только с этим ID;
4. удаляет запись после теста.

В задании 3 вы создадите поле `createdBookingIds` и метод с аннотацией
`@AfterEach`, который удаляет все ID из этого набора после каждого теста. После
этого в каждом задании достаточно одной строки сразу после создания записи:

```text
createdBookingIds.add(createdId);
```

Не откладывайте эту строку до конца теста. Если последующая проверка упадёт,
`@AfterEach` всё равно увидит ID и удалит запись.

Если тест сам успешно выполнил DELETE, удалите ID из набора:

```text
createdBookingIds.remove(deletedId);
```

Иначе очистка сделает лишний повторный DELETE.

Класс `RestAssuredTasksTest` использует стандартный для JUnit 5 жизненный цикл
`PER_METHOD`: перед каждым `@Test` создаётся новый экземпляр класса. Поэтому
`createdBookingIds` можно объявить обычным (не статическим) полем — новый тест
всегда получает пустой набор, и очищать его вручную между тестами не нужно.

## 13. Что вы создаёте сами и в каком порядке

В этом уроке нет отдельного «служебного» файла с уже написанной инфраструктурой.
Первые три задания — это не только тренировка DTO, но и постройка мини-фреймворка
для оставшихся семи. Порядок значим: каждое следующее задание использует то, что
появилось в предыдущем.

```text
Задание 1 → dto.request.AuthRequest, dto.response.AuthResponse
Задание 2 → BASE_SPEC, dto.response.BookingIdResponse
Задание 3 → dto.request.BookingDates, dto.request.BookingRequest,
             dto.response.BookingResponse, dto.response.CreateBookingResponse,
             createdBookingIds, getToken(), метод @AfterEach для очистки
Задания 4–10 → используют только перечисленное выше
```

DTO-классы вы создаёте как обычные файлы в пакетах `dto.request` и
`dto.response` рядом с тестовым классом — так же, как в реальном проекте, а не
как вложенные типы внутри теста. `BASE_SPEC`, `getToken()` и
`createdBookingIds` — это код самого теста, а не DTO, и остаются в
`RestAssuredTasksTest`.

Если вам кажется, что для очередного задания нужен новый DTO или метод, сначала
проверьте, не решает ли задачу то, что вы уже написали. Дублирование почти всегда
признак того, что задание можно решить проще.

## 14. Маршрут по заданиям

### Задание 1. DTO авторизации и получение token

Создайте класс `dto.request.AuthRequest` (поля `username`, `password`) и класс
`dto.response.AuthResponse` (поле `token`) с аннотациями `@Data`,
`@NoArgsConstructor`, `@AllArgsConstructor`. Соберите запрос вручную, указав
`baseUri` прямо в цепочке (общую спецификацию вы вынесете в следующем задании).
Отправьте `admin`/`password123` на `POST /auth`, преобразуйте ответ в
`AuthResponse` и проверьте статус, формат ответа и `getToken()`.

Порядок работы:

1. Создайте `AuthRequest` из данных условия.
2. Создайте переменную `Response` и присвойте ей цепочку REST Assured с `baseUri`,
   `contentType(ContentType.JSON)` и `body(...)`.
3. После `when()` отправьте POST на `/auth`.
4. Преобразуйте ответ в `AuthResponse`.
5. Через AssertJ проверьте статус, формат ответа и `getToken()`.

Опора по форме кода находится в разделах 4–6.

### Задание 2. Общая спецификация и DTO элемента списка

Вынесите повторяющийся `baseUri` в статическое поле `BASE_SPEC` (раздел 3). Затем
создайте класс `dto.response.BookingIdResponse` (поле `bookingid`) — DTO одного
элемента ответа `GET /booking`. В GET нет тела запроса, поэтому цепочка короче:

```text
given → spec → when → GET
```

После получения `Response` используйте пример с `TypeRef` из раздела 6, подставив
`BookingIdResponse`. Список проверяется через AssertJ:

- `isNotEmpty()` для самого списка;
- `allSatisfy(...)` для правила, которое должен выполнять каждый элемент.

Внутри `allSatisfy` можно обратиться к `booking.getBookingid()`.

### Задание 3. DTO booking, создание записи и очистка

По полному JSON из вводного раздела создайте четыре класса:

1. `dto.request.BookingDates`;
2. `dto.request.BookingRequest`;
3. `dto.response.BookingResponse`;
4. `dto.response.CreateBookingResponse`.

Затем добавьте инфраструктуру, описанную в разделах 8 и 12: поле
`createdBookingIds`, метод `getToken()` и метод `@AfterEach` для очистки. Это уже
код самого теста, а не DTO — он остаётся в `RestAssuredTasksTest`.

Наконец, самостоятельно создайте request. Для уникального имени используйте
`"Create-" + UUID.randomUUID()`. Даты, цену и остальные значения возьмите из
примера контракта в вводном разделе.

Нужно соединить:

```text
JSON Content-Type + body(request) + POST /booking
```

Ответ имеет форму обёртки, поэтому тип результата — `CreateBookingResponse`.
После десериализации сначала зарегистрируйте ID для очистки, затем проверяйте его
и вложенный `booking`. Request и Response имеют разные Java-типы, поэтому для
полного сравнения используйте `usingRecursiveComparison()`.

### Задание 4. Прочитать запись по ID

Создайте и зарегистрируйте уникальный booking, как в задании 3. Самостоятельно
напишите:

1. `Response` с GET и `pathParam`;
2. десериализацию в `BookingResponse`;
3. проверку статуса;
4. рекурсивное сравнение `actual` с отправленным `BookingRequest`.

Проверьте, что имя в `pathParam` совпадает с фигурными скобками в пути.

### Задание 5. Фильтрация списка

Создайте и зарегистрируйте уникальный booking. Для фильтра нужны два `queryParam`
со значениями `firstname` и `lastname` вашего booking (через геттеры
`getFirstname()`/`getLastname()`).

Ответ фильтра остаётся JSON-массивом, поэтому используется тот же `TypeRef`, что
в задании 2. Для проверки созданного ID примените `extracting` из раздела 9.

### Задание 6. Полная замена через PUT

Создайте и зарегистрируйте booking. Получите token через `getToken()`. PUT должен
включать:

```text
BASE_SPEC
pathParam с bookingId
cookie с token
JSON Content-Type
body с новым BookingRequest
HTTP-метод PUT
```

Преобразуйте тело PUT в `BookingResponse` и проверьте его. Затем самостоятельно
соберите отдельный GET, снова преобразуйте ответ и сравните фактически сохранённые
данные с тем, что вы отправляли в PUT.

Не переиспользуйте переменную ответа PUT для GET. Это два разных HTTP-ответа.

### Задание 7. Частичное изменение через PATCH

Создайте и зарегистрируйте booking. Сначала создайте тело изменения:

```text
Map<String, Object> changes = Map.of("lastname", newLastName);
```

Затем соберите защищённый PATCH по той же форме, что PUT. После проверки статуса
выполните GET и получите `BookingResponse`.

Проверки должны доказать две вещи:

1. `getLastname()` стал равен `newLastName`;
2. важные поля из исходного booking (`firstname`, `totalprice`, `bookingdates`) не
   изменились.

### Задание 8. Запрещённое обновление

Создайте и зарегистрируйте booking. Соберите обычный PUT с новым `BookingRequest`,
но намеренно не добавляйте `cookie`. Это единственное осознанное отличие от
задания 6.

После статуса `403` обязательно выполните GET. Сам отказ ещё не доказывает, что
данные не изменились. Прочитанный DTO должен рекурсивно совпасть с исходным
booking, а не с тем, что вы пытались записать.

### Задание 9. Удаление

Создайте и зарегистрируйте booking. Соберите защищённый DELETE с token и
`pathParam`. После проверки статуса удалите ID из `createdBookingIds`, потому что
запись уже удалена самим тестом.

Затем выполните отдельный GET по тому же ID. Здесь DTO не нужен: при статусе 404
достаточно проверить полный `Response`.

### Задание 10. Полный жизненный цикл

Это не новая технология, а самостоятельная сборка знакомых частей:

```text
POST нового booking
→ получить и зарегистрировать ID
→ GET и сравнить с отправленным booking
→ PUT нового варианта данных с token
→ GET и сравнить с обновлённым вариантом
→ DELETE с token
→ удалить ID из набора очистки
→ GET и проверить 404
```

Пишите сценарий по одному шагу. После каждого запроса сразу создавайте понятную
переменную ответа и нужный DTO. Не пытайтесь заранее объявить несколько переменных
со значением `null` — это ухудшает чтение и не помогает решить задачу.

## 15. Универсальный порядок размышления

Перед новым API-тестом ответьте по порядку:

1. Какой HTTP-метод используется?
2. Какой путь указан в документации?
3. Нужен `pathParam`, `queryParam` или оба?
4. Нужна ли авторизация и где передаётся token?
5. Есть ли тело и нужен ли JSON `Content-Type`?
6. Как выглядит JSON-ответ: объект, массив или обёртка?
7. В какой DTO его преобразовать? Может, подходящий DTO уже есть?
8. Какой статус ожидается?
9. Какие данные доказывают правильность результата?
10. Нужен ли контрольный GET?
11. Какие созданные данные нужно удалить?

После этого пишите код в том же порядке:

```text
данные → запрос → Response → DTO → проверки → регистрация или очистка ID
```

## 16. Частые ошибки

### `@Data` подчёркнут как ошибка, геттеры/сеттеры не находятся

Установите и включите плагин Lombok в IntelliJ (`Settings → Plugins → Lombok`) и
убедитесь, что включена annotation processing
(`Settings → Build, Execution, Deployment → Compiler → Annotation Processors →
Enable annotation processing`). Без этого IDE не видит код, который Lombok
генерирует во время компиляции.

### IntelliJ не знает `given`, `assertThat` или `ContentType`

В файле заданий нет заранее написанных импортов — вам нужно добавить их самим
(через быстрое исправление IntelliJ Alt+Enter или вручную): статические импорты
`given` и `assertThat`, обычные импорты `ContentType`, `Response`, `TypeRef`,
`RequestSpecification`, `RequestSpecBuilder` и стандартные классы `java.util`.

### `Response` равен `null`

Не создавайте заглушку `Response response = null`. Переменная должна сразу получить
результат законченной цепочки с `get`, `post`, `put`, `patch` или `delete`.

### Сервер не понимает тело

Проверьте, что перед `body(...)` указан `contentType(ContentType.JSON)`.

### Получен `403` в PUT, PATCH или DELETE

Проверьте, что вызвали `getToken()` и передали результат через
`.cookie("token", token)`.

### Получен `418` вместо ожидаемого статуса

Проверьте заголовок `Accept` в `BASE_SPEC`: он должен быть задан строкой
`"application/json"` через `addHeader(...)`, а не через `.setAccept(ContentType.JSON)`.

### Jackson не может создать DTO

Сравните форму JSON с типом результата:

- объект → `response.as(SomeResponse.class)`;
- массив → `response.as(new TypeRef<List<...>>() {})`;
- обёртка → отдельный DTO с вложенным объектом.

Если Jackson жалуется на отсутствие конструктора — проверьте, что на классе
стоит `@NoArgsConstructor`.

### Фильтр ничего не нашёл

Проверьте имена параметров по документации и убедитесь, что значения взяты именно
из созданного тестом booking, а не из чужой случайной записи.

### Тест оставляет данные на сервере

Добавляйте ID в `createdBookingIds` сразу после успешного создания. После успешного
DELETE убирайте его из набора. Проверьте, что метод очистки помечен `@AfterEach`,
а не просто вызывается вручную в конце одного теста.

### Тест проходит по статусу, хотя данные неверны

Статус проверяет только общий результат операции. Десериализуйте ответ и проверяйте
важные данные. После изменения используйте контрольный GET.

## Часть 2. Гибкие DTO и динамический JSON

Задания этого раздела находятся в `RestAssuredTasksTest2.java`. Они не отправляют
запросы в публичный сервис: Restful Booker не имеет методов с полиморфными batch-
контрактами. Вместо этого используются готовые JSON-примеры из
`RestAssuredDtoFlexibilitySupport.java` (только строковые константы и обёртка
`jsonResponse(...)`, без единого готового DTO), и создаются обычные REST Assured
`Response` с заданным JSON. Десериализация выполняется точно тем же способом, что
после настоящего HTTP-запроса.

DTO здесь можно объявлять как `static` nested class прямо в `RestAssuredTasksTest2`
(с тем же набором `@Data @NoArgsConstructor @AllArgsConstructor`) — в отличие от
первой части, это разовые модели для разбора конкретного JSON-примера, а не
контракт реального эндпоинта, поэтому отдельные файлы не обязательны.

### Пустая строка, явный `null` и отсутствующее поле — разные случаи

Для API эти три JSON могут означать разное:

```json
{"additionalneeds": ""}
```

```json
{"additionalneeds": null}
```

```json
{}
```

| Вариант | Что передано |
|---|---|
| `""` | поле есть, значение — пустая строка |
| `null` | поле есть, значение явно отсутствует |
| поля нет | клиент вообще не отправил это свойство |

Обычный DTO хранит значение поля, но не отдельный признак «поле было передано в
JSON». Поэтому одного Java-значения `null` недостаточно, чтобы удобно построить все
три негативных запроса одной моделью.

Практичный подход для контрактных тестов:

1. создать корректный типизированный DTO;
2. превратить его в `ObjectNode`;
3. изменить только проверяемое поле;
4. передать получившийся `ObjectNode` в `.body(...)`.

```text
FlexibleBookingRequest valid = new FlexibleBookingRequest("Anna", "Student", "Breakfast");

ObjectNode empty = MAPPER.valueToTree(valid);
empty.put("additionalneeds", "");

ObjectNode explicitNull = MAPPER.valueToTree(valid);
explicitNull.putNull("additionalneeds");

ObjectNode missing = MAPPER.valueToTree(valid);
missing.remove("additionalneeds");
```

Создавайте три независимых дерева. Если изменять один и тот же `ObjectNode`, второй
вариант будет зависеть от изменений первого.

`@JsonInclude(JsonInclude.Include.NON_NULL)` на поле или классе автоматически
убирает поля со значением `null` при сериализации. Это удобно для PATCH-моделей,
но не решает все случаи: после включения `NON_NULL` тем же полем уже нельзя
отправить явный JSON `null`. Для точечных негативных тестов `ObjectNode` понятнее.

### Как проверить наличие поля в `JsonNode`

```text
node.has("profile")
node.get("profile").isNull()
node.hasNonNull("profile")
```

- `has` отвечает, существует ли ключ, даже если его значение `null`;
- `get(...).isNull()` подтверждает явный JSON `null`;
- `hasNonNull` возвращает `true`, только если поле существует и не равно `null`.

Проверка двух вариантов:

```text
assertThat(withNull.has("profile")).isTrue();
assertThat(withNull.get("profile").isNull()).isTrue();

assertThat(withoutField.has("profile")).isFalse();
```

### Почему DTO не отличает отсутствующее поле от `null`

Пусть DTO содержит поле `Profile profile`. После десериализации обоих ответов
значение поля будет `null`:

```json
{"id": 7, "profile": null}
```

```json
{"id": 7}
```

Это нормально: DTO отвечает на вопрос «какое итоговое значение доступно Java-коду?».
Если тест проверяет точное соблюдение JSON-контракта и должен различать присутствие
ключа, дополнительно используйте `JsonNode`.

Хорошее практическое сочетание:

```text
DTO      → проверка известных бизнес-данных
JsonNode → проверка формы контракта и наличия ключей
```

### Новое необязательное поле в ответе

Сервер может добавить `traceId`, а старому клиенту оно пока не нужно. Чтобы один
конкретный DTO спокойно принимал дополнительные поля, аннотация ставится на класс:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private Integer id;
    private String name;
    private Profile profile;
}
```

Используйте это осознанно:

- для клиентского автотеста лишнее техническое поле часто не должно ломать сценарий;
- для строгого contract-теста неожиданное поле может быть именно ошибкой, и тогда
  игнорирование включать нельзя.

Глобально отключать проверку неизвестных полей для всего `ObjectMapper` обычно хуже:
локальная аннотация сразу показывает решение рядом с конкретным DTO.

### Один контейнер, но разный тип `data`: generic DTO

API нередко всегда возвращает одинаковую обёртку, но содержимое `data` различается:

```json
{
  "status": "ok",
  "data": {"bookingid": 101, "firstname": "Anna"},
  "error": null
}
```

```json
{
  "status": "ok",
  "data": [
    {"bookingid": 101, "firstname": "Anna"},
    {"bookingid": 102, "firstname": "Ivan"}
  ]
}
```

Не нужно создавать `SingleBookingEnvelope` и `BookingListEnvelope`. Обычный класс
может быть generic — точно так же, как `List<T>` или `Map<K, V>` из стандартной
библиотеки:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiEnvelope<T> {
    private String status;
    private T data;
    private ApiError error;
}
```

Десериализация сохраняет конкретный тип через REST Assured `TypeRef`:

```text
ApiEnvelope<BookingSummary> one = response.as(
        new TypeRef<ApiEnvelope<BookingSummary>>() {
        }
);

ApiEnvelope<List<BookingSummary>> many = response.as(
        new TypeRef<ApiEnvelope<List<BookingSummary>>>() {
        }
);
```

Generic подходит, когда оболочка стабильна, а тип внутри известен в конкретном
endpoint.

### Череда разных сущностей с полем-discriminator

Иногда массив содержит разные события:

```json
[
  {"type":"created", "bookingId":101, "author":"api"},
  {"type":"price_changed", "bookingId":101, "oldPrice":100, "newPrice":150},
  {"type":"deleted", "bookingId":102, "reason":"duplicate"}
]
```

Поле `type` однозначно сообщает реальный вид элемента. Это называется
discriminator. Такой контракт описывается обычным интерфейсом с настройками
Jackson и классами-реализациями:

```text
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreatedEvent.class, name = "created"),
        @JsonSubTypes.Type(
                value = PriceChangedEvent.class,
                name = "price_changed"),
        @JsonSubTypes.Type(value = DeletedEvent.class, name = "deleted")
})
public interface ApiEvent {
}
```

Реализации содержат только собственные поля:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedEvent implements ApiEvent {
    private Integer bookingId;
    private String author;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangedEvent implements ApiEvent {
    private Integer bookingId;
    private Integer oldPrice;
    private Integer newPrice;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletedEvent implements ApiEvent {
    private Integer bookingId;
    private String reason;
}
```

После `response.as(new TypeRef<List<ApiEvent>>() {})` Jackson читает `type` и создаёт
нужную реализацию для каждого элемента.

Не делайте один огромный DTO с полями `author`, `oldPrice`, `newPrice` и `reason`,
где почти всё всегда `null`. Такая модель разрешает невозможные комбинации и плохо
показывает контракт.

### Как отправить список разных сущностей

Для запроса используется тот же принцип: общий интерфейс и разные команды.

В заданиях применяются такие типы:

```text
type=create → CreateBookingCommand: String clientReference, String firstname

type=cancel → CancelBookingCommand: Integer bookingId, String reason
```

`ApiCommand` настраивается через `@JsonTypeInfo` и `@JsonSubTypes`, как `ApiEvent`.

При сериализации списка важно сохранить объявленный тип элементов. Используйте
Jackson `TypeReference`, а не REST Assured `TypeRef`:

```text
String json = MAPPER.writerFor(
        new TypeReference<List<ApiCommand>>() {
        }
).writeValueAsString(commands);
```

Почему здесь другой класс:

```text
REST Assured TypeRef       → response.as(...), чтение HTTP-ответа
Jackson TypeReference     → writerFor/readValue, работа ObjectMapper
```

### Если discriminator отсутствует

Если массив выглядит так:

```json
[
  {"bookingid": 101},
  "heartbeat",
  {"warning": "slow response"},
  null,
  42
]
```

Jackson не может надёжно угадать единый Java-тип. В этом случае честнее использовать
`JsonNode` и проверить форму каждого элемента:

```text
node.isArray()
node.get(0).isObject()
node.get(1).isTextual()
node.get(3).isNull()
node.get(4).isInt()
```

Если контракт стабилизируется и получит `type`, переходите к полиморфным DTO.
Не угадывайте тип по случайному наличию одного поля, если сервер не гарантирует это
правило документацией.

### Сложная комбинация: типизированный каркас и динамическая часть

Необязательно выбирать только DTO или только `JsonNode`. Часто лучший контракт
сочетает оба подхода:

```text
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequest {
    private String requestId;
    private List<ApiCommand> commands;
    private Map<String, JsonNode> metadata;
}
```

Здесь:

- `requestId` имеет строгий тип;
- `commands` — типизированный полиморфный список;
- `metadata` допускает дополнительные значения разной формы.

Это лучше, чем превращать весь запрос в `Map<String, Object>`. Стабильная часть
остаётся проверяемой компилятором, а гибкость ограничена только местом, где она
действительно нужна.

### Как выбрать подход

| Ситуация | Подход |
|---|---|
| Стабильный JSON-объект | обычный DTO |
| Стабильная обёртка, разный известный `data` | generic `ApiEnvelope<T>` |
| Необязательный вложенный объект | nullable поле DTO |
| Нужно отличить `null` от отсутствия | `JsonNode` / `ObjectNode` |
| Лишние поля допустимы | `@JsonIgnoreProperties` |
| Разные типы с полем `type` | полиморфные DTO (интерфейс + реализации) |
| Разные типы без discriminator | `JsonNode` |
| Стабильный каркас и динамический участок | DTO с `JsonNode` или `Map<String, JsonNode>` |

## Маршрут по заданиям `RestAssuredTasksTest2`

### Задание 1. Три состояния одного поля

Объявите класс `FlexibleBookingRequest` (поля `firstname`, `lastname`,
`additionalneeds`) в тестовом классе. Создайте один корректный объект и трижды
вызовите `MAPPER.valueToTree`, чтобы деревья не делили изменения.

Для вариантов используйте:

```text
put("additionalneeds", "")
putNull("additionalneeds")
remove("additionalneeds")
```

Проверки должны учитывать и наличие ключа, и его значение.

### Задание 2. Необязательный вложенный объект

Объявите:

```text
@Data @NoArgsConstructor @AllArgsConstructor
class Profile {
    private String city;
}

@Data @NoArgsConstructor @AllArgsConstructor
class UserResponse {
    private Integer id;
    private String name;
    private Profile profile;
}
```

Получите два `Response` через `jsonResponse(...)`. Каждый ответ преобразуйте и в DTO,
и в `JsonNode`. DTO покажет одинаковое итоговое значение `null`, а дерево позволит
проверить наличие ключа.

### Задание 3. Новое поле ответа

Добавьте `@JsonIgnoreProperties(ignoreUnknown = true)` над `UserResponse` из
задания 2. После этого тот же DTO должен прочитать JSON с `traceId`, не добавляя
поле `traceId` в класс.

### Задание 4. Generic-обёртка

Объявите `ApiError`, `BookingSummary` и `ApiEnvelope<T>`. Для первого ответа нужен
`TypeRef<ApiEnvelope<BookingSummary>>`, для второго —
`TypeRef<ApiEnvelope<List<BookingSummary>>>`.

Проверьте `status`, отсутствие ошибки, ID одного объекта, размер списка и ID его
элементов (через геттеры).

### Задание 5. Полиморфный ответ

Скопируйте не готовое решение, а структуру аннотаций из раздела про discriminator:
интерфейс `ApiEvent` с `@JsonTypeInfo`/`@JsonSubTypes` и три класса-реализации.
Сопоставления должны быть точными:

```text
created       → CreatedEvent
price_changed → PriceChangedEvent
deleted       → DeletedEvent
```

Проверьте размер списка, Java-тип каждого элемента и хотя бы одно специфичное поле
каждого события.

### Задание 6. Полиморфный запрос

Создайте `ApiCommand`, `CreateBookingCommand` и `CancelBookingCommand` с именами
типов `create` и `cancel`. После сериализации прочитайте строку обратно в `JsonNode`:
так удобно проверить `type` и специфичные поля обоих элементов.

### Задание 7. Массив без общего типа

Прочитайте `MIXED_ITEMS_JSON` через `MAPPER.readTree`. Здесь `TypeRef<List<Dto>>`
не нужен, потому что честного общего DTO у элементов нет. Проверяйте каждый элемент
соответствующим методом `JsonNode`.

### Задание 8. Итоговая комбинация

Используйте команды из задания 6 внутри `BatchRequest`. Для metadata создайте, например:

```text
source → текстовый JsonNode
retry  → boolean JsonNode
```

После проверки корректного JSON создайте `deepCopy()` корневого `ObjectNode`. В
копии установите `source` в явный `null`, а `retry` удалите. Финальные проверки
должны доказать, что эти состояния различаются.

## Критерий готовности

Блок пройден, если без готовых заготовок вы можете:

1. с нуля создать `RequestSpecification` и объяснить, что в неё стоит выносить;
2. самостоятельно смоделировать DTO по JSON-контракту с помощью Lombok: простой
   объект, вложенный объект, обёртку и элемент массива;
3. объяснить, зачем DTO нужны `@NoArgsConstructor` и `@AllArgsConstructor`;
4. собрать GET, POST, PUT, PATCH и DELETE;
5. передать `pathParam`, `queryParam`, cookie и JSON-тело;
6. получить и переиспользовать token без повторения запроса авторизации в каждом
   тесте;
7. организовать очистку тестовых данных через `@AfterEach`, не забывая
   регистрировать ID сразу после создания;
8. проверить статус и данные через AssertJ, включая рекурсивное сравнение DTO;
9. подтвердить сохранение или удаление контрольным GET;
10. различить пустую строку, явный `null` и отсутствующее поле;
11. выбрать между DTO, generic, полиморфизмом и `JsonNode`;
12. сериализовать и десериализовать список разных типов с discriminator;
13. объяснить назначение каждой строки собственного теста и почему в проекте нет
    отдельного «магического» служебного класса, который делал бы это за вас.

Печатный конспект остаётся дополнительной памяткой:
`docs/print/rest-assured-cheatsheet/rest-assured-cheatsheet.pdf`.
