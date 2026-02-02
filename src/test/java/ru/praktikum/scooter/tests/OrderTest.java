package ru.praktikum.scooter.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.praktikum.scooter.pages.MainPage;
import ru.praktikum.scooter.pages.OrderPage;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OrderTest extends BaseTest {

    private final boolean useTopButton;

    private final String firstName;
    private final String lastName;
    private final String address;
    private final String metro;
    private final String phone;

    private final String date;
    private final String rentPeriodText;
    private final String color;
    private final String comment;

    public OrderTest(boolean useTopButton,
                     String firstName, String lastName, String address, String metro, String phone,
                     String date, String rentPeriodText, String color, String comment) {
        this.useTopButton = useTopButton;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metro = metro;
        this.phone = phone;
        this.date = date;
        this.rentPeriodText = rentPeriodText;
        this.color = color;
        this.comment = comment;
    }

    @Parameterized.Parameters(name = "order: topButton={0}, {1} {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true,  "Иван", "Петров", "Тверская 1", "Тверская", "+79990000001", "10.02.2026", "сутки", "black", "Позвоните за 10 минут"},
                {false, "Владислав", "Титов", "Тверская 1", "Тверская", "+79990000001", "10.02.2026", "сутки", "black", "Позвоните за 10 минут"},

                {true,  "Анна", "Сидорова", "Арбат 12", "Арбатская", "+79990000002", "12.02.2026", "двое суток", "grey", "Оставьте у консьержа"},
                {false, "Анна", "Сидорова", "Арбат 12", "Арбатская", "+79990000002", "12.02.2026", "двое суток", "grey", "Оставьте у консьержа"},
        });
    }

    @Test
    public void shouldCreateOrder() {
        MainPage main = new MainPage(driver)
                .open()
                .acceptCookiesIfPresent();

        if (useTopButton) {
            main.clickOrderTop();
        } else {
            main.clickOrderBottom();
        }

        OrderPage order = new OrderPage(driver);

        order.fillStepOne(firstName, lastName, address, metro, phone);
        order.fillStepTwo(date, rentPeriodText, color, comment);
        order.submitOrder();

        Assert.assertTrue("Не появилось окно успешного оформления заказа", order.isOrderCreated());
    }
}
