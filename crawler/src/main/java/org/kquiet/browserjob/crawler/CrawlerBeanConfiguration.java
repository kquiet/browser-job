package org.kquiet.browserjob.crawler;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import javax.sql.DataSource;
import org.kquiet.browserjob.crawler.dao.CrawlerDao;
import org.kquiet.browserjob.crawler.house591.House591Service;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring bean configuration.
 *
 * @author monkey
 *
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.kquiet.browserjob.crawler",
    entityManagerFactoryRef = "crawlerEntityManagerFactory",
    transactionManagerRef = "crawlerTransactionManager")
@EnableTransactionManagement
public class CrawlerBeanConfiguration implements ApplicationContextAware {
  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  public static ApplicationContext getAppContext() {
    return context;
  }

  @Bean
  public OpenTelemetry openTelemetry() {
    return GlobalOpenTelemetry.get();
  }

  /**
   * data source.
   *
   * @return data source
   */
  @Bean("crawlerDataSource")
  @ConfigurationProperties(prefix = "browser-scheduler.datasource")
  public DataSource dataSource() {
    return DataSourceBuilder.create().build();
  }

  /**
   * local container entity manager factory bean.
   *
   * @param dataSource data source
   * @param builder entity manager factory builder
   * @return local container entity manager factory bean
   */
  @Bean("crawlerEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      @Qualifier("crawlerDataSource") DataSource dataSource, EntityManagerFactoryBuilder builder) {
    return builder //
        .dataSource(dataSource).packages(CrawlerBeanConfiguration.class.getPackageName())
        .persistenceUnit("crawlerPersistenceUnit").build();
  }

  /**
   * transaction manager.
   *
   * @param entityManagerFactory entity manager factory
   * @return trasaction manager
   */
  @Bean("crawlerTransactionManager")
  public PlatformTransactionManager transactionManager(
      @Qualifier("crawlerEntityManagerFactory")
      LocalContainerEntityManagerFactoryBean entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory.getObject());
  }

  @Bean()
  public CrawlerDao crawlerDao() {
    return new CrawlerDao();
  }

  @Bean()
  public CrawlerService crawlerService() {
    return new CrawlerService();
  }

  @Bean()
  @Scope
  public House591Service house591Service() {
    return new House591Service();
  }
}
