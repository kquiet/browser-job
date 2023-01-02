package org.kquiet.browserjob.crawler;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.browserjob.crawler.dao.BotConfigRepository;
import org.kquiet.browserjob.crawler.entity.BotConfig;
import org.kquiet.browserjob.crawler.entity.BotId;
import org.kquiet.browserjob.crawler.testsupport.FlywaySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * crawler integration test.
 *
 * @author monkey
 *
 */
@SpringBootTest(classes = {CrawlerBeanConfiguration.class})
@EnableConfigurationProperties
@EnableAutoConfiguration
@DirtiesContext
@FlywaySupport(cleanBeforeEach = true)
@Testcontainers
public class CrawlerIntegrationTest {
  private static final String PROPERTY_DATABASE_URL = "browser-scheduler.dataSource.jdbcUrl";
  private static final String PROPERTY_DATABASE_USERNAME = "browser-scheduler.dataSource.username";
  private static final String PROPERTY_DATABASE_PASSWORD = "browser-scheduler.dataSource.password";
  private static final String JDBC_DATABASE_IMAGE = "mysql:8.0.31";

  @Container
  private static JdbcDatabaseContainer<?> JDBC_DATABASE_CONTAINER =
      new MySQLContainer<>(JDBC_DATABASE_IMAGE);

  @Autowired
  private BotConfigRepository botConfigRepository;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(PROPERTY_DATABASE_URL, JDBC_DATABASE_CONTAINER.getJdbcUrl());
    System.setProperty(PROPERTY_DATABASE_USERNAME, JDBC_DATABASE_CONTAINER.getUsername());
    System.setProperty(PROPERTY_DATABASE_PASSWORD, JDBC_DATABASE_CONTAINER.getPassword());
  }

  @AfterAll
  static void afterAll() {
    System.clearProperty(PROPERTY_DATABASE_URL);
    System.clearProperty(PROPERTY_DATABASE_USERNAME);
    System.clearProperty(PROPERTY_DATABASE_PASSWORD);
  }

  @ParameterizedTest()
  @CsvSource({"testBotName, testKey, testValue"})
  @Order(1)
  void botConfigTest(String botName, String key, String value) {
    BotConfig botConfig = new BotConfig();
    BotId botId = new BotId();
    botId.setBotname(botName);
    botId.setKey(key);
    botConfig.setBotId(botId);
    botConfig.setValue(value);

    assertEquals(0, botConfigRepository.count());

    botConfigRepository.saveAndFlush(botConfig);
    List<BotConfig> results = botConfigRepository.findByBotIdBotname(botId.getBotname());
    assertEquals(1, results.size());
    BotConfig result = results.get(0);
    assertEquals(botName, result.getBotId().getBotname());
    assertEquals(key, result.getBotId().getKey());
    assertEquals(value, result.getValue());

    botConfigRepository.delete(botConfig);
    assertEquals(0, botConfigRepository.count());

    botConfigRepository.saveAndFlush(botConfig);
    assertEquals(1, botConfigRepository.findByBotIdBotname(botId.getBotname()).size());
  }

  @Test
  @Order(2)
  void lastOrderTest() {
    assertEquals(0, botConfigRepository.count());
  }
}
