package org.kquiet.job.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import org.kquiet.job.crawler.util.JacksonUtility;
import org.kquiet.job.crawler.util.MybatisUtility;
import org.kquiet.job.crawler.util.NetUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommonDao.class);
  private static final String MYBATIS_FILE_NAME = "mybatis-config.xml";
  private static final String MAPPER_FILE_NAME = "mybatis-mapper.xml";
  private final SqlSessionFactory sqlSessionFactory;
  private final ObjectMapper objectMapper;

  CommonDao() {
    this.objectMapper = JacksonUtility.getDefaultMapperForJson();
    this.sqlSessionFactory = MybatisUtility.getSessionFactory(MYBATIS_FILE_NAME,
        Arrays.asList(MAPPER_FILE_NAME), "crawler");
  }
  
  Map<String, String> getBotConfig(String botName) {
    Map<String, String> result = new HashMap<>();
    try {
      try (SqlSession session = sqlSessionFactory.openSession()) {
        Map<String, Object> param = new HashMap<>();
        param.put("botName", botName);

        List<Map<String, String>> queryResult = session
            .<Map<String, String>>selectList("GetBotConfig", param);
        if (queryResult != null) {
          for (Map<String, String> row :queryResult) {
            result.put(row.get("key"), row.get("value"));
          }
        }
        return result;
      }
    } catch (Exception ex) {
      LOGGER.error("getBotConfig error!", ex);
      return null;
    }
  }

  int createRent(String url, String imageUrl, String description, String price) {
    try {
      try (SqlSession session = sqlSessionFactory.openSession(true)) {
        url = Optional.ofNullable(url).orElse("");
        imageUrl = Optional.ofNullable(imageUrl).orElse("");
        description = Optional.ofNullable(description).orElse("");
        price = Optional.ofNullable(price).orElse("");

        Map<String, Object> param = new HashMap<>();
        param.put("url", url);
        param.put("imageUrl", imageUrl);
        param.put("description", description);
        param.put("price", price);
        int result = session.insert("CreateNewRent", param);
        return result;
      }
    } catch (Exception ex) {
      LOGGER.error("CreateNewRent error!", ex);
      return -1;
    }
  }

  HttpApiResult httpApiJson(String httpMethod,
      String url, Map<String,Object> jsonBodyParam) throws Exception {
    Map<String, String> httpHeader = new HashMap<>();
    httpHeader.put("Content-type", "application/json; charset=UTF-8");

    HttpResponse resp = null;
    try {
      resp = NetUtility.httpMethod(httpMethod, url, httpHeader,
          objectMapper.writeValueAsString(jsonBodyParam).getBytes("UTF-8"), 0).returnResponse();
      int statusCode = resp.getStatusLine().getStatusCode();
      String response = EntityUtils.toString(resp.getEntity(), "UTF-8");
      return new HttpApiResult(statusCode, response);
    } finally {
      if (resp != null) {
        EntityUtils.consume(resp.getEntity());
      }
    }
  }

  public class HttpApiResult {
    private final int statusCode;
    private final String response;

    public HttpApiResult(int statusCode, String response) {
      this.statusCode = statusCode;
      this.response = response;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getResponse() {
      return response;
    }
  }
}
