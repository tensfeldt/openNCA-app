package com.pfizer.equip.services.business.api.dataframe;

import com.pfizer.equip.services.business.api.input.CommentInput;

public class Promotion {
   public static final String TYPE_PROMOTED = "Promoted";
   public static final String TYPE_REVOKED = "Revoke/Fail";

   private String type = "promotion";
   private String dataStatus;
   private String dataBlindingStatus;
   private String promotionStatus;
   private CommentInput[] comments;

   public String getType() {
      return type;
   }

   public String getDataStatus() {
      return dataStatus;
   }

   public void setDataStatus(String dataStatus) {
      this.dataStatus = dataStatus;
   }

   public String getDataBlindingStatus() {
      return dataBlindingStatus;
   }

   public void setDataBlindingStatus(String dataBlindingStatus) {
      this.dataBlindingStatus = dataBlindingStatus;
   }

   public String getPromotionStatus() {
      return promotionStatus;
   }

   public void setPromotionStatus(String promotionStatus) {
      this.promotionStatus = promotionStatus;
   }

   public CommentInput[] getComments() {
      return comments;
   }

   public void setComments(CommentInput[] comments) {
      this.comments = comments;
   }
}
