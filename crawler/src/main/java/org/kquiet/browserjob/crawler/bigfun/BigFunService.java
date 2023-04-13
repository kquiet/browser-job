package org.kquiet.browserjob.crawler.bigfun;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.browserjob.crawler.bigfun.dao.BigFunRepository;
import org.kquiet.browserjob.crawler.bigfun.entity.RealEstate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * house 591 service.
 *
 * @author monkey
 *
 */
public class BigFunService {
  @Autowired
  BigFunRepository realEstateRepo;

  @WithSpan
  public boolean existsRealEstate(@SpanAttribute("url") String url) {
    return realEstateRepo.existsById(url);
  }

  @WithSpan
  @Transactional(propagation = Propagation.SUPPORTS)
  public RealEstate addRealEstate(@SpanAttribute("entity") RealEstate entity) {
    return realEstateRepo.saveAndFlush(entity);
  }
}
