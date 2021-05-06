package com.pfizer.equip.processors.framework;

/**
 * 
 * @author plee
 *
 */
public enum ProcessorStatus {
   OFF("Processor is off"),
   ON("Waiting for next run time"),
   RUNNING("Running"),
   RUNNING_ADHOC("Running"),
   ERROR("Error"),
   DONE("Done. Waiting for next run time"),
   TRANSITION_OFF("Stopping Processor");

   private String stateDescription;

   private ProcessorStatus(String stateDescription) {
      this.stateDescription = stateDescription;
   }

   public String getStateDescription() {
      return stateDescription;
   }
}
