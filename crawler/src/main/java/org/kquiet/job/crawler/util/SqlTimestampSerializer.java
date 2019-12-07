package org.kquiet.job.crawler.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class SqlTimestampSerializer extends JsonSerializer<Timestamp> {
  private static final SimpleDateFormat DEFAULT_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Override
  public void serialize(Timestamp value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    jgen.writeString(DEFAULT_FORMAT.format(value));
  }
}
