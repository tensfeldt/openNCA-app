package com.pfizer.equip.services.business.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.pfizer.equip.services.business.validation.exceptions.FileValidationRuntimeException;
import com.pfizer.equip.services.input.validation.FieldLevelLogReport;
import com.pfizer.equip.services.input.validation.FileValidationMessages;
import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;
import com.pfizer.equip.services.input.validation.xmlparser.FieldParser;

public class FieldValidator {

   private int rowIdx;
   private FieldParser fieldParser;
   private String columnValue;
   private String columnAliasName;
   private List<FieldLevelLogReport> fieldLevelMsgList = new LinkedList<>();
   ValidationStatusTypes fieldValidationStatus = ValidationStatusTypes.INFO;

   public FieldValidator(FieldParser fieldParser, String csvRowVal, int rowIndex, String columnAliasName) {
      this.fieldParser = fieldParser;
      this.rowIdx = rowIndex;
      this.columnValue = csvRowVal;
      this.columnAliasName=columnAliasName;
   }

   /**
    * Validate the input data based on the below criteria
    */
   public void validateInput() {
      // 1. Check for NonASCII characters in the value
      validateASCIICharacters();

      // 2. Mandatory / Optional Check
      if (validateIsNull()) { 
         // Only fire the following check if IsNull validation passes
         // I.e., the column is not null 
         // 3. Min and Max Length Check; if not null
         validateLength();
      };

      // 4. Allowed Values Check
      validateAllowedValues();

      // 5. validate patterns and if pattern matches validate Range
      validatePatternAndRange();

      // 6. validate leading/trailing spaces
      validateleadingTrailingSpaces();

   }

   /**
    * Validate the field value to check for Non ASCII characters
    * 
    */
   private void validateASCIICharacters() {
      // Check for NON-ASCII characters
      if (!CharMatcher.ASCII.matchesAllOf(this.columnValue))
         throw new FileValidationRuntimeException(FileValidationMessages.EQUIP_FV_ERR26.getErrMessageCode());
   }

   /**
    * Consolidate the field level error messages in to the map
    * 
    * @param colValue
    * @param errorMsg
    */
   private void addErrorLog(String colValue, ValidationStatusTypes logLevel, String errorMsg) {
      String columnName=null;
      // The "ERROR" log level should not be overridden and the "INFO" log level need not to be processed for setting validation status
      if (!fieldValidationStatus.equals(ValidationStatusTypes.ERROR) && !logLevel.equals(ValidationStatusTypes.INFO)) {
         fieldValidationStatus = logLevel;
      }
      if(columnAliasName!=null) {
         columnName = columnAliasName;
      }else {
         columnName =  this.fieldParser.getColumnName().getValue();
      }
      this.fieldLevelMsgList.add(new FieldLevelLogReport(this.rowIdx, columnName, colValue, errorMsg, logLevel));
   }

   /**
    * Required/ Mandatory check For the mandatory field. The column should be present and value should not be empty
    */
   public boolean validateIsNull() {
      if (!this.fieldParser.getIsNull().isValue() && (this.columnValue == null || this.columnValue.trim().length() == 0)) {
         addErrorLog(this.columnValue, this.fieldParser.getIsNull().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR01.getErrMessageCode());
         return false;
      }
      return true;
   }

   /**
    * To validate the data against the length of the data It should satisfy minimum and maximum length limits.
    * If the min/max property is null, then it is not applicable for the validation of this field, so skip.
    */
   public void validateLength() {
      if (this.fieldParser.getMinLength() != null && (this.fieldParser.getMinLength().getValue() > this.columnValue.trim().length())) {
         addErrorLog(this.columnValue, this.fieldParser.getMinLength().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR02.getErrMessageCode());
      }
      if (this.fieldParser.getMaxLength() != null && (this.fieldParser.getMaxLength().getValue() < this.columnValue.trim().length())) {
         addErrorLog(this.columnValue, this.fieldParser.getMaxLength().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR12.getErrMessageCode());
      }
   }

