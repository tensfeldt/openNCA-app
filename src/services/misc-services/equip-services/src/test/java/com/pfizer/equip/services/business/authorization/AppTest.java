package com.pfizer.equip.services.business.authorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pfizer.equip.shared.service.user.Permissions;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.types.EntityType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
   AccessPlan restrictionPlan = new AccessPlan();
   AccessPlan unblindingPlan = new AccessPlan();
   AccessPlan studyAccess = new AccessPlan();
   Map<String, Set<PrivilegeType>> privAccess = new HashMap<String, Set<PrivilegeType>>();

   /**
    * Create the test case
    *
    * @param testName name of the test case
    */
   public AppTest(String testName) {
      super(testName);
   }

   /**
    * @return the suite of tests being tested
    */
   public static Test suite() {
      return new TestSuite(AppTest.class);
   }

   protected void setUp() {
      Set<String> planStudies = new HashSet<String>();
      // Test data taken from "Authorization Matrix 0.a.xlsx"

      // Added below lines of code that matches with excel sheet - Ruckmani
      // Generate gloal restriction plan plan data.
      planStudies.add("A0081001");
      restrictionPlan.put("userid1", new HashSet<String>(planStudies));
      planStudies.clear();
      planStudies.add("A0011001");
      restrictionPlan.put("userid4", new HashSet<String>(planStudies));
      planStudies.clear();
      planStudies.add("A0011002");
      restrictionPlan.put("userid3", new HashSet<String>(planStudies));
      planStudies.clear();

      // Generate gloal unblinding plan data.
      planStudies.clear();
      planStudies.add("A0011001");
      unblindingPlan.put("userid1", new HashSet<String>(planStudies));
      planStudies.clear();
      planStudies.add("A0011001");
      unblindingPlan.put("userid2", new HashSet<String>(planStudies));
      planStudies.clear();
      planStudies.add("A0011002");
      unblindingPlan.put("userid3", new HashSet<String>(planStudies));
      // Added above lines of code that matches with excel sheet - Ruckmani

      // Generate global study access list.
      // For now, all users have *basic* access to all studies.
      // However, if the study is blinded or restricted,
      // they need to be part of the unblinding plan or restriction plan
      // respectively.
      planStudies.clear();
      planStudies.add("A0011001");
      planStudies.add("A0011002");
      planStudies.add("A0011003");
      planStudies.add("A0011004");
      planStudies.add("A0011005");
      planStudies.add("A0081001");
      studyAccess.put("userid1", new HashSet<String>(planStudies));
      studyAccess.put("userid2", new HashSet<String>(planStudies));
      studyAccess.put("userid3", new HashSet<String>(planStudies));
      studyAccess.put("userid4", new HashSet<String>(planStudies));
      studyAccess.put("userid5", new HashSet<String>(planStudies));
      studyAccess.put("userid6", new HashSet<String>(planStudies));

      // Generate global role list.
      List<PrivilegeType> privs = new ArrayList<PrivilegeType>();
      privs.add(PrivilegeType.PUBLISH_DATA);
      privAccess.put("userid1", new HashSet<PrivilegeType>(privs));
      privs.clear();
      privs.add(PrivilegeType.PUBLISH_DATA);
      privAccess.put("userid2", new HashSet<PrivilegeType>(privs));
      privs.clear();
      privs.add(PrivilegeType.PUBLISH_DATA);
      privAccess.put("userid3", new HashSet<PrivilegeType>(privs));
      privs.clear();
      privs.add(PrivilegeType.VIEW_BLINDED);
      privs.add(PrivilegeType.VIEW_NON_PROMOTED);
      privAccess.put("userid4", new HashSet<PrivilegeType>(privs));
      privs.clear();
      privs.add(PrivilegeType.VIEW_BLINDED);
      privs.add(PrivilegeType.VIEW_NON_PROMOTED);
      privAccess.put("userid5", new HashSet<PrivilegeType>(privs));
      privs.clear();
      privs.add(PrivilegeType.PUBLISH_DATA);
      privAccess.put("userid6", new HashSet<PrivilegeType>(privs));
      privs.clear();
   }

   public void test1a() {
      // userid4 is a CAG user and part of the restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(Arrays.asList("A0011001"))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test1b() {
      // userid1 is a PKA user (not CAG) and not part of the blinding plan for
      // A0011001
      String testUser = "userid2";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(Arrays.asList("A0011001"))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test2a() {
      // userid5 is a CAG user and not a part of any plans for A0011001
      String testUser = "userid5";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011002", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(Arrays.asList("A0011002"))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test2b() {
      // userid2 is a PKA user (not CAG) and not a part of any plans for
      // A0011001
      String testUser = "userid2";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(Arrays.asList("A0011001"))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

   public void test3a() {
      // userid5 is a CAG user and not a part of the restriction plan or
      // unblinded plan for A0081001
      String testUser = "userid5";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0081001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0081001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test3b() {
      // userid6 is a PKA user (not CAG) and not part of any restriction/blinding plan.
      // Should still have access even though the data is blinded because it's de-identified.
      String testUser = "userid6";

      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(Arrays.asList("A0011001"))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test4a() {
      // userid4 is a CAG user and not a part of any plans for for A0011003
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011003", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(new HashSet<String>(new HashSet<String>(Arrays.asList("A0011003"))))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test4b() {
      // userid2 is a PKA user (not CAG) and not part of the restriction plan
      // but a part of blinding plan for A0011001
      String testUser = "userid2";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011003", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(new HashSet<String>(new HashSet<String>(Arrays.asList("A0011003"))))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

   public void test5a() {
      // userid4 is a CAG user and part of the restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test5b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // part of blinding plan for A0011002
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test6a() {
      // userid4 is a CAG user and part of the restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test6b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // part of blinding plan for A0011002
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

   public void test7a() {
      // userid4 is a CAG user and part of the restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test7b() {
      // userid1 is a PKA user (not CAG) and part of the restriction plan for
      // A0081001
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test8a() {
      // userid4 is a CAG user and part of the restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test8b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // unblinding plan for A0011002
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", true);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

   public void test9a() {
      // userid4 is a CAG user and not a part of any plans for A0011004
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011004", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(Arrays.asList("A0011004"))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test9b() {
      // userid6 is a PKA user (not CAG) and not part of any restriction/blinding plan.
      String testUser = "userid6";

      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011004", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(Arrays.asList("A0011004"))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test10a() {
      // userid4 is a CAG user and not a part of any plans for A0011003
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011003", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(new HashSet<String>(new HashSet<String>(Arrays.asList("A0011003"))))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test10b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // unblinding plan for A0011003
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011003", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(new HashSet<String>(new HashSet<String>(Arrays.asList("A0011003")))));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

   public void test11a() {
      // userid4 is a CAG user and not a part of any plans for A0011005
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011005", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011005")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test11b() {
      // userid6 is a PKA user (not CAG) and not part of any restriction/blinding plan.
      // Should be able to see promoted data.
      String testUser = "userid6";

      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011005", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011005")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test12a() {
      // userid4 is a CAG user and not a part of any plans for A0011005
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011005", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011005")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test12b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // unblinding plan for A0011005
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(false);
      facts.setIsStudyBlinded("A0011005", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011005")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

   public void test13a() {
      // userid4 is a CAG user and part of restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test13b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // unblinding plan for A0011002
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test14a() {
      // userid4 is a CAG user and part of restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test14b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // unblinding plan for A0011002
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(true);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

   public void test15a() {
      // userid4 is a CAG user and part of restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test15b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // unblinding plan for A0011002
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(true);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test16a() {
      // userid4 is a CAG user and part of restriction plan for A0011001
      String testUser = "userid4";
      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011001", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011001")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(true, userHasAccess);
   }

   public void test16b() {
      // userid3 is a PKA user (not CAG) and part of the restriction plan and
      // unblinding plan for A0011002
      String testUser = "userid3";

      Facts facts = new Facts();
      facts.setIsPromoted(false);
      facts.setIsDataUnblinded(false);
      facts.setIsRestricted(true);
      facts.setIsStudyBlinded("A0011002", false);
      facts.addParentEntities(EntityType.PROTOCOL, new HashSet<String>(Arrays.asList("A0011002")));

      Permissions permissions = new Permissions();
      permissions.setPrivileges(privAccess.get(testUser));
      permissions.setUnblindedEntities(EntityType.PROTOCOL, unblindingPlan.get(testUser));
      permissions.setUnrestrictedEntities(EntityType.PROTOCOL, restrictionPlan.get(testUser));

      AuthorizationService authorizationService = new AuthorizationService();
      boolean userHasAccess = authorizationService.canViewDataframe(permissions, facts);
      assertEquals(false, userHasAccess);
   }

}
