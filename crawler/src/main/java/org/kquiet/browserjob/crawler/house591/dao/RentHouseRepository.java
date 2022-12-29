package org.kquiet.browserjob.crawler.house591.dao;

import org.kquiet.browserjob.crawler.house591.entity.RentHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository of {@link RentHouse}.
 *
 * @author monkey
 *
 */
@Repository
public interface RentHouseRepository extends JpaRepository<RentHouse, String> {

}
