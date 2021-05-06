package com.pfizer.equip.services.business.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.services.business.validation.exceptions.FileValidationRuntimeException;
import com.pfizer.equip.services.input.validation.CrossFileLevelLogReport;
import com.pfizer.equip.services.input.validation.EmptyLinesLog;
import com.pfizer.equip.services.input.validation.FieldSetLevelLogReport;
import com.pfizer.equip.services.input.validation.FileLevelLog;
import com.pfizer.equip.services.input.validation.FileLevelLogReport;
import com.pfizer.equip.services.input.validation.FileValidationMessages;
import com.pfizer.equip.services.input.validation.SdeidLevelLogReport;
import com.pfizer.equip.services.input.validation.UniqueSetLevelLogReport;
import com.pfizer.equip.services.input.validation.types.FileLevelLogTypes;
import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;
import com.pfizer.equip.services.input.validation.xmlparser.ColumnsParser;
import com.pfizer.equip.services.input.validation.xmlparser.CrossFileFieldSetsParser;
import com.pfizer.equip.services.input.validation.xmlparser.DuplicateRecordsParser;
import com.pfizer.equip.services.input.validation.xmlparser.EmptyColumnsParser;
import com.pfizer.equip.services.input.validation.xmlparser.ExtraneousColumnsParser;
import com.pfizer.equip.services.input.validation.xmlparser.FieldDefinitionsParser;
import com.pfizer.equip.services.input.validation.xmlparser.FieldParser;
import com.pfizer.equip.services.input.validation.xmlparser.FieldSetsParser;
import com.pfizer.equip.services.input.validation.xmlparser.FileLevelRulesParser;

public class FileValidator {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private FileLevelLogReport fileLevelLogReport = new FileLevelLogReport();
   private List<FieldSetLevelLogReport> fieldSetLevelLogReportList = new LinkedList<>();
   private List<UniqueSetLevelLogReport> uniqueSetLevelLogReport = new LinkedList<>();
   // Duplicate Records uses the same structure of attributes as SDEID
   private List<SdeidLevelLogReport> duplicateRecordsLevelLogReportList = new LinkedList<>();
   private List<CrossFileLevelLogReport> crossFileLevelLogReportList = new LinkedList<>();
   private ValidationStatusTypes fileLevelValidationStatus = ValidationStatusTypes.SUCCESS;
   private ValidationStatusTypes duplicateRecordsValidationStatus = ValidationStatusTypes.SUCCESS;
   private ValidationStatusTypes fieldSetLevelValidationStatus = ValidationStatusTypes.SUCCESS;
   private ValidationStatusTypes crossFileFieldValidationStatus = ValidationStatusTypes.SUCCESS;

