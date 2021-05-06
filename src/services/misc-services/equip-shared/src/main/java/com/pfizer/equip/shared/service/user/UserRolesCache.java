package com.pfizer.equip.shared.service.user;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pfizer.equip.shared.exceptions.EquipException;
import com.pfizer.equip.shared.service.cache.CacheInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class UserRolesCache implements CacheInterface {
    @Autowired
    UserLookupService userLookupService;

    private LoadingCache<String, Set<String>> userRolesNamesCache;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void load() {
        log.info("Building UserRoles cache...");
        userRolesNamesCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, Set<String>>() {
            @Override
            public Set<String> load(String key) throws Exception {
                String[] keys = key.split("/");
                String systemId = keys[0];
                String userId = keys[1];
                return userLookupService.getUserRoleNames(systemId, userId);
            }
        });
    }

    @Override
    public Set<String> query(String key) throws ExecutionException {
        log.info("Getting from cache user role names of systemId/userId: {}...", key);
        Set<String> roles;
        try {
            roles = userRolesNamesCache.get(key);
        } catch (Exception e) {
            if (e.getCause() instanceof EquipException) {
                throw (EquipException) e.getCause();
            } else {
                throw e;
            }
        }
        return roles;
    }

    @Override
    public void clear() throws ExecutionException {
        userRolesNamesCache.invalidateAll();
    }
}
