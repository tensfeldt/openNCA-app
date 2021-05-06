package com.pfizer.equip.services.input;

import java.util.Map;

public interface InputBuilder {
   // common key constants
   public final static String KEY_USER_ID = "user_id";
   
   // notification request key constants
   public final static String KEY_EVENT_TYPE = "event_type";
   public final static String KEY_STUDY_ID = "study_id";
   public final static String KEY_PROGRAM_NUMBER = "program_number";
   public final static String KEY_NOTIFICATION_TYPE = "notification_type";
   public static final String KEY_COMPONENT_NAME = "component_name";
   public static final String KEY_EVENT_DETAIL = "event_detail";
   public static final String KEY_EMAIL = "email";
   public static final String KEY_ENTITY_ID = "entity_id";
   public static final String KEY_ENTITY_TYPE = "entity_type";
   public static final String KEY_ARTIFACT_ID = "artifact_id";
   
   // validation service key constants
   public static final String KEY_SPEC_PATH = "specificationPath";
   public static final String KEY_SPEC_ID = "specificationId";
   public static final String KEY_SPEC_TYPE = "specificationType";
   public static final String KEY_SPEC_VERSION = "specificationVersion";
   public static final String KEY_DELIMITER = "delimiter";
   public static final String KEY_FILE_TYPE = "fileType";
   //Cross file validation service key constants
   public static final String KEY_PKS_SPEC_PATH = "pksSpecificationPath";
   public static final String KEY_PKS_SPEC_TYPE = "pksSpecificationType";
   public static final String KEY_PKS_SPEC_VERSION = "pksSspecificationVersion";
   
   // app service key constants
   public static final String KEY_MAX_VALUES = "maxvalues";
   public static final String KEY_ASSEMBLY_ID = "assemblyId";
   
   // log input constants
   public final static String KEY_LOG_ACTION = "action";
   
   // methods to implement
   public AbstractInput build(String userId, Map<String, Object> inputs);
}
