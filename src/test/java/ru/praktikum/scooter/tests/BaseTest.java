package ru.praktikum.scooter.tests;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import ru.praktikum.scooter.util.DriverFactory;

public abstract class BaseTest {

    protected WebDriver driver;

    @Before
    public void setUp() {
        driver = DriverFactory.create();
        driver.manage().window().maximize();
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
