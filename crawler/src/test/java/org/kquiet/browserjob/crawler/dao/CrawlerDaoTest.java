package org.kquiet.browserjob.crawler.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrawlerDaoTest {
  @Mock
  HttpResponse<String> response;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  HttpClient.Builder httpClientBuilder;

  @InjectMocks
  CrawlerDao daoObj;

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, imageUrl1, caption1"})
  void notifyTelegramTest(String token, String chatId, String imageUrl, String caption)
      throws IOException, InterruptedException {
    mockTelegramHttpCallChain();

    assertEquals(true, daoObj.notifyTelegram(token, chatId, imageUrl, caption));
  }

  @ParameterizedTest()
  @CsvSource({"token1, chatId1, photoPath1, caption1"})
  void notifyTelegramTest(String token, String chatId, File photo, String caption)
      throws IOException, InterruptedException {
    mockTelegramHttpCallChain();

    assertEquals(true, daoObj.notifyTelegram(token, chatId, photo, caption));
  }

  private void mockTelegramHttpCallChain() throws IOException, InterruptedException {
    when(httpClientBuilder.build().send(Mockito.<HttpRequest>any(),
        Mockito.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
    when(response.statusCode()).thenReturn(200);
  }
}
