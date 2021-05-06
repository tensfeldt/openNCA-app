package com.pfizer.equip.services.business.modeshape.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * This class handles child nodes of BaseLibraryArtifactNode, including the equip:complexData structure and equip:comment.
 * It's primarily used for serialization when POST-ing and PUT-ing to the repository.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class BaseLibraryArtifactContentNode extends BaseLibraryArtifactNode {
   @JsonProperty("children")
   // This is a list of Maps in order to support the SNS JSON format. Consider refactoring if not needed, for simplicity.
   private List<Map<String, BaseLibraryArtifactNode>> children;
   
   private final String COMPLEX_DATA = "equip:complexData";
   // Strip out array brackets for SNS support. Can remove if no SNS for libraryArtifact:
   private final String ARRAY_MARKER = "\\[[0-9]+\\]$"; 

   public void setChildren(Map<String, BaseLibraryArtifactNode> children) {
      for (String childKey : children.keySet()) {
         if (!childKey.equals(COMPLEX_DATA)) { // Retrieved separately
            putChild(childKey.replaceAll(ARRAY_MARKER, ""), children.get(childKey));
         }
      }
   }

   public List<Map<String, BaseLibraryArtifactNode>> getChildren() {
      return children;
   }

   protected void putChild(String name, BaseLibraryArtifactNode child) {
      if (this.children == null) {
         this.children = new ArrayList<Map<String, BaseLibraryArtifactNode>>();
      }
      Map<String, BaseLibraryArtifactNode> childEntry = new HashMap<String, BaseLibraryArtifactNode>();
      childEntry.put(name, child);
      this.children.add(childEntry);
   }
   
   // Builds the expected content structure, should match LibrarianService.DATA_PATH
   public void setContent(String base64Content) {
      BaseLibraryArtifactContentNode complexData = new BaseLibraryArtifactContentNode();
      BaseLibraryArtifactContentNode content = new BaseLibraryArtifactContentNode();
      content.setPrimaryType("nt:resource");
      content.setEncodedContent(base64Content);
      complexData.setPrimaryType("nt:file");
      complexData.putChild("jcr:content", content);
      this.putChild(COMPLEX_DATA, complexData);
   }

   // Builds the expected child node structure for comment. There should only be one comment per artifact.
   public void setComments(String comments) {
      BaseLibraryArtifactNode comment = new BaseLibraryArtifactNode();
      comment.setPrimaryType("equip:comment");
      comment.setBody(comments);
      comment.setCommentType("default");
      this.putChild("equip:comment", comment);
   }
}