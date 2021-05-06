package com.pfizer.equip.services.input.validation;

public enum FileValidationMessages {

   // Required /Mandatory field message
   EQUIP_FV_ERR01("EQ-FV-001 : Mandatory field"),

   // Min Length validation message
   EQUIP_FV_ERR02("EQ-FV-002 : Minimum Length violation"),

   // Allowed Values validation message
   EQUIP_FV_ERR03("EQ-FV-003 : Allowed values are"),

   // Pattern (ALPHANUMERIC, ALPHABET, DATETIME , DATE and TIME etc) validation message
   EQUIP_FV_ERR04("EQ-FV-004 : Pattern Mismatch"),

   // Field Data Set Validation for the Condition NOTNULL, validation message
   EQUIP_FV_ERR05("EQ-FV-005 : Null values in the mentioned data sets are not allowed. Data set validation failed"),

   // Required Column should be available in the Input file
   EQUIP_FV_ERR06("EQ-FV-006 : Required Column not available in the input data set"),

   // If the file name does not match with the given pattern
   EQUIP_FV_ERR07("EQ-FV-007 : Invalid file name"),

   // If the file name does not match with the given pattern
   // Removed per EQ-1399, no SDEID validation
   // EQUIP_FV_ERR08("EQ-FV-008 : Uniqueness of record for the SDEID components failed"),

   // If the input file has extraneous column(s)
   EQUIP_FV_ERR09("EQ-FV-009 : Extraneous Column(s) found"),

   // If the input file violates column order/position
   EQUIP_FV_ERR10("EQ-FV-010 : Column order/position error starting from this column. "),

   // If the given delimiter does not exists in delimiter specification list
   EQUIP_FV_ERR11("EQ-FV-011 : Invalid Delimiter"),

   // Max Length validation message
   EQUIP_FV_ERR12("EQ-FV-012 : Maximum Length violation"),

   // Min Range validation message
   EQUIP_FV_ERR13("EQ-FV-013 : Minimum Range violation"),

   // Max Range validation message
   EQUIP_FV_ERR14("EQ-FV-014 : Maximum Range violation"),

   // Case Sensitivity Check message
   EQUIP_FV_ERR15("EQ-FV-015 : Case sensitivity check failed"),

   // Leading/trailing spaces Check message
   EQUIP_FV_ERR16("EQ-FV-016 : Leading / Trailing space(s) found"),

   // Date Range Validation error message
   EQUIP_FV_ERR17("EQ-FV-017 : Exception in Date Range Validation"),

   // Date Range Validation error message
   EQUIP_FV_ERR18("EQ-FV-018 : Exception in Time Range Validation"),

   // Max Range validation message
   EQUIP_FV_ERR19("EQ-FV-019 : Empty Lines found"),

   // Case sensitivity for Allowed Values validation message
   EQUIP_FV_ERR20("EQ-FV-020 : Case sensitivity check failed. Allowed values are "),

   // Case sensitivity for Allowed Values validation message
   EQUIP_FV_ERR21("EQ-FV-021 : Mismatch between the selected delimiter and delimiter in the input file"),

   // Non Unique Field having more than one value.
   EQUIP_FV_ERR22("EQ-FV-022 : Non Unique Field having more than one value in the file. Values are: "),

   // Header is missing in the input file
   EQUIP_FV_ERR23("EQ-FV-023 : Header not found: could not find at least one defined Field in the first row of the file"),

   // Unique Field having more than one value.
   EQUIP_FV_ERR24("EQ-FV-024 : Unique Field having duplicate values. Duplicated value: "),

   // Column is mandatory for all rows even if one row has non null value.
   EQUIP_FV_ERR25("EQ-FV-025 : This value should be either null or non null for the entire file."),

   // Non ASCII character is present in the file.
   EQUIP_FV_ERR26("EQ-FV-026 : Found Non ASCII character in the file"),

   // File is not of type CSV.
   EQUIP_FV_ERR27("EQ-FV-027 : File is not with the extension .csv"),

   // Field Data Set Validation for the Condition DEPENDENTCONSTRAINT, validation message
   EQUIP_FV_ERR28("EQ-FV-028 : Null values in both the mentioned data sets are not allowed. Data set validation failed"),

   // Empty Input CSV file.
   EQUIP_FV_ERR29("EQ-FV-029 : Empty Input CSV file."),

   // Cross File Validation message for the Parent file for the columns mentioned in <CrossFileFieldSets> with respect to the Child (PKS file)
   EQUIP_FV_ERR30("EQ-FV-030 : Cross File Validation failed"),

   // Uniqueness violated for this column
   EQUIP_FV_ERR31("EQ-FV-031 : Uniqueness violated"),
   
   // Condition evaluation failed.
   EQUIP_FV_ERR32("EQ-FV-032 : Condition evaluation failed."),
   
   // If the file name does not match with the given pattern
   EQUIP_FV_ERR33("EQ-FV-033 : Duplicate Records found"),

   // Empty column
   EQUIP_FV_ERR34("EQ-FV-034 : Empty column header found"),

   // Identicalness violated for this column
   EQUIP_FV_ERR35("EQ-FV-035 : Identicalness violated starting at this row"),

   // Identicalness violated for this column
   EQUIP_FV_ERR36("EQ-FV-036 : Empty column found");

   private String errMessageCode;

   FileValidationMessages(String errMessageCode) {
      this.errMessageCode = errMessageCode;
   }

   public String getErrMessageCode() {
      return errMessageCode;
   }

}
