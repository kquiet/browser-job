package org.kquiet.browserjob.crawler.house.script;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.browserjob.crawler.CrawlerBeanConfiguration;
import org.kquiet.browserjob.crawler.house.CrawlerResultStatus;
import org.kquiet.browserjob.crawler.house.entity.SaleHouse;
import org.kquiet.browserjob.crawler.house.service.HouseService;
import org.kquiet.browserjob.crawler.service.CrawlerService;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic house crawler.
 *
 * @author monkey
 */
public class Sale591Script extends BasicActionComposer
    implements BiConsumer<Map<String, String>, ActionComposerBuilder> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Sale591Script.class);

  protected CrawlerService crawlerService =
      CrawlerBeanConfiguration.getAppContext().getBean(CrawlerService.class);
  protected HouseService houseService =
      CrawlerBeanConfiguration.getAppContext().getBean(HouseService.class);
  protected MeterRegistry meterRegistry =
      CrawlerBeanConfiguration.getAppContext().getBean(MeterRegistry.class);
  protected CrawlerResultStatus resultStatus = CrawlerResultStatus.WaitingToDo;
  protected String message;
  protected ActionComposerBuilder finalScriptBuilder = new ActionComposerBuilder();

  public Sale591Script(JobBase job) {
    super();
    config(job);
  }

  protected void config(JobBase job) {
    try {
      String botName = job.getJobName();
      Map<String, String> configMap = crawlerService.getBotConfig(botName);
      configMap.put("botName", botName);
      configMap.put("takeScreenshot", job.getParameter("takeScreenshot"));

      configScriptBuilder(configMap);
      finalScriptBuilder.build(this,
          this.getClass().getName() + "(" + configMap.get("entryUrl") + ")");
    } catch (Exception ex) {
      LOGGER.error("Create crawler error!", ex);
    }
  }

  private void configScriptBuilder(Map<String, String> configMap) {
    // common behavior when fail/success/done, could be overrided by user script
    finalScriptBuilder.onFail(ac -> {
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
        if (Arrays.asList(CrawlerResultStatus.UnknownFail, CrawlerResultStatus.AlertFail).contains(
            this.getResultStatus()) && (ac.getFailUrl() != null || ac.getFailPage() != null)) {
          // NOTHING TODO
        }
      }
    });

    // user script
    this.accept(configMap, finalScriptBuilder);
  }

  public CrawlerResultStatus getResultStatus() {
    return this.resultStatus;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public void accept(Map<String, String> configMap, ActionComposerBuilder scriptBuilder) {
    String botName = configMap.get("botName");
    String site = configMap.get("site");
    String entryUrl = configMap.get("entryUrl");
    String chatId = configMap.get("chatId");
    String chatToken = configMap.get("chatToken");
    boolean takeScreentshot = Boolean.parseBoolean(configMap.get("takeScreenshot"));

    scriptBuilder.prepareActionSequence().getUrl(entryUrl).justWait(3000)
        .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("j-loading")), 10000)
        .waitUntil(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(By
                .xpath("//div[contains(@class,'houseList-body')]/div[contains(@class,'j-house')]")),
            10000)
        .customMultiPhase(mp -> ac -> { // handle pop up box
          try {
            List<WebElement> popBoxList = ac.getWebDriver()
                .findElements(By.xpath("//div[contains(@class,'tips-popbox-img')]"));
            if (popBoxList.size() > 0 && popBoxList.get(0).isDisplayed()) {
              popBoxList.get(0).click();
            }
            mp.noNextPhase();
          } catch (Exception ex) {
            LOGGER.warn("pop up box handling failed", ex);
          }
        }).custom(ac -> {
          List<WebElement> propertyList = ac.getWebDriver().findElements(
              By.xpath("//div[contains(@class,'houseList-body')]/div[contains(@class,'j-house')]"));
          LOGGER.info("{} found {} properties", getName(), propertyList.size());
          if (takeScreentshot && propertyList.size() == 0) {
            crawlerService.takeScreenShot(ac.getWebDriver());
          }

          for (WebElement property : propertyList) {
            try {
              WebElement imageUrlE = property
                  .findElement(By.xpath("(./div[contains(@class,'houseList-item-left')]//img)[1]"));
              String imageUrl = imageUrlE.getAttribute("data-original");

              List<WebElement> descElementList = property
                  .findElements(By.xpath("./div[contains(@class,'houseList-item-main')]/div"));
              WebElement descRootE =
                  property.findElement(By.xpath("./div[contains(@class,'houseList-item-main')]"));
              WebElement descTitleE =
                  descRootE.findElement(By.xpath("./div[contains(@class,'houseList-item-title')]"));
              List<WebElement> descAttributeElementList = descRootE
                  .findElements(By.xpath("./div[contains(@class,'houseList-item-attr-row')]/span"));
              List<WebElement> descAddressElementList = descRootE.findElements(
                  By.xpath("./div[contains(@class,'houseList-item-address-row')]/span"));
              List<WebElement> descUserElementList = descRootE
                  .findElements(By.xpath("./div[contains(@class,'houseList-item-user-row')]/span"));
              List<WebElement> descTagElementList = descRootE
                  .findElements(By.xpath("./div[contains(@class,'houseList-item-tag-row')]/div"));

              String description = descTitleE.getText().trim() + System.lineSeparator()
                  + descAttributeElementList
                      .stream().map(s -> s.getText().trim()).collect(Collectors.joining(" "))
                  + System.lineSeparator()
                  + descAddressElementList
                      .stream().map(s -> s.getText().trim()).collect(Collectors.joining(" "))
                  + System.lineSeparator()
                  + descUserElementList.stream().map(s -> s.getText())
                      .collect(Collectors.joining(""))
                  + System.lineSeparator() + descTagElementList.stream().map(s -> s.getText())
                      .collect(Collectors.joining(" "));

              String url = descElementList.get(0).findElement(By.xpath("(./a)[1]"))
                  .getAttribute("href").trim();

              List<WebElement> priceElementList =
                  property.findElements(By.xpath("./div[contains(@class,'houseList-item-right')]"
                      + "/div[contains(@class,'houseList-item-price')]/*[not(self::del)]"));
              WebElement unitPriceE =
                  property.findElement(By.xpath("./div[contains(@class,'houseList-item-right')]"
                      + "/div[contains(@class,'houseList-item-unitprice')]"));
              String price = priceElementList.stream().map(s -> s.getText().trim())
                  .collect(Collectors.joining(" ")) + " " + unitPriceE.getText().trim();

              // process for parsed element
              this.processSaleHouse(site, url, imageUrl, description, price, botName, chatId,
                  chatToken);
            } catch (Exception ex) {
              LOGGER.warn(this.getClass().getName() + " encounter error", ex);
            }
          }
        }).returnToComposerBuilder();

  }

  protected void processSaleHouse(String site, String url, String imageUrl, String description,
      String price, String botName, String chatId, String chatToken) throws Exception {
    boolean isExist = houseService.existsSaleHouse(url);
    if (!isExist) {
      houseService.addSaleHouse(site, url, imageUrl, description, price, botName);
      LOGGER.info(String.format("%s:%s added", SaleHouse.class.getName(), url));
      String meterNameNew = "crawler_" + site + "_new_sale_house";
      String meterNameSendPhoto = "crawler_" + site + "_new_sale_house.telegram_send_photo";
      meterRegistry.counter(meterNameNew).increment();
      if (!"".equals(chatId) && crawlerService.telegramSendPhoto(chatId, chatToken,
          String.format("%s %s %s", url, description, price), imageUrl).block()) {
        meterRegistry.counter(meterNameSendPhoto).increment();

        // telegram request delay to not exceed rate limit
        Thread.sleep(3000);
      }
    }
  }
}
