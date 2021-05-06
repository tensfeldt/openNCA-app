package com.pfizer.equip.messages;

import java.util.List;

/**
 * Container for Messages. Handles message acknowledgement and clean up of the
 * Messaging server resources.
 * 
 * @author philip.lee
 *
 * @param <T>
 */
public interface GetMessagesResult<T> extends AutoCloseable {

   /**
    * Returns the list of messages from the message result.
    * 
    * @return
    */
   List<? extends Message<T>> messages();

   /**
    * Acknowledge that the message was received and processed successfully.
    * 
    * @param message
    */
   void acknowledge(Message<T> message);
}
