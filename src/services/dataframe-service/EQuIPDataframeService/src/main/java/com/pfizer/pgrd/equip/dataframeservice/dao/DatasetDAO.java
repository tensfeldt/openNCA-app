package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import javax.json.JsonObject;

import com.pfizer.pgrd.equip.dataframe.dto.ComplexData;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;

public interface DatasetDAO {
	/**
	 * Returns the {@link Dataset} object matching the provided ID.
	 * @param datasetId the dataset ID
	 * @return {@link Dataset}
	 */
	public Dataset getDataset(String datasetId);
	
	/**
	 * Returns a {@link List} of {@link Dataset} objects matching the provided dataset IDs.
	 * @param datasetIds the dataset IDs
	 * @return {@link List}<{@link Dataset}>
	 */
	public List<Dataset> getDataset(List<String> datasetIds);
	
	/**
	 * Returns a {@link List} of {@link Dataset} objects matching the provided dataset IDs.
	 * @param datasetIds the dataset IDs
	 * @return {@link List}<{@link Dataset}>
	 */
	public List<Dataset> getDataset(String[] datasetIds);
	
	/**
	 * Returns the {@link Dataset} object related to the provided dataframe ID.
	 * @param dataframeId the dataframe ID
	 * @return {@link Dataset} the dataset
	 */
	public Dataset getDatasetByDataframe(String dataframeId);
	
	/**
	 * Returns the content of the dataset matching the provided ID.
	 * @param datasetId the dataset ID
	 * @return {@code byte[]}
	 */
	public ComplexData getData(String datasetId);
	
	/**
	 * Inserts the provided {@code byte[]} into the dataset matching the provided ID, overwriting any existing content.
	 * @param datasetId the dataset ID
	 * @param data the data
	 * @return {@code boolean} success
	 */
	boolean insertData(String datasetId, byte[] data);

	/**
	 * Inserts the provided dataset into the dataframe matching the provided dataframe ID. Returns the newly created dataset.
	 * @param dataframeId
	 * @param dataset
	 * @return
	 */
	public Dataset insertDataset(String dataframeId, Dataset dataset);
	
	public Dataframe getParentDataframe(String complexDataId);

	public Dataframe getParentDataframeFromDataset(String datasetId);
}
