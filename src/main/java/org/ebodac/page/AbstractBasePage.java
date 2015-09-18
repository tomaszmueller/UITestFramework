package org.ebodac.page;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * A superclass for "real" pages. Has lots of handy methods for accessing
 * elements, clicking, filling fields. etc.
 */
public abstract class AbstractBasePage implements Page {

    public final String URL_ROOT;
    public static final int MAX_WAIT_SECONDS = 30;

    protected WebDriver driver;
    private final String serverURL="https://ebodac-pre-prod.soldevelo.com/module/server";
    protected WebDriverWait waiter;

    public AbstractBasePage(WebDriver driver) {
        this.driver = driver;
        URL_ROOT = "/module/" + StringUtils.substringAfterLast(serverURL, "/");
        waiter = new WebDriverWait(driver, MAX_WAIT_SECONDS);
    }

    @Override
    public void gotoPage(String address) {
        driver.get(serverURL + address);
    }

    public void go() {
        driver.get(StringUtils.removeEnd(serverURL, URL_ROOT) + expectedUrlPath());
    }

    @Override
    public WebElement findElement(By by) {
        waiter.until(ExpectedConditions.presenceOfElementLocated(by));
        return driver.findElement(by);
    }

    @Override
    public WebElement findElementById(String id) {
        return findElement(By.id(id));
    }

    @Override
    public String getText(By by) {
        return findElement(by).getText();
    }

    @Override
    public void setText(By by, String text) {
        setText(findElement(by), text);
    }

    @Override
    public void setText(String id, String text) {
        setText(findElement(By.id(id)), text);
    }

    @Override
    public void setTextToFieldNoEnter(By by, String text) {
        setTextNoEnter(findElement(by), text);
    }

    @Override
    public void setTextToFieldInsideSpan(String spanId, String text) {
        setText(findTextFieldInsideSpan(spanId), text);
    }

    private void setText(WebElement element, String text) {
        setTextNoEnter(element, text);
        element.sendKeys(Keys.RETURN);
    }

    private void setTextNoEnter(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    @Override
    public void clickOn(By by) {
        findElement(by).click();
    }

    @Override
    public void selectFrom(By by, String value){
        Select droplist = new Select(findElement(by));
        droplist.selectByVisibleText(value);
    }

    @Override
    public void hoverOn(By by) {
        Actions builder = new Actions(driver);
        Actions hover = builder.moveToElement(findElement(by));
        hover.perform();
    }

    private WebElement findTextFieldInsideSpan(String spanId) {
        return findElementById(spanId).findElement(By.tagName("input"));
    }

    @Override
    public String title() {
        return getText(By.tagName("title"));
    }

    @Override
    public String urlPath() {
        try {
            return new URL(driver.getCurrentUrl()).getPath();
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public List<WebElement> findElements(By by) {
        waiter.until(ExpectedConditions.presenceOfElementLocated(by));
        return driver.findElements(by);
    }

    /**
     * Real pages supply their expected URL path.
     *
     * @return The path portion of the url of the page.
     */
    @Override
    public abstract String expectedUrlPath();

    public void clickOnLinkFromHref(String href) throws InterruptedException{
        // We allow use of xpath here because href's tend to be quite stable.
        clickWhenVisible(byFromHref(href));
    }

    public By byFromHref(String href) {
        return By.xpath("//a[@href='" + href + "']");
    }

    public void waitForFocusById(final String id) {
        waiter.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return hasFocus(id);
            }
        });
    }

    public void waitForFocusByCss(final String tag, final String attr, final String value) {
        waiter.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return hasFocus(tag, attr, value);
            }
        });
    }

    public void waitForJsVariable(final String varName) {
        waiter.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return (Boolean) ((JavascriptExecutor)driver).executeScript("return " + varName, new Object[] {});
            }
        });
    }

    public void waitForElementToBeHidden(By by) {
        waiter.until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    public void waitForElementToBeEnabled(By by) {
        waiter.until(ExpectedConditions.elementToBeClickable(by));
    }

    boolean hasFocus(String id) {
        return (Boolean) ((JavascriptExecutor)driver).executeScript("return jQuery('#" + id +  "').is(':focus')", new Object[] {});
    }

    boolean hasFocus(String tag, String attr, String value) {
        return (Boolean) ((JavascriptExecutor)driver).executeScript("return jQuery('" + tag + "[" + attr + "=" + value + "]').is(':focus')", new Object[] {});
    }

    public void waitForElement(By by) {
        waiter.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitForTextToBePresentInElement(By by, String text) {
        waiter.until(ExpectedConditions.textToBePresentInElementLocated(by, text));
    }

    public Boolean containsText(String text) {
        return driver.getPageSource().contains(text);
    }

    public void clickWhenVisible(By by) throws InterruptedException {
        Long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < 10000) {
            try {
                clickOn(by);
                break;
            } catch (Exception e) {
                Thread.sleep(500);
            }
        }

    }

}
