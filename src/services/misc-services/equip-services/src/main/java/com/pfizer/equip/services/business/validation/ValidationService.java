package com.pfizer.equip.services.business.validation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pfizer.equip.services.business.librarian.LibrarianService;
import com.pfizer.equip.services.business.validation.exceptions.CSVRuntimeException;
import com.pfizer.equip.services.business.validation.exceptions.FileValidationRuntimeException;
import com.pfizer.equip.services.business.validation.exceptions.InvalidFileSpecificationException;
import com.pfizer.equip.services.input.specification.SpecificationInput;
import com.pfizer.equip.services.input.validation.AbstractLogLevelReport;
import com.pfizer.equip.services.input.validation.CrossFileLevelLogReport;
import com.pfizer.equip.services.input.validation.EmptyLinesLog;
import com.pfizer.equip.services.input.validation.FieldLevelLogReport;
import com.pfizer.equip.services.input.validation.FieldSetLevelLogReport;
import com.pfizer.equip.services.input.validation.FileLevelLog;
import com.pfizer.equip.services.input.validation.FileLevelLogReport;
import com.pfizer.equip.services.input.validation.FileValidationMessages;
import com.pfizer.equip.services.input.validation.GlobalInfoReport;
import com.pfizer.equip.services.input.validation.MappingInformation;
import com.pfizer.equip.services.input.validation.SdeidLevelLogReport;
import com.pfizer.equip.services.input.validation.UniqueSetLevelLogReport;
import com.pfizer.equip.services.input.validation.types.DelimiterInputTypes;
import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;
import com.pfizer.equip.services.input.validation.types.ValidationTypes;
import com.pfizer.equip.services.input.validation.xmlparser.FieldDefinitionsParser;
import com.pfizer.equip.services.input.validation.xmlparser.FieldParser;
import com.pfizer.equip.services.properties.ValidationProperties;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.responses.search.FolderResponse;
import com.pfizer.equip.shared.contentrepository.ContentInfo;

@Service
public class ValidationService {

   @Autowired
   private ValidationProperties properties;

   @Autowired
   private LibrarianService librarianService;

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private void addSpecDetails(Specification spec, String specPath) {
      // Get the field definition parser
      FieldDefinitionsParser fieldDefinitionsParser = getValidationSpecInfo(specPath);
      Map<String, FieldParser> fieldParserMap = createFieldDefinitionMap(fieldDefinitionsParser);
      List<MappingInformation> mappingInformationList = new LinkedList<>();
      spec.setSubjectIdColumnName(fieldDefinitionsParser.getSubjectIdColumnName());
      spec.setPkTermColumnName(fieldDefinitionsParser.getPkTermColumnName());
      for (String fieldName : fieldParserMap.keySet()) {
         // Get MappingInformation
         if (!fieldName.equalsIgnoreCase(fieldParserMap.get(fieldName).getColumnMapping())) {
            MappingInformation mappingInformation = new MappingInformation();
            mappingInformation.setColumnName(fieldName);
            mappingInformation.setColumnMappingName(fieldParserMap.get(fieldName).getColumnMapping());
            mappingInformationList.add(mappingInformation);
         }
      }

      // Mapping information
      spec.setMappingList(mappingInformationList);

      // SDEID Sets
      if (fieldDefinitionsParser.getSdeidSetsParser() != null) {
         spec.setSdeidColumns(fieldDefinitionsParser.getSdeidSetsParser().getColumnName());
      } else {
         spec.setSdeidColumns(new ArrayList<String>());
      }
   }

   public Specification getSpecificationById(String id) throws Exception {
      LibraryArtifactResponse response = librarianService.getArtifactById(id);
      if (!response.getPrimaryType().equals("equipLibrary:specification")) {
         throw new RuntimeException("The specified GUID does not correspond to a specification");
      }

      String path = response.getArtifactPath();
      Specification spec = new Specification();
      spec.setId(id);
      spec.setPath(path);
      spec.setType((String) response.getProperties().get("specificationType"));
      spec.setVersion((String) response.getProperties().get("specificationVersion"));
      addSpecDetails(spec, path);

      return spec;
   }

