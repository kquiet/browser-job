package org.kquiet.browserjob.crawler.house.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.browserjob.crawler.house.script.SaleSinyiScript;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SaleYungching Controller.
 *
 * @author monkey
 *
 */
public class SaleSinyiController extends JobBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(SaleSinyiController.class);

  public SaleSinyiController(JobConfig config) {
    super(config);
  }

  @Override
  @WithSpan
  public void run() {
    try {
      SaleSinyiScript bac = new SaleSinyiScript(this);
      registerInternalBrowserTask(bac);
      LOGGER.info("Browser task({}) accepted", bac.getName());

      bac.get();
      LOGGER.info("Job {} finished", getJobName());
    } catch (Exception e) {
      LOGGER.error("generating search task error:{}", e);
    }
  }
}
