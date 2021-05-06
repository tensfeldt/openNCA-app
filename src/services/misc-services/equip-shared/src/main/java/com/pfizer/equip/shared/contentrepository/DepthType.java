package com.pfizer.equip.shared.contentrepository;

/**
 * Entity types used in security tables.
 */
public enum DepthType {
   WITH_TOP_LEVEL(0),
   WITH_DIRECT_CHILDREN(1),
   WITH_FOLDERS_AND_CHILD_RECORDS(2),
   WITH_SUBFOLDERS_AND_GRANDCHILD_RECORDS(3);

   private final int value;

   private DepthType(int value) {
      this.value = value;
   }

   public int getValue() {
      return value;
   }

   @Override public String toString() {
      return String.valueOf(value);
   }
}