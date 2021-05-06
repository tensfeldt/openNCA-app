package com.pfizer.equip.services.business.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.services.business.validation.exceptions.FileValidationRuntimeException;
import com.pfizer.equip.services.input.validation.FileValidationMessages;
import com.pfizer.equip.services.input.validation.UniqueSetLevelLogReport;
import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;
import com.pfizer.equip.services.input.validation.xmlparser.GroupingParser;
import com.pfizer.equip.services.input.validation.xmlparser.SDEIDSetsParser;
import com.pfizer.equip.services.input.validation.xmlparser.UniqueSetsParser;

public class UniquenessValidator {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private List<UniqueSet> columnsUniqueAtFileLevel = new ArrayList<>();
   private List<UniqueSetSDEID> columnsUniqueAtSDEIDLevel = new ArrayList<>();
   private List<UniqueSetScoped> uniqueColumLevel = new ArrayList<>();
   
   
   private List<UniqueSet> columnsIdenticalAtFileLevel = new ArrayList<>();
   private List<UniqueSetSDEID> columnsIdenticalAtSDEIDLevel = new ArrayList<>();
   private List<UniqueSetScoped> identicalColumLevel = new ArrayList<>();
   
   private ValidationStatusTypes uniqueValidationStatus = ValidationStatusTypes.INFO;

   public List<UniqueSet> getColumnsUniqueAtFileLevel() {
      // Key is column Name and value has the list of values for that column present in the file
      return columnsUniqueAtFileLevel;
   }

   public void setColumnsUniqueAtFileLevel(List<UniqueSet> columnsUniqueAtFileLevel) {
      this.columnsUniqueAtFileLevel = columnsUniqueAtFileLevel;
   }

   public List<UniqueSetSDEID> getColumnsUniqueAtSDEIDLevel() {
      // Key is column which is unique at SDEID level and Value has the list of values prefixed by SDEID String
      return columnsUniqueAtSDEIDLevel;
   }

   public void setColumnsUniqueAtSDEIDLevel(List<UniqueSetSDEID> columnsUniqueAtSDEIDLevel) {
      this.columnsUniqueAtSDEIDLevel = columnsUniqueAtSDEIDLevel;
   }

   public List<UniqueSetScoped> getUniqueColumLevel() {
      return uniqueColumLevel;
   }

   public void setUniqueColumLevel(List<UniqueSetScoped> uniqueRulesMap) {
      this.uniqueColumLevel = uniqueRulesMap;
   }

   public ValidationStatusTypes getUniqueValidationStatus() {
      return uniqueValidationStatus;
   }

   public void setUniqueValidationStatus(ValidationStatusTypes uniqueValidationStatus) {
      this.uniqueValidationStatus = uniqueValidationStatus;
   }

   public List<UniqueSet> getColumnsIdenticalAtFileLevel() {
      return columnsIdenticalAtFileLevel;
   }

   public void setColumnsIdenticalAtFileLevel(List<UniqueSet> columnsIdenticalAtFileLevel) {
      this.columnsIdenticalAtFileLevel = columnsIdenticalAtFileLevel;
   }

   public List<UniqueSetSDEID> getColumnsIdenticalAtSDEIDLevel() {
      return columnsIdenticalAtSDEIDLevel;
   }

   public void setColumnsIdenticalAtSDEIDLevel(List<UniqueSetSDEID> columnsIdenticalAtSDEIDLevel) {
      this.columnsIdenticalAtSDEIDLevel = columnsIdenticalAtSDEIDLevel;
   }

   public List<UniqueSetScoped> getIdenticalColumLevel() {
      return identicalColumLevel;
   }

   public void setIdenticalColumLevel(List<UniqueSetScoped> identicalColumLevel) {
      this.identicalColumLevel = identicalColumLevel;
   }

