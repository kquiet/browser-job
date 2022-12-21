package org.kquiet.browserjob.crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.browserjob.crawler.CommonDao;
import org.kquiet.browserjob.crawler.util.NetHelper;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommonDaoTest {
  @Mock
  HttpEntity httpEntity;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  HttpResponse httpResp;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Request req;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  NetHelper netHelper;

  @Mock
  SqlSessionFactory sqlSessionFactory;

  @Mock
  SqlSession session;

  @InjectMocks
  CommonDao daoObj;

  @ParameterizedTest(name = "{0}")
  @CsvSource({"botName1"})
  void getBotConfigTest(String botName) {
    when(sqlSessionFactory.openSession()).thenReturn(session);
    when(session.<Map<String, String>>selectList("GetBotConfig", Map.of("botName", botName)))
        .thenReturn(List.of(Map.of("key", "param1", "value", "value1")));

    assertEquals("value1", daoObj.getBotConfig(botName).get("param1"));
  }

  @ParameterizedTest()
  @CsvSource({"url1, imageUrl1, description1, price1, maintainer1"})
  void addRentHouseTest(String url, String imageUrl, String description, String price,
      String maintainer) {
    when(sqlSessionFactory.openSession(true)).thenReturn(session);
    when(session.insert("AddRentHouse", Map.of("url", url, "imageUrl", imageUrl, "description",
        description, "price", price, "createUser", maintainer))).thenReturn(1);

    assertEquals(1, daoObj.addRentHouse(url, imageUrl, description, price, maintainer));
  }

  @ParameterizedTest()
  @CsvSource({"url1, imageUrl1, description1, price1, maintainer1"})
  void addSaleHouseTest(String url, String imageUrl, String description, String price,
      String maintainer) {
    when(sqlSessionFactory.openSession(true)).thenReturn(session);
    when(session.insert("AddSaleHouse", Map.of("url", url, "imageUrl", imageUrl, "description",
        description, "price", price, "createUser", maintainer))).thenReturn(1);

    assertEquals(1, daoObj.addSaleHouse(url, imageUrl, description, price, maintainer));
  }

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, imageUrl1, caption1"})
  void notifyTelegramTest(String token, String chatId, String imageUrl, String caption)
      throws ClientProtocolException, IOException {
    mockTelegramHttpCallChain();

    assertEquals(true, daoObj.notifyTelegram(token, chatId, imageUrl, caption));
  }

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, photoPath1, caption1"})
  void notifyTelegramTest(String token, String chatId, File photo, String caption)
      throws ClientProtocolException, IOException {
    mockTelegramHttpCallChain();

    assertEquals(true, daoObj.notifyTelegram(token, chatId, photo, caption));
  }

  private void mockTelegramHttpCallChain() throws ClientProtocolException, IOException {
    when(netHelper.httpRequest(Mockito.eq("post"), Mockito.anyString())
        .connectTimeout(Mockito.anyInt()).socketTimeout(Mockito.anyInt())).thenReturn(req);
    when(req.execute().returnResponse()).thenReturn(httpResp);
    when(httpResp.getStatusLine().getStatusCode()).thenReturn(200);
    when(httpResp.getEntity()).thenReturn(httpEntity);
  }
}
