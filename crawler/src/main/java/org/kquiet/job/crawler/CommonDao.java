package org.kquiet.job.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.kquiet.browserscheduler.JobBase;
import org.kquiet.job.crawler.util.JacksonUtility;
import org.kquiet.job.crawler.util.MybatisUtility;
import org.kquiet.job.crawler.util.NetUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommonDao.
 *
 * @author monkey
 *
 */
public class CommonDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommonDao.class);
  private static final String MYBATIS_FILE_NAME = "mybatis-config.xml";
  private static final String MAPPER_FILE_NAME = "mybatis-mapper.xml";

  private final JobBase job;
  private final SqlSessionFactory sqlSessionFactory;
  private final ObjectMapper objectMapper;

  CommonDao(JobBase job) {
    this.job = job;
    this.objectMapper = JacksonUtility.getDefaultMapperForJson();
    this.sqlSessionFactory = MybatisUtility.getSessionFactory(MYBATIS_FILE_NAME,
        Arrays.asList(MAPPER_FILE_NAME), "crawler");
  }

  Map<String, String> getBotConfig() {
    Map<String, String> result = new HashMap<>();
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Map<String, Object> param = new HashMap<>();
      param.put("botName", job.getParameter("botName"));
      List<Map<String, String>> queryResult =
          session.<Map<String, String>>selectList("GetBotConfig", param);
      if (queryResult != null) {
        for (Map<String, String> row : queryResult) {
          result.put(row.get("key"), row.get("value"));
        }
      }
      return result;
    } catch (Exception ex) {
      LOGGER.error("getBotConfig error!", ex);
      return null;
    }
  }

  int addRentHouse(String url, String imageUrl, String description, String price) {
    try {
      try (SqlSession session = sqlSessionFactory.openSession(true)) {
        url = Optional.ofNullable(url).orElse("");
        imageUrl = Optional.ofNullable(imageUrl).orElse("");
        description = Optional.ofNullable(description).orElse("");
        price = Optional.ofNullable(price).orElse("");

        Map<String, Object> param = new HashMap<>();
        param.put("url", url);
        param.put("imageUrl", imageUrl);
        param.put("description", description);
        param.put("price", price);
        param.put("createUser", job.getParameter("botName"));
        int result = session.insert("AddRentHouse", param);
        return result;
      }
    } catch (Exception ex) {
      LOGGER.error("addRentHouse error!", ex);
      return -1;
    }
  }

  int addSaleHouse(String url, String imageUrl, String description, String price) {
    try {
      try (SqlSession session = sqlSessionFactory.openSession(true)) {
        url = Optional.ofNullable(url).orElse("");
        imageUrl = Optional.ofNullable(imageUrl).orElse("");
        description = Optional.ofNullable(description).orElse("");
        price = Optional.ofNullable(price).orElse("");

        Map<String, Object> param = new HashMap<>();
        param.put("url", url);
        param.put("imageUrl", imageUrl);
        param.put("description", description);
        param.put("price", price);
        param.put("createUser", job.getParameter("botName"));
        int result = session.insert("AddSaleHouse", param);
        return result;
      }
    } catch (Exception ex) {
      LOGGER.error("addSaleHouse error!", ex);
      return -1;
    }
  }

  boolean notifyTelegram(String token, String chatId, String imageUrl, String caption) {
    String apiUrl = "https://api.telegram.org/bot" + token + "/sendPhoto";
    Map<String, Object> jsonBodyParam = new HashMap<>();
    jsonBodyParam.put("chat_id", chatId);
    jsonBodyParam.put("photo", imageUrl);
    jsonBodyParam.put("caption", caption);
    try {
      HttpResult result = httpJson("post", apiUrl, jsonBodyParam);
      LOGGER.debug("notifyTelegram result:{} {}", result.getStatusCode(), result.getResponse());
      return true;
    } catch (Exception ex) {
      LOGGER.warn("notifyTelegram fail:{}", caption, ex);
      return false;
    }
  }

  boolean notifyTelegram(String token, String chatId, File photo, String caption) {
    String apiUrl = "https://api.telegram.org/bot" + token + "/sendPhoto";
    HttpEntity entity =
        MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("photo", photo, ContentType.MULTIPART_FORM_DATA, null)
            .addTextBody("chat_id", chatId).addTextBody("caption", caption).build();

    try {
      HttpResult result = http("post", apiUrl, entity);
      LOGGER.debug("notifyTelegram result:{} {}", result.getStatusCode(), result.getResponse());
      return true;
    } catch (Exception ex) {
      LOGGER.warn("notifyTelegram fail:{}", caption, ex);
      return false;
    }
  }

  HttpResult httpJson(String httpMethod, String url, Map<String, Object> jsonBodyParam)
      throws ClientProtocolException, IOException {
    Consumer<Request> consumer = (r) -> {
    };

    if (jsonBodyParam != null && !jsonBodyParam.isEmpty()) {
      Map<String, String> httpHeader = new HashMap<>();
      httpHeader.put("Content-type", "application/json; charset=UTF-8");

      byte[] body = objectMapper.writeValueAsString(jsonBodyParam).getBytes("UTF-8");

      consumer = (r) -> {
        NetUtility.addHeader(r, httpHeader);
        r.bodyByteArray(body);
      };
    }

    return http(httpMethod, url, consumer);
  }

  HttpResult http(String httpMethod, String url, HttpEntity bodyEntity)
      throws ClientProtocolException, IOException {
    Consumer<Request> consumer = (r) -> {
    };

    if (bodyEntity != null) {
      consumer = (r) -> {
        r.body(bodyEntity);
      };
    }

    return http(httpMethod, url, consumer);
  }

  private HttpResult http(String httpMethod, String url, Consumer<Request> requestConsumer)
      throws ClientProtocolException, IOException {
    HttpResponse resp = null;
    try {
      Request req =
          NetUtility.httpRequest(httpMethod, url).connectTimeout(10000).socketTimeout(10000);
      requestConsumer.accept(req);

      resp = req.execute().returnResponse();
      int statusCode = resp.getStatusLine().getStatusCode();
      String response = EntityUtils.toString(resp.getEntity(), "UTF-8");
      return new HttpResult(statusCode, response);
    } finally {
      if (resp != null) {
        EntityUtils.consume(resp.getEntity());
      }
    }
  }

  /**
   * HttpResult.
   *
   * @author monkey
   *
   */
  public class HttpResult {
    private final int statusCode;
    private final String response;

    public HttpResult(int statusCode, String response) {
      this.statusCode = statusCode;
      this.response = response;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getResponse() {
      return response;
    }
  }
}
