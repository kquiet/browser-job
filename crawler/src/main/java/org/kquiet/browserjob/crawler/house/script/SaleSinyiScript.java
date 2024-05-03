package org.kquiet.browserjob.crawler.house.script;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browserscheduler.JobBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SaleYungching script builder.
 *
 * @author monkey
 *
 */
public class SaleSinyiScript extends Sale591Script {

  public SaleSinyiScript(JobBase job) {
    super(job);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(SaleSinyiScript.class);

  @Override
  public void accept(Map<String, String> configMap, ActionComposerBuilder scriptBuilder) {
    String botName = configMap.get("botName");
    String site = configMap.get("site");
    String entryUrl = configMap.get("entryUrl");
    String chatId = configMap.get("chatId");
    String chatToken = configMap.get("chatToken");
    boolean takeScreentshot = Boolean.parseBoolean(configMap.get("takeScreenshot"));

    scriptBuilder.prepareActionSequence().getUrl(entryUrl).justWait(3000)
        .waitUntil(ExpectedConditions.invisibilityOfElementLocated(
            By.xpath("//div[contains(@class,'loading-frame-lc')]")), 10000)
        .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(
            By.xpath("//div[contains(@class,'buy-list-item')]")), 10000)
        .waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.xpath(
            "//div[contains(@class,'buy-list-item')]/div[contains(@class,'LongInfoCardBody')]")),
            10000)
        .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(
            By.xpath("//div[contains(@class,'buy-list-item')]")), 10000)
        .custom(ac -> {
          List<WebElement> propertyList =
              ac.getWebDriver().findElements(By.xpath("//div[contains(@class,'buy-list-item')]"));
          LOGGER.info("{} found {} properties", getName(), propertyList.size());
          if (takeScreentshot && propertyList.size() == 0) {
            crawlerService.takeScreenShot(ac.getWebDriver());
          }

          for (WebElement property : propertyList) {
            try {
              WebElement urlE = property.findElement(By.xpath("./a"));
              String url = urlE.getAttribute("href").trim();

              WebElement imageUrlE =
                  property.findElement(By.xpath(".//div[contains(@class,'largeImg')]/img"));
              String imageUrl = imageUrlE.getAttribute("src");

              WebElement descRootE = property
                  .findElement(By.xpath(".//div[contains(@class,'LongInfoCard_Type_Left')]"));
              WebElement descTitleE = descRootE
                  .findElement(By.xpath("./div[contains(@class,'LongInfoCard_Type_Name')]"));
              String descTitle =
                  descTitleE.getText().trim().replaceAll(System.lineSeparator(), " ");
              List<WebElement> descAddressElementList = descRootE.findElements(
                  By.xpath("./div[contains(@class,'LongInfoCard_Type_Address')]/span"));
              String descAddress = descAddressElementList.stream().map(s -> s.getText().trim())
                  .filter(s -> !s.isBlank()).collect(Collectors.joining(" "));
              List<WebElement> descOtherElementList = descRootE.findElements(
                  By.xpath("./div[contains(@class,'LongInfoCard_Type_HouseInfo')]/span"));
              String descOther =
                  descOtherElementList.stream().map(s -> s.getText().trim().replaceAll(" ", ""))
                      .filter(s -> !s.isBlank()).collect(Collectors.joining(" "));
              WebElement descParkingE = descRootE
                  .findElement(By.xpath("./span[contains(@class,'LongInfoCard_Type_Parking')]"));
              String descParking = descParkingE.getText().trim();

              String description = descTitle + System.lineSeparator() + descAddress
                  + System.lineSeparator() + descOther
                  + (descParking.isBlank() ? "" : System.lineSeparator() + descParking);

              WebElement priceE = property.findElement(
                  By.xpath("(.//div[contains(@class,'LongInfoCard_Type_Right')]/div)[2]"));
              String price = priceE.getText().trim();

              // process for parsed element
              this.processSaleHouse(site, url, imageUrl, description, price, botName, chatId,
                  chatToken);
            } catch (Exception ex) {
              LOGGER.warn(this.getClass().getName() + " encounter error", ex);
            }
          }
        }).returnToComposerBuilder();
  }

}
