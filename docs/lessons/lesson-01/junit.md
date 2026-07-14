# JUnit 5: теория и 10 заданий

## 1. Из чего состоит JUnit 5

JUnit 5 — это не одна библиотека, а набор компонентов:

- **JUnit Platform** запускает тестовые движки и связывает их с Maven/IDE.
- **JUnit Jupiter API** содержит `@Test`, lifecycle-аннотации, assertions и extensions.
- **JUnit Jupiter Engine** находит и выполняет Jupiter-тесты.

В проекте Maven тесты обычно лежат в `src/test/java`. Maven Surefire находит классы с именами вроде `*Test`, `*Tests`, `Test*`.

## 2. Структура теста: Arrange — Act — Assert

```java
@Test
void discountIsAppliedForPremiumUser() {
    // Arrange
    PriceCalculator calculator = new PriceCalculator();

    // Act
    int result = calculator.calculate(100, true);

    // Assert
    assertEquals(80, result);
}
```

Один тест проверяет один сценарий поведения. Несколько assertions допустимы, если они описывают один результат.

## 3. Основные assertions

```java
assertEquals(expected, actual);
assertNotEquals(unexpected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(value);
assertNotNull(value);
assertSame(expectedReference, actualReference);
assertArrayEquals(expected, actual);
```

Сообщение передавайте лениво, когда его построение дорого:

```java
assertEquals(42, actual, () -> "Получено: " + actual);
```

`assertAll` выполняет все вложенные проверки и показывает сразу несколько ошибок:

```java
assertAll(
    () -> assertEquals("Ivan", user.name()),
    () -> assertTrue(user.active())
);
```

## 4. Жизненный цикл

- `@BeforeEach` — перед каждым тестом.
- `@AfterEach` — после каждого теста, в том числе упавшего.
- `@BeforeAll` — один раз перед классом.
- `@AfterAll` — один раз после класса.

По умолчанию JUnit создаёт новый экземпляр тестового класса для каждого теста. Поэтому нестатические `@BeforeAll` разрешены только с:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
```

Не храните изменяемое состояние между тестами без необходимости: это создаёт зависимость от порядка запуска.

## 5. Параметризованные тесты

```java
@ParameterizedTest
@ValueSource(strings = {"a@b.ru", "user@example.com"})
void validEmails(String email) {
    assertTrue(validator.isValid(email));
}
```

Источники:

- `@ValueSource` — один простой аргумент;
- `@NullSource`, `@EmptySource`, `@NullAndEmptySource`;
- `@CsvSource` — несколько аргументов в строке;
- `@EnumSource`;
- `@MethodSource` — объекты и сложные наборы данных;
- `@ArgumentsSource` — переиспользуемый собственный provider.

`@MethodSource` обычно возвращает `Stream<Arguments>`:

```java
static Stream<Arguments> prices() {
    return Stream.of(
        Arguments.of(100, 10, 90),
        Arguments.of(50, 0, 50)
    );
}
```

Как `@MethodSource` связывает provider и тест:

```java
static Stream<Arguments> orderCases() {
    return Stream.of(
        Arguments.of(new Order(List.of()), 0),
        Arguments.of(new Order(List.of(10, 20)), 30)
    );
}

