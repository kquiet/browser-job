package org.kquiet.browserjob.crawler.vanilla;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.kquiet.browserscheduler.BrowserSchedulerConfig.JobConfig;
import org.kquiet.browserscheduler.JobBase;

/**
 * TestJobBase.
 *
 * @author monkey
 *
 */
public class VanillaJob extends JobBase {
  public static CountDownLatch latch = null;
  public static final List<String> parameterValueList = new ArrayList<>();

  private String parameterValue = null;

  public VanillaJob(JobConfig config) {
    super(config);
  }

  @Override
  public void run() {
    parameterValue = this.getParameter("testParameter");
    VanillaJob.parameterValueList.add(parameterValue);
    VanillaJob.latch.countDown();
  }
}
