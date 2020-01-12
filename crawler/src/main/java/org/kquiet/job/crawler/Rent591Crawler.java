package org.kquiet.job.crawler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.jobscheduler.JobBase;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rent591Crawler extends BasicActionComposer {
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
      String botName = job.getParameter("botName");
      Map<String, String> configMap = bizObj.getBotConfig();
      String area = configMap.get("area");
      String entryUrl = configMap.get("entryUrl");
      String chatId = configMap.get("chatId");
      String chatToken = configMap.get("chatToken");

      new ActionComposerBuilder()
        .prepareActionSequence()
          .getUrl("https://rent.591.com.tw/")
          .justWait(3000)
          .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("j_loading")), 10000)
          .customMultiPhase(mp -> ac -> {  //handle popup area box
            try {
              List<WebElement> areaBoxList = ac.getWebDriver()
                  .findElements(By.xpath("//a[@id='area-box-close']"));
              if (areaBoxList.size() > 0 && areaBoxList.get(0).isDisplayed()) {
                areaBoxList.get(0).click();
              }
              mp.noNextPhase();
            } catch (Exception ex) {
              //TODO
            }
          })
          .waitUntil(ExpectedConditions.visibilityOfElementLocated(
              By.xpath("//span[contains(@class,'search-location-span')"
                  + " and @data-index='1']")), 10000)
          //click to show area option box
          .click(
              By.xpath("//span[contains(@class,'search-location-span') and @data-index='1']"))      
          .waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("optionBox")), 10000)
          //click specific area
          .click(By.xpath("//ul[@id='optionBox']//a[text()='" + area + "']"))
          .justWait(500)
          .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("j_loading")), 10000)
          .getUrl(entryUrl)  //entry url for searching
          .justWait(3000)
          .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("j_loading")), 10000)
          //parse search result
          .custom(ac -> {  
            List<WebElement> propertyList = ac.getWebDriver()
                .findElements(By.xpath("//div[@id='content']/ul"));
            LOGGER.info("{} found {} properties", getName(), propertyList.size());
    
            for (WebElement property: propertyList) {
              try {
                WebElement imageUrlE = property.findElement(
                    By.xpath("./li[contains(@class,'imageBox')]/img"));
                String imageUrl = imageUrlE.getAttribute("data-original");
                WebElement descE = property.findElement(
                    By.xpath("./li[contains(@class,'infoContent')]"));
                String description = descE.getText();
                WebElement urlE = descE.findElement(By.xpath("./h3/a"));
                String url = urlE.getAttribute("href");
                WebElement priceE = property.findElement(
                    By.xpath("./div[contains(@class,'price')]"));
                String price = priceE.getText();
    
                //save for each
                int createResult = bizObj.createCase(url, imageUrl, description, price);  
                if (createResult == 1) {
                  LOGGER.info(String.format("Case:%s created", url));
                  //notify
                  if (!"".equals(chatId)) {
                    bizObj.notifyTelegram(chatToken, chatId, imageUrl,
                        String.format("%s %s %s", url, description, price));
                  }
                } else if (createResult == -1) {
                  LOGGER.info(String.format("Case:%s create failed", url));
                }
              } catch (Exception ex) {
                LOGGER.warn("Case element parse error", ex);
              }
            }
          })
        .returnToComposerBuilder()
        .onFail(ac -> {
          if (this.getMessage() == null || "".equals(this.getMessage())) {
            List<Exception> errList = ac.getErrors();
            if (errList.size() > 0) {
              this.setMessage(errList.get(errList.size() - 1).getMessage());
            }
          } else {
            this.setMessage(String.format("Fail crawler:%s", this.getMessage()));
          }
  
          if (Arrays.asList(ResultStatus.AlertFail).contains(this.getResultStatus())) {
            //NOTHING TODO
          }
  
          LOGGER.info("{} fail:", this.getMessage());
        })
        .onSuccess(ac -> {
          this.setMessage(String.format("Success crawler:%s", this.getMessage()));
  
          LOGGER.info("{} succeed:", getName());
        })
        .onDone(ac -> {
          if (ac.isFail()) {
            //alert for some cases
            if (Arrays.asList(ResultStatus.UnknownFail, ResultStatus.AlertFail)
                .contains(this.getResultStatus())
                && (ac.getFailUrl() != null || ac.getFailPage() != null)) {
              //NOTHING TODO
            }
          }
        })
        .build(this, "Rent591Crawler(" + entryUrl + ")");
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

  public enum ResultStatus {
    AlertFail("AlertFail"),
    WaitingToDo("WaitingToDo"),
    Success("Success"),
    UnknownFail("UnknownFail");

    private final String name;
    private ResultStatus(String name) {
      this.name = name;
    }
    
    @Override
    public String toString() {
      return this.name;
    }
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
