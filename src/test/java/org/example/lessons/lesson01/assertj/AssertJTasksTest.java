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
    @Disabled
    void task01_basicFluentAssertions() {
        int actualPrice = 180;

        // Задание 1. Одной цепочкой проверьте, что цена положительная,
        // находится между 100 и 200 и равна 180.
        assertThat(actualPrice).isNotNull();
    }

    @Test
    @Disabled
    void task02_stringAssertions() {
        String orderNumber = "order-2026-00042";

        // Задание 2. Проверьте, что строка не пустая, начинается с order-, заканчивается на 00042
        //       и соответствует regex order-YYYY-NNNNN.
        assertThat(orderNumber).isNotNull();
    }

    @Test
    @Disabled
    void task03_collectionsAndOptional() {
        List<String> statuses = List.of("NEW", "PAID", "SHIPPED");
        Optional<User> user = Optional.of(new User("Ivan", 28, true));

        // Задание 3. Проверьте точный состав и порядок statuses, затем убедитесь, что user присутствует,
        //       и через get/extracting проверьте имя Ivan.
        assertThat(statuses).isNotNull();
        assertThat(user).isNotNull();
    }

    @Test
    @Disabled
    void task04_extractingProperties() {
        List<User> users = List.of(
                new User("Ivan", 28, true),
                new User("Anna", 31, false),
                new User("Petr", 22, true));

        // Задание 4. Через extracting(User::name) проверьте точный порядок имён.
        assertThat(users).isNotEmpty();
    }

    @Test
    @Disabled
    void task05_filteringAndTuples() {
        List<User> users = List.of(
                new User("Ivan", 28, true),
                new User("Anna", 31, false),
                new User("Petr", 22, true));

        // Задание 5. Оставьте только active-пользователей, одновременно извлеките name и age
        //       и проверьте tuple("Ivan", 28), tuple("Petr", 22).
        assertThat(users).isNotEmpty();
    }

    @Test
    @Disabled
    void task06_mapsAndNestedCollections() {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "X-Request-Id", "req-42");
        List<Order> orders = List.of(
                new Order(List.of("Book", "Pen")),
                new Order(List.of("Notebook")));

        // Задание 6. Проверьте, что headers содержит пару Content-Type/application-json и ключ X-Request-Id,
        //       но не содержит Authorization; через flatExtracting(Order::items) проверьте Book, Pen,
        //       Notebook в точном порядке.
        assertThat(headers).isNotEmpty();
        assertThat(orders).isNotEmpty();
    }

    @Test
    @Disabled
    void task07_softAssertions() {
        Product product = new Product("Keyboard", new BigDecimal("99.900"), true);

        // Задание 7. Через SoftAssertions.assertSoftly проверьте name == "Keyboard", числовое значение
        //       price == "99.90" через isEqualByComparingTo и available == true.
        assertThat(product).isNotNull();
    }

    @Test
    @Disabled
    void task08_exceptionAssertions() {
        UserService service = new UserService();

        // Задание 8. Через assertThatThrownBy вызовите service.findById(-1) и проверьте тип
        //       IllegalArgumentException, точное сообщение "id must be positive" и отсутствие cause.
        assertThat(service).isNotNull();
    }

    @Test
    @Disabled
    void task09_recursiveComparison() {
        Profile expected = new Profile(null, "Ivan", new Address("Moscow", "Tverskaya"), null);
        Profile actual = new Profile(100L, "Ivan", new Address("Moscow", "Tverskaya"),
                Instant.parse("2026-07-13T12:00:00Z"));

        // Задание 9. Выполните usingRecursiveComparison, игнорируя только id и createdAt.
        assertThat(actual).isNotNull();
    }

    @Test
    @Disabled
    void task10_conditionAndOtherAssertionStyles() {
        // Задание 10. Создайте Condition<Integer> с описанием "even number", проверьте им число 10,
        //       затем оставьте проверки значения "READY" через JUnit, Hamcrest и AssertJ
        //       и сравните сообщения этих трёх подходов при намеренном падении.
        Condition<Integer> even = null;
        assertThat(10).is(even);

        String actual = "READY";

        org.junit.jupiter.api.Assertions.assertEquals("READY", actual);
        org.hamcrest.MatcherAssert.assertThat(actual, org.hamcrest.Matchers.equalTo("READY"));
        assertThat(actual).isEqualTo("READY");

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
