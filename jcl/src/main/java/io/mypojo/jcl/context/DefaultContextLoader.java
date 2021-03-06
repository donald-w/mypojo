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

package io.mypojo.jcl.context;

import io.mypojo.jcl.JarClassLoader;
import org.slf4j.Logger;

import static io.mypojo.jcl.context.JclContext.DEFAULT_NAME;
import static io.mypojo.jcl.context.JclContext.destroy;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class is builds the context from a single JCL instance. This should be
 * used if a single JarClassLoader is instantiated programmatically.
 *
 * @author Kamran
 */
public class DefaultContextLoader implements JclContextLoader {
    private static Logger logger = getLogger(DefaultContextLoader.class);
    private final JclContext jclContext;
    private final JarClassLoader jcl;

    public DefaultContextLoader(JarClassLoader jcl) {
        jclContext = new JclContext();
        this.jcl = jcl;
    }

    /**
     * Loads a single JCL instance in context
     *
     * @see JclContextLoader#loadContext()
     */
    public void loadContext() {
        jclContext.addJcl(DEFAULT_NAME, jcl);

        logger.info("Default JarClassLoader loaded into context.");
    }

    public void unloadContext() {
        destroy();
    }
}
