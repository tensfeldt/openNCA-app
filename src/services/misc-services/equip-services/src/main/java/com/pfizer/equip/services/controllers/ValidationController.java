package com.pfizer.equip.services.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamonapi.Monitor;
import com.pfizer.equip.services.business.api.dataframe.DataframeService;
import com.pfizer.equip.services.business.librarian.LibrarianService;
import com.pfizer.equip.services.business.validation.FileValidationLog;
import com.pfizer.equip.services.business.validation.Specification;
import com.pfizer.equip.services.business.validation.ValidationService;
import com.pfizer.equip.services.business.validation.exceptions.FileValidationRuntimeException;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.input.library.LibraryInput;
import com.pfizer.equip.services.input.library.LibraryInputBuilder;
import com.pfizer.equip.services.input.specification.SpecificationInput;
import com.pfizer.equip.services.input.specification.SpecificationInputBuilder;
import com.pfizer.equip.services.input.validation.GlobalInfoReport;
import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;
import com.pfizer.equip.services.input.validation.xmlparser.FieldDefinitionsParser;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.properties.ValidationProperties;
import com.pfizer.equip.services.responses.library.LibraryArtifactResponse;
import com.pfizer.equip.services.responses.validation.CreateUpdateDeleteSpecification;
import com.pfizer.equip.services.responses.validation.GetSpecificationResponse;
import com.pfizer.equip.services.responses.validation.GetSpecificationsResponse;
import com.pfizer.equip.services.responses.validation.ValidationResponse;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.notifications.EventService;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@RestController
public class ValidationController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   @Autowired
   private ValidationService validationService;

   @Autowired
   private LibrarianService librarianService;

   @Autowired
   private ValidationProperties properties;

   @Autowired
   private EventService eventService;

   @Autowired
   private AuditService auditService;

   @Autowired
   private UserLookupService userLookupService;

   @Autowired
   private DataframeService dataframeService;

   @Autowired
   private ApplicationProperties appProperties;
   private static final String FILE_NAME = "fileName";
   public static final String SYSTEM_ID_NCA = "nca";
   private static final String FILE_NAME_STRIP_CHARS = "[^a-zA-Z0-9\\.\\-_~!$&'()*+,;=:@]";

   private LibraryInput createLibraryInputForReport(SpecificationInput input, ValidationResponse response, String userId) throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      String jsonValidationReport = mapper.writeValueAsString(response);

      String specType = input.getSpecificationType();
      String specVersion = input.getSpecificationVersion();

      DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
      Date today = Calendar.getInstance().getTime();
      String dateString = df.format(today);

      String fileName = String.format("VR-%s-%s-%s-%s", specType, specVersion, userId, dateString).replaceAll(FILE_NAME_STRIP_CHARS, "");

      // create a mock multipart file so that we can re-use the library method, which expects the content to be a file
      MockMultipartFile jsonFile = new MockMultipartFile(fileName, fileName, "application/json", jsonValidationReport.getBytes());

      // build inputs map to simulate normal add artifact call
      String primaryType = "equipLibrary:validationReport";
      Map<String, Object> inputs = new HashMap<String, Object>();
      inputs.put(LibraryInput.PROPERTY_PRIMARY_TYPE, primaryType);
      inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, jsonFile);

      LibraryInputBuilder builder = new LibraryInputBuilder();
      return builder.build(userId, inputs);
   }

   @RequestMapping(value = "{systemId}/specification/validate/{programId}/{protocolId}", method = RequestMethod.POST)
   public ValidationResponse validateFileToSpecification(@RequestParam Map<String, Object> inputs, @PathVariable("programId") String programId,
         @PathVariable("protocolId") String protocolId, @RequestParam("file") List<MultipartFile> fileContent, HttpServletRequest request) {
      Monitor monitor = null;
      List<FileValidationLog> fileValidationLogList = new LinkedList<>();
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_VALIDATOR)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform file validation.", userId));
         }
         
         List<MultipartFile> convertedFileContent = new ArrayList<>();
         for (MultipartFile file : fileContent) {
            byte[] bytes = dataframeService.convertFileToCsv(userId, file, Optional.empty(), Optional.empty()).getBytes();
            MockMultipartFile convertedFile = new MockMultipartFile(file.getOriginalFilename(), file.getOriginalFilename(), "text/csv", bytes);
            convertedFileContent.add(convertedFile);
         }

         SpecificationInputBuilder builder = new SpecificationInputBuilder();
         builder.setFileContent(convertedFileContent);
         SpecificationInput input = builder.build(userId, inputs);
         String subscriptionUserId = StringUtils.defaultIfBlank(input.getUserId(), "");
         String specificationPath = input.getSpecificationPath();
         log.info("Validation specification '{}' for user ID '{}'...", specificationPath, subscriptionUserId);

         ValidationResponse validationResponse = new ValidationResponse();
         validationResponse.setResponse(Response.OK);

         // If this fails, throw an error. We cannot even start the validation job without a spec file. RVG 04-Dec-2018
         FieldDefinitionsParser fieldDefinitionsInput = getValidationRulesLibrary(specificationPath, userId);

         // validate the input file
         fileValidationLogList = validationService.validateFile(input, fieldDefinitionsInput);

         for (FileValidationLog fileValidationLoginfo : fileValidationLogList) {
            fileValidationLoginfo.getGlobalInfoReport().setArtifactVersion(input.getSpecificationVersion());
            if (fileValidationLoginfo.getErrorOrException() != null || fileValidationLoginfo.getDelimiterError() != null) {
               validationResponse.setResponse(Response.FAILED);
            }
         }

         validationResponse.setFileValidationLog(fileValidationLogList);

         int index = 0;
         for (MultipartFile inputFile : input.getFileContent()) {
            // Raise event for new specification added
            Map<String, Object> description = eventService.createEventDescription("File Validation performed for  " + fileContent.size() + " file(s)", subscriptionUserId,
                  "false");
            description.put("validation_file_name", inputFile.getOriginalFilename());
            description.put("validation_specification", input.getSpecificationType() + " " + input.getSpecificationVersion());
            description.put("validation_number_records", fileValidationLogList.get(index).getGlobalInfoReport().getNumberRows());
            description.put("validation_number_subjects", fileValidationLogList.get(index).getGlobalInfoReport().getNumberSubjects());
            description.put("validation_message", validationService.generateNotificationMessage(fileValidationLogList));
            description.put("validation_status", fileValidationLogList.get(index).getGlobalInfoReport().getValidationStatus());
            description.put("validation_field_status", fileValidationLogList.get(index).getFieldLevelValidationStatus());
            description.put("validation_file_status", fileValidationLogList.get(index).getFileLevelValidationStatus());
            description.put("validation_field_set_status", fileValidationLogList.get(index).getFieldSetLevelValidationStatus());
            description.put("validation_duplicates_status", fileValidationLogList.get(index).getDuplicateRecordsValidationStatus());
            description.put("validation_uniqueness_status", fileValidationLogList.get(index).getUniquenessValidationStatus());

            FileValidationLog log = fileValidationLogList.get(index);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
            Date today = Calendar.getInstance().getTime();
            String dateString = df.format(today);
            String fileName = String.format("log-%s-%s-%s-%s.csv", input.getSpecificationType(), input.getSpecificationVersion(), userId, dateString)
                  .replaceAll(FILE_NAME_STRIP_CHARS, "");

            ContentInfo reportCsv = validationService.makeValidationReportCsv(log, fileName);

            eventService.createEvent(this.getClass().toString(), new Date(), specificationPath, "specification", "data_validation", protocolId, programId, description,
                  appProperties.getEventQueue(), reportCsv);
            index++;
         }

         // save the validation report in modeshape
         LibraryInput libInput = createLibraryInputForReport(input, validationResponse, userId);
         String artifactPath = String.format("Programs/%s/Protocols/%s/ValidationReports/", programId, protocolId);
         String reportId = librarianService.addArtifact(artifactPath, libInput, subscriptionUserId, false).getArtifactId();
         validationResponse.setValidationReportId(reportId);

         // Create audit entry
         auditService
               .insertAuditEntry(new AuditEntryInput("File Validation performed for  " + fileContent.size() + " file(s)", validationResponse.getValidationReportId(),
                     EntityType.SPECIFICATION.getValue(), subscriptionUserId, ActionStatusType.SUCCESS, validationResponse.getResponse().getValue()));

         return validationResponse;
      } catch (Exception e) {
         log.error("Fatal system error during validation, cannot continue", e);
         // We're already throwing FileValidationRuntimeException at lower levels, so just use RuntimeException here. Otherwise error is messy. RVG 04-Dec-2018
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/specification/multi-validate/{programId}/{protocolId}", method = RequestMethod.POST)
   public ValidationResponse validateFilesToSpecifications(@PathVariable("programId") String programId, @PathVariable("protocolId") String protocolId,
         @RequestParam("file") List<MultipartFile> fileContents, @RequestParam("specifications") List<String> specifications, @RequestParam("delimiter") String delimiter,
         HttpServletRequest request) {
      Monitor monitor = null;
      List<FileValidationLog> fileValidationLogList = new LinkedList<>();
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_VALIDATOR)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform file validation.", userId));
         }

         ValidationResponse validationResponse = new ValidationResponse();
         validationResponse.setResponse(Response.OK);
         ObjectMapper mapper = new ObjectMapper();
         for (int idx = 0; idx < fileContents.size(); idx++) {
            // Here we are grabbing the individual spec and file content and then making it into a list of one member to match the existing input class
            // TODO: refactor once we have a better idea of how this endpoint will be used, so as to avoid one-membered lists
            Specification specification = mapper.readValue(specifications.get(idx), Specification.class);
            MultipartFile fileContent = fileContents.get(idx);
            SpecificationInputBuilder builder = new SpecificationInputBuilder();
            builder.setFileContent(Arrays.asList(fileContent));

            Map<String, Object> inputs = new HashMap<String, Object>();

            inputs.put(SpecificationInputBuilder.KEY_SPEC_PATH, specification.getPath());
            inputs.put(SpecificationInputBuilder.KEY_SPEC_TYPE, specification.getType());
            inputs.put(SpecificationInputBuilder.KEY_SPEC_VERSION, specification.getVersion());
            inputs.put(SpecificationInputBuilder.KEY_DELIMITER, delimiter);

            SpecificationInput input = builder.build(userId, inputs);
            String subscriptionUserId = StringUtils.defaultIfBlank(input.getUserId(), "");
            String specificationPath = input.getSpecificationPath();
            log.info("Validation specification '{}' for user ID '{}'...", specificationPath, subscriptionUserId);

            // If this fails, throw an error. We cannot even start the validation job
            // without a spec file. RVG 04-Dec-2018
            FieldDefinitionsParser fieldDefinitionsInput = getValidationRulesLibrary(specificationPath, userId);

            // validate the input file
            FileValidationLog fileValidationLog = validationService.validateFile(input, fieldDefinitionsInput).get(0);
            fileValidationLogList.add(fileValidationLog);

            for (FileValidationLog fileValidationLoginfo : fileValidationLogList) {
               fileValidationLoginfo.getGlobalInfoReport().setArtifactVersion(input.getSpecificationVersion());
               if (fileValidationLoginfo.getErrorOrException() != null || fileValidationLoginfo.getDelimiterError() != null) {
                  validationResponse.setResponse(Response.FAILED);
               }
            }

            int index = 0;
            for (MultipartFile inputFile : input.getFileContent()) {
               // Raise event for new specification added
               Map<String, Object> description = eventService.createEventDescription("File Validation performed", subscriptionUserId, "false");
               description.put("validation_file_name", inputFile.getOriginalFilename());
               description.put("validation_specification", input.getSpecificationType() + " " + input.getSpecificationVersion());
               description.put("validation_number_records", fileValidationLogList.get(index).getGlobalInfoReport().getNumberRows());
               description.put("validation_number_subjects", fileValidationLogList.get(index).getGlobalInfoReport().getNumberSubjects());
               description.put("validation_message", validationService.generateNotificationMessage(fileValidationLogList));
               description.put("validation_status", fileValidationLogList.get(index).getGlobalInfoReport().getValidationStatus());
               description.put("validation_field_status", fileValidationLogList.get(index).getFieldLevelValidationStatus());
               description.put("validation_file_status", fileValidationLogList.get(index).getFileLevelValidationStatus());
               description.put("validation_field_set_status", fileValidationLogList.get(index).getFieldSetLevelValidationStatus());
               description.put("validation_duplicates_status", fileValidationLogList.get(index).getDuplicateRecordsValidationStatus());
               description.put("validation_uniqueness_status", fileValidationLogList.get(index).getUniquenessValidationStatus());

               DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
               Date today = Calendar.getInstance().getTime();
               String dateString = df.format(today);
               String fileName = String.format("log-%s-%s-%s-%s.csv", input.getSpecificationType(), input.getSpecificationVersion(), userId, dateString)
                     .replaceAll(FILE_NAME_STRIP_CHARS, "");

               ContentInfo reportCsv = validationService.makeValidationReportCsv(fileValidationLog, fileName);

               eventService.createEvent(this.getClass().toString(), new Date(), specificationPath, "specification", "data_validation", protocolId, programId, description,
                     appProperties.getEventQueue(), reportCsv);
               index++;
            }
            validationResponse.setFileValidationLog(fileValidationLogList);

            // save the validation report in modeshape
            LibraryInput libInput = createLibraryInputForReport(input, validationResponse, userId);
            String artifactPath = String.format("Programs/%s/Protocols/%s/ValidationReports/", programId, protocolId);
            String reportId = librarianService.addArtifact(artifactPath, libInput, subscriptionUserId, false).getArtifactId();
            validationResponse.setValidationReportId(reportId);

            // Create audit entry
            auditService.insertAuditEntry(new AuditEntryInput("File Validation performed", validationResponse.getValidationReportId(),
                  EntityType.SPECIFICATION.getValue(), subscriptionUserId, ActionStatusType.SUCCESS, validationResponse.getResponse().getValue()));
         }

         return validationResponse;
      } catch (Exception e) {
         log.error("Fatal system error during validation, cannot continue", e);
         // We're already throwing FileValidationRuntimeException at lower levels, so just use RuntimeException here. Otherwise error is messy. RVG 04-Dec-2018
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/specification/crossfilevalidate/{programId}/{protocolId}", method = RequestMethod.POST)
   public ValidationResponse validateCrossFileToSpecification(@RequestParam Map<String, Object> inputs, @PathVariable("programId") String programId,
         @PathVariable("protocolId") String protocolId, @RequestParam("file") List<MultipartFile> fileContent,
         @RequestParam("pksFile") List<MultipartFile> pksFileContent, HttpServletRequest request) {
      Monitor monitor = null;

      List<FileValidationLog> fileValidationLogList = new LinkedList<>();
      List<FieldDefinitionsParser> fieldDefinitionsParserList = new LinkedList<>();
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.RUN_VALIDATOR)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform cross file validation.", userId));
         }

         SpecificationInputBuilder builder = new SpecificationInputBuilder();
         builder.setFileContent(fileContent);
         builder.setPksFileContent(pksFileContent);
         SpecificationInput input = builder.build(userId, inputs);
         String subscriptionUserId = StringUtils.defaultIfBlank(input.getUserId(), "");
         String specificationPath = input.getSpecificationPath();
         String pksSpecificationPath = input.getPksSpecificationPath();
         log.info("Validation specification '{}' for user ID '{}'...", specificationPath, subscriptionUserId);

         ValidationResponse validationResponse = new ValidationResponse();
         validationResponse.setResponse(Response.OK);

         // Always index zero refers to the parent specification (example: pkdef)
         fieldDefinitionsParserList.add(0, getValidationRulesLibrary(specificationPath, userId));

         // Always index one refers to the child specification i.e. PKS
         fieldDefinitionsParserList.add(1, getValidationRulesLibrary(pksSpecificationPath, userId));

         // validate the input file
         // Size Should always be 2 , because the parent specification and child specification should be present to proceed further
         // There should be equal no.of fileContent and PKS File Content(always a pair of files expected)
         if (fieldDefinitionsParserList.size() == 2 && (fileContent.size() == pksFileContent.size())) {
            fileValidationLogList = validationService.validatePksFile(input, fieldDefinitionsParserList);

            for (FileValidationLog fileValidationLoginfo : fileValidationLogList) {
               fileValidationLoginfo.getGlobalInfoReport().setArtifactVersion(input.getSpecificationVersion());
               if (fileValidationLoginfo.getErrorOrException() != null || fileValidationLoginfo.getDelimiterError() != null) {
                  validationResponse.setResponse(Response.FAILED);
               }
            }

            validationResponse.setFileValidationLog(fileValidationLogList);
         } else {
            // Report the Error with Global Information
            for (MultipartFile inputFile : input.getFileContent()) {
               FileValidationLog fileValidationLog = new FileValidationLog();
               fileValidationLog.setErrorOrException("Specification Not Found");
               GlobalInfoReport globalInfoReport = new GlobalInfoReport();
               // TODO to set the exact Tool Name
               globalInfoReport.setToolName("Test Tool");
               globalInfoReport.setSpecificationType(input.getSpecificationType());
               globalInfoReport.setSpecificationVersion(input.getSpecificationVersion());
               globalInfoReport.setTester(input.getUserId());
               globalInfoReport.setPlatForm(System.getProperty("os.name"));
               globalInfoReport.setHostName(InetAddress.getLocalHost().toString());
               globalInfoReport.setFileName(inputFile.getOriginalFilename());
               globalInfoReport.setValidationDoneDate(new Date());
               globalInfoReport.setValidationStatus(ValidationStatusTypes.ERROR.getStatusType());
               fileValidationLog.setGlobalInfoReport(globalInfoReport);
               fileValidationLogList.add(fileValidationLog);
            }
            validationResponse.setResponse(Response.FAILED);
         }

         // Raise event for new specification added
         Map<String, Object> description = new HashMap<>();
         description.put("comments", "File Validation performed for  " + fileContent.size() + " file(s)");
         description.put("user_name", subscriptionUserId);
         description.put("system_initiated", "false");
         // TODO: what is file type being used for?
         description.put("file_type", input.getFileType());
         description.put("version", input.getSpecificationVersion());
         description.put("validation_status", validationResponse.getResponse());

         eventService.createEvent(this.getClass().toString(), new Date(), specificationPath, "specification", "data_validation", null, null, description,
               appProperties.getEventQueue());

         // save the validation report in modeshape
         LibraryInput libInput = createLibraryInputForReport(input, validationResponse, userId);
         String artifactPath = String.format("Programs/%s/Protocols/%s/ValidationReports/", programId, protocolId);
         String reportId = librarianService.addArtifact(artifactPath, libInput, subscriptionUserId, false).getArtifactId();
         validationResponse.setValidationReportId(reportId);

         // Create audit entry
         auditService
               .insertAuditEntry(new AuditEntryInput("File Validation performed for  " + fileContent.size() + " file(s)", validationResponse.getValidationReportId(),
                     EntityType.SPECIFICATION.getValue(), subscriptionUserId, ActionStatusType.SUCCESS, validationResponse.getResponse().getValue()));

         return validationResponse;
      } catch (Exception e) {
         String message = "Exception occured during cross validation of specification.";
         log.error(message, e);
         throw new RuntimeException(message, e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   private FieldDefinitionsParser getValidationRulesLibrary(String specificationPath, String userId) throws JAXBException {
      log.info("Getting content for artifact '{}' for user ID '{}'...", specificationPath, userId);
      FieldDefinitionsParser fieldDefinitions = null;
      ContentInfo contentInfo = null;
      try {
         contentInfo = librarianService.getArtifactContent(specificationPath, false);
      } catch (HttpClientErrorException e) {
         if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new FileValidationRuntimeException("Specification not found in repository.");
         } else {
            throw e;
         }
      }
      byte[] content = contentInfo.getContent();
      JAXBContext jc = JAXBContext.newInstance(FieldDefinitionsParser.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      InputStreamReader inputStream = new InputStreamReader(new ByteArrayInputStream(content));
      fieldDefinitions = (FieldDefinitionsParser) unmarshaller.unmarshal(inputStream);
      return fieldDefinitions;
   }

   @RequestMapping(value = "{systemId}/specification", method = RequestMethod.POST)
   public CreateUpdateDeleteSpecification addSpecification(@RequestParam Map<String, Object> inputs, @RequestParam("fileContent") MultipartFile fileContent,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADMIN_VALIDATOR)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform add file specification.", userId));
         }

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, fileContent);

         // All the Library File Specification should be of type equipLibrary:specification
         inputs.put(LibraryInput.PROPERTY_PRIMARY_TYPE, "equipLibrary:specification");
         LibraryInputBuilder builder = new LibraryInputBuilder();
         LibraryInput input = builder.build(userId, inputs);
         log.info("Adding specification for user ID '{}'...", userId);

         String artifactPath = properties.getPath() + "/";
         artifactPath = artifactPath.substring(1);

         LibraryArtifactResponse libraryArtifactResponse = librarianService.addArtifact(artifactPath, input, userId, false);
         CreateUpdateDeleteSpecification specificationResponse = new CreateUpdateDeleteSpecification();
         specificationResponse.setResponse(Response.OK);
         specificationResponse.setId(libraryArtifactResponse.getArtifactId());
         specificationResponse.setPath(libraryArtifactResponse.getArtifactPath());

         // Raise event for new specification added
         eventService.createEvent(this.getClass().toString(), new Date(), libraryArtifactResponse.getArtifactPath(), "specification", "global_library_additions", null,
               null, eventService.createEventDescription("New specification  file is added. File name is  " + fileContent.getName(), userId, "false"),
               appProperties.getEventQueue());

         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("New Specification Added. Specification :" + fileContent.getOriginalFilename(),
               libraryArtifactResponse.getArtifactId(), EntityType.SPECIFICATION.getValue(), userId, ActionStatusType.SUCCESS, null));

         return specificationResponse;
      } catch (Exception e) {
         log.error("Exception occured during creation of specification.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/specifications", method = RequestMethod.GET)
   public GetSpecificationsResponse getSpecifications(HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.VIEW_GLOBAL_LIBRARY)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform get file specifications.", userId));
         }

         GetSpecificationsResponse response = new GetSpecificationsResponse();
         response.setResponse(Response.OK);
         response.setSpecifications(validationService.getSpecificationFiles(Optional.empty(), Optional.empty(), Optional.empty()));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting list of specifications.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/specification/id/{id}", method = RequestMethod.GET)
   public GetSpecificationResponse getSpecificationById(@PathVariable("id") String id, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.VIEW_GLOBAL_LIBRARY)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform get file specifications.", userId));
         }

         GetSpecificationResponse response = new GetSpecificationResponse();
         response.setResponse(Response.OK);
         response.setSpecification(validationService.getSpecificationById(id));
         return response;
      } catch (Exception e) {
         log.error("Exception occured during getting specification.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/specification", method = RequestMethod.PUT)
   public CreateUpdateDeleteSpecification updateSpecification(@RequestParam Map<String, Object> inputs, @RequestParam("fileContent") MultipartFile fileContent,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADMIN_VALIDATOR)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform update file specification.", userId));
         }

         String fileName = (String) inputs.get(FILE_NAME);
         String artifactPath = properties.getPath() + "/" + fileName;
         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_FILE_CONTENT, fileContent);
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, artifactPath);

         // All the Library File Specification should be of type equipLibrary:specification
         inputs.put(LibraryInput.PROPERTY_PRIMARY_TYPE, "equipLibrary:specification");

         LibraryInputBuilder builder = new LibraryInputBuilder();
         LibraryInput input = builder.build(userId, inputs);
         log.info("Updating specification '{}' for user ID '{}'...", artifactPath, userId);

         LibraryArtifactResponse libraryArtifactResponse = librarianService.updateArtifact(artifactPath.substring(1), input, userId);
         CreateUpdateDeleteSpecification specificationResponse = new CreateUpdateDeleteSpecification();
         specificationResponse.setResponse(Response.OK);
         specificationResponse.setId(libraryArtifactResponse.getArtifactId());
         specificationResponse.setPath(libraryArtifactResponse.getArtifactPath());
         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput(" Specification updated. Specification :" + fileContent.getOriginalFilename(),
               libraryArtifactResponse.getArtifactId(), EntityType.SPECIFICATION.getValue(), userId, ActionStatusType.SUCCESS, null));

         return specificationResponse;
      } catch (Exception e) {
         log.error("Exception occured during update of specification.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/specification/{fileName}", method = RequestMethod.DELETE)
   public CreateUpdateDeleteSpecification deleteSpecification(@PathVariable String fileName, @RequestParam Map<String, Object> inputs, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);
         // extract the rest of the artifact path from the entire request URL
         String artifactPath = properties.getPath() + "/" + fileName;

         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.ADMIN_VALIDATOR)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to perform delete file specification.", userId));
         }

         // add to inputs map as needed
         inputs.put(LibraryInput.PROPERTY_ARTIFACT_PATH, artifactPath);
         // a 'delete' is just a soft delete with an update to a custom metadata property
         inputs.put(LibraryInput.PROPERTY_DELETED, "true");

         LibraryInputBuilder builder = new LibraryInputBuilder();
         LibraryInput input = builder.build(userId, inputs);
         log.info("Deleting specification ID '{}' for user ID '{}'...", artifactPath, userId);

         LibraryArtifactResponse libraryArtifactResponse = librarianService.updateArtifact(artifactPath, input, userId);
         CreateUpdateDeleteSpecification specificationResponse = new CreateUpdateDeleteSpecification();
         specificationResponse.setResponse(Response.OK);
         specificationResponse.setId(libraryArtifactResponse.getArtifactId());
         specificationResponse.setPath(libraryArtifactResponse.getArtifactPath());

         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput(" Specification Deleted. ", libraryArtifactResponse.getArtifactId(), EntityType.SPECIFICATION.getValue(),
               userId, ActionStatusType.SUCCESS, null));

         return specificationResponse;
      } catch (Exception e) {
         log.error("Exception occured during deletion of specification.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
}