   /**
    * To validate the data against the range of the data It should satisfy minimum and maximum range limits
    */
   public void validateRange() {
      switch (this.fieldParser.getDataType().getSubType()) {
      case "NUMBER":
         validateNumberRange(this.fieldParser.getMinRange().getValue(), this.fieldParser.getMaxRange().getValue(), this.columnValue);
         break;
      case "DATE":
         validateDateRange(this.fieldParser.getMinRange().getValue(), this.fieldParser.getMaxRange().getValue(), this.columnValue);
         break;
      case "TIME":
         validateTimeRange(this.fieldParser.getMinRange().getValue(), this.fieldParser.getMaxRange().getValue(), this.columnValue);
         break;
      default:
         break;
      }

   }

   /**
    * To Validate the allowed Values for for the field data Example: Weight units should be in KG,LB etc.,
    */
   public void validateAllowedValues() {
      if ((this.columnValue.trim().length() > 0 && this.fieldParser.getAllowedValues() != null && this.fieldParser.getAllowedValues().getValue() != null)) {
         if (!(this.fieldParser.getAllowedValues().getValue().contains(this.columnValue))) {
            List<String> allowedValuesList = new ArrayList<>();
            allowedValuesList.addAll(this.fieldParser.getAllowedValues().getValue());
            allowedValuesList.replaceAll(String::toUpperCase);
            if (!allowedValuesList.contains(this.columnValue.toUpperCase())) {
               // Value not in the allowed values list
               // This condition check for Number pattern if it is not allowed values AQL,BQL etc.,
               if (this.fieldParser.getDataType().getSubType() != null && (this.fieldParser.getDataType().getSubType().equals("NUMBER"))) {
                  validateNumber();
               } else {
                  addErrorLog(this.columnValue, this.fieldParser.getAllowedValues().getLogLevel(),
                        FileValidationMessages.EQUIP_FV_ERR03.getErrMessageCode() + " : " + this.fieldParser.getAllowedValues().getValue());
               }
            } else if (this.fieldParser.getAllowedValues().getCaseSensitive()) {
               // Case mismatch
               // This condition check for Number pattern if it is not allowed values AQL,BQL etc.,
               if (this.fieldParser.getDataType().getSubType() != null && (this.fieldParser.getDataType().getSubType().equals("NUMBER"))) {
                  validateNumber();
               } else {
                  addErrorLog(this.columnValue, this.fieldParser.getAllowedValues().getLogLevel(),
                        FileValidationMessages.EQUIP_FV_ERR20.getErrMessageCode() + " : " + this.fieldParser.getAllowedValues().getValue());
               }

            }

         }

      }
   }

   /**
    * Number validation handled separately as NUMBER can also accept Allowed Values mentioned in XML.
    */
   private void validateNumber() {
      if (!(this.fieldParser.getDataType().getValue().isEmpty())) {
         Pattern pattern = Pattern.compile(this.fieldParser.getDataType().getValue());
         if (this.columnValue.trim().length() > 0 && !pattern.matcher(this.columnValue).matches()) {
            addErrorLog(this.columnValue, this.fieldParser.getDataType().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR04.getErrMessageCode());
         } else if (this.columnValue.trim().length() > 0 && fieldParser.getMinRange() != null && fieldParser.getMaxRange() != null) {
            // Once the pattern matches , range should be validated
            validateRange();
         }
      }

   }

   /**
    * To validate pattern based on the DataType tag in library file
    */
   public void validatePatternAndRange() {
      // SubType = DATE/TIME or NUMBER
      if (this.fieldParser.getDataType().getSubType() != null) {
         if (!this.columnValue.isEmpty()
               && (this.fieldParser.getDataType().getSubType().equals("DATE") || (this.fieldParser.getDataType().getSubType().equals("TIME")))) {
            validateDatePattern();
         }

         // Do nothing for NUMBER right now as NUMBER is handled as part of ALLOWEDVALUES validation
      } else
      // For ALLCHARACTERS type, No validation needs to be performed with patterns and value is empty.
      if (!(this.fieldParser.getDataType().getValue().isEmpty())) {
         Pattern pattern = Pattern.compile(this.fieldParser.getDataType().getValue());
         if (this.columnValue.trim().length() > 0 && !pattern.matcher(this.columnValue).matches()) {
            addErrorLog(this.columnValue, this.fieldParser.getDataType().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR04.getErrMessageCode());
         }
      }

   }

