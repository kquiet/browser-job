package org.kquiet.browserjob.crawler.house591;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.browserjob.crawler.house591.dao.RentHouseRepository;
import org.kquiet.browserjob.crawler.house591.dao.SaleHouseRepository;
import org.kquiet.browserjob.crawler.house591.entity.RentHouse;
import org.kquiet.browserjob.crawler.house591.entity.SaleHouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * house 591 service.
 *
 * @author monkey
 *
 */
public class House591Service {
  @Autowired
  RentHouseRepository rentHouseRepo;

  @Autowired
  SaleHouseRepository saleHouseRepo;

  @WithSpan
  public boolean existsRentHouse(@SpanAttribute("url") String url) {
    return rentHouseRepo.existsById(url);
  }

  @WithSpan
  @Transactional(propagation = Propagation.SUPPORTS)
  public RentHouse addRentHouse(@SpanAttribute("entity") RentHouse entity) {
    return rentHouseRepo.saveAndFlush(entity);
  }

  @WithSpan
  public boolean existsSaleHouse(@SpanAttribute("url") String url) {
    return saleHouseRepo.existsById(url);
  }

  @WithSpan
  @Transactional(propagation = Propagation.SUPPORTS)
  public SaleHouse addSaleHouse(@SpanAttribute("entity") SaleHouse entity) {
    return saleHouseRepo.saveAndFlush(entity);
  }
}
