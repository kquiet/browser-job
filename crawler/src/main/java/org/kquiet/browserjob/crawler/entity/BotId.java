package org.kquiet.browserjob.crawler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Id of {@link BotConfig}.
 *
 * @author monkey
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class BotId implements Serializable {
  @Column(name = "botname")
  private String botname;

  @Column(name = "key")
  private String key;

  public BotId() {}

  public String getBotname() {
    return botname;
  }

  public void setBotname(String botname) {
    this.botname = botname;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj instanceof BotId id) {
      return Objects.equals(botname, id.botname) && Objects.equals(key, id.key);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(botname, key);
  }
}
