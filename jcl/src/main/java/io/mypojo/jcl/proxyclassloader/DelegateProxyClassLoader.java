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

import io.mypojo.jcl.JarClassLoader;

import java.io.InputStream;
import java.net.URL;

/**
 * DelegateProxyClassLoader implements a ProxyClassLoader which delegate loading
 * to a specific AbstractClassLoader loader instance..
 *
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 */
public class DelegateProxyClassLoader extends ProxyClassLoader {

    private final JarClassLoader delegate;

    /**
     * Build a new instance of DelegateProxyClassLoader.java.
     *
     * @param delegate instance of AbstractClassLoader where to delegate
     * @throws NullPointerException if delegate is null
     */
    public DelegateProxyClassLoader(JarClassLoader delegate) throws NullPointerException {
        super();
        if (delegate == null)
            throw new NullPointerException("delegate can't be null");
        this.delegate = delegate;
        this.order = 15;
    }

    @SuppressWarnings("rawtypes")
    public Class loadClass(String className, boolean resolveIt) {
        Class result;
        try {
            result = delegate.loadClass(className, resolveIt);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return result;
    }

    public InputStream loadResource(String name) {
        return delegate.getResourceAsStream(name);
    }

    @Override
    public URL findResource(String name) {
        return delegate.getResource(name);
    }

    public JarClassLoader getDelegate() {
        return delegate;
    }
}
