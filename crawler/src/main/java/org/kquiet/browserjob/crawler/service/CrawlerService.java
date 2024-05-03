package org.kquiet.browserjob.crawler.service;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.kquiet.browserjob.crawler.dao.BotConfigRepository;
import org.kquiet.browserjob.crawler.dao.CrawlerDao;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

/**
 * Common business object.
 *
 * @author monkey
 *
 */
public class CrawlerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerService.class);

  @Autowired
  private CrawlerDao dao;

  @Autowired
  private BotConfigRepository botConfigRepo;

  @WithSpan
  public Map<String, String> getBotConfig(@SpanAttribute("botName") String botName) {
    return botConfigRepo.findByBotIdBotname(botName).stream()
        .collect(Collectors.toMap(s -> s.getBotId().getKey(), s -> s.getValue()));
  }

  /**
   * Send message with photo url through telegram.
   *
   * @param chatId telegram chat id
   * @param token telegram bot token
   * @param caption message caption
   * @param photo photo url
   * @return whether messsage is sent
   */
  @WithSpan
  public Mono<Boolean> telegramSendPhoto(@SpanAttribute("chatId") String chatId,
      @SpanAttribute("token") String token, @SpanAttribute("caption") String caption,
      @SpanAttribute("photo") String photo) {

    return dao.telegramSendPhoto(createSendPhotoRequest(chatId, token, caption, photo),
        Optional.empty());
  }

  /**
   * Send message with photo url through telegram.
   *
   * @param chatId telegram chat id
   * @param token telegram bot token
   * @param caption message caption
   * @param photoFile photo file
   * @return whether messsage is sent
   */
  @WithSpan
  public Mono<Boolean> telegramSendPhoto(@SpanAttribute("chatId") String chatId,
      @SpanAttribute("token") String token, @SpanAttribute("caption") String caption,
      @SpanAttribute("photoFile") File photoFile) {

    return dao.telegramSendPhoto(createSendPhotoRequest(chatId, token, caption, null),
        Optional.of(photoFile));
  }

  private SendPhotoRequest createSendPhotoRequest(String chatId, String token, String caption,
      String photo) {
    SendPhotoRequest obj = new SendPhotoRequest();
    obj.setChatId(chatId);
    obj.setCaption(caption);
    obj.setToken(token);
    obj.setPhoto(photo);
    return obj;
  }

  /**
   * take screenshot.
   *
   * @param webdriver webdriver
   * @param parentPaths parent path to save screenshot file
   * @return list of screenshot and page content files
   */
  @WithSpan
  public List<Path> takeScreenShot(WebDriver webdriver,
      @SpanAttribute("paths") Path... parentPaths) {
    byte[] screenShotBytes = ((TakesScreenshot) webdriver).getScreenshotAs(OutputType.BYTES);
    byte[] pageBytes = webdriver.getPageSource().getBytes();
    Path parentPath = parentPaths.length > 0 ? parentPaths[0].toAbsolutePath()
        : Path.of("", "log").toAbsolutePath();
    String baseFileName =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
    String screentShotFileName = baseFileName + ".png";
    String pageFileName = baseFileName + ".html";
    Path screenshotFilePath = parentPath.resolve(screentShotFileName);
    Path pageFilePath = parentPath.resolve(pageFileName);
    try {
      Files.createDirectories(parentPath);
      Files.write(pageFilePath, pageBytes);
      Files.write(screenshotFilePath, screenShotBytes);
      LOGGER.info("takeScreentShot succeed: {}, {}", screenshotFilePath, pageFilePath);
      return List.of(screenshotFilePath, pageFilePath);
    } catch (IOException ex) {
      LOGGER.warn(String.format("takeScreenShot failed: %s, %s", screenshotFilePath, pageFilePath),
          ex);
      return List.of();
    }
  }
}