   /**
    * Validate the data in the input file(.csv)
    * 
    * @param inputRecordList
    * @param fieldDefinitionsMap
    * @param fieldDefinitionsInput
    * @param inputPksRecordList
    * @param fieldSetDefinitionsList
    * @param sDEIDSets
    * @param extraneousColumnsParser
    * @param emptyLinesParser
    * @return
    */
   public FileValidationLog validateFieldData(List<CSVRecord> inputRecordList, Map<String, FieldParser> fieldDefinitionsMap, FieldDefinitionsParser fieldDefinitionsInput,
         List<CSVRecord> inputPksRecordList) {
      FileValidationLog fileValidationLog = new FileValidationLog();
      fileValidationLog.setFieldLevelValidationStatus(ValidationStatusTypes.SUCCESS);
      // Get all the column alias and add it to the map
      List<String> columnHeaderList = new LinkedList<>();
      columnHeaderList = getHeadersFromInputRecord(inputRecordList.get(0));
      Map<String, List<String>> columnAliasMap = new LinkedHashMap<>();
      for (String fieldName : fieldDefinitionsMap.keySet()) {
         columnAliasMap.put(fieldName, fieldDefinitionsMap.get(fieldName).getColumnAlias().getValue());
      }

      List<FieldSetsParser> fieldSetDefinitionsList = fieldDefinitionsInput.getFieldSetsInput();

      // Removed SDEID Validation per EQ-1399 RVG 05-Dec-2018

      // Duplicate Records Validation
      List<String> duplicateRecordsList = new LinkedList<>();
      String duplicateRecordsColumnNames = null;
      ValidationStatusTypes duplicateRecordsLogLevel = null;
      if (fieldDefinitionsInput.getDuplicateRecordsParser() != null) {
         duplicateRecordsColumnNames = getDuplicateRecordsColumnNames(fieldDefinitionsInput.getDuplicateRecordsParser());
         duplicateRecordsLogLevel = fieldDefinitionsInput.getDuplicateRecordsParser().getLogLevel();
      }

      // Cross File Validation
      List<String> crossFileValidationList = new LinkedList<>();
      Map<String, String> pksCrossFileFieldMap = new LinkedHashMap<>();
      ValidationStatusTypes crossFileFieldLogLevel = null;
      // Perform Cross file validation only if the PKS file is present
      if (inputPksRecordList != null && !inputPksRecordList.isEmpty()) {
         crossFileFieldLogLevel = fieldDefinitionsInput.getCrossFileFieldSetsParser().getLogLevel();
         pksCrossFileFieldMap = getpksCrossFileFieldMap(fieldDefinitionsInput.getCrossFileFieldSetsParser(), inputPksRecordList);
      }
      List<String> validInputFieldNameList = new LinkedList<>();
      Map<String, Map<String, List<String>>> fileLevelValidationRules = new LinkedHashMap<>();
      if (fieldDefinitionsInput.getFileLevelRulesParser() != null) {
         fileLevelValidationRules = createFileLevelRulesMap(fieldDefinitionsInput.getFileLevelRulesParser());
      }
      UniquenessValidator uniquenessValidator = null;
      if (fieldDefinitionsInput.getUniqueSetsParser() != null) {
         uniquenessValidator = new UniquenessValidator();
         uniquenessValidator.intialiseUniquenessValidator(fieldDefinitionsInput.getUniqueSetsParser());
      }

      for (int rowIndex = 0; rowIndex < inputRecordList.size(); rowIndex++) {

         CSVRecord inputRecord = inputRecordList.get(rowIndex);

         if (fieldDefinitionsInput.getEmptyLinesParser() != null) {
            // Assume the record is empty
            boolean isEmpty = true;
            for (String column : inputRecord) {
               // If any of the fields are not blank, line is not empty
               if (!StringUtils.isBlank(column)) {
                  isEmpty = false;
               }
            }
            if (isEmpty) {
               addEmptyLinesList(new EmptyLinesLog(rowIndex, fieldDefinitionsInput.getEmptyLinesParser().getLogLevel(),
                     FileValidationMessages.EQUIP_FV_ERR19.getErrMessageCode()), fieldDefinitionsInput.getEmptyLinesParser().getLogLevel());

               // Next record..
               continue;
            }
         }
         
         Map<String, String> inputHeaderMap = new LinkedHashMap<>();
         // column alias validation
         Map<String, List<String>> columnAliasMapCurrentRow = new LinkedHashMap<>();
         Map<String, String> renamedColumnAliasMapping = new LinkedHashMap<>();
         columnAliasMapCurrentRow.putAll(columnAliasMap);
         for (String inputColumnName : columnHeaderList) {
            if (columnAliasMapCurrentRow.get(inputColumnName) != null) {
               columnAliasMapCurrentRow.remove(inputColumnName);
            } else {
               String validColumnName = getKeybyValueFromMap(columnAliasMapCurrentRow, inputColumnName);
               if (validColumnName != null) {
                  renamedColumnAliasMapping.put(validColumnName, inputColumnName);
                  inputHeaderMap = inputRecord.toMap().keySet().stream().collect(Collectors.toMap(key -> {
                     if (key.equals(inputColumnName)) {
                        return validColumnName;
                     }
                     return key;
                  }, value -> {
                     if (value.equals(inputColumnName) && value.equals(inputRecord.get(value))) {
                        return validColumnName;
                     }
                     return inputRecord.get(value);
                  }));

                  columnAliasMapCurrentRow.remove(validColumnName);
               }
            }
         }

         // updating the column headers to check extraneous column and leading/trailing spaces
         if (inputHeaderMap.isEmpty()) {
            inputHeaderMap = inputRecord.toMap();
         }
         if (rowIndex == 0) {
            inputHeaderMap.replaceAll((key, oldValue) -> oldValue.trim().toUpperCase());

            // Check extraneous column and the leading/trailing spaces in the column header
            validInputFieldNameList = validateExtraneousColumnAndSpaces(inputRecord, fieldDefinitionsMap, fieldDefinitionsInput.getExtraneousColumnsParser());

            // Check For Column Ordering
            if (fieldDefinitionsInput.getColumnOrderingParser() != null && fieldDefinitionsInput.getColumnOrderingParser().isValue()) {
               validateColumnOrder(validInputFieldNameList, fieldDefinitionsMap);
            }

            // Check For Empty Columns
            if (fieldDefinitionsInput.getEmptyColumnsParser() != null) {
               validateEmptyColumns(inputRecordList, inputRecord, fieldDefinitionsInput.getEmptyColumnsParser());
            }

         }

         // update the Input csv Header with upper case and trim the spaces
         Map<String, String> inputRecordWithValidHeader = inputHeaderMap.keySet().stream().collect(Collectors.toMap(key -> key.trim().toUpperCase(), key -> {
            if (renamedColumnAliasMapping.containsKey(key)) {
               return inputRecord.get(renamedColumnAliasMapping.get(key));
            }
            return inputRecord.get(key);
         }));

         if (fieldDefinitionsMap.keySet().size() > 0) {
            for (String fieldName : fieldDefinitionsMap.keySet()) {
               if (rowIndex == 0) {
                  // Validate Column header
                  validateInputHeader(fieldDefinitionsMap, validInputFieldNameList, fieldName, inputHeaderMap);
               } else if (rowIndex > 0 && inputRecordWithValidHeader.containsKey(fieldName)) {
                  if (!fileLevelValidationRules.isEmpty()) {
                     validateFileLevelRules(fieldDefinitionsInput.getFileLevelRulesParser(), fileLevelValidationRules, fieldName, inputRecordWithValidHeader.get(fieldName));
                  }
                  // Method to validate all fields with respect to library file
                  validateField(fieldDefinitionsMap.get(fieldName), inputRecordWithValidHeader.get(fieldName), rowIndex, fileValidationLog,
                        renamedColumnAliasMapping.get(fieldName));
               }
            }
         } else {
            fileValidationLog.setFieldLevelValidationStatus(ValidationStatusTypes.SUCCESS);
         }
         if (rowIndex != 0) {
            if (fieldDefinitionsInput.getFieldSetsInput() != null) {
               validateFieldSet(fieldSetDefinitionsList, inputRecordWithValidHeader, rowIndex, fileValidationLog, fieldDefinitionsMap);
            }
            if (uniquenessValidator != null) {
               uniqueSetLevelLogReport = uniquenessValidator.validateUniqueness(inputRecordWithValidHeader, uniqueSetLevelLogReport, rowIndex,
                     fieldDefinitionsInput.getSdeidSetsParser());
            }

            // Validate Duplicate Records
            if (fieldDefinitionsInput.getDuplicateRecordsParser() != null) {
               validateDuplicateRecords(duplicateRecordsList, fieldDefinitionsInput.getDuplicateRecordsParser(), duplicateRecordsColumnNames, duplicateRecordsLogLevel,
                     inputRecordWithValidHeader, rowIndex);
            }

            // Cross file validation only when PKS file present
            if (inputPksRecordList != null && !inputPksRecordList.isEmpty()) {
               validateCrossFileFields(crossFileValidationList, fieldDefinitionsInput.getCrossFileFieldSetsParser(), crossFileFieldLogLevel, inputRecordWithValidHeader,
                     rowIndex, pksCrossFileFieldMap);
            }
            fileValidationLog.setFileLevelValidationStatus(fileLevelValidationStatus);
            fileValidationLog.setDuplicateRecordsValidationStatus(duplicateRecordsValidationStatus);
            fileValidationLog.setFieldSetLevelValidationStatus(fieldSetLevelValidationStatus);
            fileValidationLog.setCrossFileFieldValidationStatus(crossFileFieldValidationStatus);
            if (uniquenessValidator != null) {
               fileValidationLog.setUniquenessValidationStatus(uniquenessValidator.getUniqueValidationStatus());
            } else {
               fileValidationLog.setUniquenessValidationStatus(ValidationStatusTypes.SUCCESS);
            }
         }

      }

      fileValidationLog.setFileLevelLogReport(this.fileLevelLogReport);
      fileValidationLog.setUniqueSetLevelLogReportList(uniqueSetLevelLogReport);
      fileValidationLog.setSdeIdLevelLogReportList(new LinkedList<SdeidLevelLogReport>()); // TODO: remove once front-end is updated, here for backwards compat
      fileValidationLog.setDuplicateRecordsLevelLogReportList(duplicateRecordsLevelLogReportList);
      // This list will have values only for cross validation cases
      fileValidationLog.setCrossFileLevelLogReportList(this.crossFileLevelLogReportList);
      return fileValidationLog;
   }

