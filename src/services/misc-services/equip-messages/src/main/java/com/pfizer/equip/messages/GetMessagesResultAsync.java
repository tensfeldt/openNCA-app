package com.pfizer.equip.messages;

/**
 * Represents an async operation.
 * 
 * @author philip.lee
 *
 */
public interface GetMessagesResultAsync {

   /**
    * Cancel the AsyncMessageConsumer passed to the MessagegingConnector instance.
    */
   void cancel();

   /**
    * Returns a unique Id identifying the AsyncMessageConsumer handle.
    * 
    * @return
    */
   String getId();
}
