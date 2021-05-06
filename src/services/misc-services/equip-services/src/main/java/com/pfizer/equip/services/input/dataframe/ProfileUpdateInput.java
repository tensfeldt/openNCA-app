package com.pfizer.equip.services.input.dataframe;

public class ProfileUpdateInput {
   private String id;
   private String[] profileConfig;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String[] getProfileConfig() {
      return profileConfig;
   }

   public void setProfileConfig(String[] profileConfig) {
      this.profileConfig = profileConfig;
   }
}
