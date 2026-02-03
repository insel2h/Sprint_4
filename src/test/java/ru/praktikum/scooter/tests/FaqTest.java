package ru.praktikum.scooter.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.praktikum.scooter.pages.MainPage;

import java.util.Arrays;
import java.util.Collection;

/**
 * FAQ ("Вопросы о важном"):
 * при клике на вопрос открывается соответствующий текст.
 *
 * Параметризация: один тест, много наборов (индекс + ключевая часть ответа).
 */
@RunWith(Parameterized.class)
public class FaqTest extends BaseTest {

    private final int questionIndex;
    private final String expectedPart;

    public FaqTest(int questionIndex, String expectedPart) {
        this.questionIndex = questionIndex;
        this.expectedPart = expectedPart;
    }

    @Parameterized.Parameters(name = "FAQ[{0}]")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0, "Сутки — 400 рублей. Оплата курьеру — наличными или картой."},
                {1, "Пока что у нас так: один заказ — один самокат. Если хотите покататься с друзьями, можете просто сделать несколько заказов — один за другим."},
                {2, "Допустим, вы оформляете заказ на 8 мая. Мы привозим самокат 8 мая в течение дня. Отсчёт времени аренды начинается с момента, когда вы оплатите заказ курьеру. Если мы привезли самокат 8 мая в 20:30, суточная аренда закончится 9 мая в 20:30."},
                {3, "Только начиная с завтрашнего дня. Но скоро станем расторопнее."},
                {4, "Пока что нет! Но если что-то срочное — всегда можно позвонить в поддержку по красивому номеру 1010."},
                {5, "Самокат приезжает к вам с полной зарядкой. Этого хватает на восемь суток — даже если будете кататься без передышек и во сне. Зарядка не понадобится."},
                {6, "Да, пока самокат не привезли. Штрафа не будет, объяснительной записки тоже не попросим. Все же свои."},
                {7, "Да, обязательно. Всем самокатов! И Москве, и Московской области."},
        });
    }

    @Test
    public void shouldOpenCorrectFaqAnswer() {
        MainPage main = new MainPage(driver)
                .open()
                .acceptCookiesIfPresent();

        String actual = main.openFaqAndGetAnswerText(questionIndex);

        Assert.assertFalse("Ответ в FAQ пустой — возможно, не открылся", actual.isEmpty());
        Assert.assertTrue(
                "Ответ не содержит ожидаемую часть текста.\nОжидали фрагмент: " + expectedPart + "\nФактически: " + actual,
                actual.contains(expectedPart)
        );
    }
}
