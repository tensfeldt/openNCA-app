package com.pfizer.equip.services.responses.validation;

import com.pfizer.equip.services.business.validation.Specification;
import com.pfizer.equip.shared.responses.AbstractResponse;

public class GetSpecificationResponse extends AbstractResponse {
   private Specification specification;

   public Specification getSpecification() {
      return specification;
   }

   public void setSpecification(Specification specification) {
      this.specification = specification;
   }
}
