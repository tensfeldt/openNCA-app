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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class UserInfoCache implements CacheInterface {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DirectoryService directoryService;

    private LoadingCache<String, UserInfo> userInfoLoadingCache;

    @Override
    public void load() {
        log.info("Building UserInfo cache...");
        userInfoLoadingCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, UserInfo>() {
            @Override
            public UserInfo load(String userId) throws Exception {
                return directoryService.lookupUser(userId);
            }
        });
    }

    @Override
    public UserInfo query(String userId) throws ExecutionException {
        UserInfo userInfo;
        try {
            log.info("Getting 'UserInfo' of userId: {} from cache...", userId);
            userInfo = userInfoLoadingCache.get(userId);
        } catch (Exception e) {
            if (e.getCause() instanceof EquipException) {
                throw (EquipException) e.getCause();
            } else {
                throw e;
            }
        }
        return userInfo;
    }

    @Override
    public void clear() throws ExecutionException {
        userInfoLoadingCache.invalidateAll();
    }
}
