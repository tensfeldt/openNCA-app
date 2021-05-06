package com.pfizer.equip.services.responses.validation;

import java.util.List;

import com.pfizer.equip.services.business.validation.Specification;
import com.pfizer.equip.shared.responses.AbstractResponse;

public class GetSpecificationsResponse extends AbstractResponse {
   private List<Specification> specifications;

   public List<Specification> getSpecifications() {
      return specifications;
   }

   public void setSpecifications(List<Specification> specifications) {
      this.specifications = specifications;
   }
}
