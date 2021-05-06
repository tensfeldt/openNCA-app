package com.pfizer.equip.services.business.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pfizer.equip.services.input.search.SearchCriteria;
import com.pfizer.equip.shared.relational.entity.AuditEntry;
import com.pfizer.equip.shared.relational.repository.AuditEntryRepository;

@Service
public class RelationalDatabaseTableReportService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   @Autowired
   private AuditEntryRepository auditEntryRepository;

   public List<AuditEntry> retrieveAuditEntries(List<SearchCriteria> searchCriteria) {

      return auditEntryRepository.findAll(new Specification<AuditEntry>() {

         @Override
         public Predicate toPredicate(Root<AuditEntry> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria searchCriterion : searchCriteria) {
               // create_date
               if (searchCriterion.getField().equalsIgnoreCase("create_date")) {
                  try {
                     // The input should be in the pattern YYYY-MM-DDTHH:mm:ss.SSSZ
                     Date formattedInputDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(searchCriterion.getValue());

                     Path<Tuple> tuple = root.<Tuple>get("createDate");
                     if (tuple.getJavaType().isAssignableFrom(Date.class)) {

                        Expression<Date> dateExpression = cb.function("TO_DATE", Date.class, root.get("createDate"));
                        switch (searchCriterion.getOperator()) {
                        case "=":
                           predicates.add(cb.equal(dateExpression, formattedInputDate));
                           break;
                        case ">":
                           predicates.add(cb.greaterThan(dateExpression, formattedInputDate));
                           break;
                        case "<":
                           predicates.add(cb.lessThan(dateExpression, formattedInputDate));
                           break;
                        default:
                           break;
                        }
                     }
                  } catch (ParseException e) {
                     log.error(String.format("Parsing exception occurred while parsing the input %s in retrieve audit entries.", searchCriterion.getValue()));
                     throw new RuntimeException(String.format("Parsing exception occurred while parsing the input %s in retrieve audit entries.", searchCriterion.getValue()),e);
                  }
               }

               // User_id
               if (searchCriterion.getField().equalsIgnoreCase("user_id")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("userId"), searchCriterion.getValue()));
                     break;
                  case "like":
                     predicates.add(cb.like(cb.lower(root.get("userId")), "%" + searchCriterion.getValue().toLowerCase() + "%"));
                     break;
                  default:
                     break;
                  }
               }

               // action
               if (searchCriterion.getField().equalsIgnoreCase("action")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("action"), searchCriterion.getValue()));
                     break;
                  case "like":
                     predicates.add(cb.like(cb.lower(root.get("action")), "%" + searchCriterion.getValue().toLowerCase() + "%"));
                     break;
                  default:
                     break;
                  }
               }

               // action_status
               if (searchCriterion.getField().equalsIgnoreCase("action_status")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("actionStatus"), searchCriterion.getValue()));
                     break;
                  default:
                     break;
                  }
               }
               // email_address
               if (searchCriterion.getField().equalsIgnoreCase("email_address")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("emailAddress"), searchCriterion.getValue()));
                     break;
                  case "like":
                     predicates.add(cb.like(cb.lower(root.get("emailAddress")), "%" + searchCriterion.getValue().toLowerCase() + "%"));
                     break;
                  default:
                     break;
                  }
               }

               // entity_id
               if (searchCriterion.getField().equalsIgnoreCase("entity_id")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("entityId"), searchCriterion.getValue()));
                     break;
                  default:
                     break;
                  }
               }

               // entity_type
               if (searchCriterion.getField().equalsIgnoreCase("entity_type")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("entityType"), searchCriterion.getValue()));
                     break;
                  default:
                     break;
                  }
               }

               // entity_version
               if (searchCriterion.getField().equalsIgnoreCase("entity_version")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("entityVersion"), searchCriterion.getValue()));
                     break;
                  default:
                     break;
                  }
               }

               // first_name
               if (searchCriterion.getField().equalsIgnoreCase("first_name")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("firstName"), searchCriterion.getValue()));
                     break;
                  case "like":
                     predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + searchCriterion.getValue().toLowerCase() + "%"));
                     break;
                  default:
                     break;
                  }
               }

               // last_name
               if (searchCriterion.getField().equalsIgnoreCase("last_name")) {
                  switch (searchCriterion.getOperator()) {
                  case "=":
                     predicates.add(cb.equal(root.get("lastName"), searchCriterion.getValue()));
                     break;
                  case "like":
                     predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + searchCriterion.getValue().toLowerCase() + "%"));
                     break;
                  default:
                     break;
                  }
               }

            }
            return cb.and(predicates.toArray(new Predicate[0]));
         }
      });

   }
}
