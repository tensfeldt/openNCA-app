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
package org.modeshape.jcr.index.elasticsearch.query;

import org.modeshape.jcr.index.elasticsearch.client.EsRequest;

/**
 *
 * @author kulikov
 */
public class OrQuery extends Query {
    private final Query q1, q2;
    
    public OrQuery(Query q1, Query q2) {
        this.q1 = q1;
        this.q2 = q2;
    }
    
    @Override
    public EsRequest build() {
        // EsRequest query = new EsRequest();                           // WPH - replaced these three lines
        // query.put("or", new EsRequest[]{q1.build(), q2.build()});    //  with what follows.
        // return query;                   

        // BoolQuery boolQuery = new BoolQuery();
        // boolQuery.should(q1);
        // boolQuery.should(q2);
        // return boolQuery.build();
        EsRequest query = new EsRequest();
        query.put("should", new EsRequest[]{q1.build(), q2.build()});
        EsRequest body = new EsRequest();
        body.put("bool", query);
        return body;
    }
    
}
