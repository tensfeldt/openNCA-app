package com.pfizer.equip.processors.framework;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.processors.framework.ProcessorDetail.Interval;
import com.pfizer.equip.processors.framework.ProcessorSettings.Holiday;

/**
 * 
 * @author plee
 *
 */
public class ProcessorSettingsParser {
   private static final Logger logger = LoggerFactory.getLogger(ProcessorSettingsParser.class);
   private ProcessorSettings settings;

   public ProcessorSettings parse(InputStream in) {
      XMLStreamReader reader = null;
      settings = new ProcessorSettings();
      try {
         reader = XMLInputFactory.newFactory().createXMLStreamReader(in);
         while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
            case XMLStreamConstants.START_ELEMENT:
               if (reader.getLocalName().equals("processor")) {
                  loadProcessor(reader);
               } else if (reader.getLocalName().equals("holiday")) {
                  loadHoliday(reader);
               }
            }
         }
         return settings;
      } catch (Exception e) {
         throw new RuntimeException(e.getMessage(), e);
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (XMLStreamException e) {
               logger.warn(e.getMessage(), e);
            }
         }
      }
   }

   private void loadProcessor(XMLStreamReader reader) throws XMLStreamException, ParseException {
      ProcessorDetail detail = new ProcessorDetail();
      detail.setName(reader.getAttributeValue(null, "name"));
      detail.setAutoStart(Boolean.valueOf(reader.getAttributeValue(null, "autoStart")));
      detail.setClassName(reader.getAttributeValue(null, "class"));
      detail.setDescription(reader.getAttributeValue(null, "description"));
      SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
      while (reader.hasNext()) {
         int event = reader.next();
         switch (event) {
         case XMLStreamConstants.START_ELEMENT:
            if (reader.getLocalName().equals("property")) {
               detail.addProperty(reader.getAttributeValue(null, "name"), reader.getAttributeValue(null, "value"));
            } else if (reader.getLocalName().equals("cron-expression")) {
               detail.addCronExpression(reader.getElementText());
            } else if (reader.getLocalName().equals("interval")) {
               Interval interval = new Interval();
               int seconds = Integer.parseInt(reader.getAttributeValue(null, "seconds"));
               String daysValue = reader.getAttributeValue(null, "days");
               Set<Integer> days = new HashSet<>();
               for (String dayValue : daysValue.split(",")) {
                  days.add(Integer.parseInt(dayValue));
               }
               Calendar start = Calendar.getInstance();
               Calendar end = Calendar.getInstance();
               start.setTime(formatter.parse(reader.getAttributeValue(null, "start")));
               end.setTime(formatter.parse(reader.getAttributeValue(null, "end")));
               interval.setDays(days);
               interval.setEndTime(end);
               interval.setSeconds(seconds);
               interval.setStartTime(start);
               detail.addInterval(interval);
            }
            break;
         case XMLStreamConstants.END_ELEMENT:
            if (reader.getLocalName().equals("processor")) {
               settings.addProcessorDetail(detail);
               return;
            }

         }
      }
   }

   private void loadHoliday(XMLStreamReader reader) throws XMLStreamException {
      Holiday holiday = new Holiday();
      holiday.setDay(Integer.parseInt(reader.getAttributeValue(null, "day")));
      holiday.setMonth(Integer.parseInt(reader.getAttributeValue(null, "month")));
      settings.addHoliday(holiday);
   }
}
