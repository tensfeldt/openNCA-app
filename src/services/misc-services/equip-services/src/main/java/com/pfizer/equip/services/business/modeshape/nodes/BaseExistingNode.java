package com.pfizer.equip.services.business.modeshape.nodes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * This class represents the base for a node that exists in ModeShape, i.e. not for nodes that have yet to be created.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class BaseExistingNode extends BaseLibraryArtifactNode {
   // base ModeShape properties in any node type that point to various parts of the ModeShape graph
   private String self;
   
   private String up;
   
   private String id;

   public String getSelf() throws UnsupportedEncodingException {
      // ModeShape returns an encoded URL
      // we need to decode the URL here since the Sprint REST client will attempt to re-encode an already encoded URL
      return URLDecoder.decode(self, "UTF-8");
   }

   public void setSelf(String self) {
      this.self = self;
   }

   public String getUp() throws UnsupportedEncodingException {
      // ModeShape returns an encoded URL
      // we need to decode the URL here since the Sprint REST client will attempt to re-encode an already encoded URL
      return URLDecoder.decode(up, "UTF-8");
   }

   public void setUp(String up) {
      this.up = up;
   }

   public String getId() throws UnsupportedEncodingException {
      // ModeShape returns an encoded URL
      // we need to decode the URL here since the Sprint REST client will attempt to re-encode an already encoded URL
      return URLDecoder.decode(id, "UTF-8");
   }

   public void setId(String id) {
      this.id = id;
   }

}
