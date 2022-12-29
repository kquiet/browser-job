package org.kquiet.browserjob.crawler.dao;

import java.util.List;
import org.kquiet.browserjob.crawler.entity.BotConfig;
import org.kquiet.browserjob.crawler.entity.BotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository of {@link BotConfig}.
 *
 * @author monkey
 *
 */
@Repository
public interface BotConfigRepository extends JpaRepository<BotConfig, BotId> {

  List<BotConfig> findByBotIdBotname(String botname);

}
