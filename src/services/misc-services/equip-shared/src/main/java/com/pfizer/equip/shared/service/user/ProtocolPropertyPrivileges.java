package com.pfizer.equip.shared.service.user;

import java.util.HashMap;

@SuppressWarnings("serial")
public class ProtocolPropertyPrivileges extends HashMap<String, PrivilegeType> {
   public ProtocolPropertyPrivileges() {
      this.put("studyRestrictionStatus", PrivilegeType.ALTER_PROTOCOL_RESTRICTION);
      this.put("studyBlindingStatus", PrivilegeType.ALTER_PROTOCOL_BLINDING);
      this.put("studyBlindingDescription", PrivilegeType.ALTER_PROTOCOL_BLINDING);
      this.put("clinicalDataBlindingRequired", PrivilegeType.ALTER_PROTOCOL_BLINDING);
      this.put("treatmentDataBlindingRequired", PrivilegeType.ALTER_PROTOCOL_BLINDING);
      this.put("studyBlindingStatusDate", PrivilegeType.ALTER_PROTOCOL_BLINDING);
      this.put("crfDataStatus", PrivilegeType.ALTER_PROTOCOL_CRF_DATA_STATUS);
      this.put("sdComments", PrivilegeType.ALTER_PROTOCOL_SD_COMMENTS);
   }
}