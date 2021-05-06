package com.pfizer.equip.processors.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pfizer.equip.processors.framework.ProcessorContext;
import com.pfizer.equip.processors.framework.ProcessorEngine;

@RestController
@RequestMapping("/api")
public class ProcessorController {

   private static final Logger logger = LoggerFactory.getLogger(ProcessorController.class);

   @RequestMapping(value = "processors", method = RequestMethod.GET)
   public List<ProcessorState> getProcessors() {
      ProcessorEngine engine = ProcessorEngine.getInstance();
      List<ProcessorContext> processorContexts = engine.getProcessorContextList();
      List<ProcessorState> processorStates = new ArrayList<>();
      for (int i = 0; i < processorContexts.size(); i++) {
         ProcessorContext context = (ProcessorContext) processorContexts.get(i);
         ProcessorState state = new ProcessorState();
         state.setDescription(context.getDetail().getDescription());
         state.setName(context.getDetail().getName());
         state.setStatus(context.getStatus().toString());
         state.setStatusDescription(context.getStatus().getStateDescription());
         processorStates.add(state);
      }
      return processorStates;
   }

   @RequestMapping(value = "processors/{name}/{state}", method = RequestMethod.PUT)
   public ResponseEntity<String> setState(@PathVariable String name, @PathVariable String state) {
      logger.info("Starting setState. name = {} and state = {}", name, state);
      try {
         ProcessorEngine engine = ProcessorEngine.getInstance();
         if (StringUtils.equals(state, "start")) {
            engine.scheduleProcessor(name);
            return new ResponseEntity<String>("", HttpStatus.OK);
         } else if (StringUtils.equals(state, "stop")) {
            engine.unschedule(name);
            return new ResponseEntity<String>("", HttpStatus.OK);
         } else if (StringUtils.equals(state, "run")) {
            engine.runProcessorNow(name);
            return new ResponseEntity<String>("", HttpStatus.OK);
         } else {
            return new ResponseEntity<String>("The state " + state + " is invalid", HttpStatus.BAD_REQUEST);
         }
      } catch (Exception e) {
         logger.error(e.getMessage(), e);
         return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
      }
   }

   public static class ProcessorState {
      private String name;
      private String statusDescription;
      private String status;
      private String description;

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getStatusDescription() {
         return statusDescription;
      }

      public void setStatusDescription(String statusDescription) {
         this.statusDescription = statusDescription;
      }

      public String getStatus() {
         return status;
      }

      public void setStatus(String status) {
         this.status = status;
      }

      public String getDescription() {
         return description;
      }

      public void setDescription(String description) {
         this.description = description;
      }

   }
}
