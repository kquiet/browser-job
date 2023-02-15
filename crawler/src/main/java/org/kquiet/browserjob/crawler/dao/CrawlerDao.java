package org.kquiet.browserjob.crawler.dao;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * crawler dao.
 *
 * @author monkey
 *
 */
public class CrawlerDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerDao.class);

  @Value("${crawler.hecate.endpoints.telegramSendPhoto}")
  private String telegramSendPhotoUrl;

  public String getTelegramSendPhotoUrl() {
    return telegramSendPhotoUrl;
  }

  public void setTelegramSendPhotoUrl(String telegramSendPhotoUrl) {
    this.telegramSendPhotoUrl = telegramSendPhotoUrl;
  }

  /**
   * Send message with photo through telegram.
   *
   * @param req request
   * @param photoFile optional photo file; photo in request takes precedence over this
   * @return whether messsage is sent
   */
  @WithSpan
  public Mono<Boolean> telegramSendPhoto(@SpanAttribute("req") SendPhotoRequest req,
      Optional<File> photoFile) {
    WebClient client = WebClient.create(telegramSendPhotoUrl);

    Mono<Boolean> result = client.post().body((outputMessage, context) -> Mono.defer(() -> {
      MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
      bodyBuilder.part("req", req).contentType(MediaType.APPLICATION_JSON);

      if (req.getPhoto() == null || req.getPhoto().isBlank()) {
        Flux<DataBuffer> sourceFlux = DataBufferUtils.readAsynchronousFileChannel(
            () -> AsynchronousFileChannel.open(photoFile.get().toPath(), StandardOpenOption.READ),
            new DefaultDataBufferFactory(), 512 * 1024);
        bodyBuilder.asyncPart("photoPart", sourceFlux, DataBuffer.class).filename("somefilename");
      }

      return BodyInserters.fromMultipartData(bodyBuilder.build()).insert(outputMessage, context);
    })).retrieve().bodyToMono(Boolean.class).onErrorResume(ex -> {
      LOGGER.warn("telegramSendPhoto fail: {}", req.getCaption(), ex);
      return Mono.just(false);
    });
    return result;
  }
}
