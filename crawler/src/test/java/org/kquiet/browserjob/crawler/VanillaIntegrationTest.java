package org.kquiet.browserjob.crawler;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kquiet.browserjob.crawler.vanilla.VanillaJob;
import org.kquiet.browserscheduler.BeanConfiguration;
import org.kquiet.browserscheduler.JobController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * VanillaTest.
 *
 * @author monkey
 *
 */
@SpringBootTest(classes = {BeanConfiguration.class})
@EnableConfigurationProperties
public class VanillaIntegrationTest {
  @Autowired
  private JobController controller;

  @BeforeAll
  public static void setUpClass() {}

  @AfterAll
  public static void tearDownClass() {}

  @BeforeEach
  public void setUp() {
    VanillaJob.latch = new CountDownLatch(2);
  }

  @AfterEach
  public void tearDown() {}

  @Test
  public void controllerTest() {

    controller.start();
    try {
      VanillaJob.latch.await(5, TimeUnit.SECONDS);
    } catch (Exception ex) {
      System.err.println(ex.toString());
    }

    controller.stop();
    assertAll(
        () -> assertEquals(2, VanillaJob.parameterValueList.size(), "Wrong parameter value size"),
        () -> assertEquals("VanillaTest1,VanillaTest2",
            String.join(",", VanillaJob.parameterValueList), "Wrong parameter value sequence"));
  }

}
