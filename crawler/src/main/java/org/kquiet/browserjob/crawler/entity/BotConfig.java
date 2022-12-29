package org.kquiet.browserjob.crawler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Bot config.
 *
 * @author monkey
 *
 */
@Entity
@Table(name = "botconfig")
public class BotConfig {
  @EmbeddedId
  private BotId botId;

  @Column(name = "value")
  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public BotId getBotId() {
    return botId;
  }

  public void setBotId(BotId botId) {
    this.botId = botId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj instanceof BotConfig temp) {
      return Objects.equals(botId, temp.getBotId());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return botId.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s[botname=%s, key='%s', value='%s']", BotConfig.class.getTypeName(),
        botId.getBotname(), botId.getKey(), value);
  }
}
