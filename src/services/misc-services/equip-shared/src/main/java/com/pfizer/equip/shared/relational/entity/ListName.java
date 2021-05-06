package com.pfizer.equip.shared.relational.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "list_name", schema = "equip_owner")
public class ListName {
   @Id
   @Column(name = "id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false, unique = true)
   private String name;

   @OneToMany()
   @JoinTable(name = "list", schema = "equip_owner", joinColumns = @JoinColumn(name = "list_name_id"), inverseJoinColumns = @JoinColumn(name = "list_value_id"))
   private List<ListValue> values;

   public ListName() {}

   public ListName(Long id, String name, List<ListValue> values) {
      super();
      this.id = id;
      this.name = name;
      this.values = values;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<ListValue> getValues() {
      return values;
   }

   public void setValues(List<ListValue> values) {
      this.values = values;
   }

}
