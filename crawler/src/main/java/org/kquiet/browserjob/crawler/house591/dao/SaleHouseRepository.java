package org.kquiet.browserjob.crawler.house591.dao;

import org.kquiet.browserjob.crawler.house591.entity.SaleHouse;
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
