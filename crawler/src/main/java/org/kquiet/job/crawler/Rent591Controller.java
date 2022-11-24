package org.kquiet.job.crawler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.jobscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rent591Controller.
 *
 * @author monkey
 *
 */
public class Rent591Controller extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(Rent591Controller.class);

  public Rent591Controller(String jobName) {
    super(jobName);
  }

  @Override
  public void run() {
    try {
      Rent591Crawler bac = new Rent591Crawler(this);
      this.submitBrowserTask(bac);
      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }

  private void submitBrowserTask(Rent591Crawler bac) {
    registerInternalBrowserTask(bac);
    LOGGER.info("Browser task({}) accepted", bac.getName());
  }

  private static class Rent591Crawler extends BasicActionComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rent591Crawler.class);

    private ResultStatus resultStatus = ResultStatus.WaitingToDo;
    private String message;

    public Rent591Crawler(JobBase job) {
      super();
      config(job);
    }

    private void config(JobBase job) {
      try {
        CommonBiz bizObj = new CommonBiz(job);
        Map<String, String> configMap = bizObj.getBotConfig();
        String entryUrl = configMap.get("entryUrl");
        String chatId = configMap.get("chatId");
        String chatToken = configMap.get("chatToken");

        new ActionComposerBuilder().prepareActionSequence().getUrl(entryUrl).justWait(3000)
            .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("j-loading")), 10000)
            .custom(ac -> {
              List<WebElement> propertyList = ac.getWebDriver()
                  .findElements(By.xpath("//section[contains(@class,'vue-list-rent-item')]/a"));
              LOGGER.info("{} found {} properties", getName(), propertyList.size());

              for (WebElement property : propertyList) {
                try {
                  String url = property.getAttribute("href").trim();
                  WebElement imageUrlE = property
                      .findElement(By.xpath("(./div[contains(@class,'rent-item-left')]//img)[1]"));
                  String imageUrl = imageUrlE.getAttribute("data-original");
                  List<WebElement> descElementList =
                      property.findElements(By.xpath("./div[contains(@class,'rent-item-right')]"
                          + "/*[not(contains(@class,'item-price'))]"));
                  String description = descElementList.stream().map(s -> s.getText())
                      .collect(Collectors.joining(System.lineSeparator()));
                  WebElement priceE =
                      property.findElement(By.xpath("./div[contains(@class,'rent-item-right')]"
                          + "/*[contains(@class,'item-price')]"));
                  String price = priceE.getText();

                  // save for each property
                  int addResult = bizObj.addRentHouse(url, imageUrl, description, price);
                  switch (addResult) {
                    case 1:
                      LOGGER.info(String.format("RentHouse:%s added", url));
                      if (!"".equals(chatId) && bizObj.notifyTelegram(chatToken, chatId, imageUrl,
                          String.format("%s %s %s", url, description, price))) {
                        // telegram rate limit
                        Thread.sleep(3000);
                      }
                      break;
                    case 0:
                      break;
                    default:
                      LOGGER.info(String.format("RentHouse:%s add failed", url));
                      break;
                  }
                } catch (Exception ex) {
                  LOGGER.warn("RentHouse element parse error", ex);
                }
              }
            }).returnToComposerBuilder().onFail(ac -> {
              if (this.getMessage() == null || "".equals(this.getMessage())) {
                List<Exception> errList = ac.getErrors();
                if (errList.size() > 0) {
                  this.setMessage(errList.get(errList.size() - 1).getMessage());
                }
              }
              LOGGER.info("{} fail: {}", getName(), this.getMessage());
            }).onSuccess(ac -> {
              LOGGER.info("{} succeed:", getName(), this.getMessage());
            }).onDone(ac -> {
              if (ac.isFail()) {
                // alert for some cases
                if (Arrays.asList(ResultStatus.UnknownFail, ResultStatus.AlertFail)
                    .contains(this.getResultStatus())
                    && (ac.getFailUrl() != null || ac.getFailPage() != null)) {
                  // NOTHING TODO
                }
              }
            }).build(this, "Rent591Crawler(" + entryUrl + ")");
      } catch (Exception ex) {
        LOGGER.error("Create crawler error!", ex);
      }
    }

    public ResultStatus getResultStatus() {
      return this.resultStatus;
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
