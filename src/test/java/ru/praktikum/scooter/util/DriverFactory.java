package ru.praktikum.scooter.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Paths;


public class DriverFactory {

    public static WebDriver create() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");

        // 1) Пробуем взять путь к Chrome из параметра -DchromeBinary
        String chromeBinary = System.getProperty("chromeBinary");

        // 2) Если путь передали — проверяем, что файл существует, и применяем
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            if (!Files.exists(Paths.get(chromeBinary))) {
                throw new IllegalArgumentException("chromeBinary путь не существует: " + chromeBinary);
            }
            options.setBinary(chromeBinary);
        }

        if (headless) {
            options.addArguments("--headless=new");
        }

        return new ChromeDriver(options);
    }
}
