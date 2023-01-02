package org.kquiet.browserjob.crawler.obsolete.ruten;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
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
        BasicActionComposer browserTask = getLaunchItemBrowserTask();
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
      wrapper.set(new ActionComposerBuilder().prepareActionSequence()
          .getUrl("https://mybidu.ruten.com.tw/upload/item_initial.php")
          .waitUntil(ExpectedConditions.or(
              ExpectedConditions.visibilityOfElementLocated(By.id("header_user_nick")),
              ExpectedConditions.and(ExpectedConditions.elementToBeClickable(By.id("userid")),
                  ExpectedConditions.elementToBeClickable(By.id("userpass")),
                  ExpectedConditions.elementToBeClickable(By.id("btnLogin")))),
              15000)
          .custom(ac -> {
            WebDriver driver = ac.getWebDriver();
            WebElement nicknameE =
                driver.findElements(By.id("header_user_nick")).stream().findFirst().orElse(null);
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
                  new Actions(driver).moveToElement(nicknameE)
                      .moveToElement(driver.findElement(By.id("header_logout_link"))).click()
                      .build().perform();
                  try {
                    // wait for a moment to make sure previous click's script is processed
                    Thread.sleep(3000);
                  } catch (Exception ex) {
                    LOGGER.warn("Waiting logout exception!", ex);
                  }
                }
              }
            }
          }).prepareSendKey(By.id("userid"), account).withClearBeforeSend().done()
          .prepareSendKey(By.id("userpass"), password).withClearBeforeSend().done()
          .click(By.id("btnLogin"))
          .waitUntil(ExpectedConditions.or(
              ExpectedConditions.visibilityOfElementLocated(By.id("btn_send_sms")),
              ExpectedConditions.visibilityOfElementLocated(By.id("header_user_nick"))), 15000)
          .custom(ac -> {
            WebDriver driver = ac.getWebDriver();
            WebElement nicknameE =
                driver.findElements(By.id("header_user_nick")).stream().findFirst().orElse(null);
            if (nicknameE != null && nicknameE.isDisplayed()) {
              String nameOnPage = Optional.ofNullable(nicknameE.getText()).orElse("").trim();
              if (account.equals(nameOnPage)) {
                ac.skipToSuccess();
                return;
              }
            }

            WebElement smsBtnE =
                driver.findElements(By.id("btn_send_sms")).stream().findFirst().orElse(null);
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
              wrapper.get().setLoginResultStatus(LoginTaskContext.LoginResultStatus.UnknownFail);
            }
          }).onDone(ac -> {
            if (ac.isFail()) {
              LOGGER.info("Login fail! {}", wrapper.get().getLoginResultStatus());

              if (Arrays.asList(LoginTaskContext.LoginResultStatus.UnknownFail)
                  .contains(wrapper.get().getLoginResultStatus())
                  && (ac.getFailUrl() != null || ac.getFailPage() != null)) {
                // TODO: alert
              }

              // try login again; if parent doesn't close window, then use it, or open a new window
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

  private BasicActionComposer getLaunchItemBrowserTask() {
    String taskName = String.format("%s", this.getJobName());
    BasicActionComposer task =
        new ActionComposerBuilder().prepareActionSequence()
            .getUrl("https://mybidu.ruten.com.tw/upload/item_initial.php")
            .waitUntil(
                ExpectedConditions.and(
                    ExpectedConditions
                        .visibilityOfElementLocated(By.className("class-path-node-list")),
                    ExpectedConditions.presenceOfElementLocated(By.id("shop_id")),
                    ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[@data-ref='addLocalFakeButton']")),
                    ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//*[@data-ref='addLocalInput']"))),
                10000)
            .justWait(1000).custom(ac -> {
              WebDriver driver = ac.getWebDriver();
              // category
              ((JavascriptExecutor) driver)
                  .executeScript("document.getElementById('shop_id').value='0022000900030003';");
            })
            .upload(By.xpath("//*[@data-ref='addLocalInput']"),
                "D:\\work\\daigobang\\i-img431x500-1534817987mrfqju40027.jpg",
                "D:\\work\\daigobang\\i-img500x280-1534817987exbsh940027.jpg",
                "D:\\work\\daigobang\\i-img500x281-1534817987paizzo40027.jpg")
            .waitUntil(
                ExpectedConditions.numberOfElementsToBe(
                    By.xpath(
                        "//*[@class='thumbnail-viewer']/*[@class='thumbnail']/*[@class='img-wrap']/"
                            + "*[@class='img' and starts-with(@style,'background-image: url(')]"),
                    3),
                30000)
            .prepareSendKey(By.id("g_name"),
                "PS3用ソフト　METAL GEAR SOLID 4 GUNS OF THE PATRIOTS" + "　メタルギアソリッド4 ガンズ・オブ・ザ・パトリオット")
            .withClearBeforeSend().done().prepareSendKey(By.name("g_direct_price"), "99999")
            .withClearBeforeSend().done().prepareSendKey(By.id("show_num"), "1")
            .withClearBeforeSend().done().selectByText(By.id("location_tw"), "台北市")
            .click(By.id("cb_dway_self")).click(By.cssSelector("#main_form input[type='submit']"))
            .waitUntil(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#preview_confirm_form input[type='submit']")), 10000)
            .returnToComposerBuilder().actionPerformed(ac -> at -> {
              // catch action here
            }).buildBasic(taskName).setOpenWindow(true).setCloseWindow(true).setPriority(103);
    return task;
  }
}