   public List<UniqueSetLevelLogReport> validateUniqueness(Map<String, String> inputRecord, List<UniqueSetLevelLogReport> uniqueSetLevelLogReport, int rowIndex,
         SDEIDSetsParser sdeidParser) {
      uniqueSetLevelLogReport = validateFileLevelUniquess(inputRecord, uniqueSetLevelLogReport, rowIndex);
      if (!columnsUniqueAtSDEIDLevel.isEmpty()) {
         if (sdeidParser == null) {
            throw new FileValidationRuntimeException("SDEID-level rule specified but no SDEID definition found in specification");
         }
         uniqueSetLevelLogReport = validateSDEIDLevelUniquess(inputRecord, uniqueSetLevelLogReport, rowIndex, sdeidParser);
      }
      uniqueSetLevelLogReport = validateColumnLevelUniqueness(inputRecord, uniqueSetLevelLogReport, rowIndex);
      
      uniqueSetLevelLogReport = validateFileLevelIdenticalness(inputRecord, uniqueSetLevelLogReport, rowIndex);
      if (!columnsIdenticalAtSDEIDLevel.isEmpty()) {
         if (sdeidParser == null) {
            throw new FileValidationRuntimeException("SDEID-level rule specified but no SDEID definition found in specification");
         }
         uniqueSetLevelLogReport = validateSDEIDLevelIdenticalness(inputRecord, uniqueSetLevelLogReport, rowIndex, sdeidParser);
      }
      uniqueSetLevelLogReport = validateColumnLevelIdenticalness(inputRecord, uniqueSetLevelLogReport, rowIndex);
      
      return uniqueSetLevelLogReport;
   }

   private List<UniqueSetLevelLogReport> validateFileLevelIdenticalness(Map<String, String> inputRecord,
         List<UniqueSetLevelLogReport> uniqueSetLevelLogReport, int rowIndex) {
      for (UniqueSet rule : columnsIdenticalAtFileLevel) {
         List<String> columnValues = new ArrayList<String>();
         for (String column : rule.getUniqueColumns()) {
            columnValues.add(inputRecord.get(column));
         }
         
         // First record, initialize previous value
         if (rule.getPreviousValue() == null) {
            rule.setPreviousValue(columnValues);
         }
         
         // Subsequent runs, check if the next value is identical to the previous
         if (!rule.getPreviousValue().equals(columnValues)) {
            uniqueSetLevelLogReport.add(new UniqueSetLevelLogReport(rowIndex, collectionToLogString(rule.getUniqueColumns()), collectionToLogString(columnValues),
                  FileValidationMessages.EQUIP_FV_ERR35.getErrMessageCode(), rule.getLogLevel(), "FILE"));
            uniqueValidationStatus = ValidationStatusTypes.mostSevere(uniqueValidationStatus, rule.getLogLevel());
            // If not, set the previous value to the newest value to avoid spamming the log
            rule.setPreviousValue(columnValues);
         }
      }
      return uniqueSetLevelLogReport;
   }

   private List<UniqueSetLevelLogReport> validateSDEIDLevelIdenticalness(Map<String, String> inputRecord,
         List<UniqueSetLevelLogReport> uniqueSetLevelLogReport, int rowIndex, SDEIDSetsParser sdeidParser) {
      for (UniqueSetSDEID rule : columnsIdenticalAtSDEIDLevel) {
         List<String> columnValues = new ArrayList<String>();
         List<String> scopeValues = new ArrayList<String>();

         // Get all the values for the columns we're checking uniqueness on
         for (String column : rule.getUniqueColumns()) {
            columnValues.add(inputRecord.get(column));
         }

         // Get all the values for the SDEID we're scoping against
         for (String column : sdeidParser.getColumnName()) {
            scopeValues.add(inputRecord.get(column));
         }

         // First record, initialize previous value
         if (rule.getPreviousValues().get(scopeValues) == null) {
            rule.getPreviousValues().put(scopeValues, columnValues);
         }

         // Subsequent runs, check if the next value is identical to the previous
         if (!rule.getPreviousValues().get(scopeValues).equals(columnValues)) {
            uniqueSetLevelLogReport.add(new UniqueSetLevelLogReport(rowIndex, collectionToLogString(rule.getUniqueColumns()), collectionToLogString(columnValues),
                  FileValidationMessages.EQUIP_FV_ERR35.getErrMessageCode(), rule.getLogLevel(), "SDEID"));
            uniqueValidationStatus = ValidationStatusTypes.mostSevere(uniqueValidationStatus, rule.getLogLevel());
            // If not, set the previous value to the newest value to avoid spamming the log
            rule.getPreviousValues().put(scopeValues, columnValues);
         }
      }
      return uniqueSetLevelLogReport;
   }

