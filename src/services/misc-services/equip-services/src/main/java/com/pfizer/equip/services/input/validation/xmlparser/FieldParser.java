package com.pfizer.equip.services.input.validation.xmlparser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class FieldParser {

   @XmlElement(name = "ColumnName")
   private ColumnNameParser columnName;

   @XmlElement(name = "ColumnAlias")
   private ColumnAliasParser columnAlias;

   @XmlElement(name = "ColumnMapping")
   private String columnMapping;

   @XmlElement(name = "ExtraSpacesHeader")
   private ExtraSpacesHeaderParser extraSpacesHeaderParser;

   @XmlElement(name = "ExtraSpacesValue")
   private ExtraSpacesValueParser extraSpacesValueParser;

   @XmlElement(name = "Index")
   private IndexParser index;

   @XmlElement(name = "DataType")
   private DataTypeParser dataType;

   @XmlElement(name = "MinLength")
   private MinLengthParser minLength;

   @XmlElement(name = "MaxLength")
   private MaxLengthParser maxLength;

   @XmlElement(name = "MinRange")
   private MinRangeParser minRange;

   @XmlElement(name = "MaxRange")
   private MaxRangeParser maxRange;

   @XmlElement(name = "IsNull")
   private NullCheckParser isNull;

   @XmlElement(name = "Required")
   private MandatoryCheckParser required;

   @XmlElement(name = "AllowedValues")
   private AllowedValuesParser allowedValues;

   public ColumnNameParser getColumnName() {
      return columnName;
   }

   public void setColumnName(ColumnNameParser columnName) {
      this.columnName = columnName;
   }

   public ExtraSpacesHeaderParser getExtraSpacesHeaderParser() {
      return extraSpacesHeaderParser;
   }

   public void setExtraSpacesHeaderParser(ExtraSpacesHeaderParser extraSpacesHeaderParser) {
      this.extraSpacesHeaderParser = extraSpacesHeaderParser;
   }

   public ExtraSpacesValueParser getExtraSpacesValueParser() {
      return extraSpacesValueParser;
   }

   public void setExtraSpacesValueParser(ExtraSpacesValueParser extraSpacesValueParser) {
      this.extraSpacesValueParser = extraSpacesValueParser;
   }

   public IndexParser getIndex() {
      return index;
   }

   public void setIndex(IndexParser index) {
      this.index = index;
   }

   public DataTypeParser getDataType() {
      return dataType;
   }

   public void setDataType(DataTypeParser dataType) {
      this.dataType = dataType;
   }

   public MinLengthParser getMinLength() {
      return minLength;
   }

   public void setMinLength(MinLengthParser minLength) {
      this.minLength = minLength;
   }

   public MaxLengthParser getMaxLength() {
      return maxLength;
   }

   public void setMaxLength(MaxLengthParser maxLength) {
      this.maxLength = maxLength;
   }

   public MinRangeParser getMinRange() {
      return minRange;
   }

   public void setMinRange(MinRangeParser minRange) {
      this.minRange = minRange;
   }

   public MaxRangeParser getMaxRange() {
      return maxRange;
   }

   public void setMaxRange(MaxRangeParser maxRange) {
      this.maxRange = maxRange;
   }

   public NullCheckParser getIsNull() {
      return isNull;
   }

   public void setIsNull(NullCheckParser isNull) {
      this.isNull = isNull;
   }

   public MandatoryCheckParser getRequired() {
      return required;
   }

   public void setRequired(MandatoryCheckParser required) {
      this.required = required;
   }

   public AllowedValuesParser getAllowedValues() {
      return allowedValues;
   }

   public void setAllowedValues(AllowedValuesParser allowedValues) {
      this.allowedValues = allowedValues;
   }

   public ColumnAliasParser getColumnAlias() {
      return columnAlias;
   }

   public void setColumnAlias(ColumnAliasParser columnAlias) {
      this.columnAlias = columnAlias;
   }

   public String getColumnMapping() {
      return columnMapping;
   }

   public void setColumnMapping(String columnMapping) {
      this.columnMapping = columnMapping;
   }

}
