package com.pfizer.equip.messages;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.GetResponse;

/**
 * Inbound messages retrieved from RabbitMQ. The class assumes the message is
 * stored in JSON format.
 * 
 * @author philip.lee
 *
 * @param <T>
 *           Content of the message.
 */
public class RabbitMQMessage<T> implements Message<T> {

   private GetResponse message;
   private ObjectMapper mapper = new ObjectMapper();

   RabbitMQMessage(GetResponse message) {
      this.message = Objects.requireNonNull(message);
   }

   @Override
   public T getContent(Class<T> c) {
      try {
         return mapper.readValue(message.getBody(), c);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public long getReferenceId() {
      return message.getEnvelope().getDeliveryTag();
   }
}
