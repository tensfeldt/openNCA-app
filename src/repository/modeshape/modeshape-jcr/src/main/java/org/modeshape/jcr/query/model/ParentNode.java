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
package org.modeshape.jcr.query.model;

import org.modeshape.common.annotation.Immutable;
import org.modeshape.common.util.CheckArg;
import org.modeshape.common.util.HashCode;

/**
 * A constraint requiring that the selected node is a parent of the node reachable by the supplied absolute path
 * , javax.jcr.query.qom.ChildNode
 */
@Immutable
public class ParentNode implements Constraint {
    private static final long serialVersionUID = 1L;

    private final SelectorName selectorName;
    private final String childPath;
    private final int hc;

    /**
     * Create a constraint requiring that the node identified by the selector is a parent of the node reachable by the supplied
     * absolute path.
     * 
     * @param selectorName the name of the selector
     * @param childPath the absolute path to the parent
     */
    public ParentNode( SelectorName selectorName,
                      String childPath ) {
        CheckArg.isNotNull(selectorName, "selectorName");
        CheckArg.isNotNull(childPath, "childPath");
        this.selectorName = selectorName;
        this.childPath = childPath;
        this.hc = HashCode.compute(this.selectorName, this.childPath);
    }

    /**
     * Get the name of the selector representing the parent
     * 
     * @return the selector name; never null
     */
    public final SelectorName selectorName() {
        return selectorName;
    }

    public String getSelectorName() {
        return selectorName.getString();
    }

    public final String getChildPath() {
        return childPath;
    }

    @Override
    public String toString() {
        return Visitors.readable(this);
    }

    @Override
    public int hashCode() {
        return hc;
    }

    @Override
    public boolean equals( Object obj ) {
        if (obj == this) return true;
        if (obj instanceof ParentNode) {
            ParentNode that = (ParentNode)obj;
            if (this.hc != that.hc) return false;
            if (!this.selectorName.equals(that.selectorName)) return false;
            if (!this.childPath.equals(that.childPath)) return false;
            return true;
        }
        return false;
    }

    @Override
    public void accept( Visitor visitor ) {
        visitor.visit(this);
    }
}
