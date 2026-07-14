package org.example.lessons.lesson01.assertj;

import org.assertj.core.api.Condition;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;

/**
 * Урок 1 / AssertJ.
 *
 * Удаляйте @Disabled только у текущего задания. Теория: docs/lessons/lesson-01/assertj.md
 */
class AssertJTasksTest {

    @Test
    @Disabled("Задание 1: создайте fluent-проверку")
    void task01_basicFluentAssertions() {
        int actualPrice = 180;

        // TODO: одной цепочкой проверьте, что цена положительная, между 100 и 200 и равна 180.
        // Добавьте .as("Цена после скидки").
        assertThat(actualPrice).isNotNull(); // замените эту учебную проверку
    }

    @Test
    @Disabled("Задание 2: используйте строковые assertions")
    void task02_stringAssertions() {
        String orderNumber = "order-2026-00042";

        // TODO: проверьте, что строка не пустая, начинается с order-, заканчивается на 00042
        // и соответствует regex order-YYYY-NNNNN.
        assertThat(orderNumber).isNotNull(); // замените
    }

    @Test
    @Disabled("Задание 3: выберите строгие проверки коллекции и Optional")
    void task03_collectionsAndOptional() {
        List<String> statuses = List.of("NEW", "PAID", "SHIPPED");
        Optional<User> user = Optional.of(new User("Ivan", 28, true));

        // TODO: точный состав и порядок statuses.
        // TODO: user присутствует, затем через get/extracting проверьте имя Ivan.
        assertThat(statuses).isNotNull(); // замените
        assertThat(user).isNotNull();     // замените
    }

    @Test
    @Disabled("Задание 4: примените extracting")
    void task04_extractingProperties() {
        List<User> users = List.of(
                new User("Ivan", 28, true),
                new User("Anna", 31, false),
                new User("Petr", 22, true));

        // TODO: через extracting(User::name) проверьте точный порядок имён.
        assertThat(users).isNotEmpty(); // замените/расширьте
    }

    @Test
    @Disabled("Задание 5: используйте filteredOn и tuple")
    void task05_filteringAndTuples() {
        List<User> users = List.of(
                new User("Ivan", 28, true),
                new User("Anna", 31, false),
                new User("Petr", 22, true));

        // TODO: оставьте только active пользователей.
        // Извлеките одновременно name и age и проверьте tuple("Ivan", 28), tuple("Petr", 22).
        assertThat(users).isNotEmpty(); // замените
    }

    @Test
    @Disabled("Задание 6: проверьте Map и примените flatExtracting")
    void task06_mapsAndNestedCollections() {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "X-Request-Id", "req-42");
        List<Order> orders = List.of(
                new Order(List.of("Book", "Pen")),
                new Order(List.of("Notebook")));

        // TODO: Map содержит точную пару Content-Type/application-json и ключ X-Request-Id,
        // но не содержит ключ Authorization.
        // TODO: flatExtracting(Order::items) даёт Book, Pen, Notebook в точном порядке.
        assertThat(headers).isNotEmpty(); // замените/расширьте
        assertThat(orders).isNotEmpty();  // замените/расширьте
    }

    @Test
    @Disabled("Задание 7: соберите связанные ошибки через SoftAssertions")
    void task07_softAssertions() {
        Product product = new Product("Keyboard", new BigDecimal("99.90"), true);

        // TODO: используйте SoftAssertions.assertSoftly.
        // Проверьте name, price через isEqualByComparingTo и available == true.
        assertThat(product).isNotNull(); // замените
    }

    @Test
    @Disabled("Задание 8: проверьте исключение без try/catch")
    void task08_exceptionAssertions() {
        UserService service = new UserService();

        // TODO: assertThatThrownBy для service.findById(-1).
        // Тип IllegalArgumentException, точное message "id must be positive", cause отсутствует.
        assertThat(service).isNotNull(); // замените
    }

    @Test
    @Disabled("Задание 9: примените recursive comparison")
    void task09_recursiveComparison() {
        Profile expected = new Profile(null, "Ivan", new Address("Moscow", "Tverskaya"), null);
        Profile actual = new Profile(100L, "Ivan", new Address("Moscow", "Tverskaya"),
                Instant.parse("2026-07-13T12:00:00Z"));

        // TODO: usingRecursiveComparison, игнорируйте только id и createdAt.
        assertThat(actual).isNotNull(); // замените
    }

    @Test
    @Disabled("Задание 10: Condition и сравнение assertion styles")
    void task10_conditionAndOtherAssertionStyles() {
        // TODO: создайте Condition<Integer> с описанием "even number".
        Condition<Integer> even = null;
        assertThat(10).is(even);

        String actual = "READY";

        // Один контракт тремя способами. Оставьте все три проверки и сравните сообщения при падении.
        org.junit.jupiter.api.Assertions.assertEquals("READY", actual);
        org.hamcrest.MatcherAssert.assertThat(actual, org.hamcrest.Matchers.equalTo("READY"));
        assertThat(actual).isEqualTo("READY");

        // Java assert намеренно не используется: без JVM-флага -ea он будет отключён.
    }

    record User(String name, int age, boolean active) {
    }

    record Order(List<String> items) {
    }

    record Product(String name, BigDecimal price, boolean available) {
    }

    record Address(String city, String street) {
    }

    record Profile(Long id, String name, Address address, Instant createdAt) {
    }

    static final class UserService {
        User findById(int id) {
            if (id <= 0) {
                throw new IllegalArgumentException("id must be positive");
            }
            return new User("Ivan", 28, true);
        }
    }
}
