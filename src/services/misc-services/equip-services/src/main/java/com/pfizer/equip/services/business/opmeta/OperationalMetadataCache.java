package com.pfizer.equip.services.business.opmeta;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pfizer.equip.shared.opmeta.OperationalMetadataRepositoryService;
import com.pfizer.equip.shared.opmeta.entity.Program;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.entity.ProtocolAlias;
import com.pfizer.equip.shared.service.cache.CacheInterface;

@Component
public class OperationalMetadataCache implements CacheInterface {
   @Autowired
   private OperationalMetadataRepositoryService operationalMetadataRepositoryService;

   private LoadingCache<String, Map<String, Program>> programsCache;

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   public void load() {
      log.info(String.format("Building operational metadata programs cache..."));
      programsCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, Program>>() {
         public Map<String, Program> load(String key) throws Exception {
            // call com.pfizer.equip.shared.opmeta.OperationalMetadataRepositoryService.getPrograms()
            return operationalMetadataRepositoryService.getPrograms();
         }
      });
   }

   public void clear() throws ExecutionException {
      programsCache.invalidateAll();
   }

   public Map<String, Program> query(String key) throws ExecutionException {
      log.info(String.format("Querying programs cache..."));
      return programsCache.get(key);
   }

   public void updateCacheForProgram(Program updatedProgram, String key) throws ExecutionException {
      // When the cache is empty, get programs will anyway fetch all the programs including updated programs. So no need to update the cache.
      if (programsCache.size() > 0) {

         Map<String, Program> programs = programsCache.get(key);
         if (programs.containsKey(updatedProgram.getProgramCode())) {
            // In case of updateProgram, program already present in cache. So evict the program and add the updated program.
            programs.remove(updatedProgram.getProgramCode());
         }
         programs.put(updatedProgram.getProgramCode(), updatedProgram);
         programsCache.put(key, programs);
      }
   }

   public void updateCacheForProtocol(Protocol updatedProtocol, String programCode, String key) throws ExecutionException {
      // When the cache is empty, get programs will anyway fetch all the programs including updated programs. So no need to update the cache.
      if (updatedProtocol.getProtocolAliases() == null) {
         updatedProtocol.setProtocolAliases(new HashSet<ProtocolAlias>());         
      }
      if (programsCache.size() > 0) {

         Map<String, Program> programs = programsCache.get(key);
         if (programs.containsKey(programCode)) {
            // In case program is not present, we cannot update the protocol
            Program program = programs.get(programCode);
            Protocol matchingProtocol = null;
            Set<Protocol> protocolsInProgram = program.getProtocols() != null ? program.getProtocols() : new HashSet<Protocol>();
            // TODO:Consider Refactoring ?
            if (!protocolsInProgram.isEmpty()) {
               for (Protocol existingProtocol : protocolsInProgram) {
                  if (existingProtocol.getStudyId().equals(updatedProtocol.getStudyId())) {
                     matchingProtocol = existingProtocol;
                     break;
                  }
               }
            }
            if (matchingProtocol != null) {
               protocolsInProgram.remove(matchingProtocol);
            }
            // Add the new /updated protocol to the program
            protocolsInProgram.add(updatedProtocol);
            program.setProtocols(protocolsInProgram);
            // Add the modified program to cache
            programs.put(programCode, program);
            programsCache.put(key, programs);
         }
      }
   }
}