   private List<UniqueSetLevelLogReport> validateColumnLevelIdenticalness(Map<String, String> inputRecord,
         List<UniqueSetLevelLogReport> uniqueSetLevelLogReport, int rowIndex) {
      for (UniqueSetScoped rule : identicalColumLevel) {
         List<String> columnValues = new ArrayList<String>();
         List<String> scopeValues = new ArrayList<String>();

         // Get all the values for the columns we're checking uniqueness on
         for (String column : rule.getUniqueColumns()) {
            columnValues.add(inputRecord.get(column));
         }

         // Get all the values for the columns we're scoping against
         for (String column : rule.getScopeColumns()) {
            scopeValues.add(inputRecord.get(column));
         }

         // First record, initialize previous value
         if (rule.getPreviousValues().get(scopeValues) == null) {
            rule.getPreviousValues().put(scopeValues, columnValues);
         }

         // Check if these col values are already in the list for this scope and add message if so, otherwise add it to the list for subsequent iterations.
         if (!rule.getPreviousValues().get(scopeValues).equals(columnValues)) {
            uniqueSetLevelLogReport.add(new UniqueSetLevelLogReport(rowIndex, collectionToLogString(rule.getUniqueColumns()), collectionToLogString(columnValues),
                  FileValidationMessages.EQUIP_FV_ERR35.getErrMessageCode(), rule.getLogLevel(), collectionToLogString(rule.getScopeColumns())));
            uniqueValidationStatus = ValidationStatusTypes.mostSevere(uniqueValidationStatus, rule.getLogLevel());
            rule.getPreviousValues().put(scopeValues, columnValues);
         } 
      }
      
      return uniqueSetLevelLogReport;
   }

   /**
    * Validate the uniqueness of a column through the scope of the FILE. `
    * 
    * @param inputRecord
    * @param uniqueSetLevelLogReport
    * @param rowIndex
    * @return
    */
   public List<UniqueSetLevelLogReport> validateFileLevelUniquess(Map<String, String> inputRecord,
         List<UniqueSetLevelLogReport> uniqueSetLevelLogReport, int rowIndex) {
      for (UniqueSet rule : columnsUniqueAtFileLevel) {
         List<String> columnValues = new ArrayList<String>();
         // Get all the values for the columns we're checking uniqueness on
         for (String column : rule.getUniqueColumns()) {
            columnValues.add(inputRecord.get(column));
         }
         // Check if this row is already in the list and add message if so, otherwise add it to the list for subsequent iterations.
         if (rule.getRows().contains(columnValues)) {
            uniqueSetLevelLogReport.add(new UniqueSetLevelLogReport(rowIndex, collectionToLogString(rule.getUniqueColumns()), collectionToLogString(columnValues),
                  FileValidationMessages.EQUIP_FV_ERR31.getErrMessageCode(), rule.getLogLevel(), "FILE"));
            uniqueValidationStatus = ValidationStatusTypes.mostSevere(uniqueValidationStatus, rule.getLogLevel());
         } else {
            rule.getRows().add(columnValues);
         }
      }
      return uniqueSetLevelLogReport;
   }

