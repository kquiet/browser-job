package org.kquiet.browserjob.crawler.house.service;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.time.LocalDateTime;
import org.kquiet.browserjob.crawler.house.dao.RentHouseRepository;
import org.kquiet.browserjob.crawler.house.dao.SaleHouseRepository;
import org.kquiet.browserjob.crawler.house.entity.RentHouse;
import org.kquiet.browserjob.crawler.house.entity.SaleHouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * house 591 service.
 *
 * @author monkey
 *
 */
public class HouseService {
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

  /**
   * Add Rent House.
   *
   * @param site site of house
   * @param url url of house
   * @param imageUrl image url of house
   * @param description description of house
   * @param price price of house
   * @param maintainer maintainer of house
   * @return saved house
   */
  public RentHouse addRentHouse(String site, String url, String imageUrl, String description,
      String price, String maintainer) {
    RentHouse obj = new RentHouse();
    obj.setSite(site);
    obj.setUrl(url);
    obj.setImageUrl(imageUrl);
    obj.setDescription(description);
    obj.setPrice(price);
    obj.setCreateuser(maintainer);
    obj.setCreatedate(LocalDateTime.now());
    return addRentHouse(obj);
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

  /**
   * Add Sale House.
   *
   * @param site site of house
   * @param url url of house
   * @param imageUrl image url of house
   * @param description description of house
   * @param price price of house
   * @param maintainer maintainer of house
   * @return saved house
   */
  public SaleHouse addSaleHouse(String site, String url, String imageUrl, String description,
      String price, String maintainer) {
    SaleHouse obj = new SaleHouse();
    obj.setSite(site);
    obj.setUrl(url);
    obj.setImageUrl(imageUrl);
    obj.setDescription(description);
    obj.setPrice(price);
    obj.setCreateuser(maintainer);
    obj.setCreatedate(LocalDateTime.now());
    return addSaleHouse(obj);
  }

}