   public List<Specification> getSpecificationFiles(Optional<String> specificationType, Optional<String> specificationVersion, Optional<String> userSpecificationId)
         throws Exception {
      boolean userSpecSelected = userSpecificationId.isPresent();
      List<Specification> specs = new ArrayList<>();
      if (!userSpecSelected) {
         FolderResponse response = librarianService.getFolderContents(properties.getPath(), false, "jcr:name", false);
         if (response.getRows() != null) {
            for (Map<String, Object> row : response.getRows()) {
               if (row.get("jcr:primaryType").equals("equipLibrary:specification")) {
                  Specification spec = createSpecificationResponse(row);

                  if (specificationType.isPresent() && specificationVersion.isPresent()) {
                     if (!spec.getType().equals(specificationType.get()) || !spec.getVersion().equals(specificationVersion.get())) {
                        // if criteria is specified and this specification doesn't match, ignore and move to next one
                        continue;
                     }
                  }

                  addSpecDetails(spec, spec.getPath());
                  specs.add(spec);
               }
            }
         }
      } else {
         // just get the spec directly
         Specification spec = getSpecificationById(userSpecificationId.get());
         specs.add(spec);
      }
      return specs;
   }

   private static final Specification createSpecificationResponse(Map<String, Object> input) {
      Specification spec = new Specification();
      spec.setPath((String) input.get("jcr:path"));
      spec.setType((String) input.get("equipLibrary:specificationType"));
      spec.setVersion((String) input.get("equipLibrary:specificationVersion"));
      spec.setId((String) input.get("id"));
      return spec;
   }

   private FieldDefinitionsParser getValidationSpecInfo(String specificationPath) {
      FieldDefinitionsParser fieldDefinitions = null;
      try {
         log.info("Getting content for artifact '{}'", specificationPath);
         ContentInfo contentInfo = librarianService.getArtifactContent(specificationPath + "/", false);
         byte[] content = contentInfo.getContent();
         JAXBContext jc = JAXBContext.newInstance(FieldDefinitionsParser.class);
         Unmarshaller unmarshaller = jc.createUnmarshaller();
         InputStreamReader inputStream = new InputStreamReader(new ByteArrayInputStream(content));
         fieldDefinitions = (FieldDefinitionsParser) unmarshaller.unmarshal(inputStream);
      } catch (Exception e) {
         throw new RuntimeException("Error in library xml file parsing.", e);
      }
      return fieldDefinitions;
   }

   private void updateGlobalInfoFromCsv(FileValidationLog fileValidationLog, List<CSVRecord> inputRecordList, FieldDefinitionsParser fieldDefinitions) {
      // subtract 1 because the 1st row is the column headers
      long numRows = inputRecordList.size() - 1;
      HashSet<String> subjectMap = new HashSet<String>();
      String pkterm = null;
      boolean headerRow = true;
      String subjectIdColumnName = fieldDefinitions.getSubjectIdColumnName();
      String pkTermColumnName = fieldDefinitions.getPkTermColumnName();
      if (pkTermColumnName == null) {
         log.warn("No PKTERM column name in this specification");
      }
      if (subjectIdColumnName == null) {
         log.warn("No PKTERM column name in this specification");
      }
      for (CSVRecord record : inputRecordList) {
         if (headerRow) {
            // skip header row
            headerRow = false;
            continue;
         }
         if (pkTermColumnName != null && record.isSet(pkTermColumnName)) {
            pkterm = record.get(pkTermColumnName);
         }
         if (subjectIdColumnName != null && record.isSet(subjectIdColumnName)) {
            subjectMap.add(record.get(subjectIdColumnName));
         } else {
            log.warn("Invalid row '{}' encountered during number of subjects calculation, could not find '{}' column value", record.getRecordNumber(),
                  subjectIdColumnName);
         }
      }
      Long numSubjects = null;
      if (subjectIdColumnName != null) {
         numSubjects = (long) subjectMap.size();
      }

      // update global info
      GlobalInfoReport info = fileValidationLog.getGlobalInfoReport();
      info.setNumberRows(String.valueOf(numRows));
      info.setNumberSubjects(String.valueOf(numSubjects));
      info.setPkterm(pkterm);

   }

   public void increment(AtomicLong l) {
      l.set(l.get() + 1);
   }

   private void calculateErrorsAndWarnings(List<? extends AbstractLogLevelReport> logList, AtomicLong numErrors, AtomicLong numWarnings) {
      for (AbstractLogLevelReport log : logList) {
         if (log.getLogLevel().equals(ValidationStatusTypes.ERROR)) {
            increment(numErrors);
         }
         if (log.getLogLevel().equals(ValidationStatusTypes.WARNING)) {
            increment(numWarnings);
         }
      }
   }

