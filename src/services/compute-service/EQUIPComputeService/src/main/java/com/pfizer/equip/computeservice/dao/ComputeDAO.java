package com.pfizer.equip.computeservice.dao;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.RequestBody;
import com.pfizer.equip.computeservice.exception.ComputeException;
import com.pfizer.equip.computeservice.scripts.ScriptItem;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;

public interface ComputeDAO {

	public ComputeResponse doCompute(String serverUri, String system, String user, Map<String, String> requestHeaders, RequestBody rb, boolean isVirtual) throws ComputeException, ServiceCallerException, UnknownHostException;
	public boolean privilegeCheck(String system, Map<String, String> requestHeaders, String dataframeType, String username, ScriptItem scriptItem) throws ServiceCallerException, IOException, JAXBException;

}
