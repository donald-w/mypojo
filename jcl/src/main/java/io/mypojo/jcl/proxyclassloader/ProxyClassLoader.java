/*
 * Copyright 2016 Donald W - github@donaldw.com
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

package io.mypojo.jcl.proxyclassloader;

import java.io.InputStream;
import java.net.URL;

/**
 * @author Kamran Zafar
 */
public abstract class ProxyClassLoader implements Comparable<ProxyClassLoader> {
    // Default order
    protected int order = 5;
    // Enabled by default
    protected boolean enabled = true;

    public int getOrder() {
        return order;
    }

    /**
     * Set loading order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public abstract Class loadClass(String className, boolean resolveIt);

    public abstract InputStream loadResource(String name);

    public abstract URL findResource(String name);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int compareTo(ProxyClassLoader o) {
        return order - o.getOrder();
    }
}
