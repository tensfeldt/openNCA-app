package com.pfizer.equip.services.business.modeshape.nodes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * This class represents a folder node in ModeShape.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class FolderNode extends NewFolderNode {
   private String self = "";
   private String id = "";
   private Map<String, BaseExistingNode> children;

   public String getSelf() throws UnsupportedEncodingException {
      // ModeShape returns an encoded URL
      // we need to decode the URL here since the Sprint REST client will attempt to re-encode an already encoded URL
      return URLDecoder.decode(self, "UTF-8");
   }

   public String getId() {
      return id;
   }
   
   public Map<String, BaseExistingNode> getChildren() {
      return children;
   }
   
   public void setChildren(Map<String, BaseExistingNode> children) {
      this.children = children;
   }
}
