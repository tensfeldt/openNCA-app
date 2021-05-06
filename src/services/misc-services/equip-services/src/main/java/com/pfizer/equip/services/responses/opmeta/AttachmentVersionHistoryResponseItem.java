package com.pfizer.equip.services.responses.opmeta;

import com.pfizer.equip.shared.opmeta.entity.Attachment;

public class AttachmentVersionHistoryResponseItem {
   private int version;
   private String versionId;
   private Attachment attachment;

   public int getVersion() {
      return version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public String getVersionId() {
      return versionId;
   }

   public void setVersionId(String versionId) {
      this.versionId = versionId;
   }

   public Attachment getAttachment() {
      return attachment;
   }

   public void setAttachment(Attachment attachment) {
      this.attachment = attachment;
   }
}
