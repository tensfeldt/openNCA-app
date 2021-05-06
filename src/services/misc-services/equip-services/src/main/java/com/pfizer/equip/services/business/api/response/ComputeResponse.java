package com.pfizer.equip.services.business.api.response;

import java.util.List;
import java.util.Set;

public class ComputeResponse {
   String started;
   String completed;
   String status;
   String stdin;
   String stdout;
   String stderr;
   String batch;
   String containerId;
   Set<String> dataframes;
   List<String> datasetData;

   public String getStarted() {
      return started;
   }

   public void setStarted(String started) {
      this.started = started;
   }

   public String getCompleted() {
      return completed;
   }

   public void setCompleted(String completed) {
      this.completed = completed;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getStdin() {
      return stdin;
   }

   public void setStdin(String stdin) {
      this.stdin = stdin;
   }

   public String getStdout() {
      return stdout;
   }

   public void setStdout(String stdout) {
      this.stdout = stdout;
   }

   public String getStderr() {
      return stderr;
   }

   public void setStderr(String stderr) {
      this.stderr = stderr;
   }

   public String getBatch() {
      return batch;
   }

   public void setBatch(String batch) {
      this.batch = batch;
   }

   public String getContainerId() {
      return containerId;
   }

   public void setContainerId(String containerId) {
      this.containerId = containerId;
   }

   public Set<String> getDataframes() {
      return dataframes;
   }

   public void setDataframes(Set<String> dataframes) {
      this.dataframes = dataframes;
   }

   public List<String> getDatasetData() {
      return datasetData;
   }

   public void setDatasetData(List<String> datasetData) {
      this.datasetData = datasetData;
   }
}
