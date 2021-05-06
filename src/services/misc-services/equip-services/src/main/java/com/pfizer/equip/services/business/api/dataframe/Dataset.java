package com.pfizer.equip.services.business.api.dataframe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

// Class for storing dataframes returned or passed from Dataframe Service
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {
   private String id;
   private String type;
   private String data;
   private String stdIn;
   private String stdOut;
   private String stdErr;
   private Long dataSize;
   private String mimeType;
   // RVG 18-July-2018: Removing metadata from dataset for now. 
   // RVG 18-July-2018: No uses cases in our codebase and currently causing a problem for report service.
   //private List<KeyValuePairInput> metadata; 

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getData() {
      return data;
   }

   public void setData(String data) {
      this.data = data;
   }

   public String getStdIn() {
      return stdIn;
   }

   public void setStdIn(String stdIn) {
      this.stdIn = stdIn;
   }

   public String getStdOut() {
      return stdOut;
   }

   public void setStdOut(String stdOut) {
      this.stdOut = stdOut;
   }

   public String getStdErr() {
      return stdErr;
   }

   public void setStdErr(String stdErr) {
      this.stdErr = stdErr;
   }

   public Long getDataSize() {
      return dataSize;
   }

   public void setDataSize(Long dataSize) {
      this.dataSize = dataSize;
   }

   public String getMimeType() {
      return mimeType;
   }

   public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
   }
}