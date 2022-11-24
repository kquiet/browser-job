package org.kquiet.job.crawler.test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.kquiet.job.crawler.test.ruten.LaunchItem;
import org.kquiet.jobscheduler.JobController;

/**
 * RutenTest.
 *
 * @author monkey
 *
 */
public class RutenTest {
  private CountDownLatch latch = null;

  public RutenTest() {}

  @BeforeAll
  public static void setUpClass() {}

  @AfterAll
  public static void tearDownClass() {}

  @BeforeEach
  public void setUp() {
    latch = new CountDownLatch(1);
  }

  @AfterEach
  public void tearDown() {}

  /**
   * Test for ruten.
   */
  // @Test
  public void rutenTest() {
    JobController controller = new JobController();
    LaunchItem job = new LaunchItem("RutenLaunchItem");
    job.setJobController(controller);
    controller.start(Arrays.asList(job));
    try {
      latch.await(600, TimeUnit.SECONDS);
    } catch (Exception ex) {
      System.err.println(ex.toString());
    } finally {
      controller.stop();
    }
  }
}
