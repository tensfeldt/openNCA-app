package com.pfizer.equip.shared.service.user;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pfizer.equip.shared.exceptions.EquipException;
import com.pfizer.equip.shared.service.cache.CacheInterface;

@Component
public class ExternalUserGroupsCache implements CacheInterface {

   @Autowired
   private DirectoryService directoryService;

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private LoadingCache<String, Set<String>> externalGroupsCache;

   public void load() {

      log.info(String.format("Building the external user groups cache..."));
      externalGroupsCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Set<String>>() {
         public Set<String> load(String userId) throws Exception {
            // call com.pfizer.equip.shared.opmeta.OperationalMetadataRepositoryService.getPrograms()
            // NOTE: for some reason this doesn't return an error in standalone mode even though it should:
            return directoryService.getUserGroups(userId);

         }

      });
   }

   public void clear() throws ExecutionException {
      externalGroupsCache.invalidateAll();
   }

   public Set<String> query(String userId) throws ExecutionException {
      log.info(String.format("Querying the user groups cache..."));
      Set<String> externalGroups = null;
      try {
         externalGroups = externalGroupsCache.get(userId);
      } catch (Exception e) {
         if (e.getCause() instanceof EquipException) {
            throw (EquipException) e.getCause();
         } else {
            throw e;
         }
      }
      return externalGroups;
   }
}
