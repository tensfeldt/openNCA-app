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
package org.modeshape.schematic.internal.delta;

import org.modeshape.schematic.document.Path;
import org.modeshape.schematic.internal.document.MutableArray;
import org.modeshape.schematic.internal.document.MutableDocument;

/**
 * An atomic array add operation for SchematicValueDelta.
 * 
 * @author Randall Hauch <rhauch@redhat.com> (C) 2011 Red Hat Inc.
 */
public class AddValueIfAbsentOperation extends AddValueOperation {

    protected transient boolean added;

    public AddValueIfAbsentOperation( Path path,
                                      Object value ) {
        super(path, value);
    }

    public boolean isAdded() {
        return added;
    }

    @Override
    public void rollback( MutableDocument delegate ) {
        if (added) {
            MutableArray array = mutableParent(delegate);
            array.remove(this.index);
        }
    }

    @Override
    public void replay( MutableDocument delegate ) {
        MutableArray array = mutableParent(delegate);
        added = array.addValueIfAbsent(value);
    }

    @Override
    public String toString() {
        return super.toString() + " if absent";
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        if (obj instanceof AddValueIfAbsentOperation) {
            AddValueIfAbsentOperation other = (AddValueIfAbsentOperation)obj;
            return equalsIfNotNull(value, other.value) && index == other.index
                   && equalsIfNotNull(getParentPath(), other.getParentPath());

        }
        return false;
    }
}
