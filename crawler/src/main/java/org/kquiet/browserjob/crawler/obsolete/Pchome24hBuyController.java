package org.kquiet.browserjob.crawler.obsolete;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.browserjob.crawler.CrawlerBeanConfiguration;
import org.kquiet.browserjob.crawler.CrawlerService;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pchome24hBuyController.
 *
 * @author monkey
 *
 */
public class Pchome24hBuyController extends JobBase {
  private static final Logger logger = LoggerFactory.getLogger(Pchome24hBuyController.class);

  public Pchome24hBuyController(JobConfig config) {
    super(config);
  }

  @Override
  public void run() {
    try {
      Pchome24hBuyCrawler bac = new Pchome24hBuyCrawler(this);
      this.submitBrowserTask(bac);
      bac.get();
      logger.info("Job {} finished", getJobName());
    } catch (Exception e) {
      logger.error("generating search task error:{}", e);
    }
  }

  private void submitBrowserTask(Pchome24hBuyCrawler bac) {
    registerInternalBrowserTask(bac);
    logger.info("Browser task({}) accepted", bac.getName());
  }

  private static class Pchome24hBuyCrawler extends BasicActionComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pchome24hBuyCrawler.class);
    private static final Set<String> finishedSet = new LinkedHashSet<String>();

    private ResultStatus resultStatus = ResultStatus.WaitingToDo;
    private String message;

    public Pchome24hBuyCrawler(JobBase job) {
      super();
      config(job);
    }

    private void config(JobBase job) {
      try {
        CrawlerService crawlerService =
            CrawlerBeanConfiguration.getAppContext().getBean(CrawlerService.class);
        String botName = job.getParameter("botName");
        Map<String, String> configMap = crawlerService.getBotConfig(botName);
        String trackUrl = configMap.get("trackUrl");
        String itemId = configMap.get("itemId");
        String chatId = configMap.get("chatId");
        String chatToken = configMap.get("chatToken");
        boolean skipSubmit = Boolean.parseBoolean(configMap.get("skipSubmit"));
        int itemBtnTimeout = Integer.parseInt(configMap.get("itemBtnTimeout"));
        int cartLinkTimeout = Integer.parseInt(configMap.get("cartLinkTimeout"));
        int paymentLinkTimeout = Integer.parseInt(configMap.get("paymentLinkTimeout"));
        int submitLinkTimeout = Integer.parseInt(configMap.get("submitLinkTimeout"));
        int orderFinishTimeout = Integer.parseInt(configMap.get("orderFinishTimeout"));
        String loginAcc = job.getParameter("loginAcc");
        String loginPwd = job.getParameter("loginPwd");

        By itemBtnBy = By.xpath("//button[contains(@id,'" + itemId + "')]");
        By cartLinkBy = By.xpath("//a[contains(@class,'cart_box')]");
        By paymentLinkBy = By.xpath("//a[contains(@class,'pi_pay')]");
        By submitLinkBy = By.xpath("//a[@id='btnSubmit']");
        By orderFinishBy = By.xpath("//a[contains(@class,'gotoapp-btn')]");

        By loginAccBy = By.id("loginAcc");
        By loginPwdBy = By.id("loginPwd");
        By loginBtnBy = By.id("btnLogin");

        new ActionComposerBuilder().prepareActionSequence()
            .prepareIfThenElse(ac -> finishedSet.contains(itemId)).then().custom(ac -> {
              this.setResultStatus(ResultStatus.AlreadyFinished);
              ac.skipToFail();
            }).endActionSequence().endIf().getUrl(trackUrl)
            .waitUntil(ExpectedConditions.or(ExpectedConditions.elementToBeClickable(loginAccBy),
                ExpectedConditions.elementToBeClickable(itemBtnBy)), itemBtnTimeout)
            .prepareIfThenElse(
                ac -> ExpectedConditions.elementToBeClickable(loginAccBy).apply(ac.getWebDriver()))
            .then().sendKey(loginAccBy, loginAcc).sendKey(loginPwdBy, loginPwd)
            .customMultiPhase(mp -> ac -> {
              try {
                ac.getWebDriver().findElement(loginBtnBy).click();
                mp.noNextPhase();
              } catch (ElementClickInterceptedException ex) {
                LOGGER.info("{} login click error, retrying...", getName());
              }
            }).endActionSequence().endIf()
            .waitUntil(ExpectedConditions.elementToBeClickable(itemBtnBy), itemBtnTimeout)
            .prepareIfThenElse(ac -> !ExpectedConditions
                .attributeContains(itemBtnBy, "class", "add24hCart").apply(ac.getWebDriver()))
            .then().custom(ac -> {
              this.setResultStatus(ResultStatus.NoBuyButton);
              ac.skipToFail();
            }).endActionSequence().endIf().scrollToView(itemBtnBy, false)
            .customMultiPhase(mp -> ac -> {
              try {
                ac.getWebDriver().findElement(itemBtnBy).click();
                mp.noNextPhase();
              } catch (ElementClickInterceptedException ex) {
                LOGGER.info("{} item click error, retrying...", getName());
              }
            }).waitUntil(ExpectedConditions.elementToBeClickable(cartLinkBy), cartLinkTimeout)
            .scrollToView(cartLinkBy, false).customMultiPhase(mp -> ac -> {
              try {
                ac.getWebDriver().findElement(cartLinkBy).click();
                mp.noNextPhase();
              } catch (ElementClickInterceptedException ex) {
                LOGGER.info("{} cart click error, retrying...", getName());
              }
            }).waitUntil(ExpectedConditions.elementToBeClickable(paymentLinkBy), paymentLinkTimeout)
            .scrollToView(paymentLinkBy, false).customMultiPhase(mp -> ac -> {
              try {
                ac.getWebDriver().findElement(paymentLinkBy).click();
                mp.noNextPhase();
              } catch (ElementClickInterceptedException ex) {
                LOGGER.info("{} payment click error, retrying...", getName());
              }
            }).waitUntil(ExpectedConditions.elementToBeClickable(submitLinkBy), submitLinkTimeout)
            .scrollToView(submitLinkBy, false).customMultiPhase(mp -> ac -> {
              try {
                if (!skipSubmit) {
                  ac.getWebDriver().findElement(submitLinkBy).click();
                  this.setResultStatus(ResultStatus.Submitted);
                } else {
                  ac.skipToFail();
                  LOGGER.info("{} skip submit", getName());
                }
                mp.noNextPhase();
              } catch (ElementClickInterceptedException ex) {
                LOGGER.info("{} submit click error, retrying...", getName());
              }
            }).waitUntil(ExpectedConditions.visibilityOfElementLocated(orderFinishBy),
                orderFinishTimeout)
            .custom(ac -> {
              this.setMessage("order got!");
              this.setResultStatus(ResultStatus.Bingo);
              ac.skipToSuccess();
            }).returnToComposerBuilder().onFail(ac -> {
              if (Arrays.asList(ResultStatus.Submitted).contains(this.getResultStatus())) {
                this.setCloseWindow(false);

                // notify
                if (!"".equals(chatId)) {
                  File screenshot =
                      ((TakesScreenshot) ac.getWebDriver()).getScreenshotAs(OutputType.FILE);
                  crawlerService.telegramSendPhoto(chatId, chatToken, itemId, screenshot);
                }
              }

              if (this.getMessage() == null || "".equals(this.getMessage())) {
                List<Exception> errList = ac.getErrors();
                if (errList.size() > 0) {
                  this.setMessage(errList.get(errList.size() - 1).getMessage());
                }
              }
              LOGGER.info("{} fail({}): {}", getName(), this.getResultStatus(), this.getMessage());
            }).onSuccess(ac -> {
              this.setCloseWindow(false);
              // notify
              if (!"".equals(chatId)) {
                File screenshot =
                    ((TakesScreenshot) ac.getWebDriver()).getScreenshotAs(OutputType.FILE);
                crawlerService.telegramSendPhoto(chatId, chatToken, itemId, screenshot);
              }
              LOGGER.info("{} succeed({}): {}", getName(), this.getResultStatus(),
                  this.getMessage());
            }).onDone(ac -> {
              if (Arrays.asList(ResultStatus.Submitted, ResultStatus.Bingo)
                  .contains(this.getResultStatus())) {
                finishedSet.add(itemId);
              }
            }).build(this, this.getClass().getName() + "(" + itemId + ")");
      } catch (Exception ex) {
        LOGGER.error("Create crawler error!", ex);
      }
    }

    public ResultStatus getResultStatus() {
      return this.resultStatus;
    }

    public void setResultStatus(ResultStatus st) {
      this.resultStatus = st;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

  private static enum ResultStatus {
    WaitingToDo("WaitingToDo"), NoBuyButton("NoBuyButton"), Submitted("Submitted"), Bingo(
        "Bingo"), AlreadyFinished("AlreadyFinished");

    private final String name;

    private ResultStatus(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }
}
