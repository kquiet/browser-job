package org.kquiet.job.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.kquiet.browser.ActionComposerBuilder;
import org.kquiet.browser.BasicActionComposer;
import org.kquiet.browser.action.ReplyAlert.Decision;
import org.kquiet.jobscheduler.JobBase;
import org.kquiet.jobscheduler.JobController.InteractionType;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RailwayBookController extends JobBase {
  private static final Logger logger = LoggerFactory.getLogger(RailwayBookController.class);

  public RailwayBookController(String jobName) {
    super(jobName);
  }

  @Override
  public void run() {
    try {
      RailwayBookCrawler bac = new RailwayBookCrawler(this);
      this.submitBrowserTask(bac);
      bac.get();
      logger.info("Job {} finished", getJobName());
    } catch (Exception e) {
      logger.error("generate crawler task error:{}", e);
    }
  }

  private void submitBrowserTask(RailwayBookCrawler bac) {
    registerInternalBrowserTask(bac);
    logger.info("Browser task({}) accepted", bac.getName());
  }
  
  private static class TicketOrder {
    private final String fromStation;
    private final String toStation;
    private final String atDate;
    private final String trainNo1;
    private final String trainNo2;
    private final String trainNo3;
    private final String quantity;
    private final String changeSeat;
    
    public TicketOrder(JobBase job, int sequence) {
      fromStation = job.getParameter("fromStation_" + sequence);
      toStation = job.getParameter("toStation_" + sequence);
      atDate = job.getParameter("atDate_" + sequence);
      trainNo1 = job.getParameter("trainNo1_" + sequence);
      trainNo2 = job.getParameter("trainNo2_" + sequence);
      trainNo3 = job.getParameter("trainNo3_" + sequence);
      quantity = job.getParameter("quantity_" + sequence);
      changeSeat = job.getParameter("changeSeat_" + sequence);
    }

    public String getFromStation() {
      return fromStation;
    }

    public String getToStation() {
      return toStation;
    }

    public String getAtDate() {
      return atDate;
    }

    public String getTrainNo1() {
      return trainNo1;
    }

    public String getTrainNo2() {
      return trainNo2;
    }

    public String getTrainNo3() {
      return trainNo3;
    }

    public String getQuantity() {
      return quantity;
    }

    public String getChangeSeat() {
      return changeSeat;
    }

    @Override
    public String toString() {
      return String.join("+", fromStation, toStation, atDate, trainNo1, trainNo2, trainNo3,
          quantity, changeSeat);
    }
  }

  private static class RailwayBookCrawler extends BasicActionComposer {
    private static final Logger logger = LoggerFactory.getLogger(RailwayBookCrawler.class);
    private static Set<String> submitted = Collections.newSetFromMap(
        new ConcurrentHashMap<String, Boolean>());
    
    private ResultStatus resultStatus = ResultStatus.WaitingToDo;
    private String message;

    public RailwayBookCrawler(RailwayBookController controller) {
      super();
      config(controller);
    }

    private void config(RailwayBookController controller) {
      try {
        boolean autoSubmit = Boolean.parseBoolean(controller.getParameter("autoSubmit"));
        int orderCount = Integer.parseInt(controller.getParameter("orderCount"));
        List<TicketOrder> orderList = new ArrayList<>();
        for (int i = 0; i < orderCount; i++) {
          orderList.add(new TicketOrder(controller, i + 1));
        }
        
        String pid = controller.getParameter("pid");
        String pageUrl = "https://www.railway.gov.tw/tra-tip-web/tip/tip001/tip122/trip" 
                      + (orderCount >= 2 ? "Two" : "One") + "/byTrainNo"; 
        By pidBy = By.xpath("//input[@name='pid']");
        By captchaFrameBy = By.xpath("//div[@class='g-recaptcha']/div//iframe");
        By captchaBy = By.id("recaptcha-anchor");
        Function<Integer, By> fromStationByMaker = (seq) ->
            By.xpath("(//select[contains(@name,'startStation')])[" + seq + "]");
        Function<Integer, By> toStationByMaker = (seq) ->
            By.xpath("(//select[contains(@name,'endStation')])[" + seq + "]");
        Function<Integer, By> atDateByMaker = (seq) ->
            By.xpath("(//select[contains(@name,'rideDate')])[" + seq + "]");
        BiFunction<Integer, Integer, By> trainNoByMaker = (trainNo, seq) ->
            By.xpath("(//input[contains(@name,'trainNoList[" + trainNo + "]')])[" + seq + "]");
        Function<Integer, By> quantityByMaker = (seq) ->
            By.xpath("(//select[contains(@name,'normalQty')])[" + seq + "]");
        Function<Integer, By> changeSeatByMaker = (seq) ->
            By.xpath("(//select[contains(@name,'chgSeat')])[" + seq + "]");
        BiFunction<Integer, String, By> atDateOptionByMaker = (seq, option) ->
            By.xpath("(//select[contains(@name,'rideDate')])[" + seq + "]/option[@value='"
                + option + "']");
        //1. 驗證程序已過期，請再次勾選核取方塊以產生新的問題
        //2. reCAPTCHA 要求驗證. 
        //3. 您已通過驗證
        //rc-imageselect
        By submitBy = By.id("submitBtn");
        
        new ActionComposerBuilder()
          .prepareActionSequence()
            .prepareIfThenElse(ac -> submitted.contains(orderList.toString()))
              .then()
                .custom(ac -> {
                  this.setResultStatus(ResultStatus.AlreadyFinished);
                  ac.skipToFail();
                })
                .endActionSequence()
              .endIf()
            .getUrl(pageUrl)
            .waitUntil(ExpectedConditions.and(
                ExpectedConditions.elementToBeClickable(submitBy),
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(captchaFrameBy),
                ExpectedConditions.visibilityOfElementLocated(captchaBy),
                ExpectedConditions.elementToBeClickable(captchaBy)
              ), 4000)
            .prepareIfThenElse(ac -> ExpectedConditions.numberOfElementsToBe(
                atDateOptionByMaker.apply(1, orderList.get(0).getAtDate()), 0)
                .apply(ac.getWebDriver()))
              .then()
                .custom(ac -> {
                  ac.skipToFail();
                  this.setResultStatus(ResultStatus.DateNotAvailable);
                  this.setMessage(orderList.get(0).getAtDate());
                })
                .endActionSequence()
              .endIf()
            .prepareIfThenElse(ac -> orderCount >= 2
                && ExpectedConditions.and(
                    ExpectedConditions.numberOfElementsToBe(
                        atDateOptionByMaker.apply(2, orderList.get(1).getAtDate()), 0)
                  ).apply(ac.getWebDriver()))
              .then()
                .custom(ac -> {
                  ac.skipToFail();
                  this.setResultStatus(ResultStatus.DateNotAvailable);
                  this.setMessage(orderList.get(1).getAtDate());
                })
                .endActionSequence()
              .endIf()
            .sendKey(pidBy, pid)
            .selectByValue(fromStationByMaker.apply(1), orderList.get(0).getFromStation())
            .selectByValue(toStationByMaker.apply(1), orderList.get(0).getToStation())
            .selectByValue(atDateByMaker.apply(1), orderList.get(0).getAtDate())
            .sendKey(trainNoByMaker.apply(0, 1), orderList.get(0).getTrainNo1())
            .sendKey(trainNoByMaker.apply(1, 1), orderList.get(0).getTrainNo2())
            .sendKey(trainNoByMaker.apply(2, 1), orderList.get(0).getTrainNo3())
            .selectByValue(quantityByMaker.apply(1), orderList.get(0).getQuantity())
            .selectByValue(changeSeatByMaker.apply(1), orderList.get(0).getChangeSeat())
            .prepareIfThenElse(ac -> orderCount >= 2)
              .then()
                .selectByValue(fromStationByMaker.apply(2), orderList.get(1).getFromStation())
                .selectByValue(toStationByMaker.apply(2),  orderList.get(1).getToStation())
                .selectByValue(atDateByMaker.apply(2), orderList.get(1).getAtDate())
                .sendKey(trainNoByMaker.apply(0, 2), orderList.get(1).getTrainNo1())
                .sendKey(trainNoByMaker.apply(1, 2), orderList.get(1).getTrainNo2())
                .sendKey(trainNoByMaker.apply(2, 2), orderList.get(1).getTrainNo3())
                .selectByValue(quantityByMaker.apply(2), orderList.get(1).getQuantity())
                .selectByValue(changeSeatByMaker.apply(2), orderList.get(1).getChangeSeat())
                .endActionSequence()
              .endIf()
            .prepareScrollToView(captchaBy, false).withInFrame(Arrays.asList(captchaFrameBy)).done()
            .prepareMouseOver(captchaBy).withInFrame(Arrays.asList(captchaFrameBy)).done()
            .waitUntil(ExpectedConditions.and(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(captchaFrameBy),
                ExpectedConditions.attributeContains(captchaBy,
                    "class", "recaptcha-checkbox-hover")
              ), 1000)
            .prepareClick(captchaBy).withInFrame(Arrays.asList(captchaFrameBy)).done()
            .prepareIfThenElse(ac -> autoSubmit)
              .then()
                .prepareWaitUntil(ExpectedConditions.and(
                    ExpectedConditions.frameToBeAvailableAndSwitchToIt(captchaFrameBy),
                    ExpectedConditions.attributeContains(captchaBy,
                        "class", "recaptcha-checkbox-checked"),
                    ExpectedConditions.not(
                        ExpectedConditions.attributeContains(captchaBy,
                            "class", "recaptcha-checkbox-hover"))
                    ), 5000).withTimeoutCallback(ac -> {
                      this.setResultStatus(ResultStatus.RecaptchaChallenge);
                      this.setMessage("have to try again");
                      ac.skipToFail();
                      submitted.add(orderList.toString());
                    }).done()
                .click(submitBy)
                .custom(ac -> {
                  this.setResultStatus(ResultStatus.Submitted);
                  submitted.add(orderList.toString());
                  ac.skipToSuccess();
                })
                .endActionSequence()
              .endIf()
            .custom(ac -> {
              controller.awaitInteraction();
              if (controller.getLatestInteraction() == InteractionType.Positive) {
                this.setResultStatus(ResultStatus.Submitted);
                submitted.add(orderList.toString());
                ac.skipToSuccess();
              } else {
                ac.skipToFail();
                this.setMessage("negative response");
              }
            })
            .returnToComposerBuilder()
          .onFail(ac -> {
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
          })
          .onSuccess(ac -> {
            this.setCloseWindow(false);
            logger.info("{} succeed({}): {}", getName(), this.getResultStatus(), this.getMessage());
          })
          .build(this, this.getClass().getName());
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
    WaitingToDo("WaitingToDo"),
    DateNotAvailable("DateNotAvailable"),
    RecaptchaChallenge("RecaptchaChallenge"),
    Submitted("Submitted"),
    Bingo("Bingo"),
    AlreadyFinished("AlreadyFinished");

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
