package com.pfizer.equip.services.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class SimulatedMultipartFile implements MultipartFile {
   private final byte[] content;
   private String fileName;
   private String contentType;

   public SimulatedMultipartFile(byte[] content, String fileName, String contentType) {
      this.content = content;
      this.fileName = fileName;
      this.contentType = contentType;
   }

   @Override
   public String getName() {
      return fileName;
   }

   @Override
   public String getOriginalFilename() {
      return fileName;
   }

   @Override
   public String getContentType() {
      return contentType;
   }

   @Override
   public boolean isEmpty() {
      return content == null || content.length == 0;
   }

   @Override
   public long getSize() {
      return content.length;
   }

   @Override
   public byte[] getBytes() throws IOException {
      return content;
   }

   @Override
   public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(content);
   }

   @Override
   public void transferTo(File dest) throws IOException, IllegalStateException {
      FileOutputStream fos = new FileOutputStream(dest);
      fos.write(content);
      fos.close();
   }
}
