package org.example.lessons.lesson01.assertj;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * Урок 1 / дополнительная практика по AssertJ Soft Assertions.
 *
 * Теория и печатная шпаргалка: docs/print/soft-assertions-cheatsheet/soft-assertions-cheatsheet.pdf
 */
class SoftAssertionsTasksTest {

    @Test
    @Disabled
    void task01_manualLifecycle() {
        User user = new User("Ivan", 28, true);

        // Задание 1. Создайте объект SoftAssertions, через него проверьте name == "Ivan", age == 28
        //       и active == true, после всех проверок обязательно вызовите assertAll().
        notImplemented();
    }

    @Test
    @Disabled
    void task02_assertSoftlyLambda() {
        User user = new User("Anna", 31, false);

        // Задание 2. Через SoftAssertions.assertSoftly и параметр лямбды softly проверьте name == "Anna",
        //       age больше 18 и active == false; вручную создавать SoftAssertions
        //       и вызывать assertAll() в этом задании не нужно.
        notImplemented();
    }

    @Test
    @Disabled
    void task03_descriptionsAndSeveralFields() {
        Registration actual = new Registration("reg-42", "student@example.org", "CONFIRMED", 3);

        // Задание 3. Одним блоком assertSoftly проверьте непустой id и email с окончанием "@example.org",
        //       status == "CONFIRMED" и attempts между 1 и 3 включительно; каждой проверке
        //       добавьте понятное описание через as().
        notImplemented();
    }

    @Test
    @Disabled
    void task04_bigDecimalByNumericValue() {
        Product product = new Product("Keyboard", new BigDecimal("99.900"), 2);

        // Задание 4. Через assertSoftly проверьте name == "Keyboard" и числовое значение price == "99.90"
        //       методом isEqualByComparingTo и quantity == 2; проверка цены должна пройти
        //       несмотря на разный scale BigDecimal.
        notImplemented();
    }

    @Test
    @Disabled
    void task05_complexObjectContract() {
        Order actual = new Order(
                "order-100",
                new Customer("Petr", "petr@example.org"),
                List.of("Book", "Pen"),
                new BigDecimal("125.00"),
                true);

        // Задание 5. В одном assertSoftly проверьте id == "order-100" и customer.name == "Petr",
        //       корректный customer.email, точный порядок items Book/Pen, числовое значение
        //       total == "125.0" и paid == true, используя отдельный softly.assertThat(...)
        //       для каждого самостоятельного свойства.
        notImplemented();
    }

    private static void notImplemented() {
        throw new AssertionError("Замените notImplemented() реализацией задания");
    }

    record User(String name, int age, boolean active) {
    }

    record Registration(String id, String email, String status, int attempts) {
    }

    record Product(String name, BigDecimal price, int quantity) {
    }

    record Customer(String name, String email) {
    }

    record Order(String id, Customer customer, List<String> items, BigDecimal total, boolean paid) {
    }
}