   public String generateNotificationMessage(List<FileValidationLog> fileValidationLogList) {
      // using objects instead of primitives since I can pass by reference to other methods
      AtomicLong numErrors = new AtomicLong(0), numWarnings = new AtomicLong(0);
      for (FileValidationLog fileValidationLog : fileValidationLogList) {
         // top level errors
         if (fileValidationLog.getDelimiterError() != null) {
            increment(numErrors);
         }
         if (fileValidationLog.getErrorOrException() != null) {
            increment(numErrors);
         }
         if (fileValidationLog.getFileNameError() != null) {
            increment(numErrors);
         }
         if (fileValidationLog.getGlobalInfoReport().getValidationStatus().equalsIgnoreCase(ValidationStatusTypes.ERROR.getStatusType())
               || fileValidationLog.getGlobalInfoReport().getValidationStatus().equalsIgnoreCase(ValidationStatusTypes.WARNING.getStatusType())) {
            FileLevelLogReport fileLevelLogReport = fileValidationLog.getFileLevelLogReport();
            calculateErrorsAndWarnings(fileLevelLogReport.getCaseMismatchFieldList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getEmptyLinesList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getExtraneousFieldList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getLeadingTrailingSpacesColumnsList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getMandatoryIfAvailableList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getMissingMandatoryFieldList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getNonUniqueColumnList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getUniqueColumnList(), numErrors, numWarnings);
            calculateErrorsAndWarnings(fileLevelLogReport.getUnorderedFieldList(), numErrors, numWarnings);

            List<FieldLevelLogReport> fieldLevelLogReport = fileValidationLog.getFieldLevelLogReportList();
            calculateErrorsAndWarnings(fieldLevelLogReport, numErrors, numWarnings);
            List<FieldSetLevelLogReport> fieldSetLevelLogReport = fileValidationLog.getFieldSetLevelLogReportList();
            calculateErrorsAndWarnings(fieldSetLevelLogReport, numErrors, numWarnings);
            List<UniqueSetLevelLogReport> uniqueSetLevelLogReport = fileValidationLog.getUniqueSetLevelLogReportList();
            calculateErrorsAndWarnings(uniqueSetLevelLogReport, numErrors, numWarnings);
            List<SdeidLevelLogReport> duplicateRecordsLevelLogReport = fileValidationLog.getDuplicateRecordsLevelLogReportList();
            calculateErrorsAndWarnings(duplicateRecordsLevelLogReport, numErrors, numWarnings);
            List<CrossFileLevelLogReport> crossFileLevelLogReport = fileValidationLog.getCrossFileLevelLogReportList();
            calculateErrorsAndWarnings(crossFileLevelLogReport, numErrors, numWarnings);
         }
      }

      String message = String.format("The file(s) in this validation run contained %s errors and %s warnings.", numErrors, numWarnings);
      return message;
   }

   /**
    * Validate the List of input files with respect to library file
    * 
    * @param specificationInput
    * @param fieldDefinitionsInput
    * @return
    * @throws IOException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    */
   public List<FileValidationLog> validateFile(SpecificationInput specificationInput, FieldDefinitionsParser fieldDefinitionsInput)
         throws IOException, IllegalArgumentException, IllegalAccessException {

      List<FileValidationLog> fileValidationLogList = new LinkedList<>();
      validateFieldDefinitions(fieldDefinitionsInput);

      for (int i = 0; i < specificationInput.getFileContent().size(); i++) {
         FileValidationLog fileValidationLog = validateInputFile(specificationInput, fieldDefinitionsInput, specificationInput.getFileContent().get(i), i,
               ValidationTypes.GENERAL);
         fileValidationLogList.add(fileValidationLog);
      }

      return fileValidationLogList;
   }

