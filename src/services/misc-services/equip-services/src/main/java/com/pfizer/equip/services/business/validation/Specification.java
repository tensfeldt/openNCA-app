package com.pfizer.equip.services.business.validation;

import java.util.List;

import com.pfizer.equip.services.input.validation.MappingInformation;

public class Specification {
   private String type;
   private String version;
   private String path;
   private String id;
   private String pkTermColumnName;
   private String subjectIdColumnName;
   private List<MappingInformation> mappingList;
   private List<String> sdeidColumns;

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getPkTermColumnName() {
      return pkTermColumnName;
   }

   public void setPkTermColumnName(String pkTermColumnName) {
      this.pkTermColumnName = pkTermColumnName;
   }

   public String getSubjectIdColumnName() {
      return subjectIdColumnName;
   }

   public void setSubjectIdColumnName(String subjectIdColumnName) {
      this.subjectIdColumnName = subjectIdColumnName;
   }

   public List<MappingInformation> getMappingList() {
      return mappingList;
   }

   public void setMappingList(List<MappingInformation> mappingList) {
      this.mappingList = mappingList;
   }

   public List<String> getSdeidColumns() {
      return sdeidColumns;
   }

   public void setSdeidColumns(List<String> sdeidColumns) {
      this.sdeidColumns = sdeidColumns;
   }
}