   private void validateEmptyColumns(List<CSVRecord> inputRecordList, CSVRecord inputRecord, EmptyColumnsParser emptyColumnsParser) {
      Map<String, Boolean> emptyColumnMap = new HashMap<>();
      for (String columnHeader : inputRecord) {
         if (StringUtils.isBlank(columnHeader)) {
            addFileLevelLog(FileLevelLogTypes.EMPTYCOLUMNS,
                  new FileLevelLog(columnHeader, emptyColumnsParser.getLogLevel(), FileValidationMessages.EQUIP_FV_ERR34.getErrMessageCode()));
         }
         emptyColumnMap.put(columnHeader, true); // default the values for the next loop
      }
      // If the column has a value in any row, flag it in the map
      for (int idx = 1; idx < inputRecordList.size(); idx++) {
         for (String columnHeader : inputRecord) {
            if (!StringUtils.isBlank(inputRecordList.get(idx).get(columnHeader))) {
               emptyColumnMap.put(columnHeader, false);
            }
         }
      }
      // If a column is still false, we didn't find a value, so the whole column is empty
      for (String columnHeader : emptyColumnMap.keySet()) {
         if (emptyColumnMap.get(columnHeader)) {
            addFileLevelLog(FileLevelLogTypes.EMPTYCOLUMNS,
                  new FileLevelLog(columnHeader, emptyColumnsParser.getLogLevel(), FileValidationMessages.EQUIP_FV_ERR36.getErrMessageCode()));
         }
      }
   }

   /**
    * Validate Individual fields in the input file
    * 
    * @param fieldInput
    * @param inputRecord
    * @param rowIndex
    * @param fileValidationLog
    */
   private void validateField(FieldParser fieldInput, String inputRecord, int rowIndex, FileValidationLog fileValidationLog, String columnAliasName) {
      // Method to validate input field
      FieldValidator fieldValidator = new FieldValidator(fieldInput, inputRecord, rowIndex, columnAliasName);
      fieldValidator.validateInput();

      // check for any error in the field validator and add them to the error Map
      if (!fieldValidator.getFieldLevelMsgList().isEmpty()) {
         // verify if there any other validation failed in the same row and add this error to the map
         if (fileValidationLog.getFieldLevelLogReportList() != null) {
            fieldValidator.getFieldLevelMsgList().addAll(fileValidationLog.getFieldLevelLogReportList());
         }
         fileValidationLog.setFieldLevelLogReportList(fieldValidator.getFieldLevelMsgList());
      }
      fileValidationLog.setFieldLevelValidationStatus(
            ValidationStatusTypes.mostSevere(fileValidationLog.getFieldLevelValidationStatus(), fieldValidator.getFieldValidationStatus()));
   }

   /**
    * Field set Validation for the dependent fields
    * 
    * @param fieldSetDefinitionsList
    * @param inputRecord
    * @param rowIndex
    * @param fileValidationLog
    */
   private void validateFieldSet(List<FieldSetsParser> fieldSetDefinitionsList, Map<String, String> inputRecord, int rowIndex, FileValidationLog fileValidationLog,
         Map<String, FieldParser> fieldDefinitionsMap) {
      // Field data validation for the data dependent on one other data or more
      for (FieldSetsParser fieldSetsInput : fieldSetDefinitionsList) {
         // TODO considering there will be only one rule for now. Shall change after confirmation to single rule.
         switch (fieldSetsInput.getRulesInput().getCondition().get(0)) {
         case "NOTNULL":
            validateRequiredFieldSet(fieldSetsInput, inputRecord, rowIndex);
            break;
         case "DEPENDENTCONSTRAINT":
            validateDependentConstraintsFieldSet(fieldSetsInput, inputRecord, rowIndex);
            break;
         default:
            for (String condition : fieldSetsInput.getRulesInput().getCondition()) {
               validateConditions(condition, inputRecord, fieldDefinitionsMap, rowIndex, fieldSetsInput.getLogLevel());
            }
            break;
         }
         if (!this.fieldSetLevelLogReportList.isEmpty()) {
            fileValidationLog.setFieldSetLevelLogReportList(this.fieldSetLevelLogReportList);
         }
      }
   }

