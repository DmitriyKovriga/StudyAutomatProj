package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Epic;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Project setup")
public class EnvironmentSmokeTest {

    private static final Logger log = LogManager.getLogger(EnvironmentSmokeTest.class);

    @Test
    public void dependenciesAreReady() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        TestUser user = objectMapper.readValue("{\"name\":\"Automation\"}", TestUser.class);

        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:smoke");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(1);
        }

        assertThat(user.getName()).isEqualTo("Automation");
        assertThat(Getter.class).isNotNull();
        assertThat(WebDriver.class).isNotNull();
        assertThat(WebDriverManager.class).isNotNull();
        log.info("Test automation dependencies are ready");
    }

    private static class TestUser {
        private String name;

        private TestUser() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
