package org.kquiet.job.crawler.test.ruten;

import org.kquiet.browser.BasicActionComposer;

public class LoginTaskContext extends BasicActionComposer {
  private LoginResultStatus loginResultStatus = LoginResultStatus.WaitingToLogin;
  private String contextMessage;

  public LoginTaskContext() {
    super();
  }

  public LoginResultStatus getLoginResultStatus() {
    return this.loginResultStatus;
  }

  public void setLoginResultStatus(LoginResultStatus st) {
    this.loginResultStatus = st;
  }

  public enum LoginResultStatus {
    WaitingToLogin("WaitingToLogin"),
    DifferentLogin("DifferentLogin"),
    WaitSms("WaitSms"),
    Success("Success"),
    UnknownFail("UnknownFail");

    private final String name;
    private LoginResultStatus(String name) {
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
