package org.kquiet.job.crawler.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class JacksonUtility {
  private JacksonUtility(){}

  /**
   * Get default mapper for json.
   * 
   * @return object mapper
   */
  public static ObjectMapper getDefaultMapperForJson() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getFactory().enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
    SimpleModule module = new SimpleModule();
    module.addSerializer(java.sql.Timestamp.class, new SqlTimestampSerializer());
    module.addSerializer(Double.class, new DoubleSerializer());
    module.addSerializer(double.class, new DoubleSerializer());
    module.addSerializer(Float.class, new FloatSerializer());
    module.addSerializer(float.class, new FloatSerializer());
    mapper.registerModule(module);
    return mapper;
  }
}
