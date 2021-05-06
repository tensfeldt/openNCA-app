package com.pfizer.equip.services.input.specification;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.pfizer.equip.services.input.InputBuilder;

public class SpecificationInputBuilder implements InputBuilder {
   private List<MultipartFile> fileContent;
   private List<MultipartFile> pksFileContent;

   @Override
   public SpecificationInput build(String userId, Map<String, Object> inputs) {
      String specificationPath = (String) inputs.get(KEY_SPEC_PATH);
      String specificationId = (String) inputs.get(KEY_SPEC_ID);
      String fileSpec = (String) inputs.get(KEY_SPEC_TYPE);
      String fileVersion = (String) inputs.get(KEY_SPEC_VERSION);
      String delimiter = (String) inputs.get(KEY_DELIMITER);
      String fileType = (String) inputs.get(KEY_FILE_TYPE);
      //Applicable only for cross file validation
      String pksSpecificationPath = (String) inputs.get(KEY_PKS_SPEC_PATH);
      String pksFileSpecType = (String) inputs.get(KEY_PKS_SPEC_TYPE);
      String pksFileVersion = (String) inputs.get(KEY_PKS_SPEC_VERSION);

      return new SpecificationInput(specificationId, userId, this.fileContent, this.pksFileContent, specificationPath, fileType, delimiter, fileSpec, fileVersion,pksSpecificationPath,pksFileSpecType,pksFileVersion);
   }

   public void setFileContent(List<MultipartFile> fileContent) {
      this.fileContent = fileContent;
   }

   public void setPksFileContent(List<MultipartFile> pksFileContent) {
      this.pksFileContent = pksFileContent;
   }

}
