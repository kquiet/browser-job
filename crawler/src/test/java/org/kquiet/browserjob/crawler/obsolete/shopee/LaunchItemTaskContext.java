package org.kquiet.browserjob.crawler.obsolete.shopee;

import org.kquiet.browser.BasicActionComposer;

/**
 * LaunchItemTaskContext.
 *
 * @author monkey
 *
 */
public class LaunchItemTaskContext extends BasicActionComposer {
  private LaunchItemResultStatus resultStatus = LaunchItemResultStatus.WaitingToLaunch;
  private String contextMessage;

  public LaunchItemTaskContext() {
    super();
  }

  public LaunchItemResultStatus getLaunchItemResultStatus() {
    return this.resultStatus;
  }

  public void setLaunchItemResultStatus(LaunchItemResultStatus st) {
    this.resultStatus = st;
  }

  /**
   * LaunchItemResultStatus.
   *
   * @author monkey
   *
   */
  public enum LaunchItemResultStatus {
    WaitingToLaunch("WaitingToLaunch"), ExceedLauchLimit("ExceedLauchLimit"), Success(
        "Success"), UnknownFail("UnknownFail");

    private final String name;

    private LaunchItemResultStatus(String name) {
      this.name = name;
    }

    public String toString() {
      return this.name;
    }
  }

  public String getContextMessage() {
    return contextMessage;
  }

  public void setContextMessage(String contextMessage) {
    this.contextMessage = contextMessage;
  }
}