   public FileValidationLog validateInputFile(SpecificationInput specificationInput, FieldDefinitionsParser fieldDefinitionsInput, MultipartFile inputFile, int index,
         ValidationTypes validationType) throws IOException {

      String fileName = inputFile.getOriginalFilename();
      FileValidationLog fileValidationLog = new FileValidationLog();
      List<CSVRecord> inputRecordList = null;
      Map<String, FieldParser> fieldDefinitionsMap = createFieldDefinitionMap(fieldDefinitionsInput);
      try {
         // Validate the input delimiter
         boolean isValidDelimiter = false;
         isValidDelimiter = delimiterCheck(specificationInput.getDelimiter());
         if (!isValidDelimiter) {
            fileValidationLog.setDelimiterError("Delimiter : " + specificationInput.getDelimiter() + " == >" + FileValidationMessages.EQUIP_FV_ERR11.getErrMessageCode());
            fileValidationLog.setGlobalInfoReport(getGlobalInfoReport(specificationInput, fileName, validationType));
            fileValidationLog.getGlobalInfoReport().setValidationStatus(getValidationStatus(fileValidationLog));
            return fileValidationLog;
         } else {
            FileValidator fileValidator = new FileValidator();
            // Read input csv file and map the fields
            inputRecordList = new CsvFileReader().readCsvFile(inputFile, specificationInput.getDelimiter(), fieldDefinitionsMap.keySet());

            if (validationType.equals(ValidationTypes.CROSSFILEPARENT)) {// Cross file validation goes here
               // Read the PKS input csv file and map the fields
               List<CSVRecord> inputPksRecordList = new CsvFileReader().readCsvFile(specificationInput.getPksFileContent().get(index), specificationInput.getDelimiter(),
                     fieldDefinitionsMap.keySet());
               fileValidationLog = fileValidator.validateFieldData(inputRecordList, fieldDefinitionsMap, fieldDefinitionsInput, inputPksRecordList);
            } else {// validate the input csv record across the library file, general and cross
                    // child file validation
               fileValidationLog = fileValidator.validateFieldData(inputRecordList, fieldDefinitionsMap, fieldDefinitionsInput, null);
            }
            if (fieldDefinitionsInput.getFileNamePattern() != null) {
               for (String fileNamePattern : fieldDefinitionsInput.getFileNamePattern().getValue()) {
                  Pattern pattern = Pattern.compile(fileNamePattern);
                  if (!pattern.matcher(fileName).matches()) {
                     // If the file name pattern does not matches then log an error and proceed with
                     // the validation
                     String fileNameError = "File Name : " + fileName + " == >" + FileValidationMessages.EQUIP_FV_ERR07.getErrMessageCode();
                     FileLevelLog fileNameLevelLogReport = new FileLevelLog();
                     fileNameLevelLogReport.setLogLevel(fieldDefinitionsInput.getFileNamePattern().getLogLevel());
                     fileNameLevelLogReport.setMessage(fileNameError);
                     fileValidationLog.setFileNameError(fileNameLevelLogReport);
                  } else {
                     break;
                  }
               }
            }
         }
      } catch (CSVRuntimeException e) {
         fileValidationLog.setErrorOrException("Exception occurred while parsing the input file: " + e.getMessage());
         log.error("Validation failed for '{}'. Message: '{}'.", fileName, e.getMessage());
      }
      // If there's an error here, it's most likely an issue with the validation spec or some edge case,
      // consider it unrecoverable and let it be thrown instead of catching and returning a validation log. RVG 27-Nov-2018

      fileValidationLog.setGlobalInfoReport(getGlobalInfoReport(specificationInput, fileName, validationType));
      fileValidationLog.getGlobalInfoReport().setValidationStatus(getValidationStatus(fileValidationLog));
      // update some values in the global info report based on csv
      if (inputRecordList != null) {
         updateGlobalInfoFromCsv(fileValidationLog, inputRecordList, fieldDefinitionsInput);
      }
      return fileValidationLog;
   }

   /**
    * Validate the List of input files with respect to library file
    * 
    * @param specificationInput
    * @param fieldDefinitionsInput
    * @return
    * @throws IOException
    */
   public List<FileValidationLog> validatePksFile(SpecificationInput specificationInput, List<FieldDefinitionsParser> fieldDefinitionsInputList) throws IOException {

      List<FileValidationLog> fileValidationLogList = new LinkedList<>();

      for (int i = 0; i < specificationInput.getPksFileContent().size(); i++) {
         // First validate the Child input file (i.e PKS)
         FileValidationLog childFileValidationLog = validateInputFile(specificationInput, fieldDefinitionsInputList.get(1), specificationInput.getPksFileContent().get(i),
               i, ValidationTypes.CROSSFILECHILD);
         fileValidationLogList.add(childFileValidationLog);
         // Validate the Parent input file (i.e Final PKDef)
         FileValidationLog parentFileValidationLog = validateInputFile(specificationInput, fieldDefinitionsInputList.get(0), specificationInput.getFileContent().get(i),
               i, ValidationTypes.CROSSFILEPARENT);
         fileValidationLogList.add(parentFileValidationLog);
      }

      return fileValidationLogList;
   }

