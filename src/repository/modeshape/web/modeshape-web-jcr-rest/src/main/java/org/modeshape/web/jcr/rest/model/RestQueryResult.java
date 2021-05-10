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

package org.modeshape.web.jcr.rest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import org.modeshape.common.logging.Logger;

import org.modeshape.common.util.StringUtil;

/**
 * A REST representation of a {@link javax.jcr.query.QueryResult}
 * 
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestQueryResult implements JSONAble {
    protected static final Logger LOGGER = Logger.getLogger("org.modeshape.web.jcr.rest");

    private final Map<String, String> columns;
    private final List<RestRow> rows;

    /**
     * Creates an empty instance
     */
    public RestQueryResult() {
        columns = new LinkedHashMap<String, String>();
        rows = new ArrayList<RestRow>();
    }

    /**
     * Adds a new column to this result.
     * 
     * @param name a {@code non-null} string, the name of the column
     * @param type a {@code non-null} string, the type of the column
     * @return this instance
     */
    public RestQueryResult addColumn( String name,
                                      String type ) {
        boolean trace = LOGGER.isTraceEnabled();
        if (trace && StringUtil.isBlank(name)) {
            LOGGER.trace("addColumn({0}, {1})", name, type);
        }
        if (!StringUtil.isBlank(name)) {
            columns.put(name, type);
        }
        return this;
    }

    /**
     * Adds a new row to this result
     * 
     * @param row a {@code non-null} {@link RestRow}
     * @return this instance
     */
    public RestQueryResult addRow( RestRow row ) {
        rows.add(row);
        return this;
    }

    public int getRowCount() {
        return rows.size();
    }


    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        if (!columns.isEmpty()) {
            result.put("columns", columns);
        }
        if (!rows.isEmpty()) {
            JSONArray rows = new JSONArray();
            for (RestRow row : this.rows) {
                rows.put(row.toJSON());
            }
            result.put("rows", rows);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RestRow row : rows) {
            sb.append(row.toString() + "\r\n");
        }
        return sb.toString();
    }


    public class RestRow implements JSONAble {
        private final Map<String, Object> values;

        public RestRow() {
            this.values = new LinkedHashMap<>();
        }

        public void addValue(String name,
                             Object value) {
            boolean trace = LOGGER.isTraceEnabled();
            if (trace && (value == null || StringUtil.isBlank(name))) {
                LOGGER.trace("addValue({0}, {1})", name, value);
            }
            if (value == null || StringUtil.isBlank(name)) {
                return;
            }
            if (value instanceof Collection<?>) {
                values.put(name, value);
            } else {
                String valueString = value.toString();
                if (trace && StringUtil.isBlank(valueString)) {
                    LOGGER.trace("addValue2({0}, {1})", name, valueString);
                }
                if (!StringUtil.isBlank(valueString)) {
                    values.put(name, valueString);
                }
            }
        }

        @Override
        public JSONObject toJSON() {
            return new JSONObject(values);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (java.util.Map.Entry<String, Object> entry : values.entrySet()) {
                sb.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
            }
            return sb.toString();
        }
    }
}
