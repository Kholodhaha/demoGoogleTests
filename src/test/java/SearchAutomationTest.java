import static org.assertj.core.api.Assertions.assertThat;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class SearchAutomationTest {

    private static final Logger logger = LoggerFactory.getLogger(SearchAutomationTest.class);

    @BeforeEach
    public void setUp() {
        logger.info("Открываем Google");
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
        open("https://www.google.com");
    }

    @Test
    public void searchAndCheckGoogleResults() {
        performSearch();
        checkPagination();
        checkImagesTab();
        checkOtherElementsOnPage();
    }

    private void performSearch() {
        logger.info("Шаг 1: Ищем 'Selenide Java' через Google");
        SelenideElement searchField = $("[name='q']");
        searchField.setValue("Selenide Java").pressEnter();
        sleep(5000);

        ElementsCollection resultBlocks = $$("h3[class^='LC20lb']");
        resultBlocks.shouldHave(sizeGreaterThan(0));
        logger.info("Результаты найдены: {}", resultBlocks.size());

        ElementsCollection titles = $$("h3[class^='LC20lb']").filter(visible).first(7);

        for (int i = 0; i < titles.size(); i++) {
            SelenideElement title = titles.get(i);
            logger.info("Проверка заголовка #{}: {}", i + 1, title.getText());
            title.shouldHave(text("Selenide"));
        }
    }

    private void checkPagination() {
        logger.info("Шаг 2: Проверка пагинации на странице результатов Google");
        SelenideElement nextPageButton = $("[aria-label='Page 2']");
        nextPageButton.shouldBe(visible).click();

        ElementsCollection secondPageResults = $$("h3[class^='LC20lb']");
        secondPageResults.shouldHave(sizeGreaterThan(0));

        SelenideElement activePage = $("td.YyVfkd");
        activePage.shouldHave(text("2"));

        logger.info("Переход на вторую страницу успешно выполнен. Текущий номер страницы: {}", activePage.getText());

        for (int i = 0; i < secondPageResults.size(); i++) {
            SelenideElement title = secondPageResults.get(i);
            logger.info("Проверка заголовка на второй странице #{}: {}", i + 1, title.getText());
            title.shouldHave(text("Selenide"));
        }
    }

    private void checkImagesTab() {
        logger.info("Шаг 3: Переход на вкладку 'Картинки'");

        SelenideElement imagesTab = $$("a").findBy(text("Картинки"));
        imagesTab.shouldBe(visible).click();

        ElementsCollection images = $$("img[alt]").filter(visible);
        logger.info("Нашли {} видимых изображений", images.size());

        SelenideElement firstImage = $$("img[alt]").findBy(attributeMatching("alt", ".*Selenide.*"));
        firstImage.shouldBe(visible, Duration.ofSeconds(15)).click();

        try {
            SelenideElement previewImage = $x("//g-img[contains(@class,'tb08Pd')]/img[@alt[contains(.,'Selenide')]]");

            SelenideElement enlargedImage = $("img[src*='selenide'][aria-hidden='false']")
                    .shouldBe(visible, Duration.ofSeconds(15));

            int previewWidth = previewImage.getSize().getWidth();
            int previewHeight = previewImage.getSize().getHeight();

            int enlargedWidth = enlargedImage.getSize().getWidth();
            int enlargedHeight = enlargedImage.getSize().getHeight();

            logger.info("Размеры миниатюры: Ширина = {} px, Высота = {} px", previewWidth, previewHeight);
            logger.info("Размеры увеличенного изображения: Ширина = {} px, Высота = {} px", enlargedWidth, enlargedHeight);

            assertThat(enlargedWidth).isGreaterThan(previewWidth);
            assertThat(enlargedHeight).isGreaterThan(previewHeight);

            enlargedImage.shouldBe(visible, Duration.ofSeconds(15));

            logger.info("Открылась увеличенная версия изображения и её размеры больше миниатюры.");
        } catch (Exception e) {
            logger.error("Не удалось открыть увеличенную версию изображения: {}", e.getMessage());
        }
    }

    private void checkOtherElementsOnPage() {
        logger.info("Шаг 4: Проверка других элементов на странице результатов");
        SelenideElement searchField = $("[name='q']");
        searchField.shouldBe(visible);
        logger.info("Поле поиска на странице отображается.");

        SelenideElement searchButton = $("button[aria-label='Поиск']");
        searchButton.shouldBe(visible).click();
        logger.info("Кнопка 'Поиск в Google' на странице отображается.");

        SelenideElement settingsButton = $("[aria-label='Настройки']");
        settingsButton.shouldBe(visible).click();
        logger.info("Кнопка 'Настройки' и выпадающий список отображаются.");
    }

    @AfterEach
    public void tearDown() {
        logger.info("Закрываем браузер");
        closeWebDriver();
    }
}
