package com.pfizer.equip.shared.opmeta.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfizer.equip.shared.opmeta.SourceType;
import com.pfizer.equip.shared.opmeta.folder.BaseFolder;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_NULL)
@MappedSuperclass
public abstract class BaseNode implements Serializable {
   
   @Transient
   @JsonIgnore
   Map<String, Set<? extends BaseProtocolChildNode>> children = new HashMap<String, Set<? extends BaseProtocolChildNode>>();

   @Transient
   @JsonIgnore
   Set<BaseNode> directChildren = new HashSet<BaseNode>();

   @Transient
   @JsonInclude(Include.NON_NULL)
   @JsonProperty("opmeta:assignedUsers")
   Set<String> assignedUsers;

   @Column(name = "creation_ts")
   @JsonProperty("opmeta:sourceCreationTimestamp")
   protected Date sourceCreationTimestamp;

   @JsonProperty("opmeta:modified")
   @Transient
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // So strange, none of the other fields seem to need this. Only during PUTs maybe?
   Date modified;
   
   @JsonProperty("opmeta:modifiedBy")
   @Transient
   String modifiedBy;

   @JsonInclude()
   @Transient
   @JsonProperty("jcr:primaryType")
   String primaryType;

   @Transient
   BaseFolder nodeFolder;

   @JsonIgnore
   public final BaseFolder getFolder() {
      return nodeFolder;
   };

   @JsonIgnore
   public abstract String getPath();

   @JsonIgnore
   public Map<String, Set<? extends BaseProtocolChildNode>> getChildren() { return null; };

   @JsonIgnore
   public void clearChildEntities() { };
   
   @JsonIgnore
   public Set<? extends BaseNode> getDirectChildren() { return null; };

   public String getPrimaryType() {
      return primaryType;
   }

   // official getter for serialization
   public Set<String> getAssignedUsers() {
      return assignedUsers;
   }

   public void setAssignedUsers(Set<String> assignedUsers) {
      this.assignedUsers = assignedUsers;
   }

   // getter that forces a list for data manipulation
   @JsonIgnore
   public Set<String> getAssignedUsersList() {
      if (assignedUsers == null) {
         assignedUsers = new HashSet<String>();
      }
      return assignedUsers;
   }

   public void addAssignedUsers(Collection<String> assignedUsers) {
      this.assignedUsers.addAll(assignedUsers);
   }

   public void removeAssignedUsers(Collection<String> assignedUsers) {
      this.assignedUsers.removeAll(assignedUsers);
   }

   public Date getSourceCreationTimestamp() {
      return sourceCreationTimestamp;
   }

   public void setSourceCreationTimestamp(Date sourceCreationTimestamp) {
      this.sourceCreationTimestamp = sourceCreationTimestamp;
   }

   public Date getModified() {
      return modified;
   }

   public void setModified(Date modifiedAt) {
      this.modified = modifiedAt;
   }

   public String getModifiedBy() {
      return modifiedBy;
   }

   public void setModifiedBy(String modifiedBy) {
      this.modifiedBy = modifiedBy;
   }
}
