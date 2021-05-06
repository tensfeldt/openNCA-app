package com.pfizer.equip.services.input.validation.types;

public enum ValidationTypes {
   GENERAL, // Normal file validation refers to validateFileToSpecification in ValidationController end point
   CROSSFILECHILD, // Will be used to consider the values in SpecificationInput prefixed with "pks"
   CROSSFILEPARENT; // will be used to perform cross file validation (parent dependent on child)

}