   /**
    * Validate Duplicate Records column that should be unique (not duplicated) across each row
    * 
    * @param duplicateRecordsList
    * @param duplicateRecordsParser
    * @param duplicateRecordsColumnNames
    * @param duplicateRecordsLogLevel
    * @param inputRecord
    * @param rowIndex
    */
   private void validateDuplicateRecords(List<String> duplicateRecordsList, DuplicateRecordsParser duplicateRecordsParser, String duplicateRecordsColumnNames,
         ValidationStatusTypes duplicateRecordsLogLevel, Map<String, String> inputRecord, int rowIndex) {
      StringBuilder duplicateRecordsValue = new StringBuilder("");
      for (String columnName : duplicateRecordsParser.getColumnName()) {
         duplicateRecordsValue.append(inputRecord.get(columnName) + "|");
      }

      // Validation to find duplicate entries
      if (duplicateRecordsList.contains(duplicateRecordsValue.toString())) {
         duplicateRecordsValidationStatus = ValidationStatusTypes.mostSevere(duplicateRecordsValidationStatus, duplicateRecordsLogLevel);
         this.duplicateRecordsLevelLogReportList.add(new SdeidLevelLogReport(rowIndex, duplicateRecordsColumnNames, duplicateRecordsValue.toString(),
               FileValidationMessages.EQUIP_FV_ERR33.getErrMessageCode(), duplicateRecordsLogLevel));
      } else {
         duplicateRecordsList.add(duplicateRecordsValue.toString());
      }
   }

   /**
    * Validate the cross file fields mentioned in the parent file. This method will be called only for cross file validation
    * 
    * @param crossFileFieldList
    * @param crossFileFieldSetsParser
    * @param crossFileFieldSetsLogLevel
    * @param inputRecord
    * @param rowIndex
    * @param pksCrossFileFieldMap
    */
   private void validateCrossFileFields(List<String> crossFileFieldList, CrossFileFieldSetsParser crossFileFieldSetsParser, ValidationStatusTypes crossFileFieldSetsLogLevel,
         Map<String, String> inputRecord, int rowIndex, Map<String, String> pksCrossFileFieldMap) {
      String pksCrossFileFieldValue = null;
      StringBuilder crossFileFieldValueInParent = new StringBuilder("");
      StringBuilder crossFileFieldHeaderInParent = new StringBuilder("");
      for (String columnName : crossFileFieldSetsParser.getColumnName()) {
         crossFileFieldHeaderInParent.append(columnName + "|");
         crossFileFieldValueInParent.append(inputRecord.get(columnName) + "|");
      }
      pksCrossFileFieldValue = pksCrossFileFieldMap.get(crossFileFieldHeaderInParent.toString());
      if (!crossFileFieldValueInParent.toString().equalsIgnoreCase(pksCrossFileFieldValue)) {
         // Cross file Validation to find duplicate entries
         if (crossFileFieldList.contains(crossFileFieldValueInParent.toString())) {
            crossFileFieldValidationStatus = ValidationStatusTypes.mostSevere(crossFileFieldValidationStatus, crossFileFieldSetsLogLevel);
            this.crossFileLevelLogReportList.add(new CrossFileLevelLogReport(rowIndex, crossFileFieldHeaderInParent.toString(), crossFileFieldValueInParent.toString(),
                  FileValidationMessages.EQUIP_FV_ERR30.getErrMessageCode(), crossFileFieldSetsLogLevel, pksCrossFileFieldValue));
         } else
            crossFileFieldList.add(crossFileFieldValueInParent.toString());
      }

   }

   /**
    * Validate the Not Null field sets
    * 
    * @param fieldSetsParser
    * @param inputRecord
    * @param rowIndex
    */
   private void validateRequiredFieldSet(FieldSetsParser fieldSetsParser, Map<String, String> inputRecord, int rowIndex) {
      StringBuilder colName = new StringBuilder("");
      StringBuilder colVal = new StringBuilder("");
      int errorCount = 0;
      for (int i = 0; i < fieldSetsParser.getColumnsInput().getColumnName().size(); i++) {
         String columnName = fieldSetsParser.getColumnsInput().getColumnName().get(i);
         // Check if primary field is empty. If primary field is empty then the dependent field(s) can also be empty.
         if ((inputRecord.get(columnName) == null) || (inputRecord.get(columnName).trim().length() == 0)) {
            if (i == 0) {
               // If primary column is empty, we need not check the value of the other columns.
               return;
            }
            errorCount++;
            colVal.append(inputRecord.get(columnName) + "|");
         }
         colName.append(columnName + "|");
      }

      if (errorCount != 0 && errorCount != fieldSetsParser.getColumnsInput().getColumnName().size()) {
         fieldSetLevelValidationStatus = ValidationStatusTypes.mostSevere(fieldSetLevelValidationStatus, fieldSetsParser.getLogLevel());
         this.fieldSetLevelLogReportList.add(new FieldSetLevelLogReport(rowIndex, colName.toString(), colVal.toString(),
               FileValidationMessages.EQUIP_FV_ERR05.getErrMessageCode(), fieldSetsParser.getLogLevel()));
      }
   }

