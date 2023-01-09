package org.kquiet.browserjob.crawler.dao;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.kquiet.browserjob.crawler.util.DoubleSerializer;
import org.kquiet.browserjob.crawler.util.FloatSerializer;
import org.kquiet.browserjob.crawler.util.SqlTimestampSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * crawler dao.
 *
 * @author monkey
 *
 */
public class CrawlerDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerDao.class);

  @Value("${BROWSERSCHEDULER_TELEGRAMAPI_SENDPHOTOURL:https://api.telegram.org/bot%s/sendPhoto}")
  private String telegramSendPhotoUrl = "https://api.telegram.org/bot%s/sendPhoto";

  private ObjectMapper objectMapper = getDefaultMapperForJson();

  private HttpClient.Builder httpClientBuilder =
      HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.NORMAL);

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
    String apiUrl = String.format(telegramSendPhotoUrl, token);
    Map<String, Object> jsonBodyParam = new HashMap<>();
    jsonBodyParam.put("chat_id", chatId);
    jsonBodyParam.put("photo", imageUrl);
    jsonBodyParam.put("caption", caption);
    try {
      HttpResult result = httpPostJson(apiUrl, jsonBodyParam);
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
    String apiUrl = String.format(telegramSendPhotoUrl, token);
    HttpEntity entity =
        MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("photo", photo, ContentType.MULTIPART_FORM_DATA, null)
            .addTextBody("chat_id", chatId).addTextBody("caption", caption).build();

    try {
      HttpResult result = httpPostMultipart(apiUrl, entity);
      LOGGER.debug("notifyTelegram result:{} {}", result.getStatusCode(), result.getResponse());
      return result.getStatusCode() == 200;
    } catch (Exception ex) {
      LOGGER.warn("notifyTelegram fail:{}", caption, ex);
      return false;
    }
  }

  HttpResult httpPostJson(String url, Map<String, Object> jsonBodyParam)
      throws URISyntaxException, IOException, InterruptedException {
    HttpClient httpClient = httpClientBuilder.build();
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(new URI(url)).header("Content-Type",
        "application/json; charset=UTF-8");

    if (jsonBodyParam != null && !jsonBodyParam.isEmpty()) {
      requestBuilder.POST(BodyPublishers.ofString(objectMapper.writeValueAsString(jsonBodyParam),
          StandardCharsets.UTF_8));
    }

    HttpResponse<String> resp =
        httpClient.send(requestBuilder.build(), BodyHandlers.ofString(StandardCharsets.UTF_8));

    return new HttpResult(resp.statusCode(), resp.body());
  }

  HttpResult httpPostMultipart(String url, HttpEntity httpEntity)
      throws URISyntaxException, IOException, InterruptedException {
    HttpClient httpClient = httpClientBuilder.build();
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(new URI(url)).header("Content-Type",
        httpEntity.getContentType().getValue());

    Pipe pipe = Pipe.open();
    CompletableFuture.runAsync(() -> {
      try (OutputStream outputStream = Channels.newOutputStream(pipe.sink())) {
        httpEntity.writeTo(outputStream);
      } catch (Exception ex) {
        LOGGER.warn("httpEntity write failed", ex);
      }
    });

    requestBuilder.POST(BodyPublishers.ofInputStream(() -> Channels.newInputStream(pipe.source())));

    HttpResponse<String> resp =
        httpClient.send(requestBuilder.build(), BodyHandlers.ofString(StandardCharsets.UTF_8));

    return new HttpResult(resp.statusCode(), resp.body());
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
