package org.kquiet.job.crawler.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MybatisUtility {

  /**
   * Get mybatis session factory.
   * 
   * @param mybatisFileName path of mybatis configuration file
   * @param mapperFileNameList list of mybatis mapper files
   * @param env environment
   * @return sql session factory
   */
  public static SqlSessionFactory getSessionFactory(String mybatisFileName,
      List<String> mapperFileNameList, String env) {
    SqlSessionFactory sqlSessionFactory = null;
    try {
      File baseDir = Paths.get("").toAbsolutePath().toFile();
      File mybatisFile = new File(baseDir, mybatisFileName);

      //get config from directory first; otherwise get from classpath 
      if (mybatisFile.exists()) {
        try (InputStream inputStream = new FileInputStream(mybatisFile)) {
          sqlSessionFactory = initSqlSessionFactory(inputStream, env);
        }
      } else {
        try (InputStream inputStream = Resources.getResourceAsStream(mybatisFileName)) {
          sqlSessionFactory = initSqlSessionFactory(inputStream, env);
        }
      }

      for (String mapperFileName: mapperFileNameList) {
        File mapperFile = new File(baseDir, mapperFileName);
        if (mapperFile.exists()) {
          try (InputStream inputStream = new FileInputStream(mapperFile)) {
            initMapper(inputStream, mapperFile.toURI().getPath(), sqlSessionFactory);
          }
        } else {
          try (InputStream inputStream = Resources.getResourceAsStream(mapperFileName)) {
            initMapper(inputStream, mapperFileName, sqlSessionFactory);
          }
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return sqlSessionFactory;
  }

  private static SqlSessionFactory initSqlSessionFactory(InputStream inputStream, String env) {
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();

    if (env == null || env.isEmpty()) {
      return builder.build(inputStream);
    } else {
      return builder.build(inputStream, env);
    }
  }

  private static void initMapper(InputStream inputStream,
      String mapperFilePath, SqlSessionFactory sqlSessionFactory) {
    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream,
        sqlSessionFactory.getConfiguration(), mapperFilePath,
        sqlSessionFactory.getConfiguration().getSqlFragments());
    mapperParser.parse();
  }
}
