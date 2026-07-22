# REST Assured: самостоятельное написание рабочих API-тестов

Материал связан с `RestAssuredTasksTest.java` и `RestAssuredTasksTest2.java`.
Первая часть учит создавать DTO и писать обычные API-тесты. Вторая часть учит
работать с неполными, динамическими и полиморфными JSON-контрактами.

В файле заданий намеренно нет готовых запросов, переменных `Response`,
десериализации и проверок. Там оставлены только условия и минимальные исходные
данные. Примеры ниже используют другие сущности, поэтому решение нужно собрать
самостоятельно.

## Перед первым запросом: как самостоятельно создать DTO

DTO не появляется автоматически. Сначала автотестировщик смотрит на JSON-контракт,
затем описывает ту же структуру Java-типами.

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

Это два разных контракта, поэтому создаются два DTO:

```text
record StudentAuthRequest(String username, String password) {
}

record StudentAuthResponse(String token) {
}
```

Правило чтения простое:

```text
имя JSON-поля → имя компонента record
тип JSON      → Java-тип компонента
```

В учебном файле records можно объявить внутри тестового класса, но не внутри
тестового метода. В рабочем проекте DTO обычно лежат в отдельных пакетах вроде
`dto.request` и `dto.response`.

### Шаг 2. Создайте DTO запроса

`record` автоматически создаёт конструктор. Поэтому тело запроса создаётся так:

```text
StudentAuthRequest request = new StudentAuthRequest(
        "admin",
        "password123"
);
```

После `.body(request)` Jackson вызовет методы `username()` и `password()` и
соберёт JSON. В `record` обращение к данным всегда выглядит как вызов метода:

```text
request.username()
request.password()
```

Это причина, почему пишется `request.username()`, а не `request.username`.

### Шаг 3. Создайте DTO ответа

После получения `Response` Jackson выполняет обратное преобразование:

```text
StudentAuthResponse actual = response.as(StudentAuthResponse.class);
```

Теперь token читается типобезопасно:

```text
actual.token()
```

DTO полезнее ручного `path("token")`, когда ответ используется дальше, содержит
несколько полей или должен сравниваться как единый объект.

### Как выбрать Java-тип

