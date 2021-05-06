package com.pfizer.equip.services.business.authorization;

import java.util.HashMap;
import java.util.Set;

/**
 * Test class for holding restriction, blinding, etc. plan data.
 * <p>
 * Simply a wrapper around the datatype below to avoid ugly syntax and enforce
 * standardization. Only used for testing.
 */
@SuppressWarnings("serial")
public class AccessPlan extends HashMap<String, Set<String>> {
}
