package com.pfizer.equip.services.business.search;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pfizer.equip.services.business.modeshape.nodes.SearchResultsNode;
import com.pfizer.equip.services.input.search.SearchCriteria;
import com.pfizer.equip.services.input.search.SearchGroup;
import com.pfizer.equip.services.input.search.SearchInput;
import com.pfizer.equip.services.input.search.SearchMode;
import com.pfizer.equip.services.input.search.SearchOrdering;
import com.pfizer.equip.services.properties.ModeShapeServiceProperties;
import com.pfizer.equip.services.responses.search.SearchResponse;

@Service
public class SearchService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   protected ModeShapeServiceProperties modeShapeProperties;

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   private RestTemplate restTemplate; // not a bean

   private String baseUrl;

   @PostConstruct
   private void initialize() {
      String serviceUser = modeShapeProperties.getUser();
      String servicePassword = modeShapeProperties.getPassword();
      baseUrl = modeShapeProperties.getUrl() + modeShapeProperties.getRepository() + "/" + modeShapeProperties.getWorkspace() + "/";
      restTemplate = restTemplateBuilder.basicAuthorization(serviceUser, servicePassword).build();
   }

   protected String generateSelectSql(String querySelectColumns) {
      String selectSql = "";
      for (String column : querySelectColumns.split(",")) {
         selectSql += "item." + column.trim() + ", ";
      }

      // trim trailing ', '
      selectSql = selectSql.substring(0, selectSql.length() - 2);
      return selectSql;
   }

   private String generateJoinSql(SearchInput input, String primaryType) {
      String joinSql = "JOIN [equip:comment] AS comment ON ISDESCENDANTNODE(comment, item) " + 
            "JOIN [nt:resource] AS content ON ISDESCENDANTNODE(content, item)";
      switch (primaryType) {
         case "equip:dataframe":
         case "equip:assembly":
            if (input.getFullTextQuery() != null && input.getFullTextQuery().getTextValues() != null && input.getFullTextQuery().getTextValues().size() > 0) {
               joinSql += " JOIN [nt:resource] AS r ON ISDESCENDANTNODE(r, item)";
            }
         default:
            break;
      }
      
      return joinSql;
   }

   private String joinOPMetaSql(String opMetaFieldRef, String opMetaField, String type, String operator, String value) {
      return "item.[" + opMetaFieldRef + "] IN (SELECT t.[jcr:uuid] " + "FROM [" + type + "] AS t WHERE t.[" + opMetaField + "] " + operator + " '" + value + "')";
   }

   private String generateCriteriaSql(SearchGroup searchGroup) {
      // base case, return nothing
      if (searchGroup == null) {
         return "";
      }

      String subWhereClause = "";
      subWhereClause += "(";
      String mode = searchGroup.getMode() == SearchMode.MODE_AND ? "AND" : "OR";
      for (SearchCriteria criterion : searchGroup.getCriteria()) {
         String field = criterion.getField();
         if (StringUtils.equalsIgnoreCase(field, "opmeta:studyId")) {
            subWhereClause += joinOPMetaSql("equip:protocolIds", "opmeta:studyId", "opmeta:protocol", criterion.getOperator(), criterion.getValue()) + " " + mode + " ";
         } else if (StringUtils.equalsIgnoreCase(field, "opmeta:programCode")) {
            subWhereClause += joinOPMetaSql("equip:programIds", "opmeta:programCode", "opmeta:program", criterion.getOperator(), criterion.getValue()) + " " + mode + " ";
         } else {
            String operator = criterion.getOperator();
            String value = criterion.getValue();
            subWhereClause += "item.[" + field + "] " + operator + " '" + value + "' " + mode + " ";
         }

      }

      // handle each sub group recursively
      if (searchGroup.getSubCriteriaGroups() != null) {
         for (SearchGroup subSearchGroup : searchGroup.getSubCriteriaGroups()) {
            subWhereClause += generateCriteriaSql(subSearchGroup) + " " + mode + " ";
         }
      }

      // trim trailing 'AND' or 'OR'
      subWhereClause = subWhereClause.substring(0, subWhereClause.length() - (mode.length() + 2));
      subWhereClause += ")";

      return subWhereClause;
   }

   protected String buildSql(SearchInput input) {
      String sql = "";

      // loop through each type to search, and perform a UNION on the result sets
      for (String primaryType : input.getTypesToSearch()) {
         String whereClause = "";
         String orderByClause = "";

         // check to see if this is a folder child query or not
         if (StringUtils.isNotEmpty(input.getChildFolder())) {
            whereClause += "ISCHILDNODE(item, '" + input.getChildFolder() + "') AND ";
         }

         // check to see if this is a folder descendant query or not
         if (input.getDescendantFolders() != null && input.getDescendantFolders().size() > 0) {
            whereClause += "(";
            int size = input.getDescendantFolders().size();
            for (String folder : input.getDescendantFolders()) {
               whereClause += "ISDESCENDANTNODE(item, '" + folder + "')";
               if (--size > 0) {
                  whereClause += " OR ";
               }
            }
            whereClause += ") AND ";
         }

         // check to see if this is a full text query
         if (input.getFullTextQuery() != null && input.getFullTextQuery().getTextValues() != null && input.getFullTextQuery().getTextValues().size() > 0) {
            whereClause += "(";
            String mode = input.getFullTextQuery().getMode() == SearchMode.MODE_AND ? "AND" : "OR";
            for (String fullTextValue : input.getFullTextQuery().getTextValues()) {
               String typeRefValue = StringUtils.equalsIgnoreCase(primaryType, "equip:dataframe") || StringUtils.equalsIgnoreCase(primaryType, "equip:assembly") ? "r"
                     : "item";
               whereClause += "CONTAINS(" + typeRefValue + ".[jcr:data], '" + fullTextValue + "') " + mode + " ";
            }

            // trim trailing 'AND' or 'OR'
            whereClause = whereClause.substring(0, whereClause.length() - (mode.length() + 2));
            whereClause += ") AND ";
         }

         // parse out the criteria
         if (input.getCriteriaGroups() != null) {
            for (SearchGroup searchGroup : input.getCriteriaGroups())
               whereClause += generateCriteriaSql(searchGroup) + " AND ";
         }

         // check to see if there are any excluded types
         if (input.getExcludedTypes() != null) {
            for (String excludedType : input.getExcludedTypes()) {
               whereClause += "item.[jcr:primaryType] != '" + excludedType + "' AND ";
            }
         }

         // trim trailing AND
         if (whereClause.length() > 0) {
            whereClause = whereClause.substring(0, whereClause.length() - 5);
         }

         // parse out the ordering
         if (input.getOrdering() != null) {
            for (SearchOrdering ordering : input.getOrdering()) {
               String field = ordering.getField();
               String direction = ordering.getDirection();
               orderByClause += "item.[" + field + "] " + direction + ", ";
            }

            // trim trailing ,
            if (orderByClause.length() > 0) {
               orderByClause = orderByClause.substring(0, orderByClause.length() - 2);
               whereClause += " ORDER BY " + orderByClause;
            }
         }

         String formatSql = "SELECT %s FROM %s AS item %s WHERE %s";
         String selectSql = modeShapeProperties.getQuerySelectColumns();
         String joinSql = generateJoinSql(input, primaryType);
         sql += String.format(formatSql, selectSql, "[" + primaryType + "]", joinSql, whereClause) + " UNION ";
      }

      // trim trailing UNION
      sql = sql.substring(0, sql.length() - 7);
      return sql;
   }

   private void cleanupMap(Map<String, String> map) {
      Set<String> keys = new HashSet<String>(map.keySet());
      for (String key : keys) {
         if (!key.startsWith("item.")) {
            continue;
         }

         // remove the 'item.' prefix for each value
         String value = map.get(key);
         map.remove(key);
         key = key.substring(5);
         map.put(key, value);
      }
   }

   private void cleanupRowProperties(List<Map<String, String>> rows) {
      if (rows != null) {
         for (Map<String, String> row : rows) {
            cleanupMap(row);
         }
      }
   }

   public SearchResponse executeQuery(SearchInput input) throws UnsupportedEncodingException {
      String sql = buildSql(input);
      log.debug("Querying ModeShape with SQL {}...", sql);

      String url = baseUrl + "query";
      HttpHeaders headers = new HttpHeaders();
      MediaType mediaType = new MediaType("application", "jcr+sql2");
      headers.setContentType(mediaType);
      HttpEntity<String> entity = new HttpEntity<String>(sql, headers);

      SearchResultsNode searchResultsNode = restTemplate.postForObject(url, entity, SearchResultsNode.class);

      SearchResponse response = new SearchResponse();
      cleanupMap(searchResultsNode.getColumns());
      response.setColumns(searchResultsNode.getColumns());

      // clean up URLs in result set
      List<Map<String, String>> rows = searchResultsNode.getRows();

      // cleanup row property names to remove scoped prefixes
      cleanupRowProperties(rows);

      if (rows != null) {
         for (Map<String, String> row : rows) {
            row.remove("mode:uri");
            row.remove("jcr:versionHistory");
         }
         response.setRows(rows);
      }

      return response;
   }
}
