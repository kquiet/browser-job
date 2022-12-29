package org.kquiet.browserjob.crawler;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;
import org.kquiet.browserjob.crawler.dao.BotConfigRepository;
import org.kquiet.browserjob.crawler.dao.CrawlerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Common business object.
 *
 * @author monkey
 *
 */
@Service
public class CrawlerService {
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
}
