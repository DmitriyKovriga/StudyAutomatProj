# Урок 2. Java System Properties в автотестах

## 1. Что такое property

System property — строковая настройка текущей JVM в формате `ключ → значение`.

Примеры настроек автотестов:

- `test.env=local` — окружение;
- `test.baseUrl=https://stage.example.org` — адрес приложения;
- `test.timeout.seconds=10` — timeout;
- `test.browser=chrome` — браузер.

Все значения хранятся как `String`. Это не поле Java-класса и не переменная окружения операционной системы.

## 2. Чтение property

```java
String environment = System.getProperty("test.env");
```

Если property не задана, результатом будет `null`.

Вариант со значением по умолчанию:

```java
String environment = System.getProperty("test.env", "local");
```

Теперь при отсутствии `test.env` переменная получит `local`.

## 3. Как передать property при запуске

Property передаётся JVM через параметр `-Dключ=значение`.

PowerShell и Maven Wrapper:

```powershell
.\mvnw.cmd "-Dtest.env=stage" test
```

Запуск одного учебного метода:

```powershell
.\mvnw.cmd "-Dtest.env=local" "-Dtest=JavaPropertiesTasksTest#task05_propertyAndAssumption" test
```

В IntelliJ IDEA откройте Run/Debug Configuration и добавьте в поле **VM options**:

```text
-Dtest.env=stage -Dtest.timeout.seconds=10
```

`-D` должен попасть именно в VM options. Если записать его как аргумент программы, `System.getProperty` его не увидит.

## 4. Установка и удаление внутри Java

```java
System.setProperty("test.env", "stage");
String actual = System.getProperty("test.env");
System.clearProperty("test.env");
```

System properties общие для всей JVM. Изменение в одном тесте может повлиять на другой тест, поэтому старое значение нужно восстановить:

```java
String previous = System.getProperty("test.env");
try {
    System.setProperty("test.env", "stage");
    // тест
} finally {
    if (previous == null) {
        System.clearProperty("test.env");
    } else {
        System.setProperty("test.env", previous);
    }
}
```

В реальном проекте такую очистку удобно вынести в `@AfterEach` или JUnit extension.

## 5. Property всегда является строкой

Число нужно преобразовать самостоятельно:

```java
int timeoutSeconds = Integer.parseInt(
        System.getProperty("test.timeout.seconds", "5"));
```

Boolean:

```java
boolean headless = Boolean.parseBoolean(
        System.getProperty("test.headless", "false"));
```

Неверное число вызовет `NumberFormatException`. В рабочей конфигурации лучше показать понятную ошибку с названием property.

## 6. System property и environment variable — не одно и то же

```java
System.getProperty("test.env"); // JVM: -Dtest.env=stage
System.getenv("TEST_ENV");      // переменная операционной системы
```

System properties удобно передавать в Maven и IntelliJ для конкретного запуска. Environment variables часто используются в CI и для секретов. Секреты нельзя хранить в репозитории или печатать в логах.

## 7. Property вместе с assumption

```java
String environment = System.getProperty("test.env", "local");

assumeTrue("local".equals(environment));

assertEquals("local", environment);
```

При `-Dtest.env=local` тест выполнится. При `-Dtest.env=stage` assumption остановит его со статусом skipped.

Assumption отвечает на вопрос: «этот тест применим в текущем окружении?». Она не должна заменять проверку бизнес-результата.

## 8. Практические правила

- Всегда решайте, допустимо ли значение по умолчанию.
- Не вызывайте методы у nullable-результата: безопаснее `"local".equals(environment)`.
- Преобразуйте строку в нужный тип в одном месте конфигурации.
- Восстанавливайте property, изменённую внутри теста.
- Не запускайте параллельно тесты, которые меняют один глобальный ключ.
- Не храните пароли и токены в исходном коде.

## Задания

Заготовки находятся в `JavaPropertiesTasksTest.java`.

1. Прочитать property со значением по умолчанию.
2. Установить property, прочитать её и восстановить старое значение.
3. Преобразовать числовую property в `int`.
4. Преобразовать boolean-property.
5. Совместить property окружения с JUnit assumption и попробовать два варианта запуска.
