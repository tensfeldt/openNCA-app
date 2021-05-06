package com.pfizer.equip.messages;

/**
 * Represents an Inbound message.
 * 
 * @author philip.lee
 *
 * @param <T>
 *           Content of the message.
 */
public interface Message<T> {

   /**
    * Returns the actual message or content from the messaging server as an
    * instance represented by the parameter c.
    * 
    * @param c
    * @return
    */
   T getContent(Class<T> c);

   /**
    * Unique identifier representing the message from the messaging server.
    * 
    * @return
    */
   long getReferenceId();
}
