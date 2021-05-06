package com.pfizer.equip.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

/**
 * RabbitMQ implementation of the AsyncMessage interface. Message
 * acknowledgement is handle at the Message level.
 * 
 * @author philip.lee
 *
 * @param <T>
 */
public class RabbitMQAsyncMessage<T> implements AsyncMessage<T> {

   private ObjectMapper mapper = new ObjectMapper();
   private String content;
   private Channel channel;
   private long deliveryTag;

   RabbitMQAsyncMessage(byte[] body, Channel channel, long deliveryTag) {
      try {
         this.content = new String(body, "UTF-8");
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }
      this.channel = Objects.requireNonNull(channel);
      this.deliveryTag = deliveryTag;

   }

   @Override
   public T getContent(Class<T> c) {
      try {
         return mapper.readValue(content, c);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public long getReferenceId() {
      return deliveryTag;
   }

   @Override
   public void acknowledge() {
      try {
         channel.basicAck(deliveryTag, false);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

   }

   @Override
   public void reject(boolean requeue) {
      try {
         channel.basicReject(deliveryTag, requeue);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      
   }

}
