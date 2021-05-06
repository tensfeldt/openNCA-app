package com.pfizer.equip.messages;

import java.io.IOException;
import java.util.Objects;

import com.rabbitmq.client.Channel;

/**
 * RabbitMQ implementation of the GetMessagesResultAsync interface.
 * 
 * @author philip.lee
 *
 */
public class RabbitMQMessagesResultAsync implements GetMessagesResultAsync {

   private Channel channel;
   private String consumerTag;

   RabbitMQMessagesResultAsync(Channel channel, String consumerTag) {
      this.channel = Objects.requireNonNull(channel);
      this.consumerTag = Objects.requireNonNull(consumerTag);
   }

   @Override
   public void cancel() {
      try {
         channel.basicCancel(consumerTag);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public String getId() {
      return consumerTag;
   }

}