@ParameterizedTest
@MethodSource("orderCases")
void orderTotal(Order order, int expectedTotal) {
    assertEquals(expectedTotal, order.total());
}
```

- `@MethodSource("orderCases")` находит метод `orderCases`;
- каждый `Arguments.of(...)` является одним запуском теста;
- первое значение передаётся в `Order order`, второе — в `int expectedTotal`;
- два набора `Arguments` означают два отдельных запуска `orderTotal`;
- пустой `Stream.empty()` является ошибкой конфигурации: JUnit нечего запускать.

## 6. Исключения и timeout

```java
IllegalArgumentException error = assertThrows(
    IllegalArgumentException.class,
    () -> service.findById(-1)
);
assertEquals("id must be positive", error.getMessage());
```

`assertThrowsExactly` требует точный тип. `assertDoesNotThrow` полезен, когда отсутствие исключения и есть контракт.

Проверка исключения — обычный негативный тест. Она нужна, когда система обязана отклонить неверные данные: скидку больше 100%, пустой обязательный параметр, неправильный пароль или операцию без прав. Если не проверить такой сценарий, тесты подтверждают только успешный путь и ничего не говорят о защите от некорректного ввода.

```java
assertTimeout(Duration.ofMillis(200), () -> service.calculate());
```

`assertTimeout` выполняется в текущем потоке. `assertTimeoutPreemptively` запускает код отдельно и прерывает его, но может ломать `ThreadLocal`, транзакции и контекст безопасности. Используйте preemptive-вариант осторожно.

## 7. Организация тестов

- `@DisplayName` даёт читаемое имя.
- `@Nested` группирует сценарии одного объекта.
- `@Tag("smoke")` позволяет выбирать наборы тестов.
- `@Disabled` временно отключает тест и обязательно должен иметь причину.
- `@TestMethodOrder` существует, но независимые тесты не должны полагаться на порядок.

```java
@Nested
@DisplayName("При пустой корзине")
class EmptyCart {
    @Test
    void totalIsZero() { }
}
```

## 8. Assumptions

Assumption не делает тест зелёным — она помечает его пропущенным, если окружение не подходит:

```java
assumeTrue("CI".equals(System.getenv("ENV")));
```

Это подходит для проверки ОС, переменной окружения или доступности внешней системы. Не используйте assumptions вместо бизнес-проверок.

## 9. Имена и теги тестов

`@DisplayName` делает сценарий понятным в отчёте, а `@Tag` позволяет запускать выбранные группы:

```java
@Test
@DisplayName("Скидка 25% применяется корректно")
@Tag("smoke")
void discountIsApplied() {
    int actual = calculator.priceAfterDiscount(200, 25);
    assertEquals(150, actual);
}
```

Часто используемые теги: `smoke`, `regression`, `api`, `ui`, `slow`. Тег должен описывать назначение набора, а не случайную деталь реализации.

## 10. Extensions

JUnit 5 использует extension model вместо runner/rule из JUnit 4. Основные callback-интерфейсы:

- `BeforeEachCallback`, `AfterEachCallback`;
- `BeforeAllCallback`, `AfterAllCallback`;
- `ParameterResolver`;
- `TestWatcher`;
- `ExecutionCondition`;
- `TestExecutionExceptionHandler`.

Подключение:

```java
@ExtendWith(TimingExtension.class)
class ServiceTest { }
```

Extensions подходят для логирования, выдачи тестовых объектов, скриншотов, очистки ресурсов. Бизнес-логику теста прятать в extension не следует.

## 11. Уровень middle+: практические принципы

- Изолируйте тестовые данные и не надейтесь на порядок.
- Проверяйте наблюдаемое поведение, а не приватную реализацию.
- Разделяйте fixture, действие и проверку.
- Не ловите исключение вручную, если есть `assertThrows`.
- Параметризуйте одинаковую логику, но не превращайте один тест в универсальный комбайн.
- Cleanup должен выполняться даже после падения assertion.
- Не используйте случайные данные без возможности воспроизвести seed/значение.
- Для внешних API отличайте product bug, нестабильность окружения и дефект теста.

## Задания

Все заготовки находятся в `JUnitTasksTest.java`.

1. Базовый Arrange–Act–Assert и несколько связанных проверок через `assertAll`.
2. Правильный lifecycle: новая корзина перед каждым тестом.
3. Параметризация простых значений через `@ValueSource`.
4. Таблица входов и результатов через `@CsvSource`.
5. Сложные аргументы через `@MethodSource`.
6. Проверка типа и сообщения исключения.
7. Проверка ограничения времени выполнения.
8. Группировка контекстов через `@Nested`.
9. Условный запуск через assumptions.
10. Читаемое имя теста и включение сценария в smoke-набор через `@DisplayName` и `@Tag`.

После выполнения вы должны уметь объяснить, почему выбран конкретный source, где должен находиться cleanup и чем `assertTimeout` отличается от `assertTimeoutPreemptively`.
