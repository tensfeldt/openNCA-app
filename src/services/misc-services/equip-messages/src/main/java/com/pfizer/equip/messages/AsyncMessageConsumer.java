package com.pfizer.equip.messages;

/**
 * Consumer interface to handle pushed messages from the server.
 * 
 * @author philip.lee
 *
 */
public interface AsyncMessageConsumer<T> {

   /**
    * Process the AsyncMessage instance pushed by the Messaging server.
    * 
    * @param message
    */
   void consume(AsyncMessage<T> message);

   /**
    * This method is called when the consumer is canceled. Resource clean up
    * operations should be called in this method.
    */
   void handleCancel();

   /**
    * This method is called when the Connection to the Messaging server is closed.
    * 
    * @param isError
    *           True if the connection was not closed by the client.
    */
   void handleShutdown(boolean isError);
}
