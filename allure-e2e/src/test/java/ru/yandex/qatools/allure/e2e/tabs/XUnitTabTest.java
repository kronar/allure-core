package ru.yandex.qatools.allure.e2e.tabs;

import com.google.common.base.Joiner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.yandex.qatools.allure.JSErrorsRule;
import ru.yandex.qatools.allure.Page;

import java.text.MessageFormat;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static ru.yandex.qatools.allure.Helpers.existsAndVisible;
import static ru.yandex.qatools.htmlelements.matchers.WrapsElementMatchers.exists;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.timeoutHasExpired;
import static ru.yandex.qatools.matchers.webdriver.TextMatcher.text;


public class XUnitTabTest {

    @Rule
    public JSErrorsRule jsErrorsRule = new JSErrorsRule();

    private Page page;

    WebDriver driver;

    @Before
    public void openBrowser() throws Exception {
        page = new Page(jsErrorsRule.driver());
        assertThat(page.tabContent(), should(existsAndVisible()).whileWaitingUntil(timeoutHasExpired(SECONDS.toMillis(3))));
        assertThat(page.xUnitTabContent().testsuiteAt(0), should(existsAndVisible()).whileWaitingUntil(timeoutHasExpired(SECONDS.toMillis(3))));
    }


    @Test
    public void shouldNotSeeAnOpenTestsuite() throws Exception {
        assertThat(page.xUnitTabContent().testsuite(), not(should(existsAndVisible()).whileWaitingUntil(timeoutHasExpired(SECONDS.toMillis(3)))));
    }

    @Test
    public void openTestsuiteTest() throws Exception {
        page.xUnitTabContent().testsuiteAt(2).click();

        assertThat(page.xUnitTabContent().testsuiteTitle(), should(exists()));
        assertThat(page.xUnitTabContent().testsuiteTitle(), text(not(containsString("{{testsuite.title}}"))));
    }
}
