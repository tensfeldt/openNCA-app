package com.pfizer.equip.services.controllers;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.Application;
import com.pfizer.equip.services.business.modeshape.nodes.BaseLibraryArtifactNode;
import com.pfizer.equip.services.context.SpringWithJndiRunner;
import com.pfizer.equip.services.responses.AuthorizationResponse;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.relational.entity.Group;
import com.pfizer.equip.shared.relational.entity.GroupAccess;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserInfo;

@RunWith(SpringWithJndiRunner.class)
@ContextConfiguration(classes = { Application.class }, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:testapplication.properties", "classpath:config/testapplication-dev.properties" })
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorizationControllerTest {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   public final static String USERID = "atlamr-ncadev13"; // part of EQUIP-NCA-Logon, EQUIP-NCA-SystemAdmin, EQUIP-NCA-CAG, and EQUIP-NCA-TEST1
   public final static String INVALIDUSERID = "test123";
   public final static String SYSTEMID = "nca";
   public final static String INVALIDSYSTEMID = "testsystem";
   public final static String GROUPNAME = "testgroup";
   public final static String INVALIDGROUPNAME = "invalidgroup";
   public final static String ROLENAME = "test10";
   public final static String INVALIDROLENAME = "invalidrole";
   public final static String EXTERNALGROUPNAME1 = "EQUIP-NCA-TEST1";// "GBL-BTNONColleagues"
   public final static String EXTERNALGROUPNAME2 = "test"; // this is a real group, no members
   public final static String PRIVILEGENAME = "VIEW_NON_PROMOTED";
   public final static String ENTITYID = "testgroup";
   public final static String PROGRAMSFOLDER = "/Programs";
   public final static String OPMETAURL = "/" + SYSTEMID + "/opmeta/nodes/programs";
   public final static String PROGRAMID1 = "X001";
   public final static String PROTOCOLID1 = "X0011001";
   public final static String PROGRAMID2 = "X002";
   public final static String PROTOCOLID2 = "X0021002";
   public final static String DFGROUPNAME = "test1";
   public final static String DFENTITYTYPE = "DATAFRAME";
   public final static String ACCESSGROUPNAME = "testgroup";
   public final static String ENTITYTYPE = "PROTOCOL";
   public final static String DFSOURCEENTITY = "TESTDF100";
   public final static String DFTARGETENTITY = "TESTDF101";

   private MockMvc mockMvc;

   @Autowired
   private WebApplicationContext webApplicationContext;

   @Autowired
   private RepositoryService repositoryService;

   HttpHeaders headers = new HttpHeaders();

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.set("IAMPFIZERUSERCN", USERID);
   }

   @Test
   public void test01_isValidUser() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/valid", SYSTEMID, USERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      Map<String, Boolean> permissionsInfoMap = authorizationResponse.getPermissionsInfo();
      assertTrue(permissionsInfoMap.get("isValidUser").equals(true));
   }

   @Test
   public void test01_isValidUserException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/valid", SYSTEMID, INVALIDUSERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("NoSuchUserException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test02_getUserRoles() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/roles", SYSTEMID, USERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse response = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(response.getResponse().equals(Response.OK));
      Set<String> userRoles = response.getRoles();
      assertTrue(userRoles.size() > 0);
      assertTrue(userRoles.contains("LOGON"));
   }

   @Test
   public void test02_getUserRolesException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/roles", SYSTEMID, INVALIDUSERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("NoSuchUserException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test03_getUserGroups() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/groups", SYSTEMID, USERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      assertTrue(authorizationResponse.getGroups().size() > 0);
   }

   @Test
   public void test03_getUserGroupsException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/groups", SYSTEMID, INVALIDUSERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test04_getUserPrivileges() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/privileges", SYSTEMID, USERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse response = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      Set<PrivilegeType> userPrivileges = response.getPrivileges();
      assertTrue(response.getResponse().equals(Response.OK));
      assertTrue(userPrivileges.size() > 0);
   }

   @Test
   public void test04_getUserPrivilegesException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users/{userId}/privileges", SYSTEMID, INVALIDUSERID).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("NoSuchUserException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test05_addGroup() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/groups", SYSTEMID).param("externalGroupName", EXTERNALGROUPNAME1)
                  .param("groupName", GROUPNAME).headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse response = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(response.getResponse().equals(Response.OK));
   }

   @Test
   // Test that using an external group which is already mapped throws an
   // exception.
   public void test05_addGroup_ExternalGroupAlreadyMappedException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/groups", SYSTEMID).param("groupName", GROUPNAME)
                  .param("externalGroupName", EXTERNALGROUPNAME1).headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("ExternalGroupAlreadyMappedException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   // Test that adding a group that already exists throws an exception.
   public void test05_addGroup_DuplicateSecurityPrincipalException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/groups", SYSTEMID).param("groupName", GROUPNAME)
                  .param("externalGroupName", EXTERNALGROUPNAME2).headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("DuplicateSecurityPrincipalException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   public void test06_getUsersbyGroup() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/groups/{groupName}/users", SYSTEMID, GROUPNAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      assertTrue(authorizationResponse.getUsers().size() > 0);
      boolean hasUser = false;
      for (UserInfo user : authorizationResponse.getUsers()) {
         if (user.getuserId().equals((USERID))) {
            hasUser = true;
         }
      }
      assertTrue(hasUser);
   }

   @Test
   public void test06_getUsersbyGroupException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/groups/{groupName}/users", SYSTEMID, INVALIDGROUPNAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("NoSuchGroupException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test07_addRole() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/roles/", SYSTEMID).param("externalGroupName", EXTERNALGROUPNAME1)
                  .param("roleName", ROLENAME).headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   // External group is already mapped to a role:
   public void test07_addRole_ExternalGroupAlreadyMappedException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/roles/", SYSTEMID).param("roleName", ROLENAME)
                  .param("externalGroupName", EXTERNALGROUPNAME1).headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("ExternalGroupAlreadyMappedException")));
   }

   @Test
   // Role already exists
   public void test07_addRole_DuplicateSecurityPrincipalException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/roles/", SYSTEMID).param("roleName", ROLENAME)
                  .param("externalGroupName", EXTERNALGROUPNAME2).headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("DuplicateSecurityPrincipalException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   public void test08_getUsersbyRole() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/roles/{roleName}/users", SYSTEMID, ROLENAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse response = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(response.getResponse().equals(Response.OK));
      Set<UserInfo> users = response.getUsers();
      assertTrue(users.size() > 0);
      boolean hasUser = false;
      for (UserInfo user : users) {
         if (user.getuserId().equals((USERID))) {
            hasUser = true;
         }
      }
      assertTrue(hasUser);
   }

   @Test
   public void test08_getUsersbyRoleException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/roles/{roleName}/users", SYSTEMID, INVALIDROLENAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("NoSuchRoleException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test09_dropRoleException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.delete("/{systemId}/roles/{roleName}", SYSTEMID, INVALIDROLENAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   public void test10_dropGroupException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.delete("/{systemId}/groups/{groupName}", SYSTEMID, INVALIDGROUPNAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("MissingSecurityPrincipalException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   public void test11_addPrivilegeToRole() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.put("/{systemId}/roles/{roleName}/privileges/{privKey}", SYSTEMID, ROLENAME, PRIVILEGENAME)
                  .headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   public void test12_addPrivilegeToRoleException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.put("/{systemId}/roles/{roleName}/privileges/{privKey}", SYSTEMID, INVALIDROLENAME, "VIEW_NON_PROMOTED")
                  .headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("MissingSecurityPrincipalException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   public void test13_getRolePrivileges() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/roles/{roleName}/privileges", SYSTEMID, ROLENAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse response = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(response.getResponse().equals(Response.OK));
      Set<PrivilegeType> rolePrivileges = response.getPrivileges();
      assertTrue(rolePrivileges.size() > 0);
   }

   @Test
   public void test14_getRolePrivilegesException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/roles/{roleName}/privileges", SYSTEMID, INVALIDROLENAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test15_dropPrivilegeFromRole() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.delete("/{systemId}/roles/{roleName}/privileges/{privKey}", SYSTEMID, ROLENAME, "VIEW_NON_PROMOTED")
                  .headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   public void test15_dropPrivilegeFromRoleException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders.delete("/{systemId}/roles/{roleName}/privileges/{privKey}", SYSTEMID, INVALIDROLENAME, PRIVILEGENAME)
                  .headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("MissingSecurityPrincipalException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   public void test16_addAccessToGroup() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders
                  .put("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, ACCESSGROUPNAME, ENTITYTYPE, PROTOCOLID1)
                  .param("viewBlindedFlag", "true").param("viewRestrictedFlag", "true").headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   public void test16_addAccessToGroupException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders
                  .put("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, INVALIDGROUPNAME, ENTITYTYPE, ENTITYID)
                  .param("viewBlindedFlag", "true").param("viewRestrictedFlag", "true").headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("MissingSecurityPrincipalException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   // The Response is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test17_copyAccessToGroup() throws Exception {
      headers.setContentType(MediaType.APPLICATION_JSON);
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders
                  .post("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, DFGROUPNAME, ENTITYTYPE, PROTOCOLID1)
                  .param("targetEntityId", PROTOCOLID2).headers(headers).accept("application/json"))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   // The Response is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test17_copyAccessToGroup_Exception() throws Exception {
      headers.setContentType(MediaType.APPLICATION_JSON);
      mockMvc
            .perform(MockMvcRequestBuilders
                  .post("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, INVALIDGROUPNAME, ENTITYTYPE, PROTOCOLID1)
                  .param("targetEntityId", PROTOCOLID2).headers(headers).accept("application/json"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content()
                  .string(new StringContains("Group '" + INVALIDGROUPNAME + "' does not exist in system '" + SYSTEMID + "'")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   // The Response is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test18_getAccessByGroup() throws Exception {
      headers.setContentType(MediaType.APPLICATION_JSON);
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/groups/{groupName}/access", SYSTEMID, DFGROUPNAME).headers(headers)
                  .accept("application/json"))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      assertTrue(authorizationResponse.getGroupAccesses().size() > 0);
      for (GroupAccess groupAccess : authorizationResponse.getGroupAccesses()) {
         if (groupAccess.getEntityId().equalsIgnoreCase(PROTOCOLID1)) {
            assertTrue(groupAccess.getEntityId().equalsIgnoreCase(PROTOCOLID1));
            assertTrue(groupAccess.getEntityType().getValue().equalsIgnoreCase(ENTITYTYPE));
         } else if (groupAccess.getEntityId().equalsIgnoreCase(PROTOCOLID2)) {
            assertTrue(groupAccess.getEntityId().equalsIgnoreCase(PROTOCOLID2));
            assertTrue(groupAccess.getEntityType().getValue().equalsIgnoreCase(ENTITYTYPE));
         }
      }
   }

   // The Response is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test18_getAccessByGroup_Exception() throws Exception {
      headers.setContentType(MediaType.APPLICATION_JSON);
      mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/groups/{groupName}/access", SYSTEMID, INVALIDGROUPNAME).headers(headers)
                  .accept("application/json"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("NoSuchGroupException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("Group '" + INVALIDGROUPNAME + "' could not be found")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   // The Response is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test19_copyAccessForDataframe() throws Exception {
      ObjectMapper mapper = new ObjectMapper();
      // Add access to DATAFRAME
      headers.setContentType(MediaType.APPLICATION_JSON);
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders
                  .put("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, ACCESSGROUPNAME, DFENTITYTYPE, DFSOURCEENTITY)
                  .param("viewBlindedFlag", "true").param("viewRestrictedFlag", "true").headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();

      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      // COPY access to DATAFRAME
      List<String> studyIdList = new ArrayList<>();
      studyIdList.add(PROGRAMID1 + ":" + PROTOCOLID1);
      Map<String, Object> inputs = new HashMap<>();
      inputs.put("id", DFSOURCEENTITY);
      inputs.put("studyIds", studyIdList);
      inputs.put("dataBlindingStatus", "Unblinded");
      inputs.put("dataframeType", "Dataset");
      inputs.put("promotionStatus", "Promoted");
      inputs.put("restrictionStatus", "Restricted");
      resultAction = mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/dataframe/copy-access", SYSTEMID).param("targetEntityId", DFTARGETENTITY)
                  .content(mapper.writeValueAsString(inputs)).headers(headers).accept("application/json"))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      result = resultAction.andReturn();
      authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   //// The Response is not implemented yet, hence we are checking the response as OK alone
   // @Test
   // public void test18_copyAccessForDataframe_Exception() {
   // ObjectMapper mapper = new ObjectMapper();
   //
   //// COPY access to DATAFRAME
   // try {
   // List<String> studyIdList = new ArrayList<>();
   // studyIdList.add(PROGRAMID1+":"+PROTOCOLID1);
   // Map<String, Object> inputs = new HashMap<>();
   // inputs.put("id", DFSOURCEENTITY);
   // inputs.put("studyIds", studyIdList);
   // inputs.put("dataBlindingStatus", "Unblinded");
   // inputs.put("dataframeType", "Dataset");
   // inputs.put("promotionStatus", "Promoted");
   // inputs.put("restrictionStatus", "Restricted");
   // ResultActions resultAction =
   //// mockMvc.perform(MockMvcRequestBuilders.post("/{systemId}/dataframe/copy-access",
   //// SYSTEMID)
   // .param("targetEntityId",
   //// DFTARGETENTITY).content(mapper.writeValueAsString(inputs)).headers(headers).accept("application/json"))
   // .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new
   //// StringContains("OK")))
   // .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   // MvcResult result = resultAction.andReturn();
   // AuthorizationResponse authorizationResponse =
   //// mapper.readValue(result.getResponse().getContentAsString(),
   //// AuthorizationResponse.class);
   // assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   // }

   // The Response is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test20_getGroupsByEntity() throws Exception {
      headers.setContentType(MediaType.APPLICATION_JSON);
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/entities/{entityType}/{entityId}/access", SYSTEMID, ENTITYTYPE, PROTOCOLID1)
                  .headers(headers).accept("application/json"))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      assertTrue(authorizationResponse.getGroupDetails().size() > 0);
      for (Group group : authorizationResponse.getGroupDetails()) {
         if (group.getExternalGroupName().equalsIgnoreCase(EXTERNALGROUPNAME1)) {
            assertTrue(group.getGroupName().equalsIgnoreCase(ACCESSGROUPNAME));
            assertTrue(group.getExternalGroupName().equalsIgnoreCase(EXTERNALGROUPNAME1));
         }
      }
   }

   // The Response is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test20_getGroupsByEntity_Exception() throws Exception {
      headers.setContentType(MediaType.APPLICATION_JSON);
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/entities/{entityType}/{entityId}/access", SYSTEMID, ENTITYTYPE, "InvalidEntityId")
                  .headers(headers).accept("application/json"))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   public void test21_dropAccessFromGroup_Protocol1() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders
                  .delete("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, ACCESSGROUPNAME, ENTITYTYPE, PROTOCOLID1)
                  .headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   public void test21_dropAccessFromGroup_Protocol2() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders
                  .delete("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, DFGROUPNAME, ENTITYTYPE, PROTOCOLID2)
                  .headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   public void test21_dropAccessFromGroupException() throws Exception {
      mockMvc
            .perform(MockMvcRequestBuilders
                  .delete("/{systemId}/groups/{groupName}/access/{entityType}/{entityId}", SYSTEMID, INVALIDGROUPNAME, ENTITYTYPE, ENTITYID)
                  .headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("MissingSecurityPrincipalException")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())));
   }

   @Test
   public void test22_dropRole() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.delete("/{systemId}/roles/{roleName}", SYSTEMID, ROLENAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   @Test
   public void test23_dropGroup() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.delete("/{systemId}/groups/{groupName}", SYSTEMID, GROUPNAME).headers(headers)
                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   public String createProgramsProtocols() throws Exception {
      String programsFolderExitsCreated = "ERROR";
      ResultActions resultActionUserFolderCheck = mockMvc
            .perform(MockMvcRequestBuilders.get("/librarian/artifact/current" + PROGRAMSFOLDER).headers(headers).accept(MediaType.APPLICATION_JSON));
      if (resultActionUserFolderCheck.andReturn().getResponse().getStatus() == 500) {
         // Programs Folder does not exists, hence create a folder
         // Librarian Service - Add Folder cannot be used to create /Programs folder
         // because of the access restriction. So Using Repository service to create
         // /Programs
         // Create /Programs folder
         BaseLibraryArtifactNode newNode = new BaseLibraryArtifactNode();
         newNode.setPrimaryType("opmeta:programFolder");
         repositoryService.addNode(newNode, PROGRAMSFOLDER);
         programsFolderExitsCreated = "CREATED";

      } else if (resultActionUserFolderCheck.andReturn().getResponse().getStatus() == 200) {
         // User folder exists, hence proceed with <userid> folder check
         programsFolderExitsCreated = "EXISTS";

      }

      // Add Programs and Protocols
      ObjectMapper mapper = new ObjectMapper();
      // 1. Create Program
      Map<String, String> inputs = new HashMap<>();
      inputs.put("opmeta:programCode", PROGRAMID1);
      inputs.put("opmeta:source", "EQUIP");
      mockMvc.perform(MockMvcRequestBuilders.post(OPMETAURL).headers(headers).content(mapper.writeValueAsString(inputs))
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
      // 2. Create Protocol
      Map<String, Object> protocolinputs = new HashMap<>();
      protocolinputs.put("opmeta:studyId", PROTOCOLID1);
      protocolinputs.put("opmeta:source", "EQUIP");
      ResultActions result = mockMvc
            .perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + PROGRAMID1 + "/protocols").headers(headers)
                  .content(mapper.writeValueAsString(protocolinputs)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
      result.andExpect(MockMvcResultMatchers.status().isOk());

      // 1. Create Program
      Map<String, String> inputs2 = new HashMap<>();
      inputs2.put("opmeta:programCode", PROGRAMID2);
      inputs2.put("opmeta:source", "EQUIP");
      mockMvc.perform(MockMvcRequestBuilders.post(OPMETAURL).headers(headers).content(mapper.writeValueAsString(inputs2))
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
      // 2. Create Protocol
      Map<String, Object> protocolinputs2 = new HashMap<>();
      protocolinputs2.put("opmeta:studyId", PROTOCOLID2);
      protocolinputs2.put("opmeta:source", "EQUIP");
      ResultActions result2 = mockMvc
            .perform(MockMvcRequestBuilders.post("/" + SYSTEMID + "/opmeta/nodes/programs/" + PROGRAMID2 + "/protocols").headers(headers)
                  .content(mapper.writeValueAsString(protocolinputs2)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
      result2.andExpect(MockMvcResultMatchers.status().isOk());
      return programsFolderExitsCreated;

   }

   @Test
   public void test24_checkDataframeAccess() throws Exception {
      String foldersCreated = createProgramsProtocols();
      String requestJson = "{\r\n" + "   \"id\": \"00000001-24ee-48a2-8a80-12e4b71d7b2\",\r\n" + "   \"studyIds\": [\"X001:X0011001\"],\r\n"
            + "   \"dataBlindingStatus\": \"Blinded\",\r\n" + "   \"dataframeType\": \"Data Transformation\",\r\n"
            + "   \"promotionStatus\": \"Promoted\",\r\n" + "   \"restrictionStatus\": \"Unrestricted\"\r\n" + "}";
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/dataframe/check-access", SYSTEMID).headers(headers).content(requestJson)
                  .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      if (foldersCreated.equalsIgnoreCase("CREATED")) {
         // delete the /Programs folder
         repositoryService.deleteNode(PROGRAMSFOLDER);
      } else if (foldersCreated.equalsIgnoreCase("EXISTS")) {
         deleteProgramsFolders();
      }
   }

   @Test
   public void test24_checkDataframeAccessException() throws Exception {
      String requestJson = "{}";

      mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/dataframe/check-access", SYSTEMID).headers(headers).content(requestJson)
                  .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("FAILED")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   @Test
   public void test25_checkDataframeAccesses() throws Exception {
      String foldersCreated = createProgramsProtocols();
      String requestJson = "[{\r\n" + "   \"id\": \"00000001-24ee-48a2-8a80-12e4b71d7b2\",\r\n" + "   \"studyIds\": [\"X001:X0011001\"],\r\n"
            + "   \"dataBlindingStatus\": \"Blinded\",\r\n" + "   \"dataframeType\": \"Data Transformation\",\r\n"
            + "   \"promotionStatus\": \"Promoted\",\r\n" + "   \"restrictionStatus\": \"Unrestricted\"\r\n" + "},{\r\n"
            + "   \"id\": \"00000002-24ee-48a2-8a80-12e4b71d7b2\",\r\n" + "   \"studyIds\": [\"X002:X0021002\"],\r\n"
            + "   \"dataBlindingStatus\": \"Blinded\",\r\n" + "   \"dataframeType\": \"Data Transformation\",\r\n"
            + "   \"promotionStatus\": \"Promoted\",\r\n" + "   \"restrictionStatus\": \"Unrestricted\"\r\n" + "}]";
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/dataframe/check-accesses", SYSTEMID).headers(headers).content(requestJson)
                  .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
      if (foldersCreated.equalsIgnoreCase("CREATED")) {
         // delete the /Programs folder
         repositoryService.deleteNode(PROGRAMSFOLDER);
      } else if (foldersCreated.equalsIgnoreCase("EXISTS")) {
         deleteProgramsFolders();
      }
   }

   @Test
   public void test25_checkDataframeAccessesException() throws Exception {
      String requestJson = "[{},{}]";

      mockMvc
            .perform(MockMvcRequestBuilders.post("/{systemId}/dataframe/check-accesses", SYSTEMID).headers(headers).content(requestJson)
                  .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string(new StringContains("FAILED")))
            .andExpect(MockMvcResultMatchers.content().string(new StringContains(Response.FAILED.toString())))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   }

   public void deleteProgramsFolders() {
      // delete the programs X001 and X002 under /Programs folder
      repositoryService.deleteNode(PROGRAMSFOLDER + "/X001");
      repositoryService.deleteNode(PROGRAMSFOLDER + "/X002");
   }

   // The getUsers method in the Authorization Controller is not implemented yet, hence we are checking the response as OK alone
   @Test
   public void test26_getUsers() throws Exception {
      ResultActions resultAction = mockMvc
            .perform(MockMvcRequestBuilders.get("/{systemId}/users", SYSTEMID).headers(headers).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new StringContains("OK")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
      MvcResult result = resultAction.andReturn();
      ObjectMapper mapper = new ObjectMapper();
      AuthorizationResponse authorizationResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationResponse.class);
      assertTrue(authorizationResponse.getResponse().equals(Response.OK));
   }

   // TODO need to update the below method once the logic is implemented in the controller
   // @Test
   // public void test25_getUsersException() {
   // try {
   //
   // mockMvc.perform(MockMvcRequestBuilders.get("/{systemId}/users",
   // INVALIDSYSTEMID).headers(headers).accept(MediaType.APPLICATION_JSON))
   // .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.content().string(new
   // StringContains("FAILED")))
   // .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   // }

}
