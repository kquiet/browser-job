package org.kquiet.job.crawler.test.shopee;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LaunchItem.
 *
 * @author monkey
 *
 */
public class LaunchItem extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(LaunchItem.class);

  public LaunchItem(JobConfig config) {
    super(config);
  }

  @Override
  public void run() {
    Runnable job = getJob();
    job.run();
  }

  private Runnable getJob() {
    String jobName = this.getJobName();

    return () -> {
      try {
        LOGGER.info("[{}] start!", jobName);
        LoginTaskContext loginTask =
            getLoginBrowserTask(getParameter("account"), getParameter("password"), true, true);
        LaunchItemTaskContext browserTask = getLaunchItemBrowserTask();
        loginTask.continueWith(browserTask);
        if (registerInternalBrowserTask(loginTask)) {
          browserTask.get();
        } else {
          throw new Exception("browser task not accepted!");
        }
      } catch (Exception ex) {
        LOGGER.error("[{}] fail!", jobName, ex);
      } finally {
        LOGGER.info("[{}] done!", jobName);
      }
    };
  }

  private LoginTaskContext getLoginBrowserTask(String account, String password,
      boolean openNewWindowFlag, boolean closeWindowFlag) {
    AtomicReference<LoginTaskContext> wrapper = new AtomicReference<>();
    try {
      wrapper
          .set(new ActionComposerBuilder().prepareActionSequence()
              .getUrl("https://seller.shopee.tw/portal/product/new")
              .waitUntil(
                  ExpectedConditions.or(
                      ExpectedConditions.visibilityOfElementLocated(
                          By.className("account-header-dropdown__name")),
                      ExpectedConditions.and(
                          ExpectedConditions.elementToBeClickable(
                              By.xpath("//*[contains(@class,'signin-form')]//"
                                  + "input[@placeholder='email/ 手機號碼/ 使用者名稱']")),
                          ExpectedConditions.elementToBeClickable(By.xpath(
                              "//*[contains(@class,'signin-form')]//input[@placeholder='密碼']")),
                          ExpectedConditions.elementToBeClickable(
                              By.xpath("//*[contains(@class,'signin-form')]//*[text()='登入']")))),
                  30000)
              .custom(ac -> {
                WebDriver driver = ac.getWebDriver();
                WebElement nicknameE =
                    driver.findElements(By.className("account-header-dropdown__name")).stream()
                        .findFirst().orElse(null);
                if (nicknameE != null && nicknameE.isDisplayed()) {
                  String nameOnPage = Optional.ofNullable(nicknameE.getText()).orElse("").trim();
                  if (!nameOnPage.isEmpty()) {
                    if (account.equals(nameOnPage)) {
                      LOGGER.info("Found same login acount on page");
                      ac.skipToSuccess();
                    } else {
                      ac.skipToFail();
                      wrapper.get()
                          .setLoginResultStatus(LoginTaskContext.LoginResultStatus.DifferentLogin);
                      LOGGER.info("Logged in by different account({}), logout now", nameOnPage);
                      new Actions(driver).moveToElement(nicknameE).build().perform();
                      try {
                        Thread.sleep(500);
                      } catch (Exception ex) {
                        // wait logout link to appear
                        LOGGER.warn("Waiting link-to-appear exception!", ex);
                      }
                      new Actions(driver).moveToElement(driver.findElement(
                          By.xpath("//*[contains(@class,'account-header-dropdown__menu-item')"
                              + " and text()='登出']")))
                          .click().build().perform();
                      try {
                        Thread.sleep(3000);
                      } catch (Exception ex) {
                        // wait for a moment to make sure previous click's script is processed
                        LOGGER.warn("Waiting logout exception!", ex);
                      }
                    }
                  }
                }
              })
              .prepareSendKey(By.xpath("//*[contains(@class,'signin-form')]//"
                  + "input[@placeholder='email/ 手機號碼/ 使用者名稱']"), account)
              .withClearBeforeSend().done()
              .prepareSendKey(
                  By.xpath("//*[contains(@class,'signin-form')]//input[@placeholder='密碼']"),
                  password)
              .withClearBeforeSend().done()
              .click(By.xpath("//*[contains(@class,'signin-form')]//*[text()='登入']"))
              .waitUntil(ExpectedConditions.or(
                  ExpectedConditions
                      .visibilityOfElementLocated(By.xpath("//input[@placeholder='驗證碼']")),
                  ExpectedConditions
                      .visibilityOfElementLocated(By.className("account-header-dropdown__name"))),
                  15000)
              .custom(ac -> {
                WebDriver driver = ac.getWebDriver();
                WebElement nicknameE =
                    driver.findElements(By.className("account-header-dropdown__name")).stream()
                        .findFirst().orElse(null);
                if (nicknameE != null && nicknameE.isDisplayed()) {
                  String nameOnPage = Optional.ofNullable(nicknameE.getText()).orElse("").trim();
                  if (account.equals(nameOnPage)) {
                    ac.skipToSuccess();
                    return;
                  }
                }

                WebElement smsBtnE = driver.findElements(By.xpath("//input[@placeholder='驗證碼']"))
                    .stream().findFirst().orElse(null);
                if (smsBtnE != null && smsBtnE.isDisplayed()) {
                  ac.skipToFail();
                  wrapper.get().setLoginResultStatus(LoginTaskContext.LoginResultStatus.WaitSms);
                  // TODO: alert
                  pauseInternalBrowser();
                  LOGGER.info("System paused due to awaiting sms");
                  return;
                }

                ac.skipToFail();
                wrapper.get().setLoginResultStatus(LoginTaskContext.LoginResultStatus.UnknownFail);
              }).returnToComposerBuilder().onFail(ac -> {
                if (wrapper.get().getContextMessage() == null
                    || wrapper.get().getContextMessage().isEmpty()) {
                  List<Exception> errList = ac.getErrors();
                  if (errList.size() > 0) {
                    wrapper.get().setContextMessage(errList.get(errList.size() - 1).getMessage());
                  }
                }

                // keep init status until end=> UnknownFail
                if (wrapper.get().getLoginResultStatus() == null
                    || LoginTaskContext.LoginResultStatus.WaitingToLogin
                        .equals(wrapper.get().getLoginResultStatus())) {
                  wrapper.get()
                      .setLoginResultStatus(LoginTaskContext.LoginResultStatus.UnknownFail);
                }
              }).onDone(ac -> {
                if (ac.isFail()) {
                  LOGGER.info("Login fail! {}", wrapper.get().getLoginResultStatus());


                  if (Arrays.asList(LoginTaskContext.LoginResultStatus.UnknownFail)
                      .contains(wrapper.get().getLoginResultStatus())
                      && (ac.getFailUrl() != null || ac.getFailPage() != null)) {
                    // TODO: alert
                  }

                  // try login again; if parent doesn't close window, then use it, or open a new
                  // window
                  LoginTaskContext nextLoginTask =
                      getLoginBrowserTask(account, password, closeWindowFlag, closeWindowFlag);
                  wrapper.get().continueWith(nextLoginTask);
                } else {
                  LOGGER.info("Login success!");
                }
              }).build(LoginTaskContext.class, "Login"));
    } catch (Exception ex) {
      LOGGER.error("Create LoginTaskContext error!", ex);
      return null;
    }

    wrapper.get().setOpenWindow(openNewWindowFlag).setCloseWindow(closeWindowFlag)
        .setPriority(Integer.MIN_VALUE);
    return wrapper.get();
  }

  private LaunchItemTaskContext getLaunchItemBrowserTask() {
    String taskName = String.format("%s", this.getJobName());
    AtomicReference<LaunchItemTaskContext> wrapper = new AtomicReference<>();
    try {
      wrapper.set(new ActionComposerBuilder().prepareActionSequence()
          .getUrl("https://seller.shopee.tw/portal/product/new")
          .waitUntil(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(
              By.xpath("//*[contains(@class,'shopee-dropzone')]/input[@type='file']"))), 30000)
          .custom(ac -> {
            // make upload element visible before interacting with it
            ((JavascriptExecutor) ac.getWebDriver())
                .executeScript("document.querySelectorAll(\"[class*='shopee-dropzone']"
                    + " input[type='file']\")[0].style.top='20%';");
          }).justWait(1000)
          .upload(By.xpath("//*[contains(@class,'shopee-dropzone')]/input[@type='file']"),
              "//PATH/TO/UPLOAD/image1.jpg", "//PATH/TO/UPLOAD/image2.jpg",
              "//PATH/TO/UPLOAD/image3.jpg")
          .waitUntil(ExpectedConditions.numberOfElementsToBe(
              By.xpath("//*[contains(concat(' ', @class, ' '), ' image-manager ')]//"
                  + "li[starts-with(@data-item,'data:image')]"),
              3), 30000)
          .prepareSendKey(By.xpath("//*[text()='商品名稱']/following-sibling::*//input"), "itemname")
          .withClearBeforeSend().done()
          .click(By.xpath("//*[@class='placeholder' and starts-with(text(),'商品描述與')]"))
          .waitUntil(ExpectedConditions.visibilityOfElementLocated(
              By.xpath("//*[text()='商品描述']/following-sibling::*//textarea")), 3000)
          .prepareSendKey(By.xpath("//*[text()='商品描述']/following-sibling::*//textarea"), "itemdesc")
          .withClearBeforeSend().done().click(By.xpath("//*[text()='選擇類別']"))
          .waitUntil(ExpectedConditions.elementToBeClickable(
              By.xpath("//*[contains(@class,'shopee-menu')]//*[text()='其他類別']")), 3000)
          .click(By.xpath("//*[contains(@class,'shopee-menu')]//*[text()='其他類別']"))
          .waitUntil(ExpectedConditions.elementToBeClickable(
              By.xpath("//*[contains(@class,'shopee-menu')]//*[text()='其他']")), 3000)
          .justWait(1000).click(By.xpath("//*[contains(@class,'shopee-menu')]//*[text()='其他']"))
          .waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//*[@placeholder='設定品牌']")),
              3000)
          .prepareSendKey(By.xpath("//*[@placeholder='設定品牌']"), "daigobang").withClearBeforeSend()
          .done().prepareSendKey(By.xpath("//*[text()='價格']/following-sibling::*//input"), "99999")
          .withClearBeforeSend().done()
          .click(By.xpath("//*[contains(concat(' ',@class,' '),"
              + " ' logistics-setting-panel__section--disabled ')]//*[@class='toggle-button']"))
          .waitUntil(ExpectedConditions.elementToBeClickable(
              By.xpath("//*[contains(text(),'運費選項')]/following-sibling::*[1]//input")), 3000)
          .prepareSendKey(By.xpath("//*[contains(text(),'運費選項')]/following-sibling::*[1]//input"),
              "500")
          .withClearBeforeSend().done().click(By.xpath("//*[text()='開啟此物流方式']"))
          .waitUntil(
              ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[text()='開啟此物流方式']")),
              3000)
          .click(By.xpath("//*[text()='儲存']"))
          .waitUntil(
              ExpectedConditions.or(ExpectedConditions
                  .visibilityOfElementLocated(By.xpath("//*[contains(@class, 'alert-box')]"))),
              30000)
          .custom(ac -> {
            WebElement alertE =
                ac.getWebDriver().findElements(By.xpath("//*[contains(@class, 'alert-box')]"))
                    .stream().findFirst().orElse(null);
            if (alertE != null && alertE.isDisplayed()) {
              String failStr = Optional.ofNullable(alertE.getText()).orElse("").trim();
              if (failStr.contains("請先移除賣場中既有的商品")) {
                ac.skipToFail();
                wrapper.get().setLaunchItemResultStatus(
                    LaunchItemTaskContext.LaunchItemResultStatus.ExceedLauchLimit);
                wrapper.get().setContextMessage(failStr);
              }
            }
          }).returnToComposerBuilder().actionPerformed(ac -> at -> {
            // catch action here
          }).build(LaunchItemTaskContext.class, taskName));
    } catch (Exception ex) {
      LOGGER.error("Create LaunchItemTaskContext error!", ex);
      return null;
    }
    wrapper.get().setOpenWindow(true).setCloseWindow(true).setPriority(103);
    return wrapper.get();
  }
}
