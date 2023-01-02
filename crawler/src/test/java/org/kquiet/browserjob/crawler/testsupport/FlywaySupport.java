package org.kquiet.browserjob.crawler.testsupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Flway test annotation.
 *
 * @author monkey
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith({FlywaySupportExtension.class})
public @interface FlywaySupport {
  /**
   * Clean before each method.
   */
  boolean cleanBeforeEach() default false;
}
