package com.servershepherd.beume;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.MulticastKeepaliveHeartbeatSender;

/**
 *
 * @author marc
 */
public class SPCacheFactory {

    public static final CacheManager manager = new CacheManager(CommonResources.EHCACHE_CONF_FILE);

    public static SelfPopulatingCache createCache(String ehcachename, CacheEntryFactory cu) {
        Logger.getLogger(SPCacheFactory.class.getName()).log(Level.INFO, "Setting up cache... {0}", ehcachename);
        MulticastKeepaliveHeartbeatSender.setHeartBeatInterval(1000);
        Logger.getLogger(SPCacheFactory.class.getName()).log(Level.INFO, "Waiting cluster {0}", manager.getName());
        SPCacheFactory.waitForClusterMembership(10, TimeUnit.SECONDS, Collections.singleton(ehcachename), manager);
        Logger.getLogger(SPCacheFactory.class.getName()).log(Level.INFO, "Cluster connected.");
        Cache orig_ehcache = manager.getCache(ehcachename);
        if (orig_ehcache == null) {
            return null;
        }
        orig_ehcache.bootstrap();
        SelfPopulatingCache ehcache = new SelfPopulatingCache(orig_ehcache, cu);
        ehcache.bootstrap();
        return ehcache;
    }

    protected static int waitForClusterMembership(int time, TimeUnit unit, final Collection<String> cacheNames, final CacheManager... managers) {
        Integer minimumPeers = null;
        for (CacheManager cmanager : managers) {
            CacheManagerPeerProvider peerProvider = cmanager.getCacheManagerPeerProvider("RMI");
            if (peerProvider != null) {
                for (String cacheName : cacheNames) {
                    int peers = peerProvider.listRemoteCachePeers(cmanager.getEhcache(cacheName)).size();
                    if (minimumPeers == null || peers < minimumPeers) {
                        minimumPeers = peers;
                    }
                }
            }
        }
        if (minimumPeers == null) {
            return 0;
        } else {
            return minimumPeers + 1;
        }
    }
}
