package org.kquiet.browserjob.crawler.obsolete;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.kquiet.browserscheduler.JobController.InteractionType;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EasycardJcbRegisterController.
 *
 * @author monkey
 *
 */
public class EasycardJcbRegisterController extends JobBase {
  private static final Logger logger = LoggerFactory.getLogger(EasycardJcbRegisterController.class);

  public EasycardJcbRegisterController(JobConfig config) {
    super(config);
  }

  @Override
  public void run() {
    try {
      int parallelism = 1;
      try {
        parallelism = Integer.parseInt(this.getParameter("parallelism"));
      } catch (Exception e) {
        logger.debug("Job {} default parallelism 1", getJobName());
      }
      EasycardJcbRegisterCrawler[] crawlerArr = new EasycardJcbRegisterCrawler[parallelism];
      for (int i = 0; i < parallelism; i++) {
        crawlerArr[i] = new EasycardJcbRegisterCrawler(this, UUID.randomUUID().toString());
        this.submitBrowserTask(crawlerArr[i]);
      }
      CompletableFuture.allOf(crawlerArr).get();
      logger.info("Job {} finished", getJobName());
    } catch (Exception e) {
      logger.error("generate crawler task error:{}", e);
    }
  }

  private void submitBrowserTask(EasycardJcbRegisterCrawler bac) {
    registerInternalBrowserTask(bac);
    logger.info("Browser task({}) accepted", bac.getName());
  }

  private static class EasycardJcb {
    private final String creditcard;
    private final String easycard;

    public EasycardJcb(JobBase job) {
      creditcard = job.getParameter("creditcard");
      easycard = job.getParameter("easycard");
    }

    public String getCreditcard() {
      return creditcard;
    }

    public String getEasycard() {
      return easycard;
    }

    @Override
    public String toString() {
      return String.join("+", creditcard, easycard);
    }
  }

  private static class EasycardJcbRegisterCrawler extends BasicActionComposer {
    private static final Logger logger = LoggerFactory.getLogger(EasycardJcbRegisterCrawler.class);
    private static Set<String> submitted =
        Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private ResultStatus resultStatus = ResultStatus.WaitingToDo;
    private String message;

    public EasycardJcbRegisterCrawler(EasycardJcbRegisterController controller, String postfix) {
      super();
      config(controller, postfix);
    }

