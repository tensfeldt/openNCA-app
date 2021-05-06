package com.pfizer.equip.processors.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author plee
 *
 */
public class ProcessorSettings {

   private List<ProcessorDetail> processorDetails;
   private List<Holiday> holidays;

   public ProcessorSettings() {
      processorDetails = new ArrayList<>();
      holidays = new ArrayList<>();
   }

   public void addProcessorDetail(ProcessorDetail detail) {
      processorDetails.add(detail);
   }

   public void addHoliday(Holiday holiday) {
      holidays.add(holiday);
   }

   public List<ProcessorDetail> getProcessorDetails() {
      return processorDetails;
   }

   public void setProcessorDetails(List<ProcessorDetail> processorDetails) {
      this.processorDetails = processorDetails;
   }

   public List<Holiday> getHolidays() {
      return holidays;
   }

   public void setHolidays(List<Holiday> holidays) {
      this.holidays = holidays;
   }

   public static class Holiday {
      private int day;
      private int month;

      public int getDay() {
         return day;
      }

      public void setDay(int day) {
         this.day = day;
      }

      public int getMonth() {
         return month;
      }

      public void setMonth(int month) {
         this.month = month;
      }

   }
}
