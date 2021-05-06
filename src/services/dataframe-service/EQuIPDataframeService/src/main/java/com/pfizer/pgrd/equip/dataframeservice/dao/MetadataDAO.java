package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;
import java.util.Map;

import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

/**
 * Implementing classes expose methods to retrieve, create, and modify metadata.
 * @author QUINTJ16
 *
 */
public interface MetadataDAO {
	/**
	 * Returns a key-value pair matching the provided metadatum ID.
	 * @param metadatumId
	 * @return {@link Map}<{@link String}, {@link Object}>
	 */
	public Metadatum getMetadata(String metadatumId);
	
	/**
	 * Returns a {@link Map} of key-value pairs matching the provided metadata IDs.
	 * @param metadataIds
	 * @return {@link Map}<{@link String}, {@link Object}>
	 */
	public List<Metadatum> getMetadata(List<String> metadataIds);
	
	/**
	 * Returns a {@link Map} of key-value pairs matching the provided metadata IDs.
	 * @param metadataIds
	 * @return {@link Map}<{@link String}, {@link Object}>
	 */
	public List<Metadatum> getMetadata(String[] metadataIds);
	
	/**
	 * Returns a collection of kay-value pairs matching the provided dataframe ID.
	 * @param dataframeId
	 * @return {@link Map}<{@link String}, {@link Object}>
	 */
	public List<Metadatum> getMetadataByDataframe(String dataframeId);
	
	/**
	 * Inserts the provided {@link Metadatum} object under the parent object matching the provided ID. Returns the newly created metadatum object.
	 * @param metadatum
	 * @param parentId
	 * @return
	 */
	public Metadatum insertMetadata(Metadatum metadatum, String parentId);

	/**
	 * Inserts the provided {@link Metadatum} objects under the parent object matching the provided ID. Returns the newly created metadatum objects.
	 * @param metadata
	 * @param parentId
	 * @return
	 */
	public List<Metadatum> insertMetadata(List<Metadatum> metadata, String parentId);

	public Metadatum updateMetadata(Metadatum metadata, String nodeId);
}
