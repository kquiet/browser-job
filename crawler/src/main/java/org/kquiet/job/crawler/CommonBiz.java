package org.kquiet.job.crawler;

import java.util.HashMap;
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
    String apiUrl = "https://api.telegram.org/bot" + token + "/sendPhoto";
    Map<String, Object> jsonBodyParam = new HashMap<>();
    jsonBodyParam.put("chat_id", chatId);
    jsonBodyParam.put("photo", imageUrl);
    jsonBodyParam.put("caption", caption);
    try {
      CommonDao.HttpApiResult result = dao.httpApiJson("post",apiUrl, jsonBodyParam);
      LOGGER.debug("notifyTelegram result:{} {}", result.getStatusCode(), result.getResponse());
      return true;
    } catch (Exception ex) {
      LOGGER.warn("notifyTelegram fail:{}", caption, ex);
      return false;
    }
  }
}
