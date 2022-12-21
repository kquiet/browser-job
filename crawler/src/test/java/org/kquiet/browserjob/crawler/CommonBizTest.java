package org.kquiet.browserjob.crawler;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.browserjob.crawler.CommonBiz;
import org.kquiet.browserjob.crawler.CommonDao;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommonBizTest {
  @Mock
  CommonDao daoObj;

  @InjectMocks
  CommonBiz bizObj;

  @ParameterizedTest(name = "{0}")
  @CsvSource({"botName1"})
  void getBotConfigTest(String botName) {
    when(daoObj.getBotConfig("botName1")).thenReturn(Map.of("param1", "value1"));

    assertEquals("value1", bizObj.getBotConfig(botName).get("param1"));
  }

  @ParameterizedTest()
  @CsvSource({"url1, imageUrl1, description1, price1, maintainer1"})
  void addRentHouseTest(String url, String imageUrl, String description, String price,
      String maintainer) {
    when(daoObj.addRentHouse(url, imageUrl, description, price, maintainer)).thenReturn(1);

    assertEquals(1, bizObj.addRentHouse(url, imageUrl, description, price, maintainer));
  }

  @ParameterizedTest()
  @CsvSource({"url1, imageUrl1, description1, price1, maintainer1"})
  void addSaleHouseTest(String url, String imageUrl, String description, String price,
      String maintainer) {
    when(daoObj.addSaleHouse(url, imageUrl, description, price, maintainer)).thenReturn(1);

    assertEquals(1, bizObj.addSaleHouse(url, imageUrl, description, price, maintainer));
  }

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, imageUrl1, caption1"})
  void notifyTelegramTest(String token, String chatId, String imageUrl, String caption) {
    when(daoObj.notifyTelegram(token, chatId, imageUrl, caption)).thenReturn(true);

    assertEquals(true, bizObj.notifyTelegram(token, chatId, imageUrl, caption));
  }

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, photoPath1, caption1"})
  void notifyTelegramTest(String token, String chatId, File photo, String caption) {
    when(daoObj.notifyTelegram(token, chatId, photo, caption)).thenReturn(true);

    assertEquals(true, bizObj.notifyTelegram(token, chatId, photo, caption));
  }
}
