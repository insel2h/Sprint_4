package ru.praktikum.scooter.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class OrderPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public OrderPage(WebDriver driver) {
        this.driver = driver;
        // было 20 — на модалках и анимациях часто мало
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(40));
    }

    // ====== STEP 1 locators ======
    private final By firstNameInput = By.xpath("//input[@placeholder='* Имя']");
    private final By lastNameInput  = By.xpath("//input[@placeholder='* Фамилия']");
    private final By addressInput   = By.xpath("//input[@placeholder='* Адрес: куда привезти заказ']");

    private final By metroInput   = By.cssSelector("input.select-search__input");
    private final By metroOptions = By.cssSelector(".select-search__option");

    private final By phoneInput = By.xpath("//input[@placeholder='* Телефон: на него позвонит курьер']");
    private final By nextButton = By.xpath("//button[normalize-space(.)='Далее']");

    // cookies banner
    private final By cookieAcceptButton = By.xpath("//button[contains(.,'да все привыкли')]");

    // ====== STEP 2 locators ======
    private final By dateInput = By.xpath("//input[@placeholder='* Когда привезти самокат']");

    private final By rentDropdownControl = By.xpath("//div[contains(@class,'Dropdown-control')]");
    private final By rentDropdownOptions = By.cssSelector("div.Dropdown-option");

    private final By blackColor = By.id("black");
    private final By greyColor  = By.id("grey");
    private final By commentInput = By.xpath("//input[@placeholder='Комментарий для курьера']");

    // "Заказать" на шаге 2
    private final By orderButtonStep2 = By.xpath(
            "//div[contains(@class,'Order_Buttons')]//button[normalize-space(.)='Заказать']"
    );

    // ====== MODALS ======
    // корень модалки
    private final By modalRoot = By.cssSelector("div[class*='Order_Modal']");

    // кнопка Да в подтверждении
    private final By confirmYesButton = By.xpath(
            "//div[contains(@class,'Order_Modal')]//button[normalize-space(.)='Да']"
    );

    // успех: ищем текст "Заказ оформлен" внутри модалки
    private final By successModalText = By.xpath(
            "//div[contains(@class,'Order_Modal')]//*[contains(normalize-space(.),'Заказ оформлен')]"
    );

    // ====== Public actions ======

    public void fillStepOne(String firstName, String lastName, String address, String metroStation, String phone) {
        closeCookieIfVisible();

        type(firstNameInput, firstName);
        type(lastNameInput, lastName);
        type(addressInput, address);

        selectMetro(metroStation);

        type(phoneInput, phone);
        safeClick(nextButton);
    }

    public void fillStepTwo(String date, String rentPeriodText, String color, String comment) {
        closeCookieIfVisible();

        // дата
        safeClick(dateInput);
        type(dateInput, date);
        driver.findElement(dateInput).sendKeys(Keys.ENTER);
        closeCalendarPopup();

        // срок аренды
        selectRentPeriod(rentPeriodText);

        // цвет
        if (color != null) {
            String c = color.trim().toLowerCase();
            if (c.contains("black") || c.contains("черн")) {
                safeClick(blackColor);
            } else if (c.contains("grey") || c.contains("gray") || c.contains("сер")) {
                safeClick(greyColor);
            }
        }

        // комментарий
        if (comment != null) {
            type(commentInput, comment);
        }
    }

    /**
     * Самая надежная версия:
     * - клик "Заказать"
     * - дождались модалки
     * - клик "Да" через ретраи (click → Actions → JS)
     * - дождались, что "Да" исчезла / модалка перерисовалась
     * - дождались текста "Заказ оформлен"
     */
    public void submitOrder() {
        closeCookieIfVisible();

        // 1) "Заказать"
        safeClick(orderButtonStep2);

        // 2) дождаться модалки подтверждения
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalRoot));

        // 3) дождаться "Да" (лучше presence+visibility)
        wait.until(ExpectedConditions.presenceOfElementLocated(confirmYesButton));
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmYesButton));

        // 4) КЛИК "Да" (с ретраями)
        boolean clicked = clickWithRetries(confirmYesButton, 6);
        if (!clicked) {
            throw new TimeoutException("Не удалось кликнуть кнопку 'Да' в модалке подтверждения");
        }

        // 5) подтверждаем, что клик реально отработал:
        // либо кнопка "Да" исчезнет, либо модалка станет stale (перерисуется)
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.invisibilityOfElementLocated(confirmYesButton),
                    ExpectedConditions.stalenessOf(modal)
            ));
        } catch (TimeoutException ignored) {
            // иногда модалка не становится stale и "Да" может формально оставаться в DOM,
            // но клик всё равно сработал — тогда просто продолжаем и ждём успех.
        }

        // 6) ждём успех
        wait.until(ExpectedConditions.visibilityOfElementLocated(successModalText));
    }

    public boolean isOrderCreated() {
        return isVisible(successModalText);
    }

    public boolean isSuccessModalDisplayed() {
        return isOrderCreated();
    }

    // ====== Rent dropdown ======

    private void selectRentPeriod(String rentPeriodText) {
        if (rentPeriodText == null || rentPeriodText.trim().isEmpty()) return;

        safeClick(rentDropdownControl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(rentDropdownOptions));

        String target = rentPeriodText.trim().toLowerCase();

        List<WebElement> options = driver.findElements(rentDropdownOptions);
        for (WebElement opt : options) {
            try {
                if (!opt.isDisplayed()) continue;
                String txt = opt.getText();
                if (txt == null) continue;

                if (txt.trim().toLowerCase().equals(target)) {
                    scrollIntoView(opt);
                    try {
                        opt.click();
                    } catch (WebDriverException e) {
                        jsClick(opt);
                    }
                    return;
                }
            } catch (StaleElementReferenceException ignored) {}
        }

        options = driver.findElements(rentDropdownOptions);
        for (WebElement opt : options) {
            try {
                if (!opt.isDisplayed()) continue;
                String txt = opt.getText();
                if (txt == null) continue;

                if (txt.trim().toLowerCase().contains(target)) {
                    scrollIntoView(opt);
                    try {
                        opt.click();
                    } catch (WebDriverException e) {
                        jsClick(opt);
                    }
                    return;
                }
            } catch (StaleElementReferenceException ignored) {}
        }

        throw new TimeoutException("Не удалось выбрать срок аренды: " + rentPeriodText);
    }

    private void closeCalendarPopup() {
        try {
            driver.findElement(dateInput).sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {}

        try {
            ((JavascriptExecutor) driver).executeScript("document.body.click();");
        } catch (Exception ignored) {}
    }

    // ====== Metro ======

    public void selectMetro(String stationName) {
        closeCookieIfVisible();

        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(metroInput));
        scrollIntoView(input);
        jsClick(input);

        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(stationName);

        wait.until(anyOptionVisible());

        boolean clicked = clickMetroOptionByText(stationName);
        if (!clicked) {
            input.sendKeys(Keys.ARROW_DOWN);
            input.sendKeys(Keys.ENTER);
        }

        wait.until(d -> {
            String val = d.findElement(metroInput).getAttribute("value");
            return val != null && !val.trim().isEmpty();
        });
    }

    private ExpectedCondition<Boolean> anyOptionVisible() {
        return d -> {
            List<WebElement> opts = d.findElements(metroOptions);
            for (WebElement o : opts) {
                try {
                    if (o.isDisplayed() && o.getText() != null && !o.getText().trim().isEmpty()) {
                        return true;
                    }
                } catch (StaleElementReferenceException ignored) {}
            }
            return false;
        };
    }

    private boolean clickMetroOptionByText(String stationName) {
        String target = stationName.trim().toLowerCase();
        List<WebElement> opts = driver.findElements(metroOptions);

        for (WebElement opt : opts) {
            try {
                if (!opt.isDisplayed()) continue;
                String txt = opt.getText();
                if (txt == null) continue;

                if (txt.trim().toLowerCase().contains(target)) {
                    scrollIntoView(opt);
                    try {
                        opt.click();
                    } catch (WebDriverException e) {
                        jsClick(opt);
                    }
                    return true;
                }
            } catch (StaleElementReferenceException ignored) {}
        }
        return false;
    }

    // ====== Helpers ======

    private void closeCookieIfVisible() {
        List<WebElement> btns = driver.findElements(cookieAcceptButton);
        if (!btns.isEmpty()) {
            try {
                WebElement b = btns.get(0);
                if (b.isDisplayed()) {
                    jsClick(b);
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(cookieAcceptButton));
                }
            } catch (Exception ignored) {}
        }
    }

    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(el);
        el.clear();
        el.sendKeys(text);
    }

    private void safeClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollIntoView(el);
        try {
            el.click();
        } catch (WebDriverException e) {
            jsClick(el);
        }
    }

    private boolean isVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean clickWithRetries(By locator, int attempts) {
        for (int i = 0; i < attempts; i++) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                scrollIntoView(el);

                // try #1: clickable + normal click
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
                    return true;
                } catch (WebDriverException ignored) {}

                // try #2: Actions click
                try {
                    new Actions(driver)
                            .moveToElement(el)
                            .pause(Duration.ofMillis(120))
                            .click()
                            .perform();
                    return true;
                } catch (WebDriverException ignored) {}

                // try #3: JS click
                try {
                    jsClick(el);
                    return true;
                } catch (WebDriverException ignored) {}

            } catch (TimeoutException ignored) {}

            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    private void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    private void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", el
        );
    }
}
