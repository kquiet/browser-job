package org.kquiet.browserjob.crawler.house591;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.browserjob.crawler.house591.dao.RentHouseRepository;
import org.kquiet.browserjob.crawler.house591.dao.SaleHouseRepository;
import org.kquiet.browserjob.crawler.house591.entity.RentHouse;
import org.kquiet.browserjob.crawler.house591.entity.SaleHouse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class House591ServiceTest {
  @Mock
  RentHouseRepository rentHouseRepo;

  @Mock
  SaleHouseRepository saleHouseRepo;

  @InjectMocks
  House591Service house591Service;

  @ParameterizedTest()
  @CsvSource({"url1"})
  void existsRentHouseTest(String url) {
    when(rentHouseRepo.existsById(url)).thenReturn(true);

    assertEquals(true, house591Service.existsRentHouse(url));
  }

  @ParameterizedTest()
  @CsvSource({"url1"})
  void addRentHouseTest(String url) {
    RentHouse entity = new RentHouse();
    entity.setUrl(url);
    when(rentHouseRepo.saveAndFlush(entity)).thenReturn(entity);

    assertEquals(url, house591Service.addRentHouse(entity).getUrl());
  }

  @ParameterizedTest()
  @CsvSource({"url1"})
  void existsSaleHouseTest(String url) {
    when(saleHouseRepo.existsById(url)).thenReturn(true);

    assertEquals(true, house591Service.existsSaleHouse(url));
  }

  @ParameterizedTest()
  @CsvSource({"url1"})
  void addSaleHouseTest(String url) {
    SaleHouse entity = new SaleHouse();
    entity.setUrl(url);
    when(saleHouseRepo.saveAndFlush(entity)).thenReturn(entity);

    assertEquals(url, house591Service.addSaleHouse(entity).getUrl());
  }
}
