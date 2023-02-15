package org.kquiet.browserjob.crawler.testsupport;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Flway test extension.
 *
 * @author monkey
 *
 */
public class FlywaySupportExtension implements AfterEachCallback, BeforeAllCallback {
  private FlywaySupport annotation;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    annotation = context.getRequiredTestClass().getAnnotation(FlywaySupport.class);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (annotation.cleanAfterEach()) {
      Flyway flyway = SpringExtension.getApplicationContext(context).getBean(Flyway.class);
      flyway.clean();
      flyway.migrate();
    }
  }
}
