package com.pfizer.equip.services.business.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.CharMatcher;
import com.pfizer.equip.services.business.validation.exceptions.CSVRuntimeException;
import com.pfizer.equip.services.input.validation.FileValidationMessages;
import com.pfizer.equip.services.input.validation.types.DelimiterInputTypes;

public class CsvFileReader {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   /**
    * Read the input .csv file and get the data
    * 
    * @param csvMulitipartFile
    * @return
    * @throws IOException and IllegalArgumentException
    */
   public List<CSVRecord> readCsvFile(MultipartFile csvMultipartFile, String delimiter, Set<String> fieldList) throws IOException, IllegalArgumentException {
      List<CSVRecord> csvRecordsList = null;
      // Format the delimiter based on the enum value
      String formattedDelimiter = getDelimiter(delimiter);
      try {
         String[] fileHeaderMapping = new BufferedReader(new InputStreamReader(csvMultipartFile.getInputStream())).readLine().split(formattedDelimiter);

         // If the one or many of the file header has double quotes in the header name , it should be removed
         for (int ctr = 0; ctr < fileHeaderMapping.length; ctr++) {
            if (fileHeaderMapping[ctr].startsWith("\"") && fileHeaderMapping[ctr].endsWith("\"")) {
               fileHeaderMapping[ctr] = fileHeaderMapping[ctr].substring(1, fileHeaderMapping[ctr].length() - 1);
            }
            // Check for NON-ASCII characters
            if (!CharMatcher.ASCII.matchesAllOf(fileHeaderMapping[ctr]))
               throw new CSVRuntimeException(FileValidationMessages.EQUIP_FV_ERR26.getErrMessageCode());
         }

         // Create the CSVFormat object with the header mapping
         // TODO need to check for tab delimiter
         // If the field header mapping length is one then there is an error in parsing the file
         if (fileHeaderMapping.length == 1) {
            throw new CSVRuntimeException(FileValidationMessages.EQUIP_FV_ERR21.getErrMessageCode());
         }
         // Need to verify at least one field from the header is present, if possible
         // So if there is at least one <Field> tag we will be able to match things up
         // Otherwise throw an error because we don't want to process a headerless file
         if (fieldList.size() > 0) {
            // Case insensitive check
            Set<String> fieldListUppercase = fieldList.stream().map(String::toUpperCase).collect(Collectors.toSet());
            boolean headerFound = false;
            for (String fileHeader : fileHeaderMapping) {
               //If the header is quoted with double quotes , the remove he double quotes to check with file Specs
               if (fieldListUppercase.contains(fileHeader.trim().toUpperCase())) {
                  headerFound = true;
                  break;
               }
            }
            if (!headerFound) {
               throw new CSVRuntimeException(FileValidationMessages.EQUIP_FV_ERR23.getErrMessageCode());
            }
         }

         CSVFormat csvFileFormat = CSVFormat.newFormat(delimiter.charAt(0)).withQuote('"').withHeader(fileHeaderMapping);
         InputStreamReader inputStreamReader = new InputStreamReader(csvMultipartFile.getInputStream());
         try (CSVParser csvFileParser = new CSVParser(inputStreamReader, csvFileFormat)) {
            csvRecordsList = csvFileParser.getRecords();
         } catch (IOException | IllegalArgumentException ioex) {
            log.error("IOException in CsvFileReader while parsing the input .csv file", ioex);
            throw new CSVRuntimeException(String.format("Error in parsing the input .csv file: '%s'", ioex.getMessage()));
         }
      } catch (NullPointerException npe) {
         log.error("Null pointer Exception in CsvFileReader while parsing the input .csv file. Exception messgae : '{}'", npe.getMessage());
         throw new CSVRuntimeException(FileValidationMessages.EQUIP_FV_ERR29.getErrMessageCode());
      }
      return csvRecordsList;
   }

   public static String getDelimiter(String delimiterInput) {
      String headerDelimiter = null;
      for (DelimiterInputTypes delimiter : DelimiterInputTypes.values()) {
         if (delimiter.getDelimterType().contains(delimiterInput)) {
            headerDelimiter = delimiter.getDelimterType();
            break;
         }
      }
      return headerDelimiter;
   }
}