   /**
    * Validate fieldsets with condition "DEPENDENTCONSTRAINT" . If there is a null value in the first column, the second column should have value.
    * 
    * @param fieldSetsParser
    * @param inputRecord
    * @param rowIndex
    */
   private void validateDependentConstraintsFieldSet(FieldSetsParser fieldSetsParser, Map<String, String> inputRecord, int rowIndex) {

      StringBuilder colName = new StringBuilder("");
      StringBuilder colVal = new StringBuilder("");
      int errorCount = 0;
      for (String columnName : fieldSetsParser.getColumnsInput().getColumnName()) {
         // Check if primary field is empty. If primary field is empty then the dependent field(s) should definitely have value
         if ((inputRecord.get(columnName) == null) || (inputRecord.get(columnName).trim().length() == 0)) {
            errorCount++;
            colVal.append(inputRecord.get(columnName) + "|");
         }
         colName.append(columnName + "|");
      }

      if (errorCount == fieldSetsParser.getColumnsInput().getColumnName().size()) {
         fieldSetLevelValidationStatus = ValidationStatusTypes.mostSevere(fieldSetLevelValidationStatus, fieldSetsParser.getLogLevel());
         this.fieldSetLevelLogReportList.add(new FieldSetLevelLogReport(rowIndex, colName.toString(), colVal.toString(),
               FileValidationMessages.EQUIP_FV_ERR28.getErrMessageCode(), fieldSetsParser.getLogLevel()));
      }
   }

   /**
    * Validate input (.csv) header
    * 
    * @param fieldDefinitionsMap
    * @param validInputFieldNameList
    * @param fieldName
    * @param inputHeader
    */
   private void validateInputHeader(Map<String, FieldParser> fieldDefinitionsMap, List<String> validInputFieldNameList, String fieldName,
         Map<String, String> inputHeader) {

      // 1.Check Mandatory columns are available in the Input file
      validateMandatoryColumns(fieldDefinitionsMap, inputHeader, fieldName);

      // 2. Case Sensitivity Check
      validateCaseSensitivity(fieldDefinitionsMap, validInputFieldNameList, fieldName, inputHeader);

   }

   /**
    * Validate Extraneous Column and the Column header with leading and trailing spaces
    * 
    * @param csvRecord
    * @param fieldDefinitionsMap
    * @param extraneousColumnsParser
    * @return
    */
   private List<String> validateExtraneousColumnAndSpaces(CSVRecord csvRecord, Map<String, FieldParser> fieldDefinitionsMap,
         ExtraneousColumnsParser extraneousColumnsParser) {
      // Check for extraneous column
      List<String> inputFieldNameList = new LinkedList<>();
      List<String> validInputFieldNameList = new LinkedList<>();

      for (int i = 0; i < csvRecord.size(); i++) {
         String validColumnHeader = csvRecord.get(i).toUpperCase().trim();
         // Check for leading/trailing spaces in column header
         if (fieldDefinitionsMap.get(validColumnHeader) != null && fieldDefinitionsMap.get(validColumnHeader).getColumnName().isExtraSpacesHeaderCheck()
               && (!csvRecord.get(i).equalsIgnoreCase(csvRecord.get(i).trim()))) {

            addFileLevelLog(FileLevelLogTypes.LEADINGTRAILINGSPACES, new FileLevelLog(csvRecord.get(i),
                  fieldDefinitionsMap.get(validColumnHeader).getColumnName().getExtraSpacesHeaderLogLevel(), FileValidationMessages.EQUIP_FV_ERR16.getErrMessageCode()));

         }
         inputFieldNameList.add(csvRecord.get(i).trim());
      }
      // clone a copy of input field list to get all valid columns after removing
      // extraneous column
      validInputFieldNameList.addAll(inputFieldNameList);
      inputFieldNameList.replaceAll(String::toUpperCase);
      inputFieldNameList.removeAll(fieldDefinitionsMap.keySet());
      if (extraneousColumnsParser != null) {
         for (String extraColumnName : inputFieldNameList) {
            addFileLevelLog(FileLevelLogTypes.EXTRANEOUS,
                  new FileLevelLog(extraColumnName, extraneousColumnsParser.getLogLevel(), FileValidationMessages.EQUIP_FV_ERR09.getErrMessageCode()));
         }
      }

      // below list used to check column ordering
      for (String input : inputFieldNameList) {
         // If the extraneous column is in lower case
         validInputFieldNameList.removeIf((String column) -> column.equalsIgnoreCase(input));
      }
      return validInputFieldNameList;
   }

   /**
    * Validate the Column header with the case sensitivity rule in the library file
    * 
    * @param fieldDefinitionsMap
    * @param validInputFieldNameList
    * @param fieldName
    * @param inputHeader
    */
   private void validateCaseSensitivity(Map<String, FieldParser> fieldDefinitionsMap, List<String> validInputFieldNameList, String fieldName,
         Map<String, String> inputHeader) {
      if (fieldDefinitionsMap.get(fieldName).getColumnName().isCaseSensitive()
            && (inputHeader.containsValue(fieldName) && !validInputFieldNameList.contains(fieldName))) {
         Set<String> invalidColumnName = getKeysByValue(inputHeader, fieldName);
         addFileLevelLog(FileLevelLogTypes.CASEMISMATCH, new FileLevelLog(invalidColumnName.toString(),
               fieldDefinitionsMap.get(fieldName).getColumnName().getCaseSensitivityLogLevel(), FileValidationMessages.EQUIP_FV_ERR15.getErrMessageCode()));

      }

   }

   /**
    * Validate mandatory column available in the input file
    * 
    * @param fieldDefinitionsMap
    * @param inputHeader
    * @param fieldName
    */
   private void validateMandatoryColumns(Map<String, FieldParser> fieldDefinitionsMap, Map<String, String> inputHeader, String fieldName) {
      if (fieldDefinitionsMap.get(fieldName).getRequired().isValue() && (!inputHeader.containsValue(fieldName))) {
         addFileLevelLog(FileLevelLogTypes.MISSING,
               new FileLevelLog(fieldName, fieldDefinitionsMap.get(fieldName).getRequired().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR06.getErrMessageCode()));

      }
   }

