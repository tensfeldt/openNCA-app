package com.pfizer.equip.shared;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.pfizer.equip.shared.properties.DirectoryServiceProperties;
import com.pfizer.equip.shared.properties.OpmetaModeShapeServiceProperties;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;


@EnableConfigurationProperties({DirectoryServiceProperties.class, OpmetaModeShapeServiceProperties.class, SharedApplicationProperties.class})
@Configuration
public class SharedConfiguration {

}