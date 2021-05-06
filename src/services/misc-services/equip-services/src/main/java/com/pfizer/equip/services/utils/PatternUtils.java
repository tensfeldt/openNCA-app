package com.pfizer.equip.services.utils;

public class PatternUtils {

   private PatternUtils() {
      super();
   }

   public static final String ALPHANUMERIC = "^[a-zA-Z0-9]+$"; // also we can use "^[[:alnum:]]+$"
   public static final String ALPHANUMERIC_WITHSPACE = "^[a-zA-Z0-9\\s]+$";
   public static final String ALPHANUMERIC_WITHHYPHEN = "^[a-zA-Z0-9-]+$";
   public static final String ALPHABETS_ONLY = "^[A-z]+$";
   // Integer (signed), decimal, exponents
   public static final String NUMBER = "^([-+]?\\d*\\.?\\d+)(?:[eE]([-+]?\\d+))?$";
   // Alpha Numeric with no special characters mentioned (< > , &)
   public static final String ALPHANUMERIC_NOSPLCHAR = "^[a-zA-Z0-9]+([^><,&]*)$";
   // Date in YYYY-MM-DD format
   public static final String PATTERN_DATE = "^\\d{4}-([0]{0,1}[1-9]|1[012])-([1-9]|([012][0-9])|(3[01]))$";

   // Time in HH24:MM format
   public static final String PATTERN_TIME = "^[012]{0,1}[0-9]:[0-6][0-9]$";

   // Date and Time in YYYY-MM-DD HH24:MM
   public static final String PATTERN_DATETIME = "^\\d{4}-([0]{0,1}[1-9]|1[012])-([1-9]|([012][0-9])|(3[01])) [012]{0,1}[0-9]:[0-6][0-9]$";

   // ARD_<STUDY>_<PKBDFLD>_<PKTERM>_<Date>.csv , Prefix can be either "ARD_" or "ard_" but not mixed case, i.e. "ArD_". Rest of filename can be lower/upper case
   public static final String FILENAME_ARD = "^((ARD_)|(ard_))([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(.csv)+$";

   // ARD_<STUDY>_<PKBDFLD>_<PKTERM>_<Date>_Specification.csv , Prefix can be either "ARD_" or "ard_" but not mixed case, i.e. "ArD_". Rest of filename can be lower/upper
   // case
   public static final String FILENAME_ARDSPEC = "^((ARD_)|(ard_))([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(_Specification.csv)+$";

   // <STUDY>_pkdefinitionfile_<PKTERM>_<PKBDFLD>_<DATE>.csv
   public static final String FILENAME_PPKDEF = "^([a-zA-Z0-9]+_)(pkdefinitionfile_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(.csv)+$";

   // <STUDY>_<PKTERM>_<PKBDFLD>_<DATE>_dft.csv
   public static final String FILENAME_FPKDEF_DRAFT = "^([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(_dft.csv)+$";

   // <STUDY>_<PKTERM>_<PKBDFLD>_<DATE>_final_dft.csv
   public static final String FILENAME_FPKDEF_FINALDRAFT = "^([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(_final_dft.csv)+$";

   // <STUDY>_<PKTERM>_<PKBDFLD>_<DATE>_fnl.csv
   public static final String FILENAME_FPKDEF_FINAL = "^([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(_fnl.csv)+$";

   // PKTDD_<STUDY>_<PKBDFLD>_<PKTERM>_<Date>.csv , Suffix can be either "_LCD" or "_lcd" but not mixed case, i.e. "_lCd". Rest of filename can be lower/upper case
   public static final String FILENAME_PKTDD = "((PKTDD_)|(pktdd_))([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(.csv)+$";

   // <STUDY>_<PKBDFLD>_<PKTERM>_<Date>_LCD.csv, Suffix can be either "_LPD" or "_lpd" but not mixed case, i.e. "_lPd". Rest of filename can be lower/upper case
   public static final String FILENAME_LCD = "^([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(((_LCD)|(_lcd)))(.csv)+$";

   // <STUDY>_<PKBDFLD>_<PKTERM>_<Date>_LPD.csv
   public static final String FILENAME_LPD = "^([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([a-zA-Z0-9]+_)([1-9]|([012][0-9])|(3[01]))([a-zA-Z]{3})\\d{4}(((_LPD)|(_lpd)))(.csv)+$";

}