   /**
    * Validate the uniquness of column within the scope of SDEID fields.
    * 
    * @param inputRecord
    * @param uniqueSetLevelLogReport
    * @param rowIndex
    * @param sdeidParser
    * @return
    */
   public List<UniqueSetLevelLogReport> validateSDEIDLevelUniquess(Map<String, String> inputRecord, List<UniqueSetLevelLogReport> uniqueSetLevelLogReport, int rowIndex,
         SDEIDSetsParser sdeidParser) {
      for (UniqueSetSDEID rule : columnsUniqueAtSDEIDLevel) {
         List<String> columnValues = new ArrayList<String>();
         List<String> scopeValues = new ArrayList<String>();
         Map<List<String>, List<List<String>>> scopedRows = rule.getScopedRows();

         // Get all the values for the columns we're checking uniqueness on
         for (String column : rule.getUniqueColumns()) {
            columnValues.add(inputRecord.get(column));
         }

         // Get all the values for the SDEID we're scoping against
         for (String column : sdeidParser.getColumnName()) {
            scopeValues.add(inputRecord.get(column));
         }

         // Check if these col values are already in the list for this scope and add message if so, otherwise add it to the list for subsequent iterations.
         if (scopedRows.containsKey(scopeValues) && scopedRows.get(scopeValues).contains(columnValues)) {
            uniqueSetLevelLogReport.add(new UniqueSetLevelLogReport(rowIndex, collectionToLogString(rule.getUniqueColumns()), collectionToLogString(columnValues),
                  FileValidationMessages.EQUIP_FV_ERR31.getErrMessageCode(), rule.getLogLevel(), "SDEID"));
            uniqueValidationStatus = ValidationStatusTypes.mostSevere(uniqueValidationStatus, rule.getLogLevel());
         } else {
            if (!scopedRows.containsKey(scopeValues)) {
               rule.getScopedRows().put(scopeValues, new ArrayList<>());
            }
            scopedRows.get(scopeValues).add(columnValues);
         }
      }
      
      return uniqueSetLevelLogReport;
   }

   /**
    * For validating the uniqueness of a column within the scope of another column
    * 
    * @param inputRecord
    * @param uniqueSetLevelLogReport
    * @param rowIndex
    * @return
    */
   public List<UniqueSetLevelLogReport> validateColumnLevelUniqueness(Map<String, String> inputRecord, List<UniqueSetLevelLogReport> uniqueSetLevelLogReport,
         int rowIndex) {
      for (UniqueSetScoped rule : uniqueColumLevel) {
         List<String> columnValues = new ArrayList<String>();
         List<String> scopeValues = new ArrayList<String>();
         Map<List<String>, List<List<String>>> scopedRows = rule.getScopedRows();

         // Get all the values for the columns we're checking uniqueness on
         for (String column : rule.getUniqueColumns()) {
            columnValues.add(inputRecord.get(column));
         }

         // Get all the values for the columns we're scoping against
         for (String column : rule.getScopeColumns()) {
            scopeValues.add(inputRecord.get(column));
         }

         // Check if these col values are already in the list for this scope and add message if so, otherwise add it to the list for subsequent iterations.
         if (scopedRows.containsKey(scopeValues) && scopedRows.get(scopeValues).contains(columnValues)) {
            uniqueSetLevelLogReport.add(new UniqueSetLevelLogReport(rowIndex, collectionToLogString(rule.getUniqueColumns()), collectionToLogString(columnValues),
                  FileValidationMessages.EQUIP_FV_ERR31.getErrMessageCode(), rule.getLogLevel(), collectionToLogString(rule.getScopeColumns())));
            uniqueValidationStatus = ValidationStatusTypes.mostSevere(uniqueValidationStatus, rule.getLogLevel());

         } else {
            if (!scopedRows.containsKey(scopeValues)) {
               rule.getScopedRows().put(scopeValues, new ArrayList<>());
            }
            scopedRows.get(scopeValues).add(columnValues);
         }
      }
      
      return uniqueSetLevelLogReport;
   }

