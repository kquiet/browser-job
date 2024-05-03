package org.kquiet.browserjob.crawler.house;

/**
 * Result Status.
 *
 * @author monkey
 *
 */
public enum CrawlerResultStatus {
  AlertFail("AlertFail"), WaitingToDo("WaitingToDo"), Success("Success"), UnknownFail(
      "UnknownFail");

  private final String name;

  private CrawlerResultStatus(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
