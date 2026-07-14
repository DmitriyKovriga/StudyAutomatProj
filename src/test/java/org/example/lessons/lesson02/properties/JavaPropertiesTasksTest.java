package org.example.lessons.lesson02.properties;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Урок 2 / Java System Properties.
 *
 * Теория: docs/lessons/lesson-02/java-properties.md
 */
class JavaPropertiesTasksTest {

    @Test
    @Disabled
    void task01_readPropertyWithDefault() {
        // TODO: Прочитайте property "lesson2.environment" со значением по умолчанию "local"
        //       и сохраните результат в environment для подготовленной проверки.
        String environment = null;

        assertEquals("local", environment);
    }

    @Test
    @Disabled
    void task02_setReadAndRestoreProperty() {
        String key = "lesson2.browser";
        String previousValue = System.getProperty(key);
        try {
            // TODO: Установите property key в "chrome", прочитайте её через System.getProperty
            //       и сохраните результат в actualBrowser для подготовленной проверки.
            String actualBrowser = null;

            assertEquals("chrome", actualBrowser);
        } finally {
            restoreProperty(key, previousValue);
        }
    }

    @Test
    @Disabled
    void task03_parseIntegerProperty() {
        String key = "lesson2.retries";
        String previousValue = System.getProperty(key);
        try {
            System.setProperty(key, "3");

            // TODO: Прочитайте property key, преобразуйте строку в int через Integer.parseInt
            //       и сохраните результат в retries для подготовленной проверки.
            int retries = 0;

            assertEquals(3, retries);
        } finally {
            restoreProperty(key, previousValue);
        }
    }

    @Test
    @Disabled
    void task04_parseBooleanProperty() {
        String key = "lesson2.headless";
        String previousValue = System.getProperty(key);
        try {
            System.setProperty(key, "false");

            // TODO: Прочитайте property key, преобразуйте строку через Boolean.parseBoolean
            //       и сохраните результат в headless для подготовленной проверки.
            boolean headless = true;

            assertFalse(headless);
        } finally {
            restoreProperty(key, previousValue);
        }
    }

    @Test
    @Disabled
    void task05_propertyAndAssumption() {
        String environment = System.getProperty("test.env", "local");

        // TODO: Через assumeTrue продолжите тест только для environment == "local"; запустите
        //       метод без -Dtest.env, затем с -Dtest.env=stage и сравните passed/skipped.

        assertEquals("local", environment);
    }

    private static void restoreProperty(String key, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previousValue);
        }
    }
}
