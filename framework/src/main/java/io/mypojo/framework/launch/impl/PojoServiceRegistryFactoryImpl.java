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

package io.mypojo.framework.launch.impl;

import io.mypojo.framework.PojoSR;
import io.mypojo.framework.PojoSRFrameworkImpl;
import io.mypojo.framework.launch.PojoServiceRegistry;
import io.mypojo.framework.launch.PojoServiceRegistryFactory;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.util.Map;

public class PojoServiceRegistryFactoryImpl implements PojoServiceRegistryFactory, FrameworkFactory {

    public PojoServiceRegistry newPojoServiceRegistry(Map configuration) throws Exception {
        return new PojoSR(configuration);
    }

    public Framework newFramework(Map configuration) {
        return new PojoSRFrameworkImpl(configuration);
    }
}
