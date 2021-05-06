package com.pfizer.equip.shared.relational.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "list_value", schema = "equip_owner")
public class ListValue {
   @Id
   @Column(name = "id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "display_text", nullable = false)
   private String displayText;

   @Column(name = "text", nullable = false)
   private String text;

   public ListValue() {}

   public ListValue(Long id, String displayText, String text) {
      super();
      this.id = id;
      this.displayText = displayText;
      this.text = text;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getDisplayText() {
      return displayText;
   }

   public void setDisplayText(String displayText) {
      this.displayText = displayText;
   }

   public String getText() {
      return text;
   }

   public void setText(String text) {
      this.text = text;
   }

}
