package org.kquiet.browserjob.crawler.house.dao;

import org.kquiet.browserjob.crawler.house.entity.RentHouse;
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
