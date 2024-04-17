package org.kquiet.browserjob.crawler.house.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.browserjob.crawler.house.script.Rent591Script;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rent591Controller.
 *
 * @author monkey
 *
 */
public class Rent591Controller extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(Rent591Controller.class);

  public Rent591Controller(JobConfig config) {
    super(config);
  }

  @Override
  @WithSpan
  public void run() {
    try {
      Rent591Script bac = new Rent591Script(this);
      registerInternalBrowserTask(bac);
      LOGGER.info("Browser task({}) accepted", bac.getName());

      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }
}
