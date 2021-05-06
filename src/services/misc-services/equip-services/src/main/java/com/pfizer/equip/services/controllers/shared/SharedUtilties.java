package com.pfizer.equip.services.controllers.shared;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.pfizer.equip.shared.contentrepository.ContentInfo;

public class SharedUtilties {
   public static ResponseEntity<byte[]> createBinaryResponse(ContentInfo contentInfo) {
      HttpHeaders headers = new HttpHeaders();
      String mimeType = contentInfo.getMimeType();
      String fileName = contentInfo.getFileName();
      byte[] content = contentInfo.getContent();
      headers.setContentType(MediaType.parseMediaType(mimeType));
      headers.setContentLength(content.length);
      ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(fileName).build();
      headers.setContentDisposition(contentDisposition);
      return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
   }
}
