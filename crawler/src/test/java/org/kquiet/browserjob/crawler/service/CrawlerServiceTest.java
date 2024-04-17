package org.kquiet.browserjob.crawler.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.browserjob.crawler.dao.BotConfigRepository;
import org.kquiet.browserjob.crawler.dao.CrawlerDao;
import org.kquiet.browserjob.crawler.entity.BotConfig;
import org.kquiet.browserjob.crawler.entity.BotId;
import org.kquiet.browserjob.crawler.service.CrawlerService;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceTest {
  @TempDir
  static Path tempDir;

  @Mock
  CrawlerDao daoObj;

  @Mock
  BotConfigRepository botConfigRepo;

  @Mock(extraInterfaces = {TakesScreenshot.class})
  WebDriver webdriver;

  @InjectMocks
  CrawlerService crawlerService;

  @ParameterizedTest(name = "{0}")
  @CsvSource({"botName1"})
  void getBotConfigTest(String botName) {
    BotId expectedId = new BotId();
    expectedId.setBotname(botName);
    expectedId.setKey("param1");
    BotConfig expectedEntity = new BotConfig();
    expectedEntity.setBotId(expectedId);
    expectedEntity.setValue("value1");
    when(botConfigRepo.findByBotIdBotname(botName)).thenReturn(List.of(expectedEntity));

    assertEquals("value1", crawlerService.getBotConfig(botName).get("param1"));
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

  @ParameterizedTest()
  @CsvSource({"chatId1, token1, caption1, imageUrl1"})
  void telegramSendPhotoTest(String chatId, String token, String caption, String photo) {
    SendPhotoRequest req = createSendPhotoRequest(chatId, token, caption, photo);
    when(daoObj.telegramSendPhoto(req, Optional.empty())).thenReturn(Mono.just(true));

    assertEquals(true, crawlerService.telegramSendPhoto(chatId, token, caption, photo).block());
  }

  @ParameterizedTest()
  @CsvSource({"chatId1, token1, caption1, photoPath1"})
  void telegramSendPhotoTest(String chatId, String token, String caption, File photoFile) {
    SendPhotoRequest req = createSendPhotoRequest(chatId, token, caption, null);
    when(daoObj.telegramSendPhoto(req, Optional.of(photoFile))).thenReturn(Mono.just(true));

    assertEquals(true, crawlerService.telegramSendPhoto(chatId, token, caption, photoFile).block());
  }

  @Test()
  void takeScreenShot() {
    when(((TakesScreenshot) webdriver).getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[0]);
    when(webdriver.getPageSource()).thenReturn("");

    List<Path> resultPaths = crawlerService.takeScreenShot(webdriver, tempDir);
    assertEquals(2, resultPaths.size());
    assertEquals(tempDir, resultPaths.get(0).getParent());
    assertEquals(tempDir, resultPaths.get(1).getParent());
  }
}
