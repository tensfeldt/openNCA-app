package com.pfizer.equip.services.business.api.response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskExecutor {
   private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
   private ExecutorService service;
   
   @Autowired
   public TaskExecutor(ServletContext context) {
      service = Executors.newCachedThreadPool();
      Task.context = context;
   }
   
   public <T>List<T> executeAll(List<Task<T>> tasks) {
      try {
         List<T> results = new ArrayList<T>();
         List<Future<T>> futures = service.invokeAll(tasks);
         for (Future<T> future : futures) {
            results.add(future.get());
         }
         return results;
      } catch (InterruptedException e) {
         throw new RuntimeException("Execution was interrupted", e);
      } catch (ExecutionException e) {
         throw new RuntimeException("Task failed", e.getCause());
      }
   }
   
   @PreDestroy
   private void destroy() {
      service.shutdown(); // Disable new tasks from being submitted
      try {
        // Wait a while for existing tasks to terminate
        if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
           service.shutdownNow(); // Cancel currently executing tasks
          // Wait a while for tasks to respond to being cancelled
          if (!service.awaitTermination(60, TimeUnit.SECONDS))
             log.error("Pool did not terminate");
        }
      } catch (InterruptedException ie) {
        // (Re-)Cancel if current thread also interrupted
         service.shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      }
   }
}
