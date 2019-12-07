package org.kquiet.job.crawler.test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.kquiet.job.crawler.test.shopee.LaunchItem;
import org.kquiet.jobscheduler.JobController;

public class ShopeeTest {
  private static CountDownLatch latch = null;

  public ShopeeTest() {
  }

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    latch = new CountDownLatch(1);
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test for shopee.
   */
  //@Test
  public void shopeeTest() {
    JobController controller = new JobController();
    LaunchItem job = new LaunchItem("ShopeeLaunchItem");
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
