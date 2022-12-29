package org.kquiet.browserjob.crawler.dao;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.kquiet.browserjob.crawler.util.DoubleSerializer;
import org.kquiet.browserjob.crawler.util.FloatSerializer;
import org.kquiet.browserjob.crawler.util.NetHelper;
import org.kquiet.browserjob.crawler.util.SqlTimestampSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * crawler dao.
 *
 * @author monkey
 *
 */
@Component
public class CrawlerDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerDao.class);
  private static final String TELEGRAM_URL_SEND_PHOTO = "https://api.telegram.org/bot%s/sendPhoto";

  private NetHelper netHelper = new NetHelper();
  private ObjectMapper objectMapper = getDefaultMapperForJson();

  /**
   * notify through telegram sendPhoto api.
   *
   * @param token telegram bot token
   * @param chatId telegram chat id
   * @param imageUrl image url
   * @param caption image caption
   * @return api result
   */
  public boolean notifyTelegram(String token, String chatId, String imageUrl, String caption) {
    String apiUrl = String.format(TELEGRAM_URL_SEND_PHOTO, token);
    Map<String, Object> jsonBodyParam = new HashMap<>();
    jsonBodyParam.put("chat_id", chatId);
    jsonBodyParam.put("photo", imageUrl);
    jsonBodyParam.put("caption", caption);
    try {
      HttpResult result = httpJson("post", apiUrl, jsonBodyParam);
      LOGGER.debug("notifyTelegram result:{} {}", result.getStatusCode(), result.getResponse());
      return result.getStatusCode() == 200;
    } catch (Exception ex) {
      LOGGER.warn("notifyTelegram fail:{}", caption, ex);
      return false;
    }
  }

  /**
   * notify through telegram sendPhoto api.
   *
   * @param token telegram bot token
   * @param chatId telegram chat id
   * @param photo image file
   * @param caption image caption
   * @return api result
   */
  public boolean notifyTelegram(String token, String chatId, File photo, String caption) {
    String apiUrl = String.format(TELEGRAM_URL_SEND_PHOTO, token);
    HttpEntity entity =
        MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("photo", photo, ContentType.MULTIPART_FORM_DATA, null)
            .addTextBody("chat_id", chatId).addTextBody("caption", caption).build();

    try {
      HttpResult result = http("post", apiUrl, entity);
      LOGGER.debug("notifyTelegram result:{} {}", result.getStatusCode(), result.getResponse());
      return result.getStatusCode() == 200;
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
        netHelper.addHeader(r, httpHeader);
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
          netHelper.httpRequest(httpMethod, url).connectTimeout(10000).socketTimeout(10000);
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

  private ObjectMapper getDefaultMapperForJson() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getFactory().enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
    SimpleModule module = new SimpleModule();
    module.addSerializer(java.sql.Timestamp.class, new SqlTimestampSerializer());
    module.addSerializer(Double.class, new DoubleSerializer());
    module.addSerializer(double.class, new DoubleSerializer());
    module.addSerializer(Float.class, new FloatSerializer());
    module.addSerializer(float.class, new FloatSerializer());
    mapper.registerModule(module);
    return mapper;
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
