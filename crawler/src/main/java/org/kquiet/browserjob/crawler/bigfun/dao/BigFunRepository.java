package org.kquiet.browserjob.crawler.bigfun.dao;

import org.kquiet.browserjob.crawler.bigfun.entity.RealEstate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository of {@link RealEstate}.
 *
 * @author monkey
 *
 */
@Repository
public interface BigFunRepository extends JpaRepository<RealEstate, String> {

}
