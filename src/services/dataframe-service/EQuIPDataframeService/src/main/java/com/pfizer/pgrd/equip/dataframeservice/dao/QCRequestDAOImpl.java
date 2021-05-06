package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequestItem;
import com.pfizer.pgrd.equip.dataframeservice.dto.QCRequestDTO;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public class QCRequestDAOImpl extends ModeShapeDAO implements QCRequestDAO {
	private static Logger LOGGER = LoggerFactory.getLogger(QCRequestDAOImpl.class);
	private static final String ALIAS = "qcr";
	
	@Override
	public QCRequest getQCRequest(String qcRequestId) {
		QCRequest qcrequest = null;
		if (qcRequestId != null) {
			ModeShapeClient client = this.getModeShapeClient();
			QCRequestDTO dto = client.getNode(QCRequestDTO.class, qcRequestId);
			if(dto != null) {
				qcrequest = dto.toQCRequest();
			}
		}
		
		return qcrequest;
	}

	@Override
	public List<QCRequest> getQCRequest(List<String> QCRequestIds) {
		List<QCRequest> QCRequests = new ArrayList<>();
		if (QCRequestIds != null) {
			String[] ida = new String[QCRequestIds.size()];
			QCRequestIds.toArray(ida);
			QCRequests = getQCRequest(ida);
		}

		return QCRequests;
	}

	@Override
	public List<QCRequest> getQCRequest(String[] QCRequestIds) {
		List<QCRequest> QCRequests = new ArrayList<>();
		if (QCRequestIds != null) {
			for (String id : QCRequestIds) {
				QCRequest c = getQCRequest(id);
				if (c != null) {
					QCRequests.add(c);
				}
			}
		}

		return QCRequests;
	}

 	@Override
	public QCRequest insertQCRequest(QCRequest qcRequest) {
		QCRequest request = null;
		if(qcRequest != null) {
			ModeShapeClient client = getModeShapeClient();
			QCRequestDTO dto = new QCRequestDTO(qcRequest);
			
			try {
				dto = client.postNode(dto);
				request = dto.toQCRequest();
			}
			catch(ModeShapeAPIException maie) {
				LOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon insert of qc request");				
			}
			
			/*
			// call to library service to get the id of the template summary
			LibraryReference templateId = qcrequest.getChecklistTemplateId();
			
			// now create the children
			// 1. QC Checklist Summary
			List<LibraryReference> summaryIds = getIdsFromLibraryService(templateId.getLibraryRef());
			addQCSummary(summaryIds, selfPart, client);
			
			// 2. QC Checklist Item
			List<LibraryReference> checklistItemIds = getIdsFromLibraryService(templateId.getLibraryRef());
			addQCChecklistItem(checklistItemIds, selfPart, client);
			*/
		}

		return request;
	}
 	@Override
	public QCRequestItem insertQCRequestItem(QCRequestItem qcRequestItem) {
		QCRequestItem request = null;
		if(qcRequestItem != null) {
			ModeShapeClient client = getModeShapeClient();
			QCRequestDTO dto = new QCRequestDTO(qcRequestItem);
			
			try {
				dto = client.postNode(dto);
				request = dto.toQCRequestItem();
			}
			catch(ModeShapeAPIException maie) {
				LOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon insert of qc request item");				
			}
		}

		return request;
	}
 	
	@Override
	public QCRequestItem getQCRequestItem(String qcRequestId) {
		QCRequestItem qcrequest = null;
		if (qcRequestId != null) {
			ModeShapeClient client = this.getModeShapeClient();
			QCRequestDTO dto = client.getNode(QCRequestDTO.class, qcRequestId);
			if(dto != null) {
				qcrequest = dto.toQCRequestItem();
			}
		}
		
		return qcrequest;
	}
 	
 	
 	private List<LibraryReference> addLibraryReferences(List<LibraryReference> refs, String path, ModeShapeClient client) {
 		List<LibraryReference> list = new ArrayList<>();
 		if(refs != null && path != null && client != null) {
 			
 		}
 		
 		return list;
 	}
 	
 	/*
	private void addQCSummary(List<LibraryReference> summaryIds, String selfPart, ModeShapeClient client)
			throws IllegalArgumentException, IllegalAccessException {
		for (LibraryReference summaryId : summaryIds) {
			// create a qc checklist summary item for each
			QCChecklistSummaryItem qccsi = new QCChecklistSummaryItem();
			qccsi.setChecklistTemplateSummaryItemId(summaryId);
			qccsi.setChecklistSummaryItemAnswer("");
			if( qccsi.getEquipId() == null || qccsi.getEquipId().equals( "" ) ){
				qccsi.setEquipId(EquipIdCalculator.calculate("qc checklist summary item"));
			}
			
			JsonObject qccsiNode = QCRequestDAOImpl.toChecklistSummaryItemNode(qccsi);
			// JsonObject qccsiNode = ModeShapeConverter.toInnerNode(qccsi,
			// QCChecklistSummaryItem.class, true);
			qccsiNode = client.postChildNode(qccsiNode, selfPart + "/equip:qcchecklistsummaryitem");
		}
	}

	private void addQCChecklistItem(List<LibraryReference> checklistItemRefs, String selfPart, ModeShapeClient client)
			throws IllegalArgumentException, IllegalAccessException {
		for (LibraryReference checklistRef : checklistItemRefs) {
			// create a qc checklist summary item for each
			QCChecklistItem qcci = new QCChecklistItem();
			if( qcci.getEquipId() == null || qcci.getEquipId().equals( "" ) ){
				qcci.setEquipId(EquipIdCalculator.calculate("qc checklist item"));
			}
			
			qcci.setChecklistTemplateItemId(checklistRef);
			qcci.setQcComment("");
			qcci.setSourceComment("");
			try {
				JsonObject qcciNode = QCRequestDAOImpl.toChecklistItemNode(qcci);
				// JsonObject qcciNode = ModeShapeConverter.toInnerNode(qcci,
				// QCChecklistItem.class, true);
				qcciNode = client.postChildNode(qcciNode, selfPart + "/equip:qcchecklistitem");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	*/
 	
	private List<LibraryReference> getIdsFromLibraryService(String checklistId) {
		List<LibraryReference> summaryIds = new ArrayList<LibraryReference>();
		LibraryReference lr = new LibraryReference();
		lr.setLibraryRef("36ccb70a-4f7a-444d-9ca0-113848f10e57");
		summaryIds.add(lr);
		lr = new LibraryReference();
		lr.setLibraryRef("6d6600be-5415-4843-a769-d93aa9fdaf0e");

		return summaryIds;
	}

	@Override
	public List<QCRequest> getQCRequestByDataframeId(String dataframeId) {
		return this.getQCRequestByDataframeId(new String[] { dataframeId });
	}

	@Override
	public List<QCRequest> getQCRequestByDataframeId(List<String> dataframeIds) {
		List<QCRequest> list = new ArrayList<>();
		if(dataframeIds != null) {
			list = this.getQCRequestByDataframeId(dataframeIds.toArray(new String[0]));
		}
		
		return list;
	}

	@Override
	public List<QCRequest> getQCRequestByDataframeId(String[] dataframeIds) {
		JCRQueryResultSet resultSet = this.getByStringProperty("equip:dataframeId", dataframeIds, "equip:qcrequest", ALIAS);
		return this.fromResultSet(resultSet);
	}
	
	@Override
	public List<QCRequest> getQCRequestByAssemblyId(String assemblyId) {
		return this.getQCRequestByAssemblyId(new String[] { assemblyId });
	}

	@Override
	public List<QCRequest> getQCRequestByAssemblyId(List<String> assemblyIds) {
		List<QCRequest> list = new ArrayList<>();
		if(assemblyIds != null) {
			list = this.getQCRequestByAssemblyId(assemblyIds.toArray(new String[0]));
		}
		
		return list;
	}

	@Override
	public List<QCRequest> getQCRequestByAssemblyId(String[] assemblyIds) {
		JCRQueryResultSet resultSet = this.getByStringProperty("equip:assemblyId", assemblyIds, "equip:qcrequest", ALIAS);
		return this.fromResultSet(resultSet);
	}
	
	private List<QCRequest> fromResultSet(JCRQueryResultSet resultSet) {
		List<QCRequest> list = new ArrayList<>();
		if(resultSet != null) {
			for(Row r : resultSet.getRows()) {
				QCRequest qcr = new QCRequest();
				qcr.setAssemblyId(r.getString(ALIAS + ".equip:assemblyId"));
				qcr.setCreated(r.getDate(ALIAS + ".equip:created"));
				qcr.setCreatedBy(r.getString(ALIAS + ".equip:createdBy"));
				qcr.setDataframeId(r.getString(ALIAS + "equip:dataframeId"));
				qcr.setEquipId(r.getString(ALIAS + ".equip:equipId"));
				qcr.setId(r.getString(ALIAS + ".mode:id"));
				qcr.setQcDueDate(r.getDate(ALIAS + "equip:qcDueDate"));
				qcr.setQcStatus(r.getString(ALIAS + ".equip:qcStatus"));
				
				list.add(qcr);
			}
		}
		
		return list;
	}
}