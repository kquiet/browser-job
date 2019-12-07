package org.kquiet.job.crawler.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DoubleSerializer extends JsonSerializer<Double> {
  private static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("0",
      DecimalFormatSymbols.getInstance(Locale.ENGLISH));
  
  static {
    DEFAULT_FORMAT.setMaximumFractionDigits(340);
  }

  @Override
  public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    jgen.writeNumber(DEFAULT_FORMAT.format(value));
  }
}
