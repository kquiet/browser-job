package org.kquiet.browserjob.crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
@FlywaySupport()
@Testcontainers(disabledWithoutDocker = true)
public class CrawlerIntegrationTest {
  private static final String PROPERTY_DATABASE_URL = "crawler.dataSource.jdbcUrl";
  private static final String PROPERTY_DATABASE_USERNAME = "crawler.dataSource.username";
  private static final String PROPERTY_DATABASE_PASSWORD = "crawler.dataSource.password";
  private static final String JDBC_DATABASE_IMAGE = "mysql:8.0.37";

  @Container
  private static JdbcDatabaseContainer<?> JDBC_DATABASE_CONTAINER =
      new MySQLContainer<>(JDBC_DATABASE_IMAGE);

  @Autowired
  DataSource dataSource;

  @Autowired
  Flyway flyway;

  @Autowired
  private BotConfigRepository botConfigRepository;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(PROPERTY_DATABASE_URL, JDBC_DATABASE_CONTAINER.getJdbcUrl());
    System.setProperty(PROPERTY_DATABASE_USERNAME, JDBC_DATABASE_CONTAINER.getUsername());
    System.setProperty(PROPERTY_DATABASE_PASSWORD, JDBC_DATABASE_CONTAINER.getPassword());
  }

  @BeforeEach
  void beforeEach() throws SQLException {
    String dataSourceUrl = dataSource.getConnection().getMetaData().getURL();
    String flywayUrl =
        flyway.getConfiguration().getDataSource().getConnection().getMetaData().getURL();
    String expected = JDBC_DATABASE_CONTAINER.getJdbcUrl();

    assertEquals(expected, dataSourceUrl);
    assertEquals(expected, flywayUrl);
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

    // clean
    flyway.clean();
    flyway.migrate();
  }

  @Test
  @Order(2)
  void lastOrderTest() {
    assertEquals(0, botConfigRepository.count());
  }
}
