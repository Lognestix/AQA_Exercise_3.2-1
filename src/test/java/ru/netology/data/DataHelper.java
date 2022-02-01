package ru.netology.data;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.DriverManager;

public class DataHelper {
    //Пароль qwerty123:
    private static final String passBase =
            "$2a$10$zXMspIdjEHrK4W4iueC2QO8XFxadTn0dsoyD5A/qyroJUcWigWsaO";
    //Пароль qwerty123:
    private static final String passForm = "qwerty123";

    private DataHelper() {
    }

    private static String getId() {
        Faker faker = new Faker();
        //Универсальный id:
        return faker.internet().uuid();
    }

    private static String getLogin() {
        Faker faker = new Faker();
        //Рандомный логин:
        return faker.name().firstName();
    }

    public static String getValidPass() {
        return passForm;
    }

    public static String getInvalidPass() {
        Faker faker = new Faker();
        //Рандомный пароль:
        String pass = faker.internet().password();
        if (pass.equals(passForm)) {
            pass = faker.internet().password();
            return pass;
        }
        return pass;
    }

    public static class DataBase {

        private DataBase() {
        }

        @SneakyThrows
        private static void requestForSUT() {
            var runner = new QueryRunner();
            var usersSQL = "SELECT * FROM users WHERE login = 'petya' OR login = 'vasya';";
            var delCardsSQL = "DELETE FROM cards WHERE user_id = ?;";
            var delUsersSQL = "DELETE FROM users WHERE id = ? OR id = ?;";

            try (var connection = DriverManager.getConnection(
                    "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
                var allUsers = runner.query(connection,
                        usersSQL, new BeanListHandler<>(ru.netology.data.User.class));
                //Проверка наличия в базе заданных пользователей:
                if (allUsers.size() > 0) {
                    runner.update(connection, delCardsSQL,
                            //Передача id Васи, для удаления карт:
                            allUsers.get(1).getId());
                    runner.update(connection, delUsersSQL,
                            //Передача id Пети, для удаления пользователя:
                            allUsers.get(0).getId(),
                            //Передача id Васи, для удаления пользователя:
                            allUsers.get(1).getId());
                }
            }
        }

        public static void clearingTablesForSUT() {
            requestForSUT();
        }

        @SneakyThrows
        private static void requestCreateUser() {
            var runner = new QueryRunner();
            var dataSQL = "INSERT INTO users(id, login, password) VALUES (?, ?, ?);";

            try (var connection = DriverManager.getConnection(
                    "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
                //Создание пользователя (обычная вставка в таблицу)
                runner.update(connection, dataSQL,
                        //Универсальный id:
                        getId(),
                        //Рандомный login:
                        getLogin(),
                        //Пароль qwerty123:
                        passBase);
            }
        }

        public static void userGeneration() {
            requestCreateUser();
        }

        @SneakyThrows
        private static User requestUser() {
            var runner = new QueryRunner();
            var userSQL = "SELECT * FROM users;";

            try (var connection = DriverManager.getConnection(
                    "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
                return runner.query(connection, userSQL, new BeanHandler<>(User.class));
            }
        }

        public static User getUserInfo() {
            return requestUser();
        }

        @SneakyThrows
        private static String requestCode(User user) {
            var runner = new QueryRunner();
            var codeSQL = "SELECT code FROM auth_codes WHERE user_id = ?;";

            try (var connection = DriverManager.getConnection(
                    "jdbc:mysql://185.119.57.164:3306/base", "adm", "9mRE")) {
                return runner.query(connection, codeSQL, user.getId(), new ScalarHandler<>());
            }
        }

        public static String getCode(User user) {
            return requestCode(user);
        }

        @SneakyThrows
        private static void requestDeleteUser() {
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

        public static void deletingCreatedUser() {
            requestDeleteUser();
        }
    }
}