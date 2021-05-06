package com.pfizer.equip.services.input.specification;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.pfizer.equip.services.input.AbstractInput;

public class SpecificationInput extends AbstractInput {
   private String specificationPath;
   private final String specificationId;
   private String specificationType;
   private String specificationVersion;
   private final String userId;
   private final List<MultipartFile> fileContent;
   private final List<MultipartFile> pksFileContent;
   private String fileType;
   private String delimiter;

   // Applicable only for cross file validation
   private String pksSpecificationPath;
   private String pksFileSpecType;
   private String pksFileVersion;

   /**
    * Using the builder design pattern here. Constructor is package only so that it cannot be constructed publicly and the state of this remains immutable.
    */
   SpecificationInput(final String specificationId, final String userId, final List<MultipartFile> fileContent, final List<MultipartFile> pksFileContent,
         String specificationPath, String fileType, String delimiter, String specificationType, String specificationVersion, String pksSpecificationPath,
         String pksFileSpecType, String pksFileVersion) {
      this.specificationId = specificationId;
      this.userId = userId;
      this.fileContent = fileContent;
      this.pksFileContent = pksFileContent;
      this.specificationPath = specificationPath;
      this.fileType = fileType;
      this.delimiter = delimiter;
      this.specificationType = specificationType;
      this.specificationVersion = specificationVersion;
      this.pksSpecificationPath = pksSpecificationPath;
      this.pksFileSpecType = pksFileSpecType;
      this.pksFileVersion = pksFileVersion;
   }

   public List<MultipartFile> getPksFileContent() {
      return pksFileContent;
   }

   public String getSpecificationPath() {
      return specificationPath;
   }

   public void setSpecificationPath(String specificationPath) {
      this.specificationPath = specificationPath;
   }

   public String getSpecificationType() {
      return specificationType;
   }

   public void setSpecificationType(String specificationType) {
      this.specificationType = specificationType;
   }

   public String getSpecificationVersion() {
      return specificationVersion;
   }

   public void setSpecificationVersion(String specificationVersion) {
      this.specificationVersion = specificationVersion;
   }

   public String getFileType() {
      return fileType;
   }

   public void setFileType(String fileType) {
      this.fileType = fileType;
   }

   public String getDelimiter() {
      return delimiter;
   }

   public void setDelimiter(String delimiter) {
      this.delimiter = delimiter;
   }

   public String getSpecificationId() {
      return specificationId;
   }

   public String getUserId() {
      return userId;
   }

   public List<MultipartFile> getFileContent() {
      return fileContent;
   }

   public String getPksSpecificationPath() {
      return pksSpecificationPath;
   }

   public void setPksSpecificationPath(String pksSpecificationPath) {
      this.pksSpecificationPath = pksSpecificationPath;
   }

   public String getPksFileSpecType() {
      return pksFileSpecType;
   }

   public void setPksFileSpecType(String pksFileSpecType) {
      this.pksFileSpecType = pksFileSpecType;
   }

   public String getPksFileVersion() {
      return pksFileVersion;
   }

   public void setPksFileVersion(String pksFileVersion) {
      this.pksFileVersion = pksFileVersion;
   }

}
