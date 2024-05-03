package org.kquiet.browserjob.crawler.house.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.browserjob.crawler.house.script.Sale591Script;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sale591Controller.
 *
 * @author monkey
 *
 */
public class Sale591Controller extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(Sale591Controller.class);

  public Sale591Controller(JobConfig config) {
    super(config);
  }

  @Override
  @WithSpan
  public void run() {
    try {
      Sale591Script bac = new Sale591Script(this);
      registerInternalBrowserTask(bac);
      LOGGER.info("Browser task({}) accepted", bac.getName());

      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }
}