    private void config(EasycardJcbRegisterController controller, String postfix) {
      try {
        boolean autoSubmit = Boolean.parseBoolean(controller.getParameter("autoSubmit"));
        EasycardJcb card = new EasycardJcb(controller);

        String pageUrl = "https://ezweb.easycard.com.tw/Event01/JCBLoginServlet";
        // String pageUrl = "file:///C:/Users/kimberly/Desktop/JCB_Login_Form_Before_Click.html";
        By infoBy = By.id("content");
        Pattern fullPattern = Pattern.compile("很抱歉，本月份登錄名額已滿");
        Pattern notYetOpenPattern = Pattern.compile("本月份尚未開放");
        Pattern successPattern = Pattern.compile("登錄成功");
        By agreeCheckboxBy = By.id("accept");
        By creditcard1By = By.id("txtCreditCard1");
        By creditcard2By = By.id("txtCreditCard2");
        By creditcard4By = By.id("txtCreditCard4");
        By easycard1By = By.id("txtEasyCard1");
        By easycard2By = By.id("txtEasyCard2");
        By easycard3By = By.id("txtEasyCard3");
        By easycard4By = By.id("txtEasyCard4");
        By captchaFrameBy = By.xpath("//div[@class='g-recaptcha']/div//iframe");
        By captchaBy = By.id("recaptcha-anchor");
        // 1. 驗證程序已過期，請再次勾選核取方塊以產生新的問題
        // 2. reCAPTCHA 要求驗證.
        // 3. 您已通過驗證
        // rc-imageselect
        // By captchaStatusBy = By.id("recaptcha-accessible-status");
        By submitBy = By.xpath("//a[text()='確認送出']");

        new ActionComposerBuilder().prepareActionSequence()
            .prepareIfThenElse(ac -> submitted.contains(card.toString())).then().custom(ac -> {
              this.setResultStatus(ResultStatus.AlreadyFinished);
              ac.skipToFail();
            }).endActionSequence().endIf().getUrl(pageUrl)
            .waitUntil(ExpectedConditions.or(ExpectedConditions.textMatches(infoBy, fullPattern),
                ExpectedConditions.textMatches(infoBy, notYetOpenPattern),
                ExpectedConditions.elementToBeClickable(agreeCheckboxBy)), 30000)
            .prepareIfThenElse(ac -> ExpectedConditions.elementToBeClickable(agreeCheckboxBy)
                .apply(ac.getWebDriver()))
            .then().click(agreeCheckboxBy).endActionSequence().otherwise().custom(ac -> {
              ac.skipToFail();
              String info = Optional.ofNullable(ac.getWebDriver().findElement(infoBy).getText())
                  .orElse("").trim();
              this.setMessage(info);
              if (fullPattern.matcher(info).find()) {
                this.setResultStatus(ResultStatus.Full);
              } else if (notYetOpenPattern.matcher(info).find()) {
                this.setResultStatus(ResultStatus.NotYetOpen);
              }
            }).endActionSequence().endIf()
            .waitUntil(ExpectedConditions.and(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(captchaFrameBy),
                ExpectedConditions.visibilityOfElementLocated(captchaBy),
                ExpectedConditions.elementToBeClickable(captchaBy)), 1500)
            .waitUntil(ExpectedConditions.elementToBeClickable(submitBy), 1200)
            .sendKey(creditcard1By, card.getCreditcard().substring(0, 4))
            .sendKey(creditcard2By, card.getCreditcard().substring(4, 6))
            .sendKey(creditcard4By, card.getCreditcard().substring(12, 16))
            .sendKey(easycard1By, card.getEasycard().substring(0, 4))
            .sendKey(easycard2By, card.getEasycard().substring(4, 8))
            .sendKey(easycard3By, card.getEasycard().substring(8, 12))
            .sendKey(easycard4By, card.getEasycard().substring(12, 16))
            .prepareScrollToView(captchaBy, false).withInFrame(Arrays.asList(captchaFrameBy)).done()
            .prepareMouseOver(captchaBy).withInFrame(Arrays.asList(captchaFrameBy)).done()
            .waitUntil(ExpectedConditions.and(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(captchaFrameBy),
                ExpectedConditions.attributeContains(captchaBy, "class",
                    "recaptcha-checkbox-hover")),
                1000)
            .prepareClick(captchaBy).withInFrame(Arrays.asList(captchaFrameBy)).done()
            .prepareIfThenElse(ac -> autoSubmit).then()
            .prepareWaitUntil(ExpectedConditions.and(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(captchaFrameBy),
                ExpectedConditions.attributeContains(captchaBy, "class",
                    "recaptcha-checkbox-checked"),
                ExpectedConditions.not(ExpectedConditions.attributeContains(captchaBy, "class",
                    "recaptcha-checkbox-hover"))),
                8000)
            .withTimeoutCallback(ac -> {
              this.setResultStatus(ResultStatus.RecaptchaChallenge);
              this.setMessage("have to try again");
              ac.skipToFail();
            }).done().click(submitBy)
            .prepareWaitUntil(ExpectedConditions.textMatches(infoBy, successPattern), 29000)
            .withTimeoutCallback(ac -> {
              this.setResultStatus(ResultStatus.Submitted);
              submitted.add(card.toString());
              ac.skipToSuccess();
            }).done().custom(ac -> {
              String info = Optional.ofNullable(ac.getWebDriver().findElement(infoBy).getText())
                  .orElse("").trim();
              this.setResultStatus(ResultStatus.Bingo);
              this.setMessage(info);
              submitted.add(card.toString());
              ac.skipToSuccess();
            }).endActionSequence().endIf().custom(ac -> {
              controller.awaitInteraction();
              if (controller.getLatestInteraction() == InteractionType.POSITIVE) {
                this.setResultStatus(ResultStatus.Submitted);
                submitted.add(card.toString());
                ac.skipToSuccess();
              } else {
                ac.skipToFail();
                this.setMessage("negative response");
              }
            }).returnToComposerBuilder().onFail(ac -> {
              if (this.getMessage() == null || "".equals(this.getMessage())) {
                List<Exception> errList = ac.getErrors();
                if (errList.size() > 0) {
                  this.setMessage(errList.get(errList.size() - 1).getMessage());
                }
              }
              if (Arrays.asList(ResultStatus.RecaptchaChallenge).contains(this.getResultStatus())) {
                this.setCloseWindow(false);
              }
              logger.info("{} fail({}): {}", getName(), this.getResultStatus(), this.getMessage());
            }).onSuccess(ac -> {
              this.setCloseWindow(false);
              logger.info("{} succeed({}): {}", getName(), this.getResultStatus(),
                  this.getMessage());
            }).build(this, this.getClass().getName() + "_" + postfix);
      } catch (Exception ex) {
        logger.error("Create crawler error!", ex);
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
    WaitingToDo("WaitingToDo"), RecaptchaChallenge("RecaptchaChallenge"), Full("Full"), NotYetOpen(
        "NotYetOpen"), Submitted("Submitted"), Bingo("Bingo"), AlreadyFinished("AlreadyFinished");

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
