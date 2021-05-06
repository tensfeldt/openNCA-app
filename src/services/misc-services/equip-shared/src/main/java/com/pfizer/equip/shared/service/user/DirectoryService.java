package com.pfizer.equip.shared.service.user;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.shared.exceptions.UnsupportedFeatureException;
import com.pfizer.equip.shared.properties.DirectoryServiceProperties;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchExternalGroupException;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchUserException;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchUserPropertyException;

/**
 * Generic interface for communication with the central user directory.
 * <p>
 * Application-level concepts should not be employed here. When customizing,
 * this class can be replaced to support a different user information store
 * (different directory/interface).
 */
@Service
public class DirectoryService {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   // different sub URLs for different lookups
   private static final String NTID_LOOKUP = "person/ntid/%s";
   private static final String GROUP_LOOKUP = "pxed/%s/groups";
   // private static final String GROUP_USER_LOOKUP = "pxed/%s/users/%s"; // takes too long for big groups
   private static final String USERS_BY_GROUP_LOOKUP = "pxed/%s/users";

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   // apparently RestTemplate is not a bean, probably because each instance is tied
   // to a specific service
   private RestTemplate restTemplate;

   @Autowired
   private DirectoryServiceProperties serviceProperties;

   @Autowired
   private SharedApplicationProperties applicationProperties;

   private String baseUrl = null;

   @PostConstruct
   private void initialize() {
      // for now this service uses basic authentication
      // this may change in the future
      if (!applicationProperties.isStandaloneMode()) {
         String serviceUser = serviceProperties.getUser();
         String servicePassword = serviceProperties.getPassword();
         baseUrl = serviceProperties.getUrl();
         restTemplate = restTemplateBuilder.basicAuthorization(serviceUser, servicePassword).build();
      }
   }

   protected UserInfo lookupUser(String userId) throws JsonProcessingException, IOException {
      if (applicationProperties.isStandaloneMode()) {
         return new UserInfo(userId, "Jane", "Doe", "jane.doe@opennca.com", true);
      }

      String url = baseUrl + String.format(NTID_LOOKUP, userId);
      String response = null;
      log.debug("Lookup up user ID {} against user service URL {}", userId, url);
      try {
         response = restTemplate.getForObject(url, String.class);
      } catch (HttpClientErrorException e) {
         if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new NoSuchUserException(String.format("Could not find user %s in directory.", userId));
         } else {
            throw e;
         }
      }

      // parse response with Jackson
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response);

      String firstName;
      String lastName;
      String emailAddress;
      boolean isActive;

      if (root.get("person").has("firstName") && !root.get("person").get("firstName").asText().isEmpty()) {
         firstName = root.get("person").get("firstName").asText();
      } else if (userId.startsWith("atlamr-nca")) {
         firstName = "first-name-test";
      } else {
         log.warn("The field with key 'firstName' is missing or blank for user '{}', defaulting.", userId);
         firstName = "(blank)";
      }

      if (root.get("person").has("lastName") && !root.get("person").get("lastName").asText().isEmpty()) {
         lastName = root.get("person").get("lastName").asText();
      } else if (userId.startsWith("atlamr-nca")) {
         lastName = "last-name-test";
      } else {
         log.warn("The field with key 'lastName' is missing or blank for user '{}', defaulting.", userId);
         lastName = "(blank)";
      }

      if (root.get("person").has("emailAddress")) {
         emailAddress = root.get("person").get("emailAddress").asText();
      } else if (userId.startsWith("atlamr-nca")) {
         emailAddress = userId + "@invaliddomain.pfizer.com";
      } else {
         log.warn("The field with key 'emailAddress' is missing for user '{}'", userId);
         emailAddress = "";
      }

      if (root.get("person").has("activeIndicator")) {
         isActive = root.get("person").get("activeIndicator").asText().equals("Y") ? true : false;
      } else if (userId.startsWith("atlamr-nca")) {
         isActive = true;
      } else {
         throw new NoSuchUserPropertyException(String.format("The field with key 'activeIndicator' is missing for user '%s'", userId));
      }

      UserInfo userInfo = new UserInfo(userId, firstName, lastName, emailAddress, isActive);
      return userInfo;
   }

   // Get list of AD groups from Research Service, used by other methods that map
   // to app-level groups/roles
   public Set<String> getUserGroups(String userId) throws JsonProcessingException, IOException {
      if (applicationProperties.isStandaloneMode()) {
         throw new UnsupportedFeatureException("not supported in standalone mode");
      }
      // look up the extra user level information for storage into the database
      String url = baseUrl + String.format(GROUP_LOOKUP, userId);
      log.debug("Lookup up groups for user ID '{}' against user service URL '{}'", userId, url);
      String response = null;
      try {
         response = restTemplate.getForObject(url, String.class);
      } catch (HttpClientErrorException e) {
         if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new NoSuchUserException(String.format("Could not find user %s in directory.", userId));
         } else {
            throw e;
         }
      }

      // parse response with Jackson
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response);
      JsonNode jsonGroups = root.get("userGroups").get("groups");
      Set<String> groups = new HashSet<String>();
      jsonGroups.forEach((group) -> {
         groups.add(group.asText());
      });

      return groups;
   }

   // Get a list of users in an external group, used by other methods that handle
   // the mapping/unmapping
   public Set<String> getUsersByGroup(String externalGroup) throws JsonProcessingException, IOException {
      if (applicationProperties.isStandaloneMode()) {
         return new HashSet<String>(Arrays.asList("standalone-user"));
      }
      // look up the extra user level information for storage into the database
      String url = baseUrl + String.format(USERS_BY_GROUP_LOOKUP, externalGroup);
      String response = null;
      log.debug("Lookup up users for group ID '{}' against user service URL '{}'", externalGroup, url);
      try {
         response = restTemplate.getForObject(url, String.class);
      } catch (HttpClientErrorException e) {
         if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new NoSuchExternalGroupException(String.format("Could not find group %s in directory.", externalGroup));
         } else {
            throw e;
         }
      }

      // parse response with Jackson
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response);
      JsonNode jsonUsers = root.get("groupUsers").get("users");
      Set<String> users = new HashSet<String>();
      jsonUsers.forEach((user) -> {
         users.add(user.asText());
      });

      return users;
   }

   // Get a list of users in an external group, used by other methods that handle
   // the mapping/unmapping
   public boolean isUserInGroup(String userId, String externalGroup) throws JsonProcessingException, IOException {
      if (applicationProperties.isStandaloneMode()) {
         throw new UnsupportedFeatureException("not supported in standalone mode");
      }
      boolean isUserInGroup = false;
      // Search user for the group rather than group for user, big performance impact.
      String url = baseUrl + String.format(GROUP_LOOKUP, userId);
      String response = null;
      log.debug("Checking if user '{}' is part of group '{}' against user service URL '{}'", userId, externalGroup, url);
      try {
         response = restTemplate.getForObject(url, String.class);
      } catch (HttpClientErrorException e) {
         if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new NoSuchUserException(String.format("Could not find user %s in directory", userId));
         } else {
            throw e;
         }
      }

      // parse response with Jackson
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response);
      JsonNode jsonGroups = root.get("userGroups").get("groups");
      for (JsonNode group : jsonGroups) {
         if (externalGroup.equals(group.asText())) {
            isUserInGroup = true;
         }
      }

      return isUserInGroup;
   }
}
