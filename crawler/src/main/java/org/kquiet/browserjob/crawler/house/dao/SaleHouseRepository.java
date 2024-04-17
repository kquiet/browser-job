package org.kquiet.browserjob.crawler.house.dao;

import org.kquiet.browserjob.crawler.house.entity.SaleHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository of {@link SaleHouse}.
 *
 * @author monkey
 *
 */
@Repository
public interface SaleHouseRepository extends JpaRepository<SaleHouse, String> {

}
