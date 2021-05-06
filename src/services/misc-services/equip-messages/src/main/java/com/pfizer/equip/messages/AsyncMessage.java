package com.pfizer.equip.messages;

/**
 * Represents an asynchronous message pushed by the server.
 * 
 * @author philip.lee
 *
 * @param <T>
 */
public interface AsyncMessage<T> extends Message<T> {

   /**
    * Acknowledge message reception.
    */
   void acknowledge();
   
   /**
    * Reject message reception.
    */
   void reject(boolean requeue);
}
