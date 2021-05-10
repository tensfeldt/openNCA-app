/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.jcr.index.elasticsearch;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.modeshape.common.logging.Logger;

import org.modeshape.jcr.cache.NodeKey;
import org.modeshape.jcr.index.elasticsearch.client.EsClient;
import org.modeshape.jcr.index.elasticsearch.client.EsRequest;
import org.modeshape.jcr.index.elasticsearch.client.EsResponse;
import org.modeshape.jcr.spi.index.Index;
import org.modeshape.jcr.spi.index.provider.Filter;
import org.modeshape.schematic.document.Document;

/**
 *
 * @author kulikov
 */
public class SearchResults implements Index.Results {
    protected static final Logger LOGGER = Logger.getLogger("org.modeshape.jcr.index.elasticsearch");

    private String scrollId;

    private final EsClient client;
    private final EsRequest query;
    private final String index, type;
    
    private int pos = 0;
    private int totalHits;
    
    /**
     * Creates new search result instance for the given search request.
     */
    public SearchResults(EsClient client, String index, String type, EsRequest query) {
        this.client = client;
        this.query = query;
        this.index = index;
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Filter.ResultBatch getNextBatch(int batchSize) {
        boolean trace = LOGGER.isTraceEnabled();
        query.put("size", batchSize);
        query.put("_source", "[\"\"]");
        
        try {
            if (trace) {
                LOGGER.trace("this.scrollId: {0}", scrollId);
            }
            EsResponse res = client.searchScrolling(index, type, query, scrollId);
            scrollId = res.getScrollId();
            Document hits = (Document) res.get("hits");
            totalHits = hits.getInteger("total");
            List<Document> items = (List<Document>) hits.getArray("hits");
            if (items == null || items.isEmpty()) {
                scrollId = null;
                return Filter.ResultBatch.EMPTY;
            }
            LinkedHashMap<NodeKey, Float> results = items.stream().collect(Collectors.toMap(
                    doc -> new NodeKey(doc.getString("_id")),
                    doc -> doc.getDouble("_score").floatValue(),
                    (v1, v2) -> v1,
                    LinkedHashMap::new));
            pos += results.size();
            if (trace) {
                LOGGER.trace("getNextBatch({0})", batchSize);
                for (java.util.Map.Entry<NodeKey, Float> entry : results.entrySet()) {
                    LOGGER.trace("{0}", entry.getKey().getIdentifier());
                }
            }
    
            return new Filter.ResultBatch() {
                @Override
                public Iterable<NodeKey> keys() {
                    return () -> results.keySet().iterator();
                }

                @Override
                public Iterable<Float> scores() {
                    return () -> results.values().iterator();
                }

                @Override
                public boolean hasNext() {
                    return pos < totalHits;
                }

                @Override
                public int size() {
                    return results.size();
                }
            };
        } catch (IOException e) {
            throw new EsIndexException(e);
        }
    }

    @Override
    public void close() {
        scrollId = null;
    }

    /**
     * Gets cardinality for this search request.
     * 
     * @return total hits matching search request.
     */
    public long getCardinality() {
        if (pos > 0) {
            return totalHits;
        }
        try {
            query.put("from", 0);   // WPH - added
            query.put("size", 0);   // WPH - added

            // //CHECKSTYLE:OFF
            // System.out.println("SearchResult.getCardinality() this: " + this + " index: " + index + " type: " + type + "\nquery: " + query.toString());
            // //CHECKSTYLE:ON
            EsResponse res = client.search(index, type, query);
            // EsResponse res = client.searchScrolling(index, type, query, context);
            Document hits = (Document) res.get("hits");
            totalHits = hits.getInteger("total");
            // context.remove();     // WPH - added
            return totalHits;
        } catch (Exception e) {
            throw new EsIndexException(e);
        }
    }

}
