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