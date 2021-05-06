package com.pfizer.equip.services.input.notification;

public enum NotificationType {
   REALTIME("realtime_email"),
   DAILY_EMAIL("daily_email"),
   WEEKLY_EMAIL("weekly_email");

   private final String value;

   private NotificationType(String value) {
      this.value = value;
   }

   public String getValue() {
      return value;
   }

   public static NotificationType fromString(String value) {
      if (value != null) {
         for (NotificationType notificationType : NotificationType.values()) {
            if (value.equalsIgnoreCase(notificationType.value)) {
               return notificationType;
            }
         }
      }
      return null;
   }
   
   @Override
   public String toString() {
      return this.value;
   }
}
