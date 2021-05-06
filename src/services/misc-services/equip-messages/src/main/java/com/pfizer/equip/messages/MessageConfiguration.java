package com.pfizer.equip.messages;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.pfizer.equip.messages.properties.MessagesProperties;

/**
 * Configuration file to load the MessagesProperties class.
 * 
 * @see com.pfizer.equip.messages.properties.MessagesProperties
 * 
 * @author philip.lee
 *
 */

@Configuration
@EnableConfigurationProperties(MessagesProperties.class)
public class MessageConfiguration {
}
