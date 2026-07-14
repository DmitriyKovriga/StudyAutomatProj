package org.example.lessons.lesson01.junit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Урок 1 / JUnit 5.
 *
 * Удаляйте @Disabled только у текущего задания. Теория: docs/lessons/lesson-01/junit.md
 */
class JUnitTasksTest {

    private final PriceCalculator calculator = new PriceCalculator();
    private ShoppingCart cart;

    // TODO task02: добавьте @BeforeEach и создавайте здесь новую ShoppingCart.
    // Один и тот же изменяемый экземпляр между тестами использовать нельзя.

    @Test
    @Disabled("Задание 1: реализуйте участок TODO и включите тест")
    void task01_basicAssertionsAndAssertAll() {
        // Arrange
        User user = new User("Ivan", 28, true);

        // TODO: получите имя, возраст и активность пользователя.
        String actualName = null;
        int actualAge = 0;
        boolean actualActive = false;

        assertAll("Поля пользователя",
                () -> assertEquals("Ivan", actualName),
                () -> assertEquals(28, actualAge),
                () -> assertTrue(actualActive));
    }

    @Test
    @Disabled("Задание 2: настройте @BeforeEach и включите оба теста task02")
    void task02_cartStartsEmpty() {
        assertNotNull(cart, "Корзина должна создаваться в @BeforeEach");
        assertTrue(cart.items().isEmpty());
    }

    @Test
    @Disabled("Задание 2: настройте @BeforeEach и включите оба теста task02")
    void task02_cartStateDoesNotLeakBetweenTests() {
        assertNotNull(cart, "Корзина должна создаваться в @BeforeEach");
        cart.add("book");
        assertEquals(List.of("book"), cart.items());
    }

    @ParameterizedTest(name = "{0} — корректный положительный id")
    @ValueSource(ints = {1, 2, 10, 999})
    @Disabled("Задание 3: заполните TODO и включите тест")
    void task03_valueSource(int id) {
        // TODO: вычислите условие, что id положительный.
        boolean valid = false;

        assertTrue(valid, () -> "id должен быть положительным: " + id);
    }

    @ParameterizedTest(name = "price={0}, discount={1}%, expected={2}")
    @CsvSource({
            "100, 10, 90",
            "250, 20, 200",
            "99, 0, 99",
            "80, 100, 0"
    })
    @Disabled("Задание 4: вызовите calculator и включите тест")
    void task04_csvSource(int price, int discountPercent, int expected) {
        // TODO: вызовите calculator.priceAfterDiscount(...).
        int actual = -1;

        assertEquals(expected, actual);
    }

    static Stream<Arguments> orderCases() {
        // TODO task05: верните минимум три набора Arguments.of(Order, expectedTotal).
        // Обязательно добавьте заказ с несколькими позициями и пустой заказ.
        return Stream.empty();
    }

    @ParameterizedTest(name = "order #{index} -> {1}")
    @MethodSource("orderCases")
    @Disabled("Задание 5: реализуйте orderCases и включите тест")
    void task05_methodSource(Order order, int expectedTotal) {
        assertEquals(expectedTotal, order.total());
    }

    @Test
    @Disabled("Задание 6: используйте assertThrows и включите тест")
    void task06_exceptionTypeAndMessage() {
        // TODO: через assertThrows получите исключение от calculator.priceAfterDiscount(100, 101).
        IllegalArgumentException error = null;

        assertAll(
                () -> assertNotNull(error),
                () -> assertEquals("discount must be between 0 and 100", error.getMessage()));
    }

    @Test
    @Timeout(1)
    @Disabled("Задание 7: используйте assertTimeout и включите тест")
    void task07_timeout() {
        // TODO: оберните calculator.slowOperation() в assertTimeout с лимитом 300 мс.
        // @Timeout(1) сверху является дополнительной защитой от зависшего задания.
        fail("Замените fail на assertTimeout");
    }

    @Nested
    class Task08NestedContexts {

        @Test
        @Disabled("Задание 8: проверьте пустой заказ")
        void emptyOrderHasZeroTotal() {
            Order emptyOrder = new Order(List.of());
            // TODO: получите total.
            int total = -1;
            assertEquals(0, total);
        }

        @Test
        @Disabled("Задание 8: проверьте заказ из нескольких позиций")
        void filledOrderSumsAllItems() {
            Order order = new Order(List.of(10, 20, 35));
            // TODO: получите total.
            int total = -1;
            assertEquals(65, total);
        }
    }

    @Test
    @Disabled("Задание 9: добавьте корректную assumption и включите тест")
    void task09_assumptions() {
        String environment = System.getProperty("test.env", "local");

        // TODO: продолжайте тест только для environment == "local".
        // Используйте assumeTrue, а не assertTrue.

        assertEquals("local", environment);
    }

    @TestFactory
    @Disabled("Задание 10: создайте DynamicTest для каждого значения")
    Stream<DynamicTest> task10_dynamicTests() {
        List<Integer> values = List.of(-2, -1, 0, 1, 2);

        // TODO: преобразуйте values в DynamicTest с понятным именем.
        // Каждый динамический тест должен проверять: Math.abs(value) >= 0.
        return Stream.empty();
    }

    record User(String name, int age, boolean active) {
    }

    record Order(List<Integer> itemPrices) {
        int total() {
            return itemPrices.stream().mapToInt(Integer::intValue).sum();
        }
    }

    static final class ShoppingCart {
        private final List<String> items = new ArrayList<>();

        void add(String item) {
            items.add(item);
        }

        List<String> items() {
            return List.copyOf(items);
        }
    }

    static final class PriceCalculator {
        int priceAfterDiscount(int price, int discountPercent) {
            if (discountPercent < 0 || discountPercent > 100) {
                throw new IllegalArgumentException("discount must be between 0 and 100");
            }
            return price * (100 - discountPercent) / 100;
        }

        String slowOperation() throws InterruptedException {
            Thread.sleep(50);
            return "done";
        }
    }
}