   /**
    * Create the fieldDefinition map from the library file.
    * 
    * @param fieldDefinitionsInput
    * @return
    */
   private Map<String, FieldParser> createFieldDefinitionMap(FieldDefinitionsParser fieldDefinitionsInput) {
      // Map business rules from library file for field definitions
      Map<String, FieldParser> fieldDefinitionsMap = new LinkedHashMap<>();
      if (fieldDefinitionsInput.getFieldInputList() != null) {
         for (FieldParser fieldDefinition : fieldDefinitionsInput.getFieldInputList()) {
            fieldDefinitionsMap.put(fieldDefinition.getColumnName().getValue(), fieldDefinition);
         }
      }
      return fieldDefinitionsMap;
   }

   /**
    * Check the input delimiter is available in the delimiter Input types
    * 
    * @param delimiterInput
    * @return
    */
   public static boolean delimiterCheck(String delimiterInput) {
      for (DelimiterInputTypes delimiter : DelimiterInputTypes.values()) {
         if (delimiter.getDelimterType().contains(delimiterInput)) {
            return true;
         }
      }
      return false;
   }

   /**
    * get the final Validation Status from the file level , field level , fieldset level , sdeid level validation status
    * 
    * @param fileValidationLog
    * @return
    */
   public String getValidationStatus(FileValidationLog fileValidationLog) {

      if (fileValidationLog.getDelimiterError() != null || fileValidationLog.getErrorOrException() != null || fileValidationLog.getFileNameError() != null) {
         return ValidationStatusTypes.ERROR.getStatusType();
      }
      if (fileValidationLog.getFileLevelValidationStatus().equals(ValidationStatusTypes.ERROR)
            || fileValidationLog.getFieldLevelValidationStatus().equals(ValidationStatusTypes.ERROR)
            || fileValidationLog.getFieldSetLevelValidationStatus().equals(ValidationStatusTypes.ERROR)
            || fileValidationLog.getDuplicateRecordsValidationStatus().equals(ValidationStatusTypes.ERROR)
            || fileValidationLog.getUniquenessValidationStatus().equals(ValidationStatusTypes.ERROR)) {
         return ValidationStatusTypes.ERROR.getStatusType();
      } else {
         if (fileValidationLog.getFileLevelValidationStatus().equals(ValidationStatusTypes.WARNING)
               || fileValidationLog.getFieldLevelValidationStatus().equals(ValidationStatusTypes.WARNING)
               || fileValidationLog.getFieldSetLevelValidationStatus().equals(ValidationStatusTypes.WARNING)
               || fileValidationLog.getDuplicateRecordsValidationStatus().equals(ValidationStatusTypes.WARNING)
               || fileValidationLog.getUniquenessValidationStatus().equals(ValidationStatusTypes.WARNING)) {
            return ValidationStatusTypes.WARNING.getStatusType();

         }

         return ValidationStatusTypes.SUCCESS.getStatusType();
      }

   }

   /**
    * Set the Global Information from the Specification Inputs
    * 
    * @param specificationInput
    * @param fileName
    * @return
    */
   public GlobalInfoReport getGlobalInfoReport(SpecificationInput specificationInput, String fileName, ValidationTypes validationType) {
      GlobalInfoReport globalInfoReport = new GlobalInfoReport();
      // TODO to set the exact Tool Name
      globalInfoReport.setToolName("Test Tool");
      if (validationType.equals(ValidationTypes.CROSSFILECHILD)) {
         globalInfoReport.setSpecificationType(specificationInput.getPksFileSpecType());
         globalInfoReport.setSpecificationVersion(specificationInput.getPksFileVersion());
      } else {
         globalInfoReport.setSpecificationType(specificationInput.getSpecificationType());
         globalInfoReport.setSpecificationVersion(specificationInput.getSpecificationVersion());
      }
      globalInfoReport.setTester(specificationInput.getUserId());
      globalInfoReport.setPlatForm(System.getProperty("os.name"));
      try {
         globalInfoReport.setHostName(InetAddress.getLocalHost().toString());
      } catch (UnknownHostException e) {
         String message = String.format("Validation failed for '%s'.Unknown host exception occurred while setting GlobalInfoReport. Exception is: '%s'", fileName,
               e.getMessage());
         log.error(message);
         throw new FileValidationRuntimeException(message);
      }
      globalInfoReport.setFileName(fileName);
      globalInfoReport.setValidationDoneDate(new Date());
      return globalInfoReport;
   }