   /**
    * Build UniqunessValidator Object by Taking the values from the Specification
    * 
    * @param uniqueSetsParser
    */
   public void intialiseUniquenessValidator(UniqueSetsParser uniqueSetsParser) {
      log.info("Initializing the Uniqueness Validator");
      if (uniqueSetsParser.getUnique() != null) {
         List<UniqueSet> columnsUniqueAtFileLevel = new ArrayList<>();
         List<UniqueSetSDEID> columnsUniqueAtSDEIDLevel = new ArrayList<>();
         List<UniqueSetScoped> columnsUniqueAtScopeLevel = new ArrayList<>();
         for (GroupingParser uniqueSet : uniqueSetsParser.getUnique()) {
            // List is needed in other spots, but here we convert to a set since we only have one of each column:
            LinkedHashSet<String> uniqueColumns = new LinkedHashSet<>(uniqueSet.getColumnsInput().getColumnName());

            if (uniqueSet.getScope().equals("FILE")) {
               UniqueSet uniqueSetRule = new UniqueSet();
               uniqueSetRule.setUniqueColumns(uniqueColumns);
               uniqueSetRule.setLogLevel(uniqueSet.getLogLevel());
               columnsUniqueAtFileLevel.add(uniqueSetRule);
            } else if (uniqueSet.getScope().equals("SDEID")) {
               UniqueSetSDEID uniqueSetRule = new UniqueSetSDEID();
               uniqueSetRule.setUniqueColumns(uniqueColumns);
               uniqueSetRule.setLogLevel(uniqueSet.getLogLevel());
               columnsUniqueAtSDEIDLevel.add(uniqueSetRule);
            } else {
               UniqueSetScoped uniqueSetRule = new UniqueSetScoped();
               uniqueSetRule.setUniqueColumns(uniqueColumns);
               if (uniqueSet.getScopeColumnsInput() != null) {
                  uniqueSetRule.setScopeColumns(new LinkedHashSet<>(uniqueSet.getScopeColumnsInput().getColumnName()));
               } else {
                  uniqueSetRule.setScopeColumns(new LinkedHashSet<>(Arrays.asList(uniqueSet.getScope())));
               }
               uniqueSetRule.setLogLevel(uniqueSet.getLogLevel());
               columnsUniqueAtScopeLevel.add(uniqueSetRule);
            }

         }
         this.setColumnsUniqueAtFileLevel(columnsUniqueAtFileLevel);
         this.setColumnsUniqueAtSDEIDLevel(columnsUniqueAtSDEIDLevel);
         this.setUniqueColumLevel(columnsUniqueAtScopeLevel);
      }

      if (uniqueSetsParser.getIdentical() != null) {
         List<UniqueSet> columnsIdenticalAtFileLevel = new ArrayList<>();
         List<UniqueSetSDEID> columnsIdenticalAtSDEIDLevel = new ArrayList<>();
         List<UniqueSetScoped> columnsIdenticalAtScopeLevel = new ArrayList<>();
         for (GroupingParser identicalSet : uniqueSetsParser.getIdentical()) {
            // List is needed in other spots, but here we convert to a set since we only
            // have one of each column:
            LinkedHashSet<String> identicalColumns = new LinkedHashSet<>(identicalSet.getColumnsInput().getColumnName());

            if (identicalSet.getScope().equals("FILE")) {
               UniqueSet identicalSetRule = new UniqueSet();
               identicalSetRule.setUniqueColumns(identicalColumns);
               identicalSetRule.setLogLevel(identicalSet.getLogLevel());
               columnsIdenticalAtFileLevel.add(identicalSetRule);
            } else if (identicalSet.getScope().equals("SDEID")) {
               UniqueSetSDEID identicalSetRule = new UniqueSetSDEID();
               identicalSetRule.setUniqueColumns(identicalColumns);
               identicalSetRule.setLogLevel(identicalSet.getLogLevel());
               columnsIdenticalAtSDEIDLevel.add(identicalSetRule);
            } else {
               UniqueSetScoped identicalSetRule = new UniqueSetScoped();
               identicalSetRule.setUniqueColumns(identicalColumns);
               if (identicalSet.getScopeColumnsInput() != null) {
                  identicalSetRule.setScopeColumns(new LinkedHashSet<>(identicalSet.getScopeColumnsInput().getColumnName()));
               } else {
                  identicalSetRule.setScopeColumns(new LinkedHashSet<>(Arrays.asList(identicalSet.getScope())));
               }
               identicalSetRule.setLogLevel(identicalSet.getLogLevel());
               columnsIdenticalAtScopeLevel.add(identicalSetRule);
            }
            this.setColumnsIdenticalAtFileLevel(columnsIdenticalAtFileLevel);
            this.setColumnsIdenticalAtSDEIDLevel(columnsIdenticalAtSDEIDLevel);
            this.setIdenticalColumLevel(columnsIdenticalAtScopeLevel);
         }
      }
   }
   
   private String collectionToLogString(Collection<String> collection) {
      List<String> list = new ArrayList<>(collection);
      return StringUtils.join(list, ", ");
   }
}