# AssertJ и другие способы проверок: теория и 10 заданий

## 1. Почему AssertJ

AssertJ предоставляет fluent API: после `assertThat(actual)` IDE предлагает проверки, подходящие типу значения.

```java
assertThat(user.name())
    .as("Имя пользователя")
    .isNotBlank()
    .startsWith("Iv")
    .isEqualTo("Ivan");
```

Преимущества:

- читаемая цепочка;
- подробные сообщения об ошибках;
- сильная поддержка коллекций, исключений, Optional, дат и объектов;
- `extracting`, `filteredOn`, recursive comparison, soft assertions;
- расширение через `Condition` и custom assertions.

Статический импорт:

```java
import static org.assertj.core.api.Assertions.assertThat;
```

## 2. Описание проверки

```java
assertThat(actual)
    .as("Цена после скидки для заказа %s", orderId)
    .isEqualTo(expected);
```

`as`/`describedAs` ставится **до** assertion, который может упасть.

Для собственного сообщения:

```java
assertThat(actual)
    .withFailMessage("Ожидали %s, получили %s", expected, actual)
    .isEqualTo(expected);
```

Не добавляйте сообщение, которое лишь повторяет `isEqualTo`: AssertJ уже хорошо показывает expected/actual. Описание полезно для бизнес-контекста.

## 3. Строки и числа

```java
assertThat(text)
    .isNotBlank()
    .contains("order")
    .startsWith("new")
    .matches("[a-z-]+")
    .doesNotContain("error");

assertThat(price)
    .isPositive()
    .isBetween(100, 500)
    .isGreaterThanOrEqualTo(minPrice);
```

Для floating point не сравнивайте результат вычислений точным `isEqualTo`:

```java
assertThat(actual).isCloseTo(10.0, within(0.001));
```

Для денег предпочтительнее `BigDecimal` и осознанный выбор между `isEqualByComparingTo` и `isEqualTo` (scale имеет значение для `equals`).

## 4. Коллекции

```java
assertThat(names)
    .isNotEmpty()
    .hasSize(3)
    .contains("Ivan")
    .doesNotContain("Admin");
```

Разница:

- `contains` — элементы присутствуют, порядок и лишние элементы допускаются;
- `containsOnly` — только эти элементы, порядок не важен;
- `containsExactly` — точный состав и порядок;
- `containsExactlyInAnyOrder` — точный состав без учёта порядка;
- `containsOnlyOnce` — элемент встречается один раз;
- `allMatch`, `anyMatch`, `noneMatch` — предикаты.

Выбирайте наиболее строгую проверку, соответствующую контракту.

## 5. extracting, tuple и flatExtracting

```java
assertThat(users)
    .extracting(User::name)
    .containsExactly("Ivan", "Anna");

assertThat(users)
    .extracting(User::name, User::age)
    .containsExactly(
        tuple("Ivan", 28),
        tuple("Anna", 31)
    );
```

`flatExtracting` объединяет вложенные коллекции:

```java
assertThat(orders)
    .flatExtracting(Order::items)
    .extracting(Item::name)
    .contains("Book", "Pen");
```

Метод references безопаснее строковых имён полей: рефакторинг поддерживается компилятором.

## 6. filteredOn

```java
assertThat(users)
    .filteredOn(User::active)
    .extracting(User::name)
    .containsExactlyInAnyOrder("Ivan", "Anna");
```

`filteredOn` удобен, когда фильтрация является частью ожидаемого контракта. Если тест начинает повторять сложный production-алгоритм, сначала вычислите ожидаемый результат проще и сравните его целиком.

## 7. Map и Optional

```java
assertThat(headers)
    .containsEntry("Content-Type", "application/json")
    .containsKeys("X-Request-Id")
    .doesNotContainKey("Password");

assertThat(optionalUser)
    .isPresent()
    .get()
    .extracting(User::name)
    .isEqualTo("Ivan");
```

Не вызывайте `optional.get()` до проверки presence: иначе получите слабое диагностическое сообщение от `NoSuchElementException`.

## 8. Soft assertions

Обычная assertion останавливает тест на первой ошибке. Soft assertions собирают ошибки:

```java
SoftAssertions.assertSoftly(softly -> {
    softly.assertThat(user.name()).isEqualTo("Ivan");
    softly.assertThat(user.age()).isGreaterThan(18);
    softly.assertThat(user.active()).isTrue();
});
```

