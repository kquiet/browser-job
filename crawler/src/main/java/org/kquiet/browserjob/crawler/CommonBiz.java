package org.kquiet.browserjob.crawler;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.io.File;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

class CommonBiz {
  @Autowired
  private CommonDao dao;

  @WithSpan
  Map<String, String> getBotConfig(@SpanAttribute("botName") String botName) {
    return dao.getBotConfig(botName);
  }

  @WithSpan
  int addRentHouse(@SpanAttribute("url") String url, @SpanAttribute("imageUrl") String imageUrl,
      @SpanAttribute("description") String description, @SpanAttribute("price") String price,
      @SpanAttribute("maintainer") String maintainer) {
    return dao.addRentHouse(url, imageUrl, description, price, maintainer);
  }

  @WithSpan
  int addSaleHouse(@SpanAttribute("url") String url, @SpanAttribute("imageUrl") String imageUrl,
      @SpanAttribute("description") String description, @SpanAttribute("price") String price,
      @SpanAttribute("maintainer") String maintainer) {
    return dao.addSaleHouse(url, imageUrl, description, price, maintainer);
  }

  @WithSpan
  boolean notifyTelegram(String token, @SpanAttribute("chatId") String chatId,
      @SpanAttribute("imageUrl") String imageUrl, @SpanAttribute("caption") String caption) {

    return dao.notifyTelegram(token, chatId, imageUrl, caption);
  }

  @WithSpan
  boolean notifyTelegram(String token, @SpanAttribute("chatId") String chatId, File photo,
      @SpanAttribute("caption") String caption) {
    return dao.notifyTelegram(token, chatId, photo, caption);
  }
}
