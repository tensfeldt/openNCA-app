package com.pfizer.equip.services.input.validation.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "FieldDefinitions")
@XmlAccessorType(XmlAccessType.FIELD)
public class FieldDefinitionsParser {

   @XmlElement(name = "FileNamePattern")
   private FileNamePatternParser fileNamePattern;

   @XmlElement(name = "Field")
   private List<FieldParser> fieldInputList;

   @XmlElement(name = "FieldSets")
   private List<FieldSetsParser> fieldSetsInput;

   @XmlElement(name = "UniqueSets")
   private UniqueSetsParser uniqueSetsParser;

   @XmlElement(name = "FileLevelRules")
   private List<FileLevelRulesParser> fileLevelRulesParser;

   @XmlElement(name = "SDEIDSets")
   private SDEIDSetsParser sdeidSetsParser;

   @XmlElement(name = "CrossFileFieldSets")
   private CrossFileFieldSetsParser crossFileFieldSetsParser;

   @XmlElement(name = "ExtraneousColumns")
   private ExtraneousColumnsParser extraneousColumnsParser;

   @XmlElement(name = "DuplicateRecords")
   private DuplicateRecordsParser duplicateRecordsParser;

   @XmlElement(name = "EmptyLines")
   private EmptyLinesParser emptyLinesParser;

   @XmlElement(name = "EmptyColumns")
   private EmptyColumnsParser emptyColumnsParser;

   @XmlElement(name = "ColumnOrdering")
   private ColumnOrderingParser columnOrderingParser;

   @XmlElement(name = "SubjectIdColumnName")
   private String SubjectIdColumnName;

   @XmlElement(name = "PkTermColumnName")
   private String pkTermColumnName;

   public FileNamePatternParser getFileNamePattern() {
      return fileNamePattern;
   }

   public void setFileNamePattern(FileNamePatternParser fileNamePattern) {
      this.fileNamePattern = fileNamePattern;
   }

   public List<FieldParser> getFieldInputList() {
      return fieldInputList;
   }

   public void setFieldInputList(List<FieldParser> fieldInputList) {
      this.fieldInputList = fieldInputList;
   }

   public List<FieldSetsParser> getFieldSetsInput() {
      return fieldSetsInput;
   }

   public void setFieldSetsInput(List<FieldSetsParser> fieldSetsInput) {
      this.fieldSetsInput = fieldSetsInput;
   }

   public UniqueSetsParser getUniqueSetsParser() {
      return uniqueSetsParser;
   }

   public void setUniqueSetsParser(UniqueSetsParser uniqueSetsParser) {
      this.uniqueSetsParser = uniqueSetsParser;
   }

   public List<FileLevelRulesParser> getFileLevelRulesParser() {
      return fileLevelRulesParser;
   }

   public void setFileLevelRulesParser(List<FileLevelRulesParser> fileLevelRulesParser) {
      this.fileLevelRulesParser = fileLevelRulesParser;
   }

   public SDEIDSetsParser getSdeidSetsParser() {
      return sdeidSetsParser;
   }

   public void setSdeidSetsParser(SDEIDSetsParser sdeidSetsParser) {
      this.sdeidSetsParser = sdeidSetsParser;
   }

   public CrossFileFieldSetsParser getCrossFileFieldSetsParser() {
      return crossFileFieldSetsParser;
   }

   public void setCrossFileFieldSetsParser(CrossFileFieldSetsParser crossFileFieldSetsParser) {
      this.crossFileFieldSetsParser = crossFileFieldSetsParser;
   }

   public ExtraneousColumnsParser getExtraneousColumnsParser() {
      return extraneousColumnsParser;
   }

   public void setExtraneousColumnsParser(ExtraneousColumnsParser extraneousColumnsParser) {
      this.extraneousColumnsParser = extraneousColumnsParser;
   }

   public EmptyLinesParser getEmptyLinesParser() {
      return emptyLinesParser;
   }

   public void setEmptyLinesParser(EmptyLinesParser emptyLinesParser) {
      this.emptyLinesParser = emptyLinesParser;
   }

   public ColumnOrderingParser getColumnOrderingParser() {
      return columnOrderingParser;
   }

   public void setColumnOrderingParser(ColumnOrderingParser columnOrderingParser) {
      this.columnOrderingParser = columnOrderingParser;
   }

   public DuplicateRecordsParser getDuplicateRecordsParser() {
      return duplicateRecordsParser;
   }

   public void setDuplicateRecordsParser(DuplicateRecordsParser duplicateRecordsParser) {
      this.duplicateRecordsParser = duplicateRecordsParser;
   }

   public String getSubjectIdColumnName() {
      return SubjectIdColumnName;
   }

   public void setSubjectIdColumnName(String subjectIdColumnName) {
      SubjectIdColumnName = subjectIdColumnName;
   }

   public String getPkTermColumnName() {
      return pkTermColumnName;
   }

   public void setPkTermColumnName(String pkTermColumnName) {
      this.pkTermColumnName = pkTermColumnName;
   }

   public EmptyColumnsParser getEmptyColumnsParser() {
      return emptyColumnsParser;
   }

   public void setEmptyColumnsParser(EmptyColumnsParser emptyColumnsParser) {
      this.emptyColumnsParser = emptyColumnsParser;
   }
}
