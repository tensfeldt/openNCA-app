package com.pfizer.equip.services.input.dataframe;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.pfizer.equip.services.input.InputBuilder;

public class DataloadInputBuilder implements InputBuilder {
   @Override
   public DataloadInput build(String userId, Map<String, Object> inputs) {
      String programId = (String) inputs.get(DataloadInput.KEY_PROGRAM_ID);
      String protocolId = (String) inputs.get(DataloadInput.KEY_PROTOCOL_ID);
      String validationReportId = (String) inputs.get(DataloadInput.KEY_VALIDATION_REPORT_ID);
      String mappingInformationList = (String) inputs.get(DataloadInput.KEY_MAPPING_INFO_LIST);
      String specificationType = (String) inputs.get(DataloadInput.KEY_SPECIFICATION_TYPE);
      String specificationVersion = (String) inputs.get(DataloadInput.KEY_SPECIFICATION_VERSION);
      String specificationId = (String) inputs.get(DataloadInput.KEY_SPECIFICATION_ID);
      String pkterm = (String) inputs.get(DataloadInput.KEY_PKTERM);
      String analytes = (String) inputs.get(DataloadInput.KEY_ANALYTES);
      String dataType = (String) inputs.get(DataloadInput.KEY_DATATYPE);
      String numberRows = (String) inputs.get(DataloadInput.KEY_NUMBER_ROWS);
      String description = (String) inputs.get(DataloadInput.KEY_DESCRIPTION);
      String filePath = (String) inputs.get(DataloadInput.KEY_FILE_PATH);
      String dataStatus = (String) inputs.get(DataloadInput.KEY_DATA_STATUS);
      MultipartFile fileContent = (MultipartFile) inputs.get(DataloadInput.KEY_FILE_CONTENT);
      boolean doPimsLoad = inputs.get(DataloadInput.KEY_DO_PIMS_LOAD) != null ? (boolean) inputs.get(DataloadInput.KEY_DO_PIMS_LOAD) : false;

      DataloadInput input = new DataloadInput(programId, protocolId, validationReportId, mappingInformationList, specificationType, specificationVersion,
            specificationId, pkterm, analytes, dataType, numberRows, description, filePath, dataStatus, fileContent, doPimsLoad);
      return input;
   }
}
