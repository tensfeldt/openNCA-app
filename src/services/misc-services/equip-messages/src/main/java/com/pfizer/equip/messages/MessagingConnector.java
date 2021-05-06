package com.pfizer.equip.messages;

import javax.validation.constraints.NotNull;

/**
 * Sends and receives messages from a Messaging server.
 * 
 * @author philip.lee
 *
 */
public interface MessagingConnector {

   /**
    * Submits a message to the messaging server.
    * 
    * @param content
    *           Object representing the content.
    * @param queueName
    *           The queue name in the messaging server.
    */
   <T> void sendMessage(@NotNull T content, @NotNull String queueName);

   /**
    * Returns all messages from the queue represented by the queueName parameter.
    * 
    * @param queueName
    * @return
    */
   <T> GetMessagesResult<T> getMessages(@NotNull String queueName);

   /**
    * Returns "limit" number of messages from the queue represented by the
    * queueName parameter. If limit is less than or equal to 0, this method returns
    * all messages from the queue.
    * 
    * @param queueName
    * @param limit
    * @return
    */
   <T> GetMessagesResult<T> getMessages(@NotNull String queueName, int limit);

   /**
    * Asynchronously process messages from the messaging server.
    * 
    * @param queueName
    * @param consumer
    * @return
    */
   <T> GetMessagesResultAsync getMesages(@NotNull String queueName, @NotNull AsyncMessageConsumer<T> consumer);

}
