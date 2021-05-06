package com.pfizer.equip.messages;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * RabbitMQ implementation of the GetMessageResult interface. Instances of this
 * class hold a reference to the RabbitMQ Connection and Channel objects to
 * handle message acknowledgement. It is important to call the close method to
 * release these resources.
 * 
 * @author philip.lee
 *
 * @param <T>
 *           The Type of the retrieved message.
 */
public class RabbitMQMessagesResult<T> implements GetMessagesResult<T> {

   private Channel channel;
   private List<RabbitMQMessage<T>> results;
   private Connection conn;

   private static final Logger logger = LoggerFactory.getLogger(RabbitMQMessagesResult.class);

   RabbitMQMessagesResult(Connection conn, Channel channel, List<RabbitMQMessage<T>> results) {
      this.channel = Objects.requireNonNull(channel);
      this.results = Objects.requireNonNull(results);
      this.conn = Objects.requireNonNull(conn);
   }

   @Override
   public void close() throws Exception {
      try {
         channel.close();
      } catch (Exception e1) {
         logger.error("Could not close channel", e1);
      }

      try {
         conn.close();
      } catch (Exception e1) {
         logger.error("Could not close connection", e1);
      }

   }

   @Override
   public List<? extends Message<T>> messages() {
      return results;
   }

   @Override
   public void acknowledge(Message<T> message) {
      try {
         channel.basicAck(message.getReferenceId(), false);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

   }

}
