package org.kquiet.browserjob.crawler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.kquiet.browserscheduler.BeanConfiguration;
import org.kquiet.browserscheduler.JobController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ShopeeTest.
 *
 * @author monkey
 *
 */
@SpringBootTest(classes = {BeanConfiguration.class})
@EnableConfigurationProperties
public class ShopeeIntegrationTest {
  private CountDownLatch latch = null;
  @Autowired
  private JobController controller;

  public ShopeeIntegrationTest() {}

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
   * Test for shopee.
   */
  // @Test
  public void shopeeTest() {
    controller.start();
    try {
      latch.await(600, TimeUnit.SECONDS);
    } catch (Exception ex) {
      System.err.println(ex.toString());
    } finally {
      controller.stop();
    }
  }
}
