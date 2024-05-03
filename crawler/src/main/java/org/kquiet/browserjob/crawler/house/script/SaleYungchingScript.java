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
public class SaleYungchingScript extends Sale591Script {

  public SaleYungchingScript(JobBase job) {
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
        .waitUntil(ExpectedConditions.visibilityOfAllElementsLocatedBy(
            By.xpath("//li[contains(@class,'m-list-item')]")), 10000)
        .custom(ac -> {
          List<WebElement> propertyList =
              ac.getWebDriver().findElements(By.xpath("//li[contains(@class,'m-list-item')]"));
          LOGGER.info("{} found {} properties", getName(), propertyList.size());
          if (takeScreentshot && propertyList.size() == 0) {
            crawlerService.takeScreenShot(ac.getWebDriver());
          }

          for (WebElement property : propertyList) {
            try {
              WebElement urlE = property.findElement(By.xpath("./a[contains(@class,'item-img')]"));
              String url = urlE.getAttribute("href").trim();

              WebElement imageUrlE = urlE.findElement(By.xpath("(.//img)[1]"));
              String imageUrl = imageUrlE.getAttribute("src");

              WebElement descRootE =
                  property.findElement(By.xpath("./div[contains(@class,'item-info')]"));
              WebElement descTitleE =
                  descRootE.findElement(By.xpath("./a[contains(@class,'item-title')]/h3"));
              String descTitle = descTitleE.getText().trim();
              WebElement descAddressE = descRootE
                  .findElement(By.xpath("(./div[contains(@class,'item-description')]/span)[1]"));
              String descAddress = descAddressE.getText().trim();
              List<WebElement> descDetailElementList =
                  descRootE.findElements(By.xpath("./ul[contains(@class,'item-info-detail')]/li"));
              String descDetail = descDetailElementList.stream().map(s -> s.getText().trim())
                  .filter(s -> !s.isBlank()).collect(Collectors.joining(" "));
              List<WebElement> descTagElementList =
                  descRootE.findElements(By.xpath("./div[contains(@class,'item-tags')]/span"));
              String descTag = descTagElementList.stream().map(s -> s.getText().trim())
                  .filter(s -> !s.isBlank()).collect(Collectors.joining(" "));

              String description = descTitle + System.lineSeparator() + descAddress
                  + System.lineSeparator() + descDetail + System.lineSeparator() + descTag;

              WebElement priceE = property.findElement(
                  By.xpath("./div[contains(@class,'item-price')]/div[contains(@class,'price')]"));
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
