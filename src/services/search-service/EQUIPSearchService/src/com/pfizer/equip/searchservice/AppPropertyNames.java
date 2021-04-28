package com.pfizer.equip.searchservice;

/**
 * Constants for retrieving values from the application.properties file.
 * 
 * @author HeinemanWP
 *
 */
public class AppPropertyNames {
	public static final String VERSION = "version";
	public static final String UPDATE_INDEXES = "updateIndexes";
	public static final String AUDIT_SERVICE_HOST_DEFAULT = "localhost";
	public static final String AUDIT_SERVICE_PORT = "AuditServicePort";
	public static final String AUDIT_SERVICE_HOST = "AuditServiceHost";
	public static final String AUDIT_SERVICE_PORT_DEFAULT = "8080";
	public static final String AUTH_SERVICE_PORT = "AuthorizationServicePort";
	public static final String AUTH_SERVICE_HOST = "AuthorizationServiceHost";
	public static final String AUTH_SERVICE_HOST_DEFAULT = "localhost";
	public static final String AUTH_SERVICE_PORT_DEFAULT = "8080";
	public static final String ELASTICSEARCH_SERVER = "ElasticSearch.server";
	public static final String ELASTICSEARCH_USERNAME = "ElasticSearch.username";
	public static final String ELASTICSEARCH_PASSWORD = "ElasticSearch.password";
	public static final String MODESHAPE_SERVER = "Modeshape.server";
	public static final String MODESHAPE_USERNAME = "Modeshape.username";
	public static final String MODESHAPE_PASSWORD = "Modeshape.password";
	public static final String SEARCH_METADATA_INDEX = "Search.metadata.index";
	public static final String SEARCH_COMMENTS_INDEX = "Search.comments.index";
	public static final String SEARCH_FILEDATA_INDEX = "Search.filedata.index";
	public static final String SEARCH_FILETEXT_INDEX = "Search.filetext.index";
	public static final String METADATA_SEARCH_SOURCES_INCLUDE = "MetaDataSearch.sources.include";
	public static final String METADATA_SEARCH_SOURCES_EXCLUDE = "MetaDataSearch.sources.exclude";
	public static final String COMMENTS_SEARCH_SOURCES_INCLUDE = "CommentsSearch.sources.include";
	public static final String COMMENTS_SEARCH_SOURCES_EXCLUDE = "CommentsSearch.sources.exclude";
	public static final String FILEDATA_SEARCH_SOURCES_INCLUDE = "FileDataSearch.sources.include";
	public static final String FILEDATA_SEARCH_SOURCES_EXCLUDE = "FileDataSearch.sources.exclude";
	public static final String FILETEXT_SEARCH_SOURCES_INCLUDE = "FileTextSearch.sources.include";
	public static final String FILETEXT_SEARCH_SOURCES_EXCLUDE = "FileTextSearch.sources.exclude";
	public static final String UNIFIED_SEARCH_SOURCES_INCLUDE = "UnifiedSearch.sources.include";
	public static final String UNIFIED_SEARCH_SOURCES_EXCLUDE = "UnifiedSearch.sources.exclude";
	public static final String INDEXING_ENABLED = "IndexingEnabled";
	public static final String INDEXING_ENABLED_DEFAULT = "false";
	public static final String INDEXING_SLEEP_TIME = "IndexingSleepTime";
	public static final String INDEXING_SLEEP_TIME_DEFAULT = "10000";	
	public static final String INDEX_UPDATE_FILE = "IndexUpdateFile";
	public static final String INDEX_UPDATE_FILE_DEFAULT = "/app/3rdparty/equip/EquipSearchService/index-update";
	
	private AppPropertyNames() {}
	
}
