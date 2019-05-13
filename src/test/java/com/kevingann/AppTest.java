package com.kevingann;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public class AppTest {

  protected static final Logger LOG = LoggerFactory.getLogger(AppTest.class);

  private WebDriver webDriver;
  private WebDriverWait wait;

  @BeforeEach
  public void startWebDriver() throws MalformedURLException {
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.addArguments("start-maximized");
    chromeOptions.addArguments("disable-infobars");
    chromeOptions.addArguments("no-default-browser-check");
    chromeOptions.addArguments("disable-default-apps");
    setChromePreferences(chromeOptions);
    chromeOptions.setCapability(CapabilityType.PLATFORM_NAME, Platform.LINUX);
    chromeOptions.setCapability(
        CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
    chromeOptions.setCapability("screenResolution", "1280x1400x24");
    chromeOptions.setCapability("enableVNC", false);
    chromeOptions.setCapability("enableVideo", false);
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
    webDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), chromeOptions);

    wait = new WebDriverWait(webDriver, 30, 1000);
  }

  @AfterEach
  public void stopWebDriver() {
    webDriver.close();
    webDriver.quit();
  }

  private int computerMoves = 0;
  private int myMoves = 0;

  @Test
  public void shouldAnswerWithTrue() {
    webDriver.navigate().to("https://playtictactoe.org/");

    wait.until(ExpectedConditions.visibilityOf(webDriver.findElement(By.cssSelector(".board"))));
    Random random = new Random();

    wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".board .square"), 9));

    while (!isGameOver()) {
      wait.until(
          ExpectedConditions.numberOfElementsToBe(
              By.cssSelector(".board .square .o"), computerMoves));
      wait.until(
          ExpectedConditions.numberOfElementsToBe(By.cssSelector(".board .square .x"), myMoves));

      List<WebElement> currentSquares = webDriver.findElements(By.cssSelector(".board .square"));
      List<WebElement> availableSquares = getAvailableSquares(currentSquares);

      int availableSquareIndex = random.nextInt(availableSquares.size());
      WebElement randomSquare = availableSquares.get(availableSquareIndex);

      do {
        wait.until(ExpectedConditions.visibilityOf(randomSquare)).click();
      } while (randomSquare.findElements(By.cssSelector(".x")).isEmpty());

      wait.until(
          ExpectedConditions.numberOfElementsToBe(
              By.cssSelector(".board .square .x"), myMoves + 1));
      wait.until(
          ExpectedConditions.numberOfElementsToBe(
              By.cssSelector(".board .square .o"), computerMoves + 1));
      computerMoves++;
      myMoves++;

      if (isGameOver()) {
        LOG.info(
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".scores")))
                .getText());
      }
    }
  }

  private List<WebElement> getAvailableSquares(List<WebElement> squares) {
    List<WebElement> availableSquares = new ArrayList<>();
    for (WebElement square : squares) {
      if (square.findElements(By.cssSelector(".x")).isEmpty()
          && square.findElements(By.cssSelector(".o")).isEmpty()) {
        availableSquares.add(square);
      }
    }
    return availableSquares;
  }

  private boolean isGameOver() {
    wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".board .square"), 9));

    List<WebElement> scoreElements = webDriver.findElements(By.cssSelector(".score.appear"));
    WebElement restartDiv = webDriver.findElement(By.cssSelector(".restart"));

    if (!scoreElements.isEmpty() && restartDiv.isDisplayed()) {
      return true;
    }
    return false;
  }

  private void setChromePreferences(ChromeOptions chromeOptions) {
    Map<String, Object> prefs = new HashMap<>();

    // Don't prompt to save passwords
    prefs.put("credentials_enable_service", false);
    prefs.put("profile.password_manager_enabled", false);

    // Text on Toolbar instead of icons
    prefs.put("browser.chrome.toolbar_style", 1);

    chromeOptions.setExperimentalOption("prefs", prefs);
  }
}
