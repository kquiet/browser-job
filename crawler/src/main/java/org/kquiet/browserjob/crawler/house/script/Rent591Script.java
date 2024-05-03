package org.kquiet.browserjob.crawler.house.script;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browserjob.crawler.house.entity.RentHouse;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rent house 591 crawler.
 *
 * @author monkey
 */
public class Rent591Script extends Sale591Script {

  public Rent591Script(JobBase job) {
    super(job);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(SaleYungchingScript.class);

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
        .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(
            By.xpath("//section[contains(@class,'vue-list-rent-item')]/a")), 10000)
        .custom(ac -> {
          List<WebElement> propertyList = ac.getWebDriver()
              .findElements(By.xpath("//section[contains(@class,'vue-list-rent-item')]/a"));
          LOGGER.info("{} found {} properties", getName(), propertyList.size());
          if (takeScreentshot && propertyList.size() == 0) {
            crawlerService.takeScreenShot(ac.getWebDriver());
          }

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

              // process for parsed element
              this.processRentHouse(site, url, imageUrl, description, price, botName, chatId,
                  chatToken);
            } catch (Exception ex) {
              LOGGER.warn(RentHouse.class.getName() + " element parse error", ex);
            }
          }
        }).returnToComposerBuilder();

  }

  protected void processRentHouse(String site, String url, String imageUrl, String description,
      String price, String botName, String chatId, String chatToken) throws Exception {
    boolean isExist = houseService.existsRentHouse(url);
    if (!isExist) {
      houseService.addRentHouse(site, url, imageUrl, description, price, botName);
      LOGGER.info(String.format("%s:%s added", RentHouse.class.getName(), url));
      String meterNameNew = "crawler_" + site + "_new_rent_house";
      String meterNameSendPhoto = "crawler_" + site + "_new_rent_house.telegram_send_photo";
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
