package com.pfizer.equip.processors.framework;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author plee
 *
 */
public class ProcessorDetail {
   private String name;
   private String description;
   private String className;
   private boolean autoStart;
   private List<String> cronExpressions;
   private Map<String, String> properties;
   private List<Interval> intervals;

   public ProcessorDetail() {
      cronExpressions = new ArrayList<>();
      properties = new HashMap<>();
      intervals = new ArrayList<>();
   }

   public void addCronExpression(String cronExpression) {
      cronExpressions.add(cronExpression);
   }

   public void addProperty(String name, String value) {
      properties.put(name, value);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public List<String> getCronExpressions() {
      return cronExpressions;
   }

   public void setCronExpressions(List<String> cronExpressions) {
      this.cronExpressions = cronExpressions;
   }

   public Map<String, String> getProperties() {
      return properties;
   }

   public void setProperties(Map<String, String> properties) {
      this.properties = properties;
   }

   public List<Interval> getIntervals() {
      return intervals;
   }

   public void setIntervals(List<Interval> intervals) {
      this.intervals = intervals;
   }

   public void addInterval(Interval interval) {
      intervals.add(interval);
   }

   public boolean isAutoStart() {
      return autoStart;
   }

   public void setAutoStart(boolean autoStart) {
      this.autoStart = autoStart;
   }

   public static class Interval {
      private int seconds;
      private Set<Integer> days;
      private Calendar startTime;
      private Calendar endTime;

      public int getSeconds() {
         return seconds;
      }

      public void setSeconds(int seconds) {
         this.seconds = seconds;
      }

      public Set<Integer> getDays() {
         return days;
      }

      public void setDays(Set<Integer> days) {
         this.days = days;
      }

      public Calendar getStartTime() {
         return startTime;
      }

      public void setStartTime(Calendar startTime) {
         this.startTime = startTime;
      }

      public Calendar getEndTime() {
         return endTime;
      }

      public void setEndTime(Calendar endTime) {
         this.endTime = endTime;
      }

   }

}
