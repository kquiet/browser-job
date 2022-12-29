package org.kquiet.browserjob.crawler;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.browserjob.crawler.dao.BotConfigRepository;
import org.kquiet.browserjob.crawler.dao.CrawlerDao;
import org.kquiet.browserjob.crawler.entity.BotConfig;
import org.kquiet.browserjob.crawler.entity.BotId;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceTest {
  @Mock
  CrawlerDao daoObj;

  @Mock
  BotConfigRepository botConfigRepo;

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

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, imageUrl1, caption1"})
  void notifyTelegramTest(String token, String chatId, String imageUrl, String caption) {
    when(daoObj.notifyTelegram(token, chatId, imageUrl, caption)).thenReturn(true);

    assertEquals(true, crawlerService.notifyTelegram(token, chatId, imageUrl, caption));
  }

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, photoPath1, caption1"})
  void notifyTelegramTest(String token, String chatId, File photo, String caption) {
    when(daoObj.notifyTelegram(token, chatId, photo, caption)).thenReturn(true);

    assertEquals(true, crawlerService.notifyTelegram(token, chatId, photo, caption));
  }
}
