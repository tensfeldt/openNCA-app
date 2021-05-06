package com.pfizer.equip.services.business.opmeta;

import java.util.HashSet;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.opmeta.OperationalMetadataEventService;

@Ignore
@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OperationalMetadataEventServiceTest {
   // Not an official test yet, just use this for development/debugging.
   @Autowired
   private RepositoryService repositoryService;
   
   @Autowired 
   OperationalMetadataEventService operationalMetadataEventService;

   //TODO Need to update the below method
   //@Test
   public void test01() {
      Set<String> protocolPaths = new HashSet<String>();
      protocolPaths.add("/Programs/X258/Protocols/X2581003");
      operationalMetadataEventService.processMilestones(protocolPaths);
   }
}