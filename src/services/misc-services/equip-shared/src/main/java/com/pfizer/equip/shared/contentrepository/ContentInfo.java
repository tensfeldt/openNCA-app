package com.pfizer.equip.shared.contentrepository;

public class ContentInfo {
   private byte[] content;
   private String mimeType;
   private String fileName;

   public ContentInfo(byte[] content, String mimeType, String fileName) {
      this.content = content;
      this.mimeType = mimeType;
      this.fileName = fileName;
   }

   public byte[] getContent() {
      return content;
   }

   public void setContent(byte[] content) {
      this.content = content;
   }

   public String getMimeType() {
      return mimeType;
   }

   public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
   }

   public String getFileName() {
      return fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }
}
