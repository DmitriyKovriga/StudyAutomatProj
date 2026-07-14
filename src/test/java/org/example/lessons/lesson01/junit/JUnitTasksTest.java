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

    // TODO task02: Добавьте @BeforeEach и перед каждым тестом создавайте новую ShoppingCart
    //              в поле cart, чтобы изменяемое состояние не переходило между тестами.

    @Test
    @Disabled
    void task01_basicAssertionsAndAssertAll() {
        User user = new User("Ivan", 28, true);

        // TODO: Получите из user фактические имя, возраст и активность так, чтобы подготовленный
        //       assertAll успешно проверил все три значения.
        String actualName = null;
        int actualAge = 0;
        boolean actualActive = false;

        assertAll("Поля пользователя",
                () -> assertEquals("Ivan", actualName),
                () -> assertEquals(28, actualAge),
                () -> assertTrue(actualActive));
    }

    @Test
    @Disabled
    void task02_cartStartsEmpty() {
        assertNotNull(cart, "Корзина должна создаваться в @BeforeEach");
        assertTrue(cart.items().isEmpty());
    }

    @Test
    @Disabled
    void task02_cartStateDoesNotLeakBetweenTests() {
        assertNotNull(cart, "Корзина должна создаваться в @BeforeEach");
        cart.add("book");
        assertEquals(List.of("book"), cart.items());
    }

    @ParameterizedTest(name = "{0} — корректный положительный id")
    @ValueSource(ints = {1, 2, 10, 999})
    @Disabled
    void task03_valueSource(int id) {
        // TODO: Вычислите boolean-условие, подтверждающее, что параметр id положительный,
        //       для всех значений из @ValueSource.
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
    @Disabled
    void task04_csvSource(int price, int discountPercent, int expected) {
        // TODO: Вызовите calculator.priceAfterDiscount с параметрами price и discountPercent
        //       и сохраните результат в actual для подготовленного сравнения с expected.
        int actual = -1;

        assertEquals(expected, actual);
    }

    static Stream<Arguments> orderCases() {
        // TODO task05: Верните минимум три набора Arguments.of(Order, expectedTotal),
        //              обязательно включив заказ с несколькими позициями и пустой заказ.
        return Stream.empty();
    }

    @ParameterizedTest(name = "order #{index} -> {1}")
    @MethodSource("orderCases")
    @Disabled
    void task05_methodSource(Order order, int expectedTotal) {
        assertEquals(expectedTotal, order.total());
    }

    @Test
    @Disabled
    void task06_exceptionTypeAndMessage() {
        // TODO: Через assertThrows вызовите calculator.priceAfterDiscount(100, 101) и сохраните
        //       полученное исключение в error для подготовленных проверок типа и сообщения.
        IllegalArgumentException error = null;

        assertAll(
                () -> assertNotNull(error),
                () -> assertEquals("discount must be between 0 and 100", error.getMessage()));
    }

    @Test
    @Timeout(1)
    @Disabled
    void task07_timeout() {
        // TODO: Замените fail вызовом assertTimeout, внутри которого выполняется
        //       calculator.slowOperation(), установив лимит 300 мс; аннотацию @Timeout(1)
        //       оставьте как защиту от зависания.
        fail("Замените fail на assertTimeout");
    }

    @Nested
    class Task08NestedContexts {

        @Test
        @Disabled
        void emptyOrderHasZeroTotal() {
            Order emptyOrder = new Order(List.of());
            // TODO: Получите total пустого заказа и сохраните результат в переменную total
            //       для подготовленной проверки нулевой суммы.
            int total = -1;
            assertEquals(0, total);
        }

        @Test
        @Disabled
        void filledOrderSumsAllItems() {
            Order order = new Order(List.of(10, 20, 35));
            // TODO: Получите total заказа из нескольких позиций и сохраните результат
            //       в переменную total для подготовленной проверки суммы 65.
            int total = -1;
            assertEquals(65, total);
        }
    }

    @Test
    @Disabled
    void task09_assumptions() {
        String environment = System.getProperty("test.env", "local");

        // TODO: Через assumeTrue разрешите продолжение теста только при environment == "local",
        //       не заменяя assumption обычным assertion.

        assertEquals("local", environment);
    }

    @TestFactory
    @Disabled
    Stream<DynamicTest> task10_dynamicTests() {
        List<Integer> values = List.of(-2, -1, 0, 1, 2);

        // TODO: Преобразуйте каждый элемент values в DynamicTest с понятным именем
        //       и проверкой Math.abs(value) >= 0, вернув итоговый Stream<DynamicTest>.
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
