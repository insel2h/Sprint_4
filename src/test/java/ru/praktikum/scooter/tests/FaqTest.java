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
                {0, "Сутки — 400 рублей"},
                {1, "один заказ — один самокат"},
                {2, "Отсчёт времени аренды начинается"},
                {3, "Только начиная с завтрашнего дня"},
                {4, "можно позвонить в поддержку"},
                {5, "Самокат приезжает к вам с полной зарядкой"},
                {6, "Штрафа не будет"},
                {7, "И Москве, и Московской области"},
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
