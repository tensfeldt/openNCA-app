package com.pfizer.equip.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.messages.properties.MessagesProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * RabbitMQ implementation of the MessagingConnector interface. By default this
 * class serializes Java objects to JSON using the Jackson parser.
 * 
 * @author philip.lee
 *
 */
@Service
public class RabbitMQConnector implements MessagingConnector {

   private static final Logger logger = LoggerFactory.getLogger(RabbitMQConnector.class);

   private ObjectMapper mapper = new ObjectMapper();
   
   @Autowired
   private MessagesProperties properties;

   @Override
   public <T> void sendMessage(T content, String queueName) {
      Objects.requireNonNull(content);
      Objects.requireNonNull(queueName);
      ConnectionFactory factory = getConnectionFactory();
      try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel();) {
         channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, mapper.writeValueAsString(content).getBytes("UTF-8"));
      } catch (IOException | TimeoutException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public <T> GetMessagesResult<T> getMessages(String queueName) {
      return getMessages(queueName, 0);
   }

   @Override
   public <T> GetMessagesResult<T> getMessages(String queueName, int limit) {
      Objects.requireNonNull(queueName);
      ConnectionFactory factory = getConnectionFactory();
      Connection connection = null;
      Channel channel = null;
      try {
         connection = factory.newConnection();
         channel = connection.createChannel();
         boolean autoAck = false;
         GetResponse response = null;
         List<RabbitMQMessage<T>> messages = new ArrayList<>();
         while ((response = channel.basicGet(queueName, autoAck)) != null) {
            RabbitMQMessage<T> message = new RabbitMQMessage<T>(response);
            messages.add(message);
            if (limit != 0 && messages.size() == limit) {
               break;
            }
         }
         RabbitMQMessagesResult<T> messagesResults = new RabbitMQMessagesResult<T>(connection, channel, messages);
         return messagesResults;
      } catch (Exception e) {
         closeResources(channel, connection);
         throw new RuntimeException(e);
      }
   }

   @Override
   public <T> GetMessagesResultAsync getMesages(String queueName, AsyncMessageConsumer<T> consumer) {
      Objects.requireNonNull(queueName);
      Objects.requireNonNull(consumer);
      ConnectionFactory factory = getConnectionFactory();
      Connection connection = null;
      Channel channel = null;
      boolean autoAck = false;
      try {
         connection = factory.newConnection();
         channel = connection.createChannel();
         channel.basicQos(1);
         String consumerTag = channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
               try {
                  AsyncMessage<T> message = new RabbitMQAsyncMessage<T>(body, getChannel(), envelope.getDeliveryTag());
                  consumer.consume(message);
               } catch (Exception e) {
                  logger.error("Exception in channel " + getChannel().getChannelNumber() + " by AsyncMessageConsumer", e);
               }
            }

            @Override
            public void handleCancelOk(String consumerTag) {
               consumer.handleCancel();
               closeResources();
            }

            @Override
            public void handleCancel(String consumerTag) {
               consumer.handleCancel();
               closeResources();
            }

            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
               consumer.handleShutdown(sig.isInitiatedByApplication() == false);
               if (!sig.isHardError()) {
                  Connection connection = getChannel().getConnection();
                  try {
                     connection.close();
                  } catch (Exception e1) {
                     logger.error("Could not close connection", e1);
                  }
               }
            }

            private void closeResources() {
               Connection connection = getChannel().getConnection();
               try {
                  getChannel().close();
               } catch (Exception e1) {
                  logger.error("Could not close channel", e1);
               }
               try {
                  connection.close();
               } catch (Exception e1) {
                  logger.error("Could not close connection", e1);
               }
            }
         });
         RabbitMQMessagesResultAsync async = new RabbitMQMessagesResultAsync(channel, consumerTag);
         return async;
      } catch (Exception e) {
         closeResources(channel, connection);
         throw new RuntimeException(e);
      }
   }

   private ConnectionFactory getConnectionFactory() {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(properties.getHost());
      factory.setPort(properties.getPort());
      if (StringUtils.isNotBlank(properties.getUser())) {
         factory.setUsername(properties.getUser());
      }
      if (StringUtils.isNotBlank(properties.getPassword())) {
         factory.setPassword(properties.getPassword());
      }
      return factory;
   }

   private static void closeResources(Channel channel, Connection conn) {
      if (channel != null) {
         try {
            channel.close();
         } catch (Exception e1) {
            logger.error("Could not close channel", e1);
         }
      }
      if (conn != null) {
         try {
            conn.close();
         } catch (Exception e1) {
            logger.error("Could not close connection", e1);
         }
      }
   }

}
