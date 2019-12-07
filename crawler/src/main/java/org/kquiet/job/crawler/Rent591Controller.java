package org.kquiet.job.crawler;

import org.kquiet.jobscheduler.JobBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rent591Controller extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(Rent591Controller.class);

  public Rent591Controller(String jobName) {
    super(jobName);
  }

  @Override
  public void run() {
    try {
      Rent591Crawler bac = new Rent591Crawler(this);
      this.submitBrowserTask(bac);
      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }

  private void submitBrowserTask(Rent591Crawler bac) {
    registerInternalBrowserTask(bac);
    LOGGER.info("Browser task({}) accepted", bac.getName());
  }

}
