package ru.praktikum.scooter.tests;

import org.junit.Assert;
import org.junit.Test;

public class SmokeTest extends BaseTest {

    @Test
    public void shouldOpenMainPage() {
        driver.get("https://qa-scooter.praktikum-services.ru/");
        Assert.assertTrue("Страница не открылась: title пустой", driver.getTitle() != null);
    }
}
