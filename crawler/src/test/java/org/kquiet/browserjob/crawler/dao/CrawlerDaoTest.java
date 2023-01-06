package org.kquiet.browserjob.crawler.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.browserjob.crawler.util.NetHelper;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrawlerDaoTest {
  @Mock
  HttpEntity httpEntity;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  HttpResponse httpResp;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Request req;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  NetHelper netHelper;

  @InjectMocks
  CrawlerDao daoObj;

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
    when(netHelper.httpRequest(Mockito.eq("post"), Mockito.anyString())).thenReturn(req);
    when(req.execute().returnResponse()).thenReturn(httpResp);
    when(httpResp.getStatusLine().getStatusCode()).thenReturn(200);
    when(httpResp.getEntity()).thenReturn(httpEntity);
  }
}
