package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.services.notification.client.event_detail;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.notification.client.NotificationRequestBody;
import com.pfizer.pgrd.equip.services.notification.client.NotificationServiceClient;

public class NotificationDAO extends ModeShapeDAO {

	private NotificationServiceClient nsc;
	private List<NotificationRequestBody> bodyList;

	public NotificationServiceClient getNsc() {
		return nsc;
	}

	public void setNsc(NotificationServiceClient nsc) {
		this.nsc = nsc;
	}

	public NotificationDAO() throws ServiceCallerException {

		this.setNsc(NotificationServiceClient.getClient(Props.getNotifServiceServer(), Props.getNotifSerivcePort(), Props.getNotificationServiceSystemId()));

	}

	
	public boolean notifyEvent (NotificationRequestBody body, List<String> studyIds) {
		
		for (String protocolId:studyIds){
			String studyId = protocolId.split(":")[1];
			String programId = protocolId.split(":")[0];
			body.setStudy_id(studyId);
			body.setProgram_number(programId);
			
			if (!nsc.postNotification(body.getEventDetail().getUser_name(), body)) return false;
		}

		return true;
	}
	
	



}