Используйте их для группы полей одного объекта/ответа. Не объединяйте soft assertions несвязанные сценарии — тест станет плохо диагностируемым.

Альтернатива — поле `@InjectSoftAssertions`, но для него нужен AssertJ extension и правильный lifecycle. Локальный `assertSoftly` проще.

## 9. Исключения

```java
assertThatThrownBy(() -> service.find(-1))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("id must be positive")
    .hasNoCause();

assertThatExceptionOfType(IOException.class)
    .isThrownBy(() -> client.read());

assertThatCode(() -> service.process())
    .doesNotThrowAnyException();
```

Не пишите ручной `try/catch` с флагом `exceptionThrown`.

## 10. Recursive comparison

```java
assertThat(actual)
    .usingRecursiveComparison()
    .ignoringFields("id", "createdAt")
    .isEqualTo(expected);
```

Полезные настройки:

- `ignoringFields`;
- `ignoringCollectionOrder`;
- `withComparatorForType`;
- `withEqualsForFields`;
- `ignoringActualNullFields` — используйте осторожно, она может скрыть дефект.

Recursive comparison удобен для DTO, но не должен бездумно проверять весь огромный объект. Изменение технического поля не должно ломать бизнес-тест, если поле не входит в контракт.

## 11. Condition и custom assertions

```java
Condition<Integer> even = new Condition<>(value -> value % 2 == 0, "even number");
assertThat(10).is(even);
```

Если доменная проверка часто повторяется, создайте custom assertion:

```java
class UserAssert extends AbstractAssert<UserAssert, User> {
    UserAssert(User actual) {
        super(actual, UserAssert.class);
    }

    static UserAssert assertThatUser(User actual) {
        return new UserAssert(actual);
    }

    UserAssert isActive() {
        isNotNull();
        if (!actual.active()) {
            failWithMessage("Expected user <%s> to be active", actual.name());
        }
        return this;
    }
}
```

Custom assertion оправдана, когда она выражает доменный язык, а не просто скрывает `isEqualTo`.

## 12. Другие способы делать проверки

### JUnit Assertions

```java
assertEquals(expected, actual);
assertAll(() -> assertTrue(a), () -> assertNotNull(b));
```

Плюсы: нет дополнительной библиотеки, понятный базовый набор. Минусы: меньше возможностей для коллекций/объектов, expected и actual легко перепутать.

### Hamcrest

```java
org.hamcrest.MatcherAssert.assertThat(actual, allOf(
    greaterThan(0),
    lessThan(100)
));
```

Matcher-based API широко используется в REST Assured. Хорош для декларативных inline-проверок JSON body. Для сложных объектов AssertJ обычно читается проще.

### REST Assured body assertions

```java
response.then()
    .statusCode(200)
    .body("firstname", equalTo("Ivan"));
```

Это Hamcrest matchers поверх JsonPath/GPath. Для большого числа связанных полей можно десериализовать DTO и перейти к AssertJ.

### Java `assert`

```java
assert value > 0;
```

Для автотестов почти не используется: проверки отключены без JVM-флага `-ea`, сообщения слабее, отчётность хуже.

### Mockito verify

```java
verify(repository).save(expectedUser);
```

Это interaction verification, а не обычное сравнение результата. Используйте только когда взаимодействие действительно является контрактом. Не проверяйте каждую внутреннюю операцию — тест станет привязан к реализации.

## 13. Типичные ошибки

- `assertThat(true).isTrue()` после вычислений, которые на самом деле не проверяются.
- Слишком слабый `contains`, когда контракт требует `containsExactly`.
- Извлечение `Optional.get()` до assertion.
- Сравнение double без tolerance.
- Soft assertions без финального `assertAll` (при ручном объекте `SoftAssertions`).
- Проверка `toString()` вместо полей/DTO.
- Огромный recursive comparison с десятками случайно игнорируемых полей.
- Проверка моков вместо результата, доступного пользователю.

## Задания

Все заготовки находятся в `AssertJTasksTest.java`.

1. Базовая fluent-цепочка и описание проверки.
2. Строковые assertions.
3. Строгие проверки коллекций и Optional.
4. `extracting` через method reference.
5. `filteredOn` и `tuple`.
6. Проверки Map и вложенных коллекций через `flatExtracting`.
7. Группа полей через soft assertions.
8. Проверка исключения и его сообщения.
9. Recursive comparison с игнорированием технических полей.
10. Собственный `Condition` и сравнение стилей JUnit/Hamcrest/AssertJ.
