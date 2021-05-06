package com.pfizer.equip.services.business.modeshape.nodes;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * This class represents a version history node in ModeShape.
 *
 */
@JsonInclude(Include.NON_ABSENT)
public class VersionHistoryNode extends BaseExistingNode {
   private Map<String, Map<String, Object>> children;
   
   public Map<String, Map<String, Object>> getChildren() {
      return children;
   }
   
   public void setChildren(Map<String, Map<String, Object>> children) {
      this.children = children;
   }
   
   public int getVersionCount() {
      // we know that the number of versions is the number of keys minus 2
      // since the version history always has 2 properties in addition to each version
      // - jcr:versionLabels: a node pointing at all version labels
      // - jcr:rootVersion: the 'root' version, which we don't use since it doesn't retain metadata or content history
      // - 1.0: the real 1st version
      // - 1.1: the 2nd version
      // ...etc
      int versionCount = children.keySet().size() - 2;
      return versionCount;
   }
   
   /**
    * 
    * @param versionNumber a version number starting at 1
    * @return a URL to that version's specific history
    * @throws UnsupportedEncodingException 
    */
   public String getVersionPath(long versionNumber) throws UnsupportedEncodingException {
      // TODO: figure out how to implement major versioning
      // currently each version will increment the minor version as such: 1.0, 1.1, 1.2, etc
      // since we are using the REST API, we can construct the path to the version node using this fact
      if (versionNumber < 1) {
         throw new IllegalArgumentException("The version number must be an integer greater than or equal to 1.");
      }
      String restVersion = String.valueOf((versionNumber - 1));
      String path = getSelf() + "/1." + restVersion + "/jcr:frozenNode";
      return path;
   }
}
