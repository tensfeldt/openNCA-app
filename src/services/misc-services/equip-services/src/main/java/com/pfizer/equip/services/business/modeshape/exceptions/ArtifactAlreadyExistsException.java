package com.pfizer.equip.services.business.modeshape.exceptions;

@SuppressWarnings("serial")
public class ArtifactAlreadyExistsException extends RuntimeException {
   public ArtifactAlreadyExistsException() {}

   public ArtifactAlreadyExistsException(String msg) {
      super(msg);
   }

   public ArtifactAlreadyExistsException(String msg, Throwable t) {
      super(msg, t);
   }
}