| JSON | Обычный Java-тип |
|---|---|
| строка | `String` |
| целое число | `int` или `Integer` |
| дробное число | `BigDecimal` или `double` |
| `true` / `false` | `boolean` или `Boolean` |
| объект | отдельный DTO |
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
record StudentBookingDates(String checkin, String checkout) {
}
```

Затем основной DTO с компонентом этого типа:

```text
record StudentBookingRequest(
        String firstname,
        String lastname,
        int totalprice,
        boolean depositpaid,
        StudentBookingDates bookingdates,
        String additionalneeds) {
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
record StudentBookingResponse(
        String firstname,
        String lastname,
        int totalprice,
        boolean depositpaid,
        StudentBookingDates bookingdates,
        String additionalneeds) {
}

record StudentCreateBookingResponse(
        int bookingid,
        StudentBookingResponse booking) {
}
```

Request и Response могут выглядеть одинаково, но это разные роли. В реальном API
ответ со временем может получить серверные поля, которых нет в запросе. Поэтому
раздельные типы обычно проще поддерживать.

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
record StudentBookingIdResponse(int bookingid) {
}
```

А весь ответ имеет тип `List<StudentBookingIdResponse>`. Не создавайте DTO с полями
полного booking, если фактический элемент JSON содержит только `bookingid`.

### Если имена JSON и Java отличаются

При JSON-поле `first_name` и Java-компоненте `firstName` используется аннотация:

```text
record UserResponse(
        @JsonProperty("first_name") String firstName) {
}
```

В Restful Booker названия совпадают, поэтому в первой части `@JsonProperty` не нужен.

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
    assertThat(actual.name()).isEqualTo(expected.name());
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

Статусы зависят от конкретного API. В Restful Booker создание возвращает `200`,
а удаление — `201`. В другом проекте те же операции могут вернуть `201` и `204`.

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

### Что уже хранит `BASE_SPEC`

В уроке общая спецификация заранее содержит:

- `baseUri` — `https://restful-booker.herokuapp.com`;
- `Accept: application/json` — просьбу вернуть JSON.

Заголовок `Accept` в служебном классе задан точной строкой `application/json`.
Не заменяйте его в этом уроке на `setAccept(ContentType.JSON)`: REST Assured может
развернуть такое значение в список MIME-типов, а Restful Booker отвечает на него
статусом `418` вместо создания booking.

Поэтому в тесте не нужно повторять полный адрес. Достаточно написать путь:

```text
.get("/booking")
```

Спецификация не содержит HTTP-метод, путь, тело, параметры пути, параметры запроса
или token. Эти части ученик добавляет самостоятельно.

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
- `body(request)` передаёт Java-объект, который Jackson превратит в JSON.

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

DTO описывает структуру JSON в виде Java-типа:

```text
record UserResponse(int id, String name, boolean active) {
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
Java DTO → JSON → сервер     сериализация
сервер → JSON → Java DTO     десериализация
```

Имена и типы в DTO должны соответствовать JSON. Вложенный JSON-объект требует
вложенного DTO, а JSON-массив нельзя преобразовать в один объект.

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
record UserResponse(String name) {
}

record CreateUserResponse(int id, UserResponse user) {
}
```

После десериализации доступны обе части:

```text
CreateUserResponse created = response.as(CreateUserResponse.class);
int id = created.id();
UserResponse user = created.user();
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
Общий порядок:

```text
создать DTO логина
→ отправить DTO в JSON-теле POST
→ получить Response
→ response.as(TokenResponse.class)
→ прочитать token
```

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

В служебном классе есть `getToken()`. В заданиях после первого его можно вызывать,
чтобы не повторять авторизацию и сосредоточиться на защищённой операции.

## 9. Как строить проверки через AssertJ

Хороший API-тест обычно проверяет несколько уровней.

### Технический результат

```text
assertThat(response.statusCode()).isEqualTo(200);
assertThat(response.contentType()).contains("application/json");
```

### Важные данные

```text
assertThat(actual.id()).isPositive();
assertThat(actual.name()).isEqualTo(expected.name());
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
        .extracting(UserIdResponse::id)
        .contains(createdId);
```

`extracting` превращает список объектов в список выбранных значений, после чего
можно проверить нужный ID.

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

1. создаёт собственную уникальную запись;
2. получает её ID;
3. выполняет сценарий только с этим ID;
4. удаляет запись после теста.

В уроке `uniqueBooking(prefix)` создаёт DTO с уникальным `firstname`.
`createTrackedBooking(request)` создаёт запись и сразу регистрирует её ID для
очистки.

Если ID создаётся вручную внутри задания, его нужно зарегистрировать сразу:

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

## 13. Что находится в служебном классе

Чтобы условия не терялись среди инфраструктуры, она вынесена в
`RestAssuredLessonSupport.java`. В заданиях этот файл менять не нужно.

Из него доступны:

- `BASE_SPEC` — адрес сервера и ожидаемый JSON;
- `uniqueBooking(prefix)` — уникальный DTO запроса;
- `createTrackedBooking(request)` — подготовка записи с автоматической очисткой;
- `getToken()` — получение рабочего token;
- `createdBookingIds` — ID, которые очистит `@AfterEach`;
- DTO запросов и ответов.

DTO урока:

```text
AuthRequest             логин и пароль
AuthResponse            token
BookingIdResponse       один ID из массива GET /booking
BookingRequest          тело создания или обновления
BookingResponse         сохранённый booking
CreateBookingResponse   bookingid и вложенный booking
BookingDates            вложенные даты
```

Служебный класс не является примером того, как нужно решить тест. Он только убирает
повторяющуюся подготовку и гарантирует очистку данных.

## 14. Маршрут по заданиям

### Задание 1. Создать DTO авторизации и получить token

Сначала объявите в `RestAssuredTasksTest` два собственных record из вводного
раздела: `StudentAuthRequest` и `StudentAuthResponse`. Не используйте одноимённые
типы служебного класса — смысл задания именно в самостоятельном моделировании.

Затем соберите полный POST.

Порядок работы:

1. Создайте `StudentAuthRequest` из данных условия.
2. Создайте переменную `Response` и присвойте ей цепочку REST Assured.
3. Подключите `BASE_SPEC`.
4. Укажите JSON `Content-Type` и положите DTO в `body`.
5. После `when()` отправьте POST на путь из условия.
6. Преобразуйте ответ в `StudentAuthResponse`.
7. Через AssertJ проверьте статус, формат ответа и `token`.

Опора по форме кода находится в разделах 3–6. Не копируйте пример с пользователем
целиком: сначала сопоставьте поля JSON и компоненты собственных records.

### Задание 2. Создать DTO элемента списка и получить ID

Объявите `StudentBookingIdResponse(int bookingid)`. DTO описывает один объект
внутри массива, а не весь ответ и не полный booking.

В GET нет тела запроса, поэтому цепочка короче:

```text
given → spec → when → GET
```

После получения `Response` используйте пример с `TypeRef` из раздела 6, но
подставьте `StudentBookingIdResponse`. Список проверяется через AssertJ:

- `isNotEmpty()` для самого списка;
- `allSatisfy(...)` для правила, которое должен выполнять каждый элемент.

Внутри `allSatisfy` можно обратиться к `booking.bookingid()`.

### Задание 3. Создать вложенные DTO и booking

По полному JSON из вводного раздела объявите четыре records:

1. `StudentBookingDates`;
2. `StudentBookingRequest`;
3. `StudentBookingResponse`;
4. `StudentCreateBookingResponse`.

Затем самостоятельно создайте request. Для уникального имени можно использовать
`"Create-" + UUID.randomUUID()`. Даты, цену и остальные значения возьмите из
примера контракта выше.

Нужно соединить:

```text
JSON Content-Type + body(expected) + POST /booking
```

Ответ имеет форму обёртки, поэтому тип результата — `StudentCreateBookingResponse`.
После десериализации сначала зарегистрируйте ID для очистки, затем проверяйте его
и вложенный `booking`. Request и Response имеют разные Java-типы, поэтому для
полного сравнения используйте `usingRecursiveComparison()`.

### Задание 4. Прочитать запись по ID

Запись создаётся двумя подготовительными строками, потому что тема задания — GET
с параметром пути, а не повторение POST.

Самостоятельно напишите:

1. `Response` с GET и `pathParam`;
2. десериализацию в `BookingResponse`;
3. проверку статуса;
4. рекурсивное сравнение `actual` с `expected`.

Проверьте, что имя в `pathParam` совпадает с фигурными скобками в пути.

### Задание 5. Фильтрация списка

Здесь нужны два `queryParam`. Значения берутся из методов `expected.firstname()`
и `expected.lastname()`.

Ответ фильтра остаётся JSON-массивом, поэтому используется тот же `TypeRef`, что
в задании 2. Для проверки созданного ID примените `extracting` из раздела 9.

### Задание 6. Полная замена через PUT

Сначала получите token через служебный метод. PUT должен включать:

```text
BASE_SPEC
pathParam с bookingId
cookie с token
JSON Content-Type
body с expected
HTTP-метод PUT
```

Преобразуйте тело PUT в `BookingResponse` и проверьте его. Затем самостоятельно
соберите отдельный GET, снова преобразуйте ответ и сравните фактически сохранённые
данные с `expected`.

Не переиспользуйте переменную ответа PUT для GET. Это два разных HTTP-ответа.

### Задание 7. Частичное изменение через PATCH

Сначала создайте тело изменения:

```text
Map<String, Object> changes = Map.of("lastname", newLastName);
```

Затем соберите защищённый PATCH по той же форме, что PUT. После проверки статуса
выполните GET и получите `BookingResponse`.

Проверки должны доказать две вещи:

1. `lastname` стал равен `newLastName`;
2. важные поля из `original` не изменились.

### Задание 8. Запрещённое обновление

Соберите обычный PUT, но намеренно не добавляйте `cookie`. Это единственное
осознанное отличие от задания 6.

После статуса `403` обязательно выполните GET. Сам отказ ещё не доказывает, что
данные не изменились. Прочитанный DTO должен рекурсивно совпасть с `original`, а
не с `forbiddenUpdate`.

### Задание 9. Удаление

Соберите защищённый DELETE с token и `pathParam`. После проверки статуса удалите
ID из `createdBookingIds`, потому что запись уже удалена самим тестом.

Затем выполните отдельный GET по тому же ID. Здесь DTO не нужен: при статусе 404
достаточно проверить полный `Response`.

### Задание 10. Полный жизненный цикл

Это не новая технология, а самостоятельная сборка знакомых частей:

```text
POST initial
→ получить и зарегистрировать ID
→ GET и сравнить с initial
→ PUT updated с token
→ GET и сравнить с updated
→ DELETE с token
→ удалить ID из набора очистки
→ GET и проверить 404
```

Пишите сценарий по одному шагу. После каждого запроса сразу создавайте понятную
переменную ответа и нужный DTO. Не пытайтесь заранее объявить десять переменных со
значением `null` — это ухудшает чтение и не помогает решить задачу.

## 15. Универсальный порядок размышления

Перед новым API-тестом ответьте по порядку:

1. Какой HTTP-метод используется?
2. Какой путь указан в документации?
3. Нужен `pathParam`, `queryParam` или оба?
4. Нужна ли авторизация и где передаётся token?
5. Есть ли тело и нужен ли JSON `Content-Type`?
6. Как выглядит JSON-ответ: объект, массив или обёртка?
7. В какой DTO его преобразовать?
8. Какой статус ожидается?
9. Какие данные доказывают правильность результата?
10. Нужен ли контрольный GET?
11. Какие созданные данные нужно удалить?

После этого пишите код в том же порядке:

```text
данные → запрос → Response → DTO → проверки → регистрация или очистка ID
```

## 16. Частые ошибки

### IntelliJ не знает `given`, `assertThat` или `ContentType`

Нужные импорты уже находятся в файле заданий. Если вы случайно удалили их,
восстановите статические импорты `given` и `assertThat`, а также обычный импорт
`ContentType`.

### `Response` равен `null`

Не создавайте заглушку `Response response = null`. Переменная должна сразу получить
результат законченной цепочки с `get`, `post`, `put`, `patch` или `delete`.

### Сервер не понимает тело

Проверьте, что перед `body(...)` указан `contentType(ContentType.JSON)`.

### Получен `403` в PUT, PATCH или DELETE

Проверьте, что вызвали `getToken()` и передали результат через
`.cookie("token", token)`.

### Jackson не может создать DTO

Сравните форму JSON с типом результата:

- объект → `response.as(SomeResponse.class)`;
- массив → `response.as(new TypeRef<List<...>>() {})`;
- обёртка → отдельный DTO с вложенным объектом.

### Фильтр ничего не нашёл

Проверьте имена параметров по документации и убедитесь, что значения взяты именно
из созданного тестом `expected`.

### Тест оставляет данные на сервере

Добавляйте ID в `createdBookingIds` сразу после успешного создания. После успешного
DELETE убирайте его из набора.

### Тест проходит по статусу, хотя данные неверны

Статус проверяет только общий результат операции. Десериализуйте ответ и проверяйте
важные данные. После изменения используйте контрольный GET.

## Часть 2. Гибкие DTO и динамический JSON

Задания этого раздела находятся в `RestAssuredTasksTest2.java`. Они не отправляют
запросы в публичный сервис: Restful Booker не имеет методов с полиморфными batch-
контрактами. Вместо этого создаются обычные REST Assured `Response` с заданным JSON.
Десериализация выполняется точно тем же способом, что после настоящего HTTP-запроса.

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

Обычный DTO хранит значение поля, но не отдельный признак «компонент был передан в
JSON». Поэтому одного Java-значения `null` недостаточно, чтобы удобно построить все
три негативных запроса одной моделью.

Практичный подход для контрактных тестов:

1. создать корректный типизированный DTO;
2. превратить его в `ObjectNode`;
3. изменить только проверяемое поле;
4. передать получившийся `ObjectNode` в `.body(...)`.

```text
FlexibleRequest valid = new FlexibleRequest("Anna", "Breakfast");

ObjectNode empty = MAPPER.valueToTree(valid);
empty.put("additionalneeds", "");

ObjectNode explicitNull = MAPPER.valueToTree(valid);
explicitNull.putNull("additionalneeds");

ObjectNode missing = MAPPER.valueToTree(valid);
missing.remove("additionalneeds");
```

Создавайте три независимых дерева. Если изменять один и тот же `ObjectNode`, второй
вариант будет зависеть от изменений первого.

`@JsonInclude(JsonInclude.Include.NON_NULL)` автоматически убирает поля со значением
`null`. Это удобно для PATCH-моделей, но не решает все случаи: после включения
`NON_NULL` тем же компонентом уже нельзя отправить явный JSON `null`. Для точечных
негативных тестов `ObjectNode` понятнее.

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

Пусть DTO содержит компонент `StudentProfile profile`. После десериализации обоих
ответов значение компонента будет `null`:

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
конкретный DTO спокойно принимал дополнительные поля:

```text
@JsonIgnoreProperties(ignoreUnknown = true)
record UserResponse(
        Integer id,
        String name,
        Profile profile) {
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

Не нужно создавать `SingleBookingEnvelope` и `BookingListEnvelope`. Тип данных можно
сделать параметром:

```text
record ApiEnvelope<T>(String status, T data, ApiError error) {
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
discriminator. Такой контракт можно описать полиморфно:

```text
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreatedEvent.class, name = "created"),
        @JsonSubTypes.Type(
                value = PriceChangedEvent.class,
                name = "price_changed"),
        @JsonSubTypes.Type(value = DeletedEvent.class, name = "deleted")
})
sealed interface ApiEvent
        permits CreatedEvent, PriceChangedEvent, DeletedEvent {
}
```

Реализации содержат только собственные поля:

```text
record CreatedEvent(Integer bookingId, String author)
        implements ApiEvent {
}

record PriceChangedEvent(
        Integer bookingId,
        Integer oldPrice,
        Integer newPrice) implements ApiEvent {
}

record DeletedEvent(Integer bookingId, String reason)
        implements ApiEvent {
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
type=create → CreateBookingCommand(
        String clientReference,
        String firstname)

type=cancel → CancelBookingCommand(
        Integer bookingId,
        String reason)
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
record BatchRequest(
        String requestId,
        List<ApiCommand> commands,
        Map<String, JsonNode> metadata) {
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
| Необязательный вложенный объект | nullable DTO-компонент |
| Нужно отличить `null` от отсутствия | `JsonNode` / `ObjectNode` |
| Лишние поля допустимы | `@JsonIgnoreProperties` |
| Разные типы с полем `type` | полиморфные DTO |
| Разные типы без discriminator | `JsonNode` |
| Стабильный каркас и динамический участок | DTO с `JsonNode` или `Map<String, JsonNode>` |

## Маршрут по заданиям `RestAssuredTasksTest2`

### Задание 1. Три состояния одного поля

Объявите `FlexibleBookingRequest` в тестовом классе. Создайте один корректный DTO и
трижды вызовите `MAPPER.valueToTree`, чтобы деревья не делили изменения.

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
record StudentProfile(String city) {
}

record StudentUserResponse(
        Integer id,
        String name,
        StudentProfile profile) {
}
```

Получите два `Response` через `jsonResponse(...)`. Каждый ответ преобразуйте и в DTO,
и в `JsonNode`. DTO покажет одинаковое итоговое значение `null`, а дерево позволит
проверить наличие ключа.

### Задание 3. Новое поле ответа

Добавьте `@JsonIgnoreProperties(ignoreUnknown = true)` над `StudentUserResponse` из
задания 2. После этого тот же DTO должен прочитать JSON с `traceId`, не добавляя
компонент `traceId` в record.

### Задание 4. Generic-обёртка

Объявите `ApiError`, `BookingSummary` и `ApiEnvelope<T>`. Для первого ответа нужен
`TypeRef<ApiEnvelope<BookingSummary>>`, для второго —
`TypeRef<ApiEnvelope<List<BookingSummary>>>`.

Проверьте `status`, отсутствие ошибки, ID одного объекта, размер списка и ID его
элементов.

### Задание 5. Полиморфный ответ

Скопируйте не готовое решение, а структуру аннотаций из раздела про discriminator.
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

Блок пройден, если без копирования готового теста вы можете:

1. собрать GET, POST, PUT, PATCH и DELETE;
2. передать `pathParam`, `queryParam`, cookie и JSON-тело;
3. сохранить полный `Response`;
4. преобразовать один объект, обёртку и JSON-массив в DTO;
5. проверить статус и данные через AssertJ;
6. подтвердить сохранение или удаление контрольным GET;
7. создавать независимые данные и гарантировать их очистку;
8. самостоятельно построить простой, вложенный и списочный DTO по JSON;
9. различить пустую строку, явный `null` и отсутствующее поле;
10. выбрать между DTO, generic, полиморфизмом и `JsonNode`;
11. сериализовать и десериализовать список разных типов с discriminator;
12. объяснить назначение каждой строки собственного теста.

Печатный конспект остаётся дополнительной памяткой:
`docs/print/rest-assured-cheatsheet/rest-assured-cheatsheet.pdf`.