   /**
    * To validate the date based on pattern taken directly from the value of the dataType tag in XML
    * 
    * Pattern format examples : dd-MM-yyyy , yyyy-MM-dd , HH:mm
    */
   private void validateDatePattern() {
      try {
         SimpleDateFormat dateTimeFormat = new SimpleDateFormat(this.fieldParser.getDataType().getValue());
         dateTimeFormat.setLenient(false);
         dateTimeFormat.parse(this.columnValue);
         if (this.columnValue.trim().length() > 0 && fieldParser.getMinRange() != null && fieldParser.getMaxRange() != null) {
            // Once the pattern matches , range should be validated
            validateRange();
         }
      } catch (ParseException e) {
         // Pattern Mismatch
         addErrorLog(this.columnValue, this.fieldParser.getDataType().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR04.getErrMessageCode());
      }

   }

   /**
    * Validate the date is within the min date rage and max date range limit
    * 
    * @param minRange
    * @param maxRange
    * @param inputDate
    */
   private void validateDateRange(String minRange, String maxRange, String inputDate) {
      try {
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
         LocalDate minDate = LocalDate.parse(minRange, formatter);
         LocalDate maxDate = LocalDate.parse(maxRange, formatter);
         LocalDate date = LocalDate.parse(inputDate, formatter);

         if (date.isBefore(minDate)) {
            addErrorLog(this.columnValue, this.fieldParser.getMinRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR13.getErrMessageCode());
         }
         if (date.isAfter(maxDate)) {
            addErrorLog(this.columnValue, this.fieldParser.getMaxRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR14.getErrMessageCode());
         }
      } catch (Exception e) {
         addErrorLog(this.columnValue, this.fieldParser.getMinRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR17.getErrMessageCode());
      }
   }

   /**
    * Validate the input value is within the range limit
    * 
    * @param minRange
    * @param maxRange
    * @param inputValue
    */
   private void validateNumberRange(String minRange, String maxRange, String inputValue) {
      if ((Double.parseDouble(minRange) >= Double.parseDouble(inputValue))) {
         addErrorLog(this.columnValue, this.fieldParser.getMinRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR13.getErrMessageCode());
      }
      if ((Double.parseDouble(maxRange) <= Double.parseDouble(inputValue))) {
         addErrorLog(this.columnValue, this.fieldParser.getMaxRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR14.getErrMessageCode());
      }
   }

   /**
    * Validate input value is within the time limits
    * 
    * @param minRange
    * @param maxRange
    * @param inputValue
    */
   private void validateTimeRange(String minRange, String maxRange, String inputValue) {
      try {
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
         LocalTime minTime = LocalTime.parse(minRange, formatter);
         LocalTime maxTime = LocalTime.parse(maxRange, formatter);
         LocalTime givenTime = LocalTime.parse(inputValue, formatter);
         if (givenTime.isBefore(minTime)) {
            addErrorLog(this.columnValue, this.fieldParser.getMinRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR13.getErrMessageCode());
         }
         if (givenTime.isAfter(maxTime)) {
            addErrorLog(this.columnValue, this.fieldParser.getMaxRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR14.getErrMessageCode());
         }
      } catch (Exception e) {
         addErrorLog(this.columnValue, this.fieldParser.getMinRange().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR18.getErrMessageCode());
      }
   }

   /**
    * Validate the leading/trailing spaces in the column value
    */
   private void validateleadingTrailingSpaces() {
      if (this.fieldParser.getExtraSpacesValueParser().isValue() && (!this.columnValue.equals(this.columnValue.trim()))) {
         addErrorLog(this.columnValue, this.fieldParser.getExtraSpacesValueParser().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR16.getErrMessageCode());
      }
   }

   public List<FieldLevelLogReport> getFieldLevelMsgList() {
      return fieldLevelMsgList;
   }

   public ValidationStatusTypes getFieldValidationStatus() {
      return fieldValidationStatus;
   }

   public void setFieldValidationStatus(ValidationStatusTypes fieldValidationStatus) {
      this.fieldValidationStatus = fieldValidationStatus;
   }

}
