package org.kquiet.job.crawler;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CheckExistController.
 *
 * @author monkey
 *
 */
public class CheckExistController extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckExistController.class);

  public CheckExistController(JobConfig config) {
    super(config);
  }

  @Override
  public void run() {
    try {
      CheckExistCrawler bac = new CheckExistCrawler(this);
      this.submitBrowserTask(bac);
      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }

  private void submitBrowserTask(CheckExistCrawler bac) {
    registerInternalBrowserTask(bac);
    LOGGER.info("Browser task({}) accepted", bac.getName());
  }

  private static class CheckExistCrawler extends BasicActionComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckExistCrawler.class);

    private ResultStatus resultStatus = ResultStatus.WaitingToDo;
    private String message;

    public CheckExistCrawler(JobBase job) {
      super();
      config(job);
    }

    private void config(JobBase job) {
      try {
        CommonBiz bizObj = new CommonBiz(job);
        Map<String, String> configMap = bizObj.getBotConfig();
        String checkUrl = configMap.get("checkUrl");
        String existPattern = configMap.get("existPattern");
        int timeout = Integer.parseInt(configMap.get("checkTimeout"));
        String chatId = configMap.get("chatId");
        String chatToken = configMap.get("chatToken");

        new ActionComposerBuilder().prepareActionSequence().getUrl(checkUrl)
            .prepareWaitUntil(ExpectedConditions.visibilityOfElementLocated(By.xpath(existPattern)),
                timeout)
            .withTimeoutCallback(ac -> {
              this.setMessage("Not exist after checking");
              ac.skipToSuccess();
            }).done().scrollToView(By.xpath(existPattern), false).custom(ac -> {
              File screenshot =
                  ((TakesScreenshot) ac.getWebDriver()).getScreenshotAs(OutputType.FILE);

              // notify
              if (!"".equals(chatId)) {
                bizObj.notifyTelegram(chatToken, chatId, screenshot, checkUrl);
              }
            }).returnToComposerBuilder().onFail(ac -> {
              if (this.getMessage() == null || "".equals(this.getMessage())) {
                List<Exception> errList = ac.getErrors();
                if (errList.size() > 0) {
                  this.setMessage(errList.get(errList.size() - 1).getMessage());
                }
              }

              if (Arrays.asList(ResultStatus.AlertFail).contains(this.getResultStatus())) {
                // NOTHING TODO
              }

              LOGGER.info("{} fail: {}", getName(), this.getMessage());
            }).onSuccess(ac -> {
              LOGGER.info("{} succeed: {}", getName(), this.getMessage());
            }).onDone(ac -> {
              if (ac.isFail()) {
                // alert for some cases
                if (Arrays.asList(ResultStatus.UnknownFail, ResultStatus.AlertFail)
                    .contains(this.getResultStatus())
                    && (ac.getFailUrl() != null || ac.getFailPage() != null)) {
                  // NOTHING TODO
                }
              }
            }).build(this, "CheckExistCrawler(" + checkUrl + ")");
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
    AlertFail("AlertFail"), WaitingToDo("WaitingToDo"), Success("Success"), UnknownFail(
        "UnknownFail");

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
