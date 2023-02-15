package org.kquiet.browserjob.crawler.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ExtendWith(MockitoExtension.class)
class CrawlerDaoTest {
  private static MockWebServer mockWebServer;

  @TempDir
  static Path tempDir;

  private CrawlerDao daoObj = new CrawlerDao();

  @BeforeAll
  static void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @ParameterizedTest()
  @CsvSource({
      "-123456789, 987654321:XXXXXXX, test11, https://localhost/path/to/sendPhotoTest.jpg, ",
      "-234567890, 098765432:YYYYY, test12, , sendPhotoTest.png"})
  void telegramSendPhotoTest(String chatId, String token, String caption, String photo,
      String photoFileName) throws IOException, InterruptedException {
    String sendPhotoUrl = mockWebServer.url("/telegram/sendPhoto").toString();
    daoObj.setTelegramSendPhotoUrl(sendPhotoUrl);
    mockWebServer.enqueue(new MockResponse().setResponseCode(200)
        .setHeader("Content-Type", "application/json").setBody("true"));

    SendPhotoRequest req = new SendPhotoRequest();
    req.setChatId(chatId);
    req.setToken(token);
    req.setCaption(caption);
    req.setPhoto(photo);

    FirstStep<Boolean> firstStep;
    if (photo != null && !photo.isBlank()) {
      firstStep = StepVerifier.create(daoObj.telegramSendPhoto(req, Optional.empty()));
    } else {
      firstStep = StepVerifier.create(daoObj.telegramSendPhoto(req,
          Optional.of(Files.createFile(tempDir.resolve(photoFileName)).toFile())));
    }
    firstStep.assertNext(s -> assertEquals(true, s)).verifyComplete();

    // assert request
    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("POST", request.getMethod());
    assertEquals(sendPhotoUrl, request.getRequestUrl().toString());
    assertEquals(0, request.getHeader("Content-Type").indexOf("multipart/form-data;boundary="));

    String requestBody = request.getBody().readUtf8().replaceAll("\r\n", System.lineSeparator());
    String chatIdBody = String.format("\"chatId\":\"%s\"", req.getChatId());
    assertEquals(true, requestBody.indexOf(chatIdBody) > 0);
    String tokenBody = String.format("\"token\":\"%s\"", req.getToken());
    assertEquals(true, requestBody.indexOf(tokenBody) > 0);
    String captionBody = String.format("\"caption\":\"%s\"", req.getCaption());
    assertEquals(true, requestBody.indexOf(captionBody) > 0);
    String photoBody = String.format("\"photo\":%s",
        req.getPhoto() == null ? "null" : "\"" + req.getPhoto() + "\"");
    assertEquals(true, requestBody.indexOf(photoBody) > 0);
    String headerBody = String.format("""
        Content-Type: application/json
        Content-Disposition: form-data; name="req"
        Content-Length: %s
        """,
        chatIdBody.length() + tokenBody.length() + captionBody.length() + photoBody.length() + 5);
    assertEquals(true, requestBody.indexOf(headerBody) > 0);

    if (photo == null || photo.isBlank()) {
      String photoFileBody =
          "Content-Disposition: form-data; name=\"photoPart\"; filename=\"somefilename\"";
      assertEquals(true, requestBody.indexOf(photoFileBody) > 0);
    }
  }
}
