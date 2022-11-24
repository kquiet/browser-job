package org.kquiet.job.crawler;

import java.io.File;
import java.util.Map;
import org.kquiet.jobscheduler.JobBase;

/**
 * CommonBiz.
 *
 * @author monkey
 *
 */
public class CommonBiz {
  private final CommonDao dao;

  CommonBiz(JobBase job) {
    this.dao = new CommonDao(job);
  }

  Map<String, String> getBotConfig() {
    return dao.getBotConfig();
  }

  int addRentHouse(String url, String imageUrl, String description, String price) {
    return dao.addRentHouse(url, imageUrl, description, price);
  }

  int addSaleHouse(String url, String imageUrl, String description, String price) {
    return dao.addSaleHouse(url, imageUrl, description, price);
  }

  boolean notifyTelegram(String token, String chatId, String imageUrl, String caption) {
    return dao.notifyTelegram(token, chatId, imageUrl, caption);
  }

  boolean notifyTelegram(String token, String chatId, File photo, String caption) {
    return dao.notifyTelegram(token, chatId, photo, caption);
  }
}
