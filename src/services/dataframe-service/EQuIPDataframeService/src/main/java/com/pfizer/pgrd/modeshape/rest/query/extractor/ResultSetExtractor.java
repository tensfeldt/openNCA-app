package com.pfizer.pgrd.modeshape.rest.query.extractor;

import java.util.List;

import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public interface ResultSetExtractor<T> {
	/**
	 * Returns the node type alias used when extracting properties from {@link JCRQueryResultSet} objects.
	 * @return
	 */
	public String getAlias();
	
	/**
	 * Sets the node type alias used when extracting properties from {@link JCRQueryResultSet} objects.
	 * @param alias
	 */
	public void setAlias(String alias);
	
	/**
	 * Returns a {@link List} of objects of type {@code T} created from the properties in the provided {@link JCRQueryResultSet} using the 
	 * node type alias set.
	 * @param resultSet the result set
	 * @return {@link List}<{@code T}>
	 */
	public List<T> extract(JCRQueryResultSet resultSet);
	
	/**
	 * Returns a {@link List} of objects of type {@code T} created from the properties in the provided {@link JCRQueryResultSet} using the provided 
	 * node type alias.
	 * @param resultSet the result set
	 * @param alias the alias of the node type used in the result set
	 * @return {@link List}<{@code T}>
	 */
	public List<T> extract(JCRQueryResultSet resultSet, String alias);
	
	/**
	 * Returns a {@code T} object created from the properties in the provided {@link Row} object using the provided alias.
	 * @param row
	 * @param alias
	 * @return {@code T}
	 */
	public T extract(Row row, String alias);
}
