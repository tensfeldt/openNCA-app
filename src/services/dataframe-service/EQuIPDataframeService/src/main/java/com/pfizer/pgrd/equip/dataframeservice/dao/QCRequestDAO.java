package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequestItem;

/**
 * Implementing classes expose methods to retrieve, create, and modify qc requests.
 * @author HIRSCM08
 *
 */
public interface QCRequestDAO {
	/**
	 * Returns the QCRequest matching the provided QCRequest ID.
	 * @param QCRequestId
	 * @return {@link QCRequest} the QCRequest
	 */
	public QCRequest getQCRequest(String QCRequestId);
	
	/**
	 * Returns a collection of QCRequests matching the provided QCRequest IDs.
	 * @param QCRequestIds
	 * @return {@link List}<{@link QCRequest}> the QCRequests
	 */
	public List<QCRequest> getQCRequest(List<String> QCRequestIds);
	
	/**
	 * Returns a collection of QCRequests matching the provided QCRequest IDs.
	 * @param QCRequestIds
	 * @return {@link List}<{@link QCRequest}> the QCRequests
	 */
	public List<QCRequest> getQCRequest(String[] QCRequestIds);
	
	/**
	 * Inserts the provided {@link QCRequest} object. Returns the newly created QCRequest object.
	 * @param qcrequest
	 * @return @{link QCRequest}
	 */
	public QCRequest insertQCRequest(QCRequest qcrequest);
	
	public List<QCRequest> getQCRequestByDataframeId(String dataframeId);
	
	public List<QCRequest> getQCRequestByDataframeId(List<String> dataframeIds);
	
	public List<QCRequest> getQCRequestByDataframeId(String[] dataframeIds);
	
	public List<QCRequest> getQCRequestByAssemblyId(String assemblyId);
	
	public List<QCRequest> getQCRequestByAssemblyId(List<String> assemblyIds);
	
	public List<QCRequest> getQCRequestByAssemblyId(String[] assemblyIds);

	public QCRequestItem insertQCRequestItem(QCRequestItem qcrequest);

	QCRequestItem getQCRequestItem(String qcRequestId);
}
