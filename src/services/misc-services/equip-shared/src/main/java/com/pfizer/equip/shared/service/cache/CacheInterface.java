package com.pfizer.equip.shared.service.cache;

import java.util.concurrent.ExecutionException;

public interface CacheInterface {
   void load();
   Object query(String key) throws ExecutionException;
   void clear() throws ExecutionException;
}