package org.kquiet.job.crawler.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.kquiet.jobscheduler.JobBase;
import org.kquiet.jobscheduler.JobController;

public class VanillaTest {
  private CountDownLatch latch = null;
  private final List<String> parameterValueList = new ArrayList<>();

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    latch = new CountDownLatch(2);
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void controllerTest() {
    JobController controller = new JobController();
    TestJobBase job1 = new TestJobBase("VanillaTest1");
    TestJobBase job2 = new TestJobBase("VanillaTest2");
    job1.setJobController(controller);
    job2.setJobController(controller);
    controller.start(Arrays.asList(job2, job1));
    try {
      latch.await(30, TimeUnit.SECONDS);
    } catch (Exception ex) {
      System.err.println(ex.toString());
    }
    //try{Thread.sleep(3600000);}catch(Exception x) {}
    controller.stop();
    assertAll(
        () -> assertEquals("CrawlerTest", job1.getInstanceName(), "Wrong instance name on job1"),
        () -> assertEquals("CrawlerTest", job2.getInstanceName(), "Wrong instance name on job2"),
        () -> assertEquals(2, parameterValueList.size(), "Wrong parameter value size"),
        () -> assertEquals("VanillaTest2,VanillaTest1",
            String.join(",", parameterValueList), "Wrong parameter value sequence")
    );
  }

  class TestJobBase extends JobBase {
    private String parameterValue = null;


    public TestJobBase(String jobName) {
      super(jobName);
    }

    @Override
    public void run() {
      parameterValue = this.getParameter("testParameter");
      parameterValueList.add(parameterValue);
      latch.countDown();
    }
  }
}
