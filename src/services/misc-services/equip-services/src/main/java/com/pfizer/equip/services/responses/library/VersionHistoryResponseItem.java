package com.pfizer.equip.services.responses.library;

public class VersionHistoryResponseItem {
   private int version;
   private String versionId;
   private LibraryArtifactResponse artifactInfo;

   public int getVersion() {
      return version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public String getVersionId() {
      return versionId;
   }

   public void setVersionId(String versionId) {
      this.versionId = versionId;
   }

   public LibraryArtifactResponse getArtifactInfo() {
      return artifactInfo;
   }

   public void setArtifactInfo(LibraryArtifactResponse artifactInfo) {
      this.artifactInfo = artifactInfo;
   }
}
