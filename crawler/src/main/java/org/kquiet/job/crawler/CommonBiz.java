package org.kquiet.job.crawler;

import java.io.File;
import java.util.Map;

import org.kquiet.jobscheduler.JobBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonBiz {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommonBiz.class);

  private final CommonDao dao;

  CommonBiz(JobBase job) {
    this.dao = new CommonDao(job);
  }

  Map<String, String> getBotConfig() {
    return dao.getBotConfig();
  }

  int createCase(String url, String imageUrl, String description, String price) {
    return dao.createCase(url, imageUrl, description, price);
  }

  boolean notifyTelegram(String token, String chatId, String imageUrl, String caption) {
    return dao.notifyTelegram(token, chatId, imageUrl, caption);
  }
  
  boolean notifyTelegram(String token, String chatId, File photo, String caption) {
    return dao.notifyTelegram(token, chatId, photo, caption);
  }
}