   public ContentInfo makeValidationReportCsv(FileValidationLog log, String fileName) throws IOException {
      StringBuilder builder = new StringBuilder();
      CSVPrinter printer = new CSVPrinter(builder, CSVFormat.DEFAULT);
      List<String> columnHeaders = new ArrayList<String>(Arrays.asList("validation type", "column", "value", "type", "line", "message"));

      for (String header : columnHeaders) {
         printer.print(header);
      }
      printer.println();

         if (log.getDelimiterError() != null) {
            printer.print("Delimiter");
            printer.print("");
            printer.print("");
            printer.print(ValidationStatusTypes.ERROR.toString());
            printer.print("");
            printer.print(log.getDelimiterError());
            printer.println();
         }
         if (log.getErrorOrException() != null) {
            printer.print("Other");
            printer.print("");
            printer.print("");
            printer.print(ValidationStatusTypes.ERROR.toString());
            printer.print("");
            printer.print(log.getErrorOrException());
            printer.println();
         }
         if (log.getFileNameError() != null) {
            printer.print("Filename");
            printer.print("");
            printer.print(fileName);
            printer.print(ValidationStatusTypes.ERROR.toString());
            printer.print("");
            printer.print(log.getFileNameError());
            printer.println();
         }

      for (FieldLevelLogReport report : log.getFieldLevelLogReportList()) {
         printer.print("Field");
         printer.print(report.getName());
         printer.print(report.getColumnValue());
         printer.print(report.getLogLevel());
         printer.print(report.getRowIndex());
         printer.print(report.getMessage());
         printer.println();
      }

      for (FieldSetLevelLogReport report : log.getFieldSetLevelLogReportList()) {
         printer.print("FieldSet");
         printer.print(report.getName());
         printer.print(report.getColumnValue());
         printer.print(report.getLogLevel());
         printer.print(report.getRowIndex());
         printer.print(report.getMessage());
         printer.println();
      }

      for (SdeidLevelLogReport report : log.getDuplicateRecordsLevelLogReportList()) {
         printer.print("DuplicateRecords");
         printer.print(report.getName());
         printer.print(report.getColumnValue());
         printer.print(report.getLogLevel());
         printer.print(report.getRowIndex());
         printer.print(report.getMessage());
         printer.println();
      }

      for (UniqueSetLevelLogReport report : log.getUniqueSetLevelLogReportList()) {
         printer.print("UniqueSet");
         printer.print(report.getName());
         printer.print(report.getColumnValue());
         printer.print(report.getLogLevel());
         printer.print(report.getRowIndex());
         printer.print(report.getMessage());
         printer.println();
      }

      FileLevelLogReport fileLevelLog = log.getFileLevelLogReport();

      if (fileLevelLog != null) {
         for (EmptyLinesLog report : fileLevelLog.getEmptyLinesList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print(report.getRowIndex());
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getCaseMismatchFieldList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getExtraneousFieldList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getLeadingTrailingSpacesColumnsList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getMandatoryIfAvailableList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getMissingMandatoryFieldList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getNonUniqueColumnList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getUniqueColumnList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }

         for (FileLevelLog report : fileLevelLog.getUnorderedFieldList()) {
            printer.print("File");
            printer.print(report.getName());
            printer.print("");
            printer.print(report.getLogLevel());
            printer.print("");
            printer.print(report.getMessage());
            printer.println();
         }
      }

      byte[] bytes = builder.toString().getBytes("UTF-8");
      printer.close();
      return new ContentInfo(bytes, "text/csv", fileName);
   }

   private void validateFieldDefinitions(FieldDefinitionsParser fieldDefinitions) throws IllegalArgumentException, IllegalAccessException {
      List<String> errorFields = new ArrayList<String>();
      for (Field field : FieldDefinitionsParser.class.getDeclaredFields()) {
         XmlElement xmlElement = field.getAnnotation(XmlElement.class);
         String name = xmlElement.name();
         field.setAccessible(true);
         if (xmlElement.required() && field.get(fieldDefinitions) == null) {
            errorFields.add(name);
         }
      }
      if (errorFields.size() > 0) {
         throw new InvalidFileSpecificationException(String.format("Invalid specification, the following fields are null: %s", errorFields));
      }
   }
}
