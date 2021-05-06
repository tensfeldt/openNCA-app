package com.pfizer.equip.services.business.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.stereotype.Service;

import com.pfizer.equip.services.input.search.SearchCriteria;
import com.pfizer.equip.services.input.search.SearchGroup;
import com.pfizer.equip.services.input.search.SearchInput;

@Service
public class DataSearchService extends SearchService {
   
   @Override
   protected String buildSql(SearchInput input) {
      String sql = "SELECT %s FROM %s WHERE item.[equip:equipId] LIKE '%%' AND (%s)";
      String selectPart = generateSelectSql(modeShapeProperties.getQuerySelectColumns());
      if (input.getCriteriaGroups().size() > 1) {
         throw new RuntimeException("Data search requires only 1 Criteria Group");
      }
      if (input.getTypesToSearch().size() > 1) {
         throw new RuntimeException("Data search supports only 1 type");
      }
      SearchGroup group = input.getCriteriaGroups().get(0);
      return String.format(sql, selectPart, getFromClause(group, new MutableInt(), input.getTypesToSearch().get(0)), getWhereClause(group, new MutableInt()));

   }

   private static String getWhereClause(SearchGroup group, MutableInt counter) {
      String where = "";
      int criteriaIndex = 0;
      if (group.getCriteria() != null) {
         for (SearchCriteria criteria : group.getCriteria()) {
            if (counter.getValue() == 0) {
               where = "(cc.[cell:name] = '" + criteria.getField() + "' AND cr.[cell:data] " + criteria.getOperator() + " "
                     + getValue(criteria.getOperator(), criteria.getValue()) + ")";
            } else {
               where += ((criteriaIndex > 0) ? (" " + group.getMode() + " ") : ("")) + "(cc" + counter.getValue() + ".[cell:name] = '" + criteria.getField() + "' AND cr"
                     + counter.getValue() + ".[cell:data] " + criteria.getOperator() + " " + getValue(criteria.getOperator(), criteria.getValue()) + ")";
            }
            counter.increment();
            criteriaIndex++;
         }
      }

      if (group.getSubCriteriaGroups() != null) {
         for (SearchGroup subGroup : group.getSubCriteriaGroups()) {
            if (counter.getValue() == 0 || criteriaIndex == 0) {
               where += "(" + getWhereClause(subGroup, counter) + ")";
            } else {
               where += " " + group.getMode() + " (" + getWhereClause(subGroup, counter) + ")";
            }
            criteriaIndex++;
         }
      }
      return where;
   }

   private static String getValue(String operator, String value) {
      if (StringUtils.equalsIgnoreCase(operator, "IN")) {
         String[] values = value.split(",");
         String returnValue = "(";
         int size = values.length;
         for (String v : values) {
            returnValue += "'" + v.trim() + "'";
            if (--size > 0) {
               returnValue += ", ";
            }
         }
         return returnValue + ")";
      } else {
         return "'" + value + "'";
      }
   }

   private static String getFromClause(SearchGroup group, MutableInt counter, String type) {
      String from = "";
      if (group.getCriteria() != null) {
         for (@SuppressWarnings("unused")
         SearchCriteria criteria : group.getCriteria()) {
            if (counter.getValue() == 0) {
               from = "[cell:column] AS cc JOIN [cell:row] as cr ON ISCHILDNODE(cr, cc) JOIN [" + type + "] AS item ON ISCHILDNODE(cc, item)";
            } else {
               from += " JOIN [cell:column] AS cc" + counter.getValue() + " ON ISCHILDNODE(cc" + counter.getValue() + ", item) JOIN [cell:row] AS cr" + counter.getValue()
                     + " ON ISCHILDNODE(cr" + counter.getValue() + ", cc" + counter.getValue() + ")";
            }
            counter.increment();
         }
      }
      if (group.getSubCriteriaGroups() != null) {
         for (SearchGroup subGroup : group.getSubCriteriaGroups()) {
            from += " " + getFromClause(subGroup, counter, type);
         }
      }
      return from;
   }

//   public static void main(String[] args) {
//      SearchInput search = new SearchInput();
//      
//      SearchGroup group = new SearchGroup();
//      search.setCriteriaGroups(Arrays.asList(group));
//      group.setMode(SearchMode.MODE_AND);
//      List<SearchCriteria> mainCriterias = new ArrayList<>();
//      SearchCriteria crit = new SearchCriteria();
//      crit.setField("TestField");
//      crit.setOperator("=");
//      crit.setValue("MyValue");
//      mainCriterias.add(crit);
//      // group.setCriteria(mainCriterias);
//
//      SearchGroup subNoCriteria = new SearchGroup();
//      subNoCriteria.setMode(SearchMode.MODE_OR);
//      group.setSubCriteriaGroups(Arrays.asList(subNoCriteria));
//
//      SearchGroup subCriteria1 = new SearchGroup();
//      subCriteria1.setMode(SearchMode.MODE_AND);
//      subCriteria1.setCriteria(Arrays.asList(crit, crit));
//      SearchGroup subCriteria2 = new SearchGroup();
//      subCriteria2.setMode(SearchMode.MODE_AND);
//      subCriteria2.setCriteria(mainCriterias);
//      subNoCriteria.setSubCriteriaGroups(Arrays.asList(subCriteria1, subCriteria2));
//
//      System.out.println(getFromClause(group, new MutableInt(), "equip:dataframe"));
//      String x = "adsf % adf";
//      String.format(x, "asfd");
//   }
}
