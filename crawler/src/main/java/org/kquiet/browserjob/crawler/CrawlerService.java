package org.kquiet.browserjob.crawler;

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
import java.util.stream.Collectors;
import org.kquiet.browserjob.crawler.dao.BotConfigRepository;
import org.kquiet.browserjob.crawler.dao.CrawlerDao;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

  @WithSpan
  public boolean notifyTelegram(String token, @SpanAttribute("chatId") String chatId,
      @SpanAttribute("imageUrl") String imageUrl, @SpanAttribute("caption") String caption) {

    return dao.notifyTelegram(token, chatId, imageUrl, caption);
  }

  @WithSpan
  public boolean notifyTelegram(String token, @SpanAttribute("chatId") String chatId, File photo,
      @SpanAttribute("caption") String caption) {
    return dao.notifyTelegram(token, chatId, photo, caption);
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
