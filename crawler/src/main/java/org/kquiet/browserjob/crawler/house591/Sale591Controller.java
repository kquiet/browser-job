package org.kquiet.browserjob.crawler.house591;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.browserjob.crawler.CrawlerBeanConfiguration;
import org.kquiet.browserjob.crawler.CrawlerService;
import org.kquiet.browserjob.crawler.house591.entity.SaleHouse;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sale591Controller.
 *
 * @author monkey
 *
 */
public class Sale591Controller extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(Sale591Controller.class);
  private static final Meter meter = CrawlerBeanConfiguration.getAppContext()
      .getBean(OpenTelemetry.class).getMeter(Sale591Controller.class.getCanonicalName());
  private static final LongCounter instrumentTelegramSendPhoto =
      meter.counterBuilder("telegram_sendPhoto")
          .setDescription("count of succesful telegram sendPhoto api").build();
  private static final LongCounter instrumentNewSaleHouse = meter.counterBuilder("new_sale_house")
      .setDescription("count of creation of new sale house").build();

  public Sale591Controller(JobConfig config) {
    super(config);
  }

  @Override
  @WithSpan
  public void run() {
    try {
      Sale591Crawler bac = new Sale591Crawler(this);
      this.submitBrowserTask(bac);
      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }

  private void submitBrowserTask(Sale591Crawler bac) {
    registerInternalBrowserTask(bac);
    LOGGER.info("Browser task({}) accepted", bac.getName());
  }

  private static class Sale591Crawler extends BasicActionComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sale591Crawler.class);


    private ResultStatus resultStatus = ResultStatus.WaitingToDo;
    private String message;

    public Sale591Crawler(JobBase job) {
      super();
      config(job);
    }

    private void config(JobBase job) {
      try {
        CrawlerService crawlerService =
            CrawlerBeanConfiguration.getAppContext().getBean(CrawlerService.class);
        String botName = job.getParameter("botName");
        Map<String, String> configMap = crawlerService.getBotConfig(botName);
        String entryUrl = configMap.get("entryUrl");
        String chatId = configMap.get("chatId");
        String chatToken = configMap.get("chatToken");

        new ActionComposerBuilder().prepareActionSequence().getUrl(entryUrl).justWait(3000)
            .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("j-loading")), 10000)
            .customMultiPhase(mp -> ac -> { // handle pop up box
              try {
                List<WebElement> popBoxList = ac.getWebDriver()
                    .findElements(By.xpath("//div[contains(@class,'tips-popbox-img')]"));
                if (popBoxList.size() > 0 && popBoxList.get(0).isDisplayed()) {
                  popBoxList.get(0).click();
                }
                mp.noNextPhase();
              } catch (Exception ex) {
                LOGGER.warn(String.format("pop up box handling failed", ex));
              }
            }).custom(ac -> {
              List<WebElement> propertyList = ac.getWebDriver().findElements(By.xpath(
                  "//div[contains(@class,'houseList-body')]/div[contains(@class,'j-house')]"));
              LOGGER.info("{} found {} properties", getName(), propertyList.size());

              for (WebElement property : propertyList) {
                try {
                  WebElement imageUrlE = property.findElement(
                      By.xpath("(./div[contains(@class,'houseList-item-left')]//img)[1]"));
                  String imageUrl = imageUrlE.getAttribute("data-original");

                  List<WebElement> descElementList = property
                      .findElements(By.xpath("./div[contains(@class,'houseList-item-main')]/div"));
                  WebElement descRootE = property
                      .findElement(By.xpath("./div[contains(@class,'houseList-item-main')]"));
                  WebElement descTitleE = descRootE
                      .findElement(By.xpath("./div[contains(@class,'houseList-item-title')]"));
                  List<WebElement> descAttributeElementList = descRootE.findElements(
                      By.xpath("./div[contains(@class,'houseList-item-attr-row')]/span"));
                  List<WebElement> descAddressElementList = descRootE.findElements(
                      By.xpath("./div[contains(@class,'houseList-item-address-row')]/span"));
                  List<WebElement> descUserElementList = descRootE.findElements(
                      By.xpath("./div[contains(@class,'houseList-item-user-row')]/span"));
                  List<WebElement> descTagElementList = descRootE.findElements(
                      By.xpath("./div[contains(@class,'houseList-item-tag-row')]/div"));

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

                  List<WebElement> priceElementList = property
                      .findElements(By.xpath("./div[contains(@class,'houseList-item-right')]"
                          + "/div[contains(@class,'houseList-item-price')]/*[not(self::del)]"));
                  WebElement unitPriceE =
                      property.findElement(By.xpath("./div[contains(@class,'houseList-item-right')]"
                          + "/div[contains(@class,'houseList-item-unitprice')]"));
                  String price = priceElementList.stream().map(s -> s.getText().trim())
                      .collect(Collectors.joining(" ")) + " " + unitPriceE.getText().trim();

                  // save for each property
                  House591Service house591Service =
                      CrawlerBeanConfiguration.getAppContext().getBean(House591Service.class);
                  SaleHouse toAdd = newSaleHouse(url, imageUrl, description, price, botName);
                  boolean isExist = house591Service.existsSaleHouse(toAdd.getUrl());
                  if (!isExist) {
                    house591Service.addSaleHouse(toAdd);
                    LOGGER.info(String.format("SaleHouse:%s added", url));
                    instrumentNewSaleHouse.add(1L);
                    if (!"".equals(chatId) && crawlerService.notifyTelegram(chatToken, chatId,
                        imageUrl, String.format("%s %s %s", url, description, price))) {
                      instrumentTelegramSendPhoto.add(1L);

                      // telegram rate limit
                      Thread.sleep(3000);
                    }
                  }
                } catch (Exception ex) {
                  LOGGER.warn("SaleHouse element parse error", ex);
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
            }).build(this, "Sale591Crawler(" + entryUrl + ")");
      } catch (Exception ex) {
        LOGGER.error("Create crawler error!", ex);
      }
    }

    private static SaleHouse newSaleHouse(String url, String imageUrl, String description,
        String price, String maintainer) {
      SaleHouse obj = new SaleHouse();
      obj.setUrl(url);
      obj.setSite("591");
      obj.setImageUrl(imageUrl);
      obj.setDescription(description);
      obj.setPrice(price);
      obj.setCreateuser(maintainer);
      obj.setCreatedate(LocalDateTime.now());
      return obj;
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
