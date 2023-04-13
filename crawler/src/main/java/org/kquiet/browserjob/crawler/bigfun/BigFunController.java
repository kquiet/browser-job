package org.kquiet.browserjob.crawler.bigfun;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.browserjob.crawler.CrawlerBeanConfiguration;
import org.kquiet.browserjob.crawler.CrawlerService;
import org.kquiet.browserjob.crawler.bigfun.entity.RealEstate;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sale591Controller.
 *
 * @author monkey
 *
 */
public class BigFunController extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(BigFunController.class);

  public BigFunController(JobConfig config) {
    super(config);
  }

  @Override
  @WithSpan
  public void run() {
    try {
      BigFunCrawler bac = new BigFunCrawler(this);
      this.submitBrowserTask(bac);
      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }

  private void submitBrowserTask(BigFunCrawler bac) {
    registerInternalBrowserTask(bac);
    LOGGER.info("Browser task({}) accepted", bac.getName());
  }

  private static class BigFunCrawler extends BasicActionComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BigFunCrawler.class);

    private ResultStatus resultStatus = ResultStatus.WaitingToDo;
    private String message;

    public BigFunCrawler(JobBase job) {
      super();
      config(job);
    }

    private void config(JobBase job) {
      try {
        CrawlerService crawlerService =
            CrawlerBeanConfiguration.getAppContext().getBean(CrawlerService.class);
        String botName = job.getJobName();
        Map<String, String> configMap = crawlerService.getBotConfig(botName);
        String loginUrl = configMap.get("loginUrl");
        String userName = configMap.get("userName");
        String password = configMap.get("password");
        String entryUrl = configMap.get("entryUrl");
        String chatId = configMap.get("chatId");
        String chatToken = configMap.get("chatToken");
        boolean takeScreentshot = Boolean.parseBoolean(job.getParameter("takeScreenshot"));

        new ActionComposerBuilder().prepareActionSequence().getUrl(loginUrl).justWait(5000)
            .prepareIfThenElse(ac -> ExpectedConditions
                .visibilityOfElementLocated(
                    By.xpath("//*[@id='content']//button[contains(@value,'login')]"))
                .apply(ac.getWebDriver()))
            .then()
            .sendKey(By.xpath("//*[@id='content']//input[@id='login-form-username']"), userName)
            .sendKey(By.xpath("//*[@id='content']//input[@id='login-form-password']"), password)
            .click(By.xpath("//*[@id='content']//button[contains(@value,'login')]")).justWait(5000)
            .endActionSequence().endIf().customMultiPhase(mp -> ac -> {
              try {
                List<WebElement> popup =
                    ac.getWebDriver().findElements(By.id("//div[@id='swal2-content']"));
                if (popup.size() > 0 && popup.get(0).isDisplayed()) {
                  throw new Exception(popup.get(0).getText().trim());
                }
                mp.noNextPhase();
              } catch (Exception ex) {
                LOGGER.warn("pop up handling failed", ex);
              }
            })
            .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//div[@id='top-link']//a[contains(text(),'登出')]")), 10000)
            .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("total_result")),
                10000)
            .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("loading-cover")),
                10000)
            .getUrl(entryUrl).justWait(5000)
            .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//div[@id='top-link']//a[contains(text(),'登出')]")), 10000)
            .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("total_result")),
                10000)
            .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.id("loading-cover")),
                10000)
            .customMultiPhase(mp -> ac -> {
              // find target items
              List<WebElement> itemList = ac.getWebDriver().findElements(By
                  .xpath("//table[contains(@class,'ttable')]/tbody/tr[not(@class) and not(@id)]"));
              LOGGER.info("{} found {} items", getName(), itemList.size());
              if (takeScreentshot && itemList.size() == 0) {
                crawlerService.takeScreenShot(ac.getWebDriver());
              }

              // process each item
              for (WebElement item : itemList) {
                try {
                  WebElement urlElement =
                      item.findElement(By.xpath(".//a[contains(@class,'subject_href')]"));
                  String url = urlElement.getAttribute("href").trim();
                  String title = urlElement.getText().trim();

                  WebElement imageUrlElement =
                      item.findElement(By.xpath(".//div[contains(@class,'houseImg')]/img"));
                  String imageUrl = imageUrlElement.getAttribute("src");

                  WebElement typeElement = item.findElement(By.xpath("(./td)[6]"));
                  String[] typeStrAry = typeElement.getText().trim().split(System.lineSeparator());
                  String type = typeStrAry[0];
                  String layout = typeStrAry.length > 1 ? typeStrAry[1] : "";

                  WebElement communityElement = item
                      .findElement(By.xpath("(.//div[contains(@class,'houseAddress')]/span)[2]"));
                  String community = communityElement.getText().trim();

                  List<WebElement> addressElementList =
                      item.findElements(By.xpath(".//span[contains(@class,'address_k')]/a"));
                  String address = addressElementList.stream().map(s -> s.getText().trim())
                      .collect(Collectors.joining(System.lineSeparator()));
                  if (address.isBlank()) {
                    WebElement addressElement =
                        item.findElement(By.xpath(".//div[contains(@class,'houseAddress')]"));
                    address = addressElement.getText();
                    List<WebElement> toRemoveElementList =
                        item.findElements(By.xpath(".//div[contains(@class,'houseAddress')]"
                            + "/*[self::a or self::span or self::div]"));
                    List<String> toRemoveList = toRemoveElementList.stream().map(s -> s.getText())
                        .sorted((x, y) -> x.length() < y.length() ? 1 : -1).toList();
                    for (String toRemove : toRemoveList) {
                      address = address.replace(toRemove, "").trim();
                    }
                  }

                  WebElement postDateElement = item.findElement(By.xpath("(./td)[2]"));
                  String postDate = postDateElement.getText().trim();

                  WebElement floorElement = item.findElement(By.xpath("(./td)[5]"));
                  String[] floorStrAry =
                      floorElement.getText().trim().split(System.lineSeparator());
                  String areaLand = floorStrAry[0];
                  String floor = floorStrAry.length > 1 ? floorStrAry[1] : "";

                  WebElement ageElement = item.findElement(By.xpath("(./td)[7]"));
                  String[] ageStrAry = ageElement.getText().trim().split(System.lineSeparator());
                  String age = ageStrAry[0];
                  String parking = ageStrAry.length > 1 ? ageStrAry[1] : "";

                  List<WebElement> sourceElementList =
                      item.findElements(By.xpath(".//span[contains(@class,'source')]/a"));
                  String source = sourceElementList.size() > 0
                      ? sourceElementList.stream().map(s -> s.getText().trim()).distinct()
                          .collect(Collectors.joining("/"))
                      : item.findElement(By.xpath(".//*[@class='houseSource']")).getText().trim();

                  WebElement priceElement = item.findElement(By.xpath("(./td)[3]"));
                  String[] priceStrAry =
                      priceElement.getText().trim().split(System.lineSeparator());
                  String priceTotal = priceStrAry[0];
                  String priceAveragePing = priceStrAry.length > 1 ? priceStrAry[1] : "";

                  WebElement areaElement = item.findElement(By.xpath("(./td)[4]"));
                  String[] areaStrAry = areaElement.getText().trim().split(System.lineSeparator());
                  String areaTotal = areaStrAry[0];
                  String areaMain = areaStrAry.length > 1 ? areaStrAry[1] : "";

                  String description = title + System.lineSeparator() + type + " " + layout + " "
                      + age + "y " + areaTotal + " " + areaMain + " " + floor + " " + community
                      + " " + address + " " + postDate + " " + parking + System.lineSeparator()
                      + priceTotal + " " + priceAveragePing;

                  // save for each property
                  BigFunService bigFunService =
                      CrawlerBeanConfiguration.getAppContext().getBean(BigFunService.class);
                  RealEstate toAdd = newRealEstate(url, imageUrl, title, type, layout, community,
                      address, floor, age, parking, priceTotal, priceAveragePing, areaLand,
                      areaTotal, areaMain, source, postDate, botName);
                  boolean isExist = bigFunService.existsRealEstate(toAdd.getUrl());
                  if (!isExist) {
                    bigFunService.addRealEstate(toAdd);
                    LOGGER.info(String.format("%s:%s added", RealEstate.class.getName(), url));
                    MeterRegistry meterRegistry =
                        CrawlerBeanConfiguration.getAppContext().getBean(MeterRegistry.class);
                    String meterNameNew = "crawler_bigfun_new_real_estate";
                    String meterNameSendPhoto =
                        "crawler_bigfun_new_real_estate.telegram_send_photo";
                    meterRegistry.counter(meterNameNew).increment();
                    if (false && !"".equals(chatId) && crawlerService.telegramSendPhoto(chatId,
                        chatToken, String.format("%s %s", url, description), imageUrl).block()) {
                      meterRegistry.counter(meterNameSendPhoto).increment();

                      // telegram rate limit
                      Thread.sleep(3000);
                    }
                  }
                } catch (Exception ex) {
                  LOGGER.warn(RealEstate.class.getName() + " element parse error", ex);
                }
              }

              // to next page
              try {
                WebElement nextLinkElement = ac.getWebDriver().findElement(By.xpath(
                    "//li[contains(@class,'pagination-item-active')]/following-sibling::li"));
                if (nextLinkElement.getAttribute("class").contains("pagination-item-disable")) {
                  mp.noNextPhase();
                } else {
                  nextLinkElement.click();
                  FluentWait<WebDriver> wait =
                      new FluentWait<>(ac.getWebDriver()).withTimeout(Duration.ofMillis(10000));
                  wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                      By.xpath("//div[@id='top-link']//a[contains(text(),'登出')]")));
                  wait.until(
                      ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("total_result")));
                  wait.until(
                      ExpectedConditions.invisibilityOfElementLocated(By.id("loading-cover")));
                }
              } catch (ElementClickInterceptedException ex) {
                LOGGER.info("{} next link error, retrying...", getName());
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
            }).build(this, BigFunCrawler.class.getName() + "(" + entryUrl + ")");
      } catch (Exception ex) {
        LOGGER.error("Create crawler error!", ex);
      }
    }

    private static RealEstate newRealEstate(String url, String imageUrl, String title, String type,
        String layout, String community, String address, String floor, String age, String parking,
        String priceTotal, String priceAveragePing, String areaLand, String areaTotal,
        String areaMain, String source, String postDate, String maintainer) {
      RealEstate obj = new RealEstate();
      obj.setUrl(url);
      obj.setImageUrl(imageUrl);
      obj.setTitle(title);
      obj.setType(type);
      obj.setLayout(layout);
      if (community.equals("社區")) {
        community = "";
      }
      obj.setCommunity(community);
      obj.setAddress(address);
      obj.setFloor(floor);
      if (!age.isBlank() && !age.contains("-")) {
        obj.setAge(new BigDecimal(age));
      }
      obj.setParking(parking);
      if (!priceTotal.isBlank() && !priceTotal.contains("-")) {
        obj.setPriceTotal(new BigDecimal(priceTotal.replace("萬", "")));
      }
      if (!priceAveragePing.isBlank() && !priceAveragePing.contains("-")) {
        obj.setPriceAveragePing(new BigDecimal(priceAveragePing.replace("萬/坪", "")));
      }
      if (!areaLand.isBlank() && !areaLand.contains("-")) {
        obj.setAreaLand(new BigDecimal(areaLand.replace("坪", "")));
      }
      if (!areaTotal.isBlank() && !areaTotal.contains("-")) {
        obj.setAreaTotal(new BigDecimal(areaTotal.replace("坪", "")));
      }
      if (!areaMain.isBlank() && !areaMain.contains("-")) {
        obj.setAreaMain(new BigDecimal(areaMain.replace("坪", "")));
      }
      obj.setSite("BigFun");
      obj.setSource(source);
      if (!postDate.isBlank()) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        obj.setPostDate(LocalDateTime.parse(postDate + " 00:00:00", formatter));
      }
      obj.setCreateUser(maintainer);
      obj.setCreateDate(LocalDateTime.now());
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
