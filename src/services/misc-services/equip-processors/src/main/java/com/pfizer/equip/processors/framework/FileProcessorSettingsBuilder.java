package com.pfizer.equip.processors.framework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileProcessorSettingsBuilder implements ProcessorSettingsBuilder {
   private static final Logger logger = LoggerFactory.getLogger(FileProcessorSettingsBuilder.class);
   
   private static final String PROCESSOR_SETTINGS_PATH = "processor_settings";
   private static final String PROCESSOR_SETTINGS_DEFAULT = "ProcessorSettings.xml";

   @Override
   public ProcessorSettings get() {
      ProcessorSettingsParser parser = new ProcessorSettingsParser();
      try (InputStream in = getInputStream()) {
         return parser.parse(in);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static InputStream getInputStream() throws IOException {
      InputStream in = null;
      String path = System.getProperty(PROCESSOR_SETTINGS_PATH);
      if (StringUtils.isNotEmpty(path)) {
         logger.info("Using path {}", path);
         in = Files.newInputStream(Paths.get(path));
      } else {
         logger.info("Using classpath");
         in = ProcessorInitializer.class.getClassLoader().getResourceAsStream(PROCESSOR_SETTINGS_DEFAULT);
      }
      return in;
   }

}
