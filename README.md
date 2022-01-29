## В build.gradle добавленна поддержка JUnit-Jupiter, Selenide и headless-режим, SLF4J-Simple, Lombok, JavaFaker, MySQL-connector-Java, Commons-DBUtils.
```gradle
plugins {
    id 'java'
}

group 'ru.netology'
version '1.0-SNAPSHOT'

sourceCompatibility = 11
compileJava.options.encoding = "UTF-8"
compileTestJava.options.encoding = "UTF-8"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.22'
    annotationProcessor 'org.projectlombok:lombok:1.18.22'

    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.2'
    testImplementation 'com.codeborne:selenide:6.2.0'
    testImplementation 'com.github.javafaker:javafaker:1.0.2'
    testImplementation 'org.slf4j:slf4j-simple:1.7.33'
    testImplementation 'mysql:mysql-connector-java:8.0.28'
    testImplementation 'commons-dbutils:commons-dbutils:1.7'

    testCompileOnly 'org.projectlombok:lombok:1.18.22'
    testAnnotationProcessor 'org.projectlombok:lombok:1.18.22'
}

test {
    useJUnitPlatform()
    //В тестах, при вызове `gradlew test -Dselenide.headless=true`
    //будет передаватся этот параметр в JVM (где его подтянет Selenide)
    systemProperty 'selenide.headless', System.getProperty('selenide.headless')
}
```
## Код Java для оптимизации авто-тестов.
```Java
package ru.netology.page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage {
  private final SelenideElement loginField = $("[data-test-id=login] input");
  private final SelenideElement passwordField = $("[data-test-id=password] input");
  private final SelenideElement loginButton = $("[data-test-id=action-login]");
  private final SelenideElement error = $("[data-test-id=error-notification]");

  public VerificationPage validLogin(String login, String password) {
    loginField.setValue(login);
    passwordField.setValue(password);
    loginButton.click();
    return new VerificationPage();
  }

  private void fieldClearing() {
    loginField.doubleClick();
    loginField.sendKeys(Keys.DELETE);
    passwordField.doubleClick();
    passwordField.sendKeys(Keys.DELETE);
  }

  public LoginPage InvalidPassword(String login, String password) {
    fieldClearing();
    loginField.setValue(login);
    passwordField.setValue(password);
    loginButton.click();
    error.shouldBe(Condition.visible);
    return new LoginPage();
  }
}
```
```Java
package ru.netology.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class VerificationPage {
  private final SelenideElement codeField = $("[data-test-id=code] input");
  private final SelenideElement verifyButton = $("[data-test-id=action-verify]");

  public VerificationPage() {
    codeField.shouldBe(visible);
  }

  public DashboardPage validVerify(String code) {
    codeField.setValue(code);
    verifyButton.click();
    return new DashboardPage();
  }
}
```
```Java
package ru.netology.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class DashboardPage {
  private final SelenideElement heading = $("[data-test-id=dashboard]");

  public DashboardPage() {
    heading.shouldBe(visible);
  }
}
```
```Java
package ru.netology.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class User {
    private String id;
    private String login;
    private String password;
    private String status;
}
```
## Авто-тесты находящиеся в этом репозитории.
```Java
package ru.netology;

import com.codeborne.selenide.Condition;
import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.jupiter.api.*;
import ru.netology.data.User;
import ru.netology.page.LoginPage;

import java.sql.DriverManager;

import static com.codeborne.selenide.Selenide.*;

public class AuthTest {

    @BeforeAll
    @SneakyThrows
    public static void clearingTablesForSUT() {
        var runner = new QueryRunner();
        var usersSQL = "SELECT * FROM users WHERE login = 'petya' OR login = 'vasya';";
        var delCardsSQL = "DELETE FROM cards WHERE user_id = ?;";
        var delUsersSQL = "DELETE FROM users WHERE id = ? OR id = ?;";

        try (var connection = DriverManager.getConnection(
                "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
            var allUsers = runner.query(connection, usersSQL, new BeanListHandler<>(User.class));
            if (allUsers.size() > 0) {                  //Проверка наличия в базе заданных пользователей
                runner.update(connection, delCardsSQL,
                        allUsers.get(1).getId());       //Передача id Васи, для удаления карт
                runner.update(connection, delUsersSQL,
                        allUsers.get(0).getId(),        //Передача id Пети, для удаления пользователя
                        allUsers.get(1).getId());       //Передача id Васи, для удаления пользователя
            }
        }
    }

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        var faker = new Faker();
        var runner = new QueryRunner();
        var dataSQL = "INSERT INTO users(id, login, password) VALUES (?, ?, ?);";

        try (var connection = DriverManager.getConnection(
                "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
            //Создание пользователя (обычная вставка в таблицу)
            runner.update(connection, dataSQL,
                    faker.internet().uuid(),                                    		//Универсальный id
                    faker.name().firstName(),                                           //Рандомный login
                    "$2a$10$zXMspIdjEHrK4W4iueC2QO8XFxadTn0dsoyD5A/qyroJUcWigWsaO");    //Пароль qwerty123
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Positive login scenario")
    public void authTestPositive() {
        var runner = new QueryRunner();
        var userSQL = "SELECT * FROM users;";
        var codeSQL = "SELECT code FROM auth_codes WHERE user_id = ?;";

        try (var connection = DriverManager.getConnection(
                "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
            var user = runner.query(connection, userSQL, new BeanHandler<>(User.class));
            open("http://localhost:9999");
            var loginPage = new LoginPage();
            var verificationPage = loginPage.validLogin(user.getLogin(), "qwerty123");
            String code = runner.query(connection, codeSQL, user.getId(), new ScalarHandler<>());
            verificationPage.validVerify(code);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Checking the blocking of the authorization system " +
            "when the password is entered incorrectly 3 times")
    public void authBlocked() {
        var faker = new Faker();
        var runner = new QueryRunner();
        var userSQL = "SELECT * FROM users;";

        try (var connection = DriverManager.getConnection(
                "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
            var user = runner.query(connection, userSQL, new BeanHandler<>(User.class));
            open("http://localhost:9999");
            var loginPage = new LoginPage();
            for (int cycle = 0; cycle < 3; cycle++) {
                loginPage.InvalidPassword(user.getLogin(), faker.internet().password());
            }
            $("[data-test-id=action-login]").shouldBe(Condition.disabled);
        }
    }

    @AfterEach
    @SneakyThrows
    public void deletingCreatedUser() {
        var runner = new QueryRunner();
        var usersSQL = "SELECT * FROM users;";
        var delUserSQL = "DELETE FROM users WHERE id = ?;";
        var delAuthCodeSQL = "DELETE FROM auth_codes WHERE user_id = ?;";

        try (var connection = DriverManager.getConnection(
                "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
            var user = runner.query(connection, usersSQL, new BeanHandler<>(User.class));
            if (user != null) {                         //Проверка наличия в базе сгенерированного пользователя
                runner.update(connection, delAuthCodeSQL,
                        user.getId());                  //Передача id пользователя для удаления кода верификации
                runner.update(connection, delUserSQL,
                        user.getId());                  //Передача id пользователя для удаления
            }
        }
    }
}
```