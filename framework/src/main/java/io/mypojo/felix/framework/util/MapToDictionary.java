/*
 * Copyright 2016 Donald W - github@donaldw.com
 * Copyright 2011 Karl Pauls karlpauls@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mypojo.felix.framework.util;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

/**
 * This is a simple class that implements a <tt>Dictionary</tt> from a
 * <tt>Map</tt>. The resulting dictionary is immutable.
 **/
public class MapToDictionary extends Dictionary {
    /**
     * Map source.
     **/
    private Map m_map = null;

    public MapToDictionary(Map map) {
        if (map == null) {
            throw new IllegalArgumentException("Source map cannot be null.");
        }
        m_map = map;
    }

    public Enumeration elements() {
        return Collections.enumeration(m_map.values());
    }

    public Object get(Object key) {
        return m_map.get(key);
    }

    public boolean isEmpty() {
        return m_map.isEmpty();
    }

    public Enumeration keys() {
        return Collections.enumeration(m_map.keySet());
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return m_map.size();
    }

    public String toString() {
        return m_map.toString();
    }
}