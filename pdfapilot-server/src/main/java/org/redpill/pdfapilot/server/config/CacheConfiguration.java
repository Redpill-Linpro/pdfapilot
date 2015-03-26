package org.redpill.pdfapilot.server.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;

@Configuration
@EnableCaching
@AutoConfigureAfter(value = {MetricsConfiguration.class, DatabaseConfiguration.class})
@Profile("!" + Constants.SPRING_PROFILE_FAST)
public class CacheConfiguration {

    private final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    private static HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();

    @Inject
    private Environment env;

    private CacheManager cacheManager;

    @PreDestroy
    public void destroy() {
        log.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
    }

    @Bean
    public CacheManager cacheManager() {
        log.debug("No cache");
        cacheManager = new NoOpCacheManager();
        return cacheManager;
    }

    @PostConstruct
    private HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setInstanceName("pdfaPilotServer");
        config.getNetworkConfig().setPort(5701);
        config.getNetworkConfig().setPortAutoIncrement(true);

        if (env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
          System.setProperty("hazelcast.local.localAddress", "127.0.0.1");

          config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
          config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
          config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        }
        
        config.getMapConfigs().put("my-sessions", initializeClusteredSession());

        hazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(config);

        return hazelcastInstance;
    }

    private MapConfig initializeClusteredSession() {
        MapConfig mapConfig = new MapConfig();

        mapConfig.setBackupCount(env.getProperty("cache.hazelcast.backupCount", Integer.class, 1));
        mapConfig.setTimeToLiveSeconds(env.getProperty("cache.timeToLiveSeconds", Integer.class, 3600));
        return mapConfig;
    }

    /**
    * @return the unique instance.
    */
    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }
}
