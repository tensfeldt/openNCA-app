package com.pfizer.equip.services.input.dataframe;

import org.springframework.web.multipart.MultipartFile;

import com.pfizer.equip.services.input.AbstractInput;

public class DataloadInput extends AbstractInput {
   public static final String KEY_PROGRAM_ID = "programId";
   public static final String KEY_PROTOCOL_ID = "protocolId";
   public static final String KEY_SPECIFICATION_TYPE = "specificationType";
   public static final String KEY_SPECIFICATION_VERSION = "specificationVersion";
   public static final String KEY_SPECIFICATION_ID = "specificationId";
   public static final String KEY_PKTERM = "pkterm";
   public static final String KEY_ANALYTES = "analytes";
   public static final String KEY_DATATYPE = "dataType";
   public static final String KEY_NUMBER_ROWS = "numberRows";
   public static final String KEY_DESCRIPTION = "description";
   public static final String KEY_FILE_PATH = "filePath";
   public static final String KEY_FILE_CONTENT = "fileContent";
   public static final String KEY_VALIDATION_REPORT_ID = "validationReportId";
   public static final String KEY_MAPPING_INFO_LIST = "mappingInformationList";
   public static final String KEY_DO_PIMS_LOAD = "doPimsLoad";
   public static final String KEY_DATA_STATUS = "dataStatus";

   private String programId;
   private String protocolId;
   private String validationReportId;
   private String mappingInformationList;
   private String specificationType;
   private String specificationVersion;
   private String specificationId;
   private String pkterm;
   private String analytes;
   private String dataType;
   private String numberRows;
   private String description;
   private String filePath;
   private String dataStatus;
   private MultipartFile fileContent;
   private boolean doPimsLoad;

   /**
    * Using the builder design pattern here. Constructor is package only so that it cannot be constructed publicly and the state of this remains immutable.
    */
   DataloadInput(final String programId, final String protocolId, final String validationReportId, final String mappingInformationList, final String specificationType,
         final String specificationVersion, final String specificationId, final String pkterm, final String analytes, final String dataType, final String numberRows,
         final String description, final String filePath, final String dataStatus, final MultipartFile fileContent, boolean doPimsLoad) {
      this.programId = programId;
      this.protocolId = protocolId;
      this.validationReportId = validationReportId;
      this.mappingInformationList = mappingInformationList;
      this.specificationType = specificationType;
      this.specificationVersion = specificationVersion;
      this.specificationId = specificationId;
      this.pkterm = pkterm;
      this.analytes = analytes;
      this.dataType = dataType;
      this.numberRows = numberRows;
      this.description = description;
      this.filePath = filePath;
      this.dataStatus = dataStatus;
      this.fileContent = fileContent;
      this.doPimsLoad = doPimsLoad;
   }

   public String getProgramId() {
      return programId;
   }

   public String getProtocolId() {
      return protocolId;
   }

   public String getValidationReportId() {
      return validationReportId;
   }

   public String getMappingInformationList() {
      return mappingInformationList;
   }

   public String getSpecificationType() {
      return specificationType;
   }

   public String getSpecificationVersion() {
      return specificationVersion;
   }

   public String getSpecificationId() {
      return specificationId;
   }

   public String getPkterm() {
      return pkterm;
   }

   public String getAnalytes() {
      return analytes;
   }

   public String getDataType() {
      return dataType;
   }

   public String getNumberRows() {
      return numberRows;
   }

   public String getDescription() {
      return description;
   }

   public String getFilePath() {
      return filePath;
   }
   
   public String getDataStatus() {
	   return dataStatus;
   }

   public MultipartFile getFileContent() {
      return fileContent;
   }

   public boolean isDoPimsLoad() {
      return doPimsLoad;
   }

   public void setDoPimsLoad(boolean doPimsLoad) {
      this.doPimsLoad = doPimsLoad;
   }
}