   /**
    * Get the Key by value from the map
    * 
    * @param map
    * @param value
    * @return
    */
   public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
      return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey).collect(Collectors.toSet());
   }

   private String getKeybyValueFromMap(Map<String, List<String>> columnAliasMap, String inputColumnName) {
      String columnName = null;
      for (List<String> columnAliasNames : columnAliasMap.values()) {
         if (columnAliasNames.contains(inputColumnName)) {
            columnName = getKeysByValue(columnAliasMap, columnAliasNames).iterator().next();
         }
      }
      return columnName;
   }

   /**
    * Validate Column Order in the csv with respect to library file
    * 
    * @param inputColumnNameList
    * @param fieldDefinitionsMap
    */
   public void validateColumnOrder(List<String> inputColumnNameList, Map<String, FieldParser> fieldDefinitionsMap) {
      List<String> validInputColumnNameList = new LinkedList<>();
      validInputColumnNameList.addAll(inputColumnNameList);
      validInputColumnNameList.replaceAll(String::toUpperCase);
      for (String column : validInputColumnNameList) {
         int index = validInputColumnNameList.indexOf(column);
         String previousColumn = null;
         String nextColumn = null;
         // Index is 0 for first column and there is no previous column
         if (index != 0) {
            previousColumn = validInputColumnNameList.get(index - 1);
         }
         // For last column there is no nextColumn value
         if (index + 1 < validInputColumnNameList.size()) {
            nextColumn = validInputColumnNameList.get(index + 1);
         }

         // Column Ordering Wrong.
         if (!checkColumnOrder(previousColumn, column, nextColumn, fieldDefinitionsMap)) {
            addFileLevelLog(FileLevelLogTypes.UNORDERED,
                  new FileLevelLog(column, fieldDefinitionsMap.get(column).getIndex().getLogLevel(), FileValidationMessages.EQUIP_FV_ERR10.getErrMessageCode()));
            break;

         }
      }
   }

   /**
    * Check column order based on previous and next column's index (taken from the library)
    * 
    * @param previousColumn
    * @param currentColumn
    * @param nextColumn
    * @param fieldDefinitionsMap
    * @return
    */
   private boolean checkColumnOrder(String previousColumn, String currentColumn, String nextColumn, Map<String, FieldParser> fieldDefinitionsMap) {

      // Validation for first and last column
      if ((previousColumn == null && (fieldDefinitionsMap.get(currentColumn).getIndex().getValue()) > fieldDefinitionsMap.get(nextColumn).getIndex().getValue())
            || ((nextColumn == null) && (fieldDefinitionsMap.get(previousColumn).getIndex().getValue()) > fieldDefinitionsMap.get(currentColumn).getIndex().getValue())) {
         return false;
      }
      // Validation for the remaining columns
      if ((previousColumn != null && nextColumn != null)
            && (((fieldDefinitionsMap.get(previousColumn).getIndex().getValue()) > fieldDefinitionsMap.get(currentColumn).getIndex().getValue())
                  || fieldDefinitionsMap.get(currentColumn).getIndex().getValue() > fieldDefinitionsMap.get(nextColumn).getIndex().getValue())) {
         return false;
      }

      return true;
   }

   /**
    * Get Duplicate Records Column Names from the Library file
    * 
    * @param duplicateRecords
    * @return
    */
   private String getDuplicateRecordsColumnNames(DuplicateRecordsParser duplicateRecords) {
      StringBuilder duplicateRecordsColumnNames = new StringBuilder("");
      for (String columnName : duplicateRecords.getColumnName()) {
         duplicateRecordsColumnNames.append(columnName + "|");
      }
      return duplicateRecordsColumnNames.toString();
   }

   private Map<String, String> getpksCrossFileFieldMap(CrossFileFieldSetsParser crossFileFieldSets, List<CSVRecord> inputPksRecordList) {
      // update the Input csv Header with upper case and trim the spaces
      Map<String, String> pksInputRecordWithValidHeader = inputPksRecordList.get(1).toMap().keySet().stream()
            .collect(Collectors.toMap(key -> key.trim().toUpperCase(), key -> inputPksRecordList.get(1).get(key)));
      Map<String, String> pksCrossFileFieldMap = new LinkedHashMap<>();
      StringBuilder crossFileFieldColumnNames = new StringBuilder("");
      StringBuilder crossFileFieldColumnValues = new StringBuilder("");
      for (String columnName : crossFileFieldSets.getColumnName()) {
         crossFileFieldColumnNames.append(columnName + "|");
         crossFileFieldColumnValues.append(pksInputRecordWithValidHeader.get(columnName) + "|");// It is assumed that PKS file will always have one record in PKS File
      }
      pksCrossFileFieldMap.put(crossFileFieldColumnNames.toString(), crossFileFieldColumnValues.toString());

      return pksCrossFileFieldMap;
   }

   /**
    * Add the file level logs to the various lists based on the log type
    * 
    * @param type
    * @param fileLevelLog
    */
   public void addFileLevelLog(FileLevelLogTypes type, FileLevelLog fileLevelLog) {

      fileLevelValidationStatus = ValidationStatusTypes.mostSevere(fileLevelValidationStatus, fileLevelLog.getLogLevel());
      switch (type) {
      case EXTRANEOUS:
         fileLevelLogReport.getExtraneousFieldList().add(fileLevelLog);
         break;

      case UNORDERED:
         fileLevelLogReport.getUnorderedFieldList().add(fileLevelLog);
         break;

      case MISSING:
         fileLevelLogReport.getMissingMandatoryFieldList().add(fileLevelLog);
         break;

      case CASEMISMATCH:
         fileLevelLogReport.getCaseMismatchFieldList().add(fileLevelLog);
         break;

      case LEADINGTRAILINGSPACES:
         fileLevelLogReport.getLeadingTrailingSpacesColumnsList().add(fileLevelLog);
         break;

      case NONUNIQUEVALUES:
         fileLevelLogReport.getNonUniqueColumnList().add(fileLevelLog);
         break;

      case UNIQUEVALUES:
         fileLevelLogReport.getUniqueColumnList().add(fileLevelLog);
         break;

      case MANDATORYIFAVAILABLE:
         fileLevelLogReport.getMandatoryIfAvailableList().add(fileLevelLog);
         break;

      case EMPTYCOLUMNS:
         fileLevelLogReport.getEmptyColumnsList().add(fileLevelLog);
         break;

      default:
         break;

      }

   }

   /**
    * Add log if the empty line(s) found in the input csv file and update the validation status
    * 
    * @param emptyLine
    * @param logLevel
    */
   public void addEmptyLinesList(EmptyLinesLog emptyLine, ValidationStatusTypes logLevel) {
      fileLevelValidationStatus = ValidationStatusTypes.mostSevere(fileLevelValidationStatus, logLevel);
      fileLevelLogReport.getEmptyLinesList().add(emptyLine);
   }

   /**
    * Create a map from the specification for the fileLevelValidationRules
    * 
    * @param fileLevelRulesParser
    * 
    * @return
    */
   public Map<String, Map<String, List<String>>> createFileLevelRulesMap(List<FileLevelRulesParser> fileLevelRulesParser) {

      Map<String, Map<String, List<String>>> fileLevelRules = new HashMap<>();
      for (FileLevelRulesParser fileLevelRuleParser : fileLevelRulesParser) {
         fileLevelRules.put(fileLevelRuleParser.getRuleType(), new HashMap<String, List<String>>());
         ColumnsParser columnsInput = fileLevelRuleParser.getColumnsInput();
         for (String column : columnsInput.getColumnName()) {
            fileLevelRules.get(fileLevelRuleParser.getRuleType()).put(column, new ArrayList<>());
         }

      }

      return fileLevelRules;

   }

   /**
    * Validate rules defined under the FileLevelValidation Tag.
    * 
    * @param fileLevelRulesParser
    * @param fileLevelRules
    * @param fieldName
    * @param fieldValue
    * @return
    */
   public Map<String, Map<String, List<String>>> validateFileLevelRules(List<FileLevelRulesParser> fileLevelRulesParser,
         Map<String, Map<String, List<String>>> fileLevelRules, String fieldName, String fieldValue) {

      for (FileLevelRulesParser fileLevelParser : fileLevelRulesParser) {

         // Check if this column has a file level rule associated.
         if (fileLevelRules.get(fileLevelParser.getRuleType()).get(fieldName) != null) {
            switch (fileLevelParser.getRuleType()) {
            case "UNIQUE":
               fileLevelRules.put(fileLevelParser.getRuleType(),
                     validateUniqueValues(fileLevelRules.get(fileLevelParser.getRuleType()), fieldName, fieldValue, fileLevelParser.getLogLevel()));
               break;

            case "NONUNIQUE":

               fileLevelRules.put(fileLevelParser.getRuleType(),
                     validateNonUniqueValues(fileLevelRules.get(fileLevelParser.getRuleType()), fieldName, fieldValue, fileLevelParser.getLogLevel()));
               break;

            case "MANDATORYIFAVAILABLE":

               fileLevelRules.put(fileLevelParser.getRuleType(),
                     validateMandatoryIfAvailable(fileLevelRules.get(fileLevelParser.getRuleType()), fieldName, fieldValue, fileLevelParser.getLogLevel()));

               break;
            default:
               break;
            }

         }

      }
      return fileLevelRules;
   }

   /**
    * Validate to verify if the column in non unique ( same for all rows) Example, DATASTATUS
    * 
    * @param nonUniqueMap
    * @param fieldName
    * @param fieldValue
    * @param logLevel
    * @return
    */

   public Map<String, List<String>> validateNonUniqueValues(Map<String, List<String>> nonUniqueMap, String fieldName, String fieldValue, ValidationStatusTypes logLevel) {
      if (!nonUniqueMap.get(fieldName).isEmpty() && !nonUniqueMap.get(fieldName).contains(fieldValue)) {
         addFileLevelLog(FileLevelLogTypes.NONUNIQUEVALUES,
               new FileLevelLog(fieldName, logLevel, FileValidationMessages.EQUIP_FV_ERR22.getErrMessageCode() + fieldValue));

      }
      nonUniqueMap.get(fieldName).add(fieldValue);
      return nonUniqueMap;
   }

   /**
    * Validate all the rows have a unique value for that field. Example, PKUSMID
    * 
    * @param uniqueValueMap
    * @param fieldName
    * @param fieldValue
    * @param logLevel
    * @return
    */

   public Map<String, List<String>> validateUniqueValues(Map<String, List<String>> uniqueValueMap, String fieldName, String fieldValue, ValidationStatusTypes logLevel) {

      if (uniqueValueMap.get(fieldName).contains(fieldValue)) {
         addFileLevelLog(FileLevelLogTypes.UNIQUEVALUES, new FileLevelLog(fieldName, logLevel, FileValidationMessages.EQUIP_FV_ERR24.getErrMessageCode() + fieldValue));
      } else
         uniqueValueMap.get(fieldName).add(fieldValue);

      return uniqueValueMap;
   }

   /**
    * Validate the mandatory if available condition . The field should have values for all rows even if one row is having a non null value. Example, UDSDEID
    * 
    * @param mandatoryMap
    * @param fieldName
    * @param fieldValue
    * @param logLevel
    * @return
    */

   public Map<String, List<String>> validateMandatoryIfAvailable(Map<String, List<String>> mandatoryMap, String fieldName, String fieldValue, ValidationStatusTypes logLevel) {

      if (mandatoryMap.get(fieldName).isEmpty()) {
         // For the first element the list is empty.
         if (fieldValue == null || fieldValue.isEmpty()) {
            mandatoryMap.get(fieldName).add(null);
         } else
            mandatoryMap.get(fieldName).add(fieldValue);
      } else {
         // If the value is not null for one row, it is mandatory for all rows.
         if (((fieldValue == null || fieldValue.isEmpty()) && mandatoryMap.get(fieldName).get(0) != null)
               || (fieldValue != null && mandatoryMap.get(fieldName).get(0) == null))
            addFileLevelLog(FileLevelLogTypes.MANDATORYIFAVAILABLE,
                  new FileLevelLog(fieldName, logLevel, FileValidationMessages.EQUIP_FV_ERR25.getErrMessageCode() + fieldValue));
      }
      return mandatoryMap;
   }

   /**
    * Validate the <Condition> given in the file specification
    * 
    * @param condition
    * @param inpuRecord
    * @param fieldDefinitionMap
    * @param rowIndex
    * @param logLevel
    */
   public void validateConditions(String condition, Map<String, String> inpuRecord, Map<String, FieldParser> fieldDefinitionMap, int rowIndex, ValidationStatusTypes logLevel) {
      String conditionExpression = condition;
      ScriptEngineManager mgr = new ScriptEngineManager();
      ScriptEngine engine = mgr.getEngineByName("JavaScript");
      long errorCount = 0;
      boolean columnMissing = false;
      try {
         log.info("Condition from file spec:: '{}' ", condition);
         // get the value within ()
         Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(condition);
         while (m.find()) {
            log.info("Column Name extracted from <Condition>: '{}'", m.group(1));
            String columnName = "{" + m.group(1) + "}";
            String columnValue = inpuRecord.get(m.group(1).toString());
            // If column doesn't exist, go to next loop
            if (columnValue == null) {
               columnMissing = true;
               break;
            }
            log.info("Column Value from input csv for the column '{}'  is :: '{}'", columnName, columnValue);
            FieldParser field = fieldDefinitionMap.get(m.group(1));
            try {
               String dataType = field.getDataType().getSubType();

               if (dataType.equalsIgnoreCase("DATE") || dataType.equalsIgnoreCase("TIME")) {
                  if (columnValue.length() > 0) {
                     columnValue = getDateTimeInMillSec(columnValue, field.getDataType().getValue()).toString();
                  } else {
                     this.fieldSetLevelLogReportList.add(
                           new FieldSetLevelLogReport(rowIndex, conditionExpression, condition, FileValidationMessages.EQUIP_FV_ERR32.getErrMessageCode(), logLevel));
                     errorCount++;
                     break;
                  }
               }
               condition = condition.replace(columnName, columnValue);
            } catch (Exception ex) {
               String message = String.format("Unable to get the field data type for the field '%s' from the file specification. Exception occured while evalating condition. Exception is: '%s'", m.group(1), ex.getMessage());
               log.error(message);
               throw new FileValidationRuntimeException(message);
            }
         }
         log.info("Condition  after value replacement from input csv :: '{}'", condition);
         if (!columnMissing && !Boolean.parseBoolean(engine.eval(condition).toString())) {
            this.fieldSetLevelLogReportList
                  .add(new FieldSetLevelLogReport(rowIndex, conditionExpression, condition, FileValidationMessages.EQUIP_FV_ERR32.getErrMessageCode(), logLevel));
            errorCount++;
         }

      } catch (ScriptException e) {
         log.error("Evaluation failed for the condition'{}' for the row index '{}'. Script exception occured while evaluating condition. Exception is: '{}'", condition,
               rowIndex, e.getMessage());
         this.fieldSetLevelLogReportList
               .add(new FieldSetLevelLogReport(rowIndex, conditionExpression, condition, FileValidationMessages.EQUIP_FV_ERR32.getErrMessageCode(), logLevel));
         errorCount++;
         // throw new FileValidationRuntimeException("Script exception occured while evalating condition");
      } catch (Exception ex) {
         log.error("Evaluation failed for the condition'{}' for the row index '{}'. Exception occured while evaluating condition. Exception is: '{}'", condition,
               rowIndex, ex.getMessage());
         this.fieldSetLevelLogReportList
               .add(new FieldSetLevelLogReport(rowIndex, conditionExpression, condition, FileValidationMessages.EQUIP_FV_ERR32.getErrMessageCode(), logLevel));
         errorCount++;
         // throw new FileValidationRuntimeException("Exception occured while evaluating condition");
      }
      if (errorCount != 0) {
         fieldSetLevelValidationStatus = ValidationStatusTypes.mostSevere(fieldSetLevelValidationStatus, logLevel);
      }
   }

   /**
    * Convert date or time input to milliseconds
    * 
    * @param date_time
    * @param format
    * @return
    */
   public Long getDateTimeInMillSec(String date_time, String format) {
      long milliseconds = 0L;
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
      try {
         Date d = simpleDateFormat.parse(date_time);
         milliseconds = d.getTime();
      } catch (ParseException e) {
         String message = String.format("Parsing Exception Occured while parsing the column value '%s' with the format '%s'. Exception is: '%s'", date_time, format, e.getMessage());
         log.error(message);
         throw new FileValidationRuntimeException(message);
      }
      return milliseconds;
   }

   public List<String> getHeadersFromInputRecord(CSVRecord csvRecord) {
      List<String> headerList = new LinkedList<>();
      for (int i = 0; i < csvRecord.size(); i++) {
         headerList.add(csvRecord.get(i));
      }
      return headerList;
   }
}
