package ru.praktikum.scooter.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class MainPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public static final String URL = "https://qa-scooter.praktikum-services.ru/";

    // --- Элементы страницы (список по заданию) ---

    // Кнопка "Заказать" в шапке (верхняя)
    private final By orderButtonTop = By.xpath("//button[contains(@class,'Button_Button') and text()='Заказать']");

    // Кнопка "Заказать" внизу страницы
    private final By orderButtonBottom = By.xpath("//div[contains(@class,'Home_FinishButton')]/button[text()='Заказать']");

    // Кнопка принятия cookies (может появляться)
    private final By cookieAcceptButton = By.id("rcc-confirm-button");

    // Вопрос FAQ по индексу (0..7): id accordion__heading-0
    private By faqQuestionByIndex(int index) {
        return By.id("accordion__heading-" + index);
    }

    // Ответ FAQ по индексу (0..7): id accordion__panel-0
    private By faqAnswerByIndex(int index) {
        return By.id("accordion__panel-" + index);
    }

    public MainPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public MainPage open() {
        driver.get(URL);
        return this;
    }

    public MainPage acceptCookiesIfPresent() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(cookieAcceptButton));
            driver.findElement(cookieAcceptButton).click();
        } catch (TimeoutException ignored) {
        }
        return this;
    }

    /** Открыть вопрос FAQ и вернуть текст ответа */
    public String openFaqAndGetAnswerText(int index) {
        WebElement question = wait.until(ExpectedConditions.presenceOfElementLocated(faqQuestionByIndex(index)));
        scrollTo(question);
        question.click();

        WebElement answer = wait.until(ExpectedConditions.visibilityOfElementLocated(faqAnswerByIndex(index)));
        return answer.getText().trim();
    }

    private void scrollTo(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element
        );
    }

    /** Нажать "Заказать" в шапке */
    public void clickOrderTop() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(orderButtonTop));
        btn.click();
    }

    /** Нажать "Заказать" внизу страницы */
    public void clickOrderBottom() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(orderButtonBottom));
        scrollTo(btn);
        wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
    }

}
