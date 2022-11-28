package org.kquiet.job.crawler.test.vanilla;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.kquiet.jobscheduler.JobBase;
import org.kquiet.jobscheduler.JobSchedulerConfig.JobConfig;

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
