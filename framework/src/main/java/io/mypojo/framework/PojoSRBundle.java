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

package io.mypojo.framework;

import io.mypojo.felix.framework.ServiceRegistry;
import io.mypojo.felix.framework.util.EventDispatcher;
import io.mypojo.felix.framework.util.MapToDictionary;
import io.mypojo.felix.framework.util.StringMap;
import io.mypojo.framework.revision.Revision;
import org.osgi.framework.*;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.*;

@SuppressWarnings("PackageAccessibility")
public class PojoSRBundle implements Bundle, BundleRevisions, BundleRevision {
    private final Revision m_revision;
    private final Map<String, String> m_manifest;
    private final Version m_version;
    private final String m_location;
    private final Map<Long, Bundle> m_bundles;
    private final ServiceRegistry m_reg;
    private final String m_activatorClass;
    private final long m_id;
    private final String m_symbolicName;
    private final EventDispatcher m_dispatcher;
    private final ClassLoader m_loader;
    private final Map m_config;
    private final Map<String, Map<String, String>> m_cachedHeaders = new HashMap<>();
    volatile int m_state = Bundle.RESOLVED;
    volatile BundleContext m_context = null;
    private volatile BundleActivator m_activator = null;
    private long m_cachedHeadersTimestamp;

    public PojoSRBundle(Revision revision, Map<String, String> manifest,
                        Version version, String location, ServiceRegistry reg,
                        EventDispatcher dispatcher, String activatorClass, long id,
                        String symbolicName, Map<Long, Bundle> bundles, ClassLoader loader, Map config) {
        m_revision = revision;
        m_manifest = manifest;
        m_version = version;
        m_location = location;
        m_reg = reg;
        m_dispatcher = dispatcher;
        m_activatorClass = activatorClass;
        m_id = id;
        m_symbolicName = symbolicName;
        bundles.put(m_id, this);
        m_bundles = bundles;
        m_loader = loader;
        m_config = config;
    }

    private static List<String> createLocalizationResourceList(String basename, String locale) {
        List<String> result = new ArrayList<>(4);

        StringTokenizer tokens;
        StringBuilder tempLocale = new StringBuilder(basename);

        result.add(tempLocale.toString());

        if (locale.length() > 0) {
            tokens = new StringTokenizer(locale, "_");
            while (tokens.hasMoreTokens()) {
                tempLocale.append("_").append(tokens.nextToken());
                result.add(tempLocale.toString());
            }
        }
        return result;
    }

    Revision getRevision() {
        return m_revision;
    }

    public int getState() {
        return m_state;
    }

    public void start(int options) throws BundleException {
        // TODO: lifecycle - fix this
        start();
    }

    public synchronized void start() throws BundleException {
        if (m_state != Bundle.RESOLVED) {
            if (m_state == Bundle.ACTIVE) {
                return;
            }
            throw new BundleException("Bundle is in wrong state for start");
        }
        try {
            m_state = Bundle.STARTING;

            m_context = new PojoSRBundleContext(this, m_reg, m_dispatcher,
                    m_bundles, m_config);
            m_dispatcher.fireBundleEvent(new BundleEvent(BundleEvent.STARTING,
                    this));
            if (m_activatorClass != null) {
                m_activator = (BundleActivator) m_loader.loadClass(
                        m_activatorClass).newInstance();
                m_activator.start(m_context);
            }
            m_state = Bundle.ACTIVE;
            m_dispatcher.fireBundleEvent(new BundleEvent(BundleEvent.STARTED,
                    this));
        } catch (Throwable ex) {
            m_state = Bundle.RESOLVED;
            m_activator = null;
            m_dispatcher.fireBundleEvent(new BundleEvent(BundleEvent.STOPPED,
                    this));
            throw new BundleException("Unable to start bundle", ex);
        }
    }

    public void stop(int options) throws BundleException {
        // TODO: lifecycle - fix this
        stop();
    }

    public synchronized void stop() throws BundleException {
        if (m_state != Bundle.ACTIVE) {
            if (m_state == Bundle.RESOLVED) {
                return;
            }
            throw new BundleException("Bundle is in wrong state for stop");
        }
        try {
            m_state = Bundle.STOPPING;
            m_dispatcher.fireBundleEvent(new BundleEvent(BundleEvent.STOPPING,
                    this));
            if (m_activator != null) {
                m_activator.stop(m_context);
            }
        } catch (Throwable ex) {
            throw new BundleException("Error while stopping bundle", ex);
        } finally {
            m_reg.unregisterServices(this);
            m_dispatcher.removeListeners(m_context);
            m_activator = null;
            m_context = null;
            m_state = Bundle.RESOLVED;
            m_dispatcher.fireBundleEvent(new BundleEvent(BundleEvent.STOPPED,
                    this));
        }
    }

    public void update(InputStream input) throws BundleException {
        throw new BundleException("pojosr bundles can't be updated");
    }

    public void update() throws BundleException {
        throw new BundleException("pojosr bundles can't be updated");
    }

    public void uninstall() throws BundleException {
        throw new BundleException("pojosr bundles can't be uninstalled");
    }

    public Dictionary<String, String> getHeaders() {
        return getHeaders(Locale.getDefault().toString());
    }

    public long getBundleId() {
        return m_id;
    }

    public String getLocation() {
        return m_location;
    }

    public ServiceReference[] getRegisteredServices() {
        return m_reg.getRegisteredServices(this);
    }

    public ServiceReference[] getServicesInUse() {
        return m_reg.getServicesInUse(this);
    }

    public boolean hasPermission(Object permission) {
        // TODO: security - fix this
        return true;
    }

    public URL getResource(String name) {
        // TODO: module - implement this based on the revision
        URL result = m_loader.getResource(name);
        return result;
    }

    public Dictionary<String, String> getHeaders(String locale) {
        return new MapToDictionary(getCurrentLocalizedHeader(locale));
    }

    Map<String, String> getCurrentLocalizedHeader(String locale) {
        Map<String, String> result = null;

        // Spec says empty local returns raw headers.
        if ((locale == null) || (locale.length() == 0)) {
            result = new StringMap(m_manifest, false);
        }

        // If we have no result, try to get it from the cached headers.
        if (result == null) {
            synchronized (m_cachedHeaders) {
                // If the bundle is uninstalled, then the cached headers should
                // only contain the localized headers for the default locale at
                // the time of uninstall, so just return that.
                if (getState() == Bundle.UNINSTALLED) {
                    result = m_cachedHeaders.values().iterator().next();
                }
                // If the bundle has been updated, clear the cached headers.
                else if (getLastModified() > m_cachedHeadersTimestamp) {
                    m_cachedHeaders.clear();
                }
                // Otherwise, returned the cached headers if they exist.
                else {
                    // Check if headers for this locale have already been
                    // resolved
                    if (m_cachedHeaders.containsKey(locale)) {
                        result = m_cachedHeaders.get(locale);
                    }
                }
            }
        }

        // If the requested locale is not cached, then try to create it.
        if (result == null) {
            // Get a modifiable copy of the raw headers.
            Map<String, String> headers = new StringMap(m_manifest, false);
            // Assume for now that this will be the result.
            result = headers;

            // Check to see if we actually need to localize anything
            boolean localize = false;
            for (Iterator it = headers.values().iterator(); !localize
                    && it.hasNext(); ) {
                if (((String) it.next()).startsWith("%")) {
                    localize = true;
                }
            }

            if (!localize) {
                // If localization is not needed, just cache the headers and
                // return
                // them as-is. Not sure if this is useful
                updateHeaderCache(locale, headers);
            } else {
                // Do localization here and return the localized headers
                String basename = (String) headers
                        .get(Constants.BUNDLE_LOCALIZATION);
                if (basename == null) {
                    basename = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
                }

                // Create ordered list of files to load properties from
                List<String> resourceList = createLocalizationResourceList(basename,
                        locale);

                // Create a merged props file with all available props for this
                // locale
                boolean found = false;
                Properties mergedProperties = new Properties();
                for (String aResourceList : resourceList) {
                    URL temp = m_revision.getEntry(aResourceList + ".properties");
                    if (temp != null) {
                        found = true;
                        try {
                            mergedProperties.load(temp.openConnection().getInputStream());
                        } catch (IOException ex) {
                            // File doesn't exist, just continue loop
                        }
                    }
                }

                // If the specified locale was not found, then the spec says we
                // should
                // return the default localization.
                if (!found && !locale.equals(Locale.getDefault().toString())) {
                    result = getCurrentLocalizedHeader(Locale.getDefault().toString());
                }
                // Otherwise, perform the localization based on the discovered
                // properties and cache the result.
                else {
                    // Resolve all localized header entries
                    for (Iterator it = headers.entrySet().iterator(); it
                            .hasNext(); ) {
                        Map.Entry entry = (Map.Entry) it.next();
                        String value = (String) entry.getValue();
                        if (value.startsWith("%")) {
                            String newvalue;
                            String key = value
                                    .substring(value.indexOf("%") + 1);
                            newvalue = mergedProperties.getProperty(key);
                            if (newvalue == null) {
                                newvalue = key;
                            }
                            entry.setValue(newvalue);
                        }
                    }

                    updateHeaderCache(locale, headers);
                }
            }
        }

        return result;
    }

    private void updateHeaderCache(String locale, Map<String, String> localizedHeaders) {
        synchronized (m_cachedHeaders) {
            m_cachedHeaders.put(locale, localizedHeaders);
            m_cachedHeadersTimestamp = System.currentTimeMillis();
        }
    }

    public String getSymbolicName() {
        return m_symbolicName;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return m_loader.loadClass(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        // TODO: module - implement this based on the revision
        return m_loader.getResources(name);
    }

    public Enumeration<String> getEntryPaths(String path) {
        return new EntryFilterEnumeration<>(m_revision, false, path, null, false, false);
    }

    public URL getEntry(String path) {
        return m_revision.getEntry(path);
    }

    public long getLastModified() {
        return m_revision.getLastModified();
    }

    public Enumeration<URL> findEntries(String path, String filePattern,
                                        boolean recurse) {
        // TODO: module - implement this based on the revision
        return new EntryFilterEnumeration<>(m_revision, false, path, filePattern, recurse, true);
    }

    public BundleContext getBundleContext() {
        return m_context;
    }

    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
        // TODO: security - fix this
        return new HashMap<>();
    }

    public Version getVersion() {
        return m_version;
    }

    public boolean equals(Object o) {
        return (o instanceof PojoSRBundle) && ((PojoSRBundle) o).m_id == m_id;
    }

    public int compareTo(Bundle o) {
        long thisBundleId = this.getBundleId();
        long thatBundleId = o.getBundleId();
        return (thisBundleId < thatBundleId ? -1 : (thisBundleId == thatBundleId ? 0 : 1));
    }

    public <A> A adapt(Class<A> type) {
        if (type == BundleStartLevel.class) {
            return (A) new BundleStartLevel() {

                public Bundle getBundle() {
                    return PojoSRBundle.this;
                }

                public int getStartLevel() {
                    // TODO Implement this?
                    return 1;
                }

                public void setStartLevel(int startlevel) {
                    // TODO Implement this?
                }

                public boolean isPersistentlyStarted() {
                    return true;
                }

                public boolean isActivationPolicyUsed() {
                    return false;
                }
            };
        } else if (type == BundleRevisions.class) {
            return (A) this;
        } else if (type == BundleWiring.class) {
            return (A) this.getWiring();
        }
        return null;
    }

    public File getDataFile(String filename) {
        return m_context.getDataFile(filename);
    }

    public String toString() {
        String sym = getSymbolicName();
        if (sym != null) {
            return sym + " [" + getBundleId() + "]";
        }
        return "[" + getBundleId() + "]";
    }

    public Bundle getBundle() {
        return this;
    }

    public List<BundleRevision> getRevisions() {
        return Collections.singletonList((BundleRevision) this);
    }

    public List<BundleCapability> getDeclaredCapabilities(String namespace) {
        return Collections.emptyList();
    }

    public List<BundleRequirement> getDeclaredRequirements(String namespace) {
        return Collections.emptyList();
    }

    public int getTypes() {
        if (getHeaders().get(Constants.FRAGMENT_HOST) != null) {
            return BundleRevision.TYPE_FRAGMENT;
        }
        return 0;
    }

    public BundleWiring getWiring() {
        return new BundleWiring() {

            public Bundle getBundle() {
                return PojoSRBundle.this;
            }

            public Collection<String> listResources(String path, String filePattern, int options) {
                Collection<String> result = new ArrayList<>();
                for (URL u : findEntries(path, filePattern, options)) {
                    result.add(u.toString());
                }
                // TODO: implement this
                return result;
            }

            public boolean isInUse() {
                return true;
            }

            public boolean isCurrent() {
                return true;
            }

            public BundleRevision getRevision() {
                return PojoSRBundle.this;
            }

            public List<BundleRequirement> getRequirements(String namespace) {
                return getDeclaredRequirements(namespace);
            }

            public List<BundleWire> getRequiredWires(String namespace) {
                return Collections.emptyList();
            }

            public List<BundleWire> getProvidedWires(String namespace) {
                return Collections.emptyList();
            }

            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }

            public List<BundleCapability> getCapabilities(String namespace) {
                return Collections.emptyList();
            }

            public List<URL> findEntries(String path, String filePattern, int options) {
                List<URL> result = new ArrayList<>();
                for (Enumeration<URL> e = PojoSRBundle.this.findEntries(path, filePattern, options == BundleWiring.FINDENTRIES_RECURSE); e.hasMoreElements(); ) {
                    result.add(e.nextElement());
                }
                return result;
            }
        };
    }

    @SuppressWarnings("PackageAccessibility")
    public static class PojoSRInternals {
        public final Map<String, Bundle> m_symbolicNameToBundle = new HashMap<>();
        public final EventDispatcher m_dispatcher;
        public final ServiceRegistry m_reg = new ServiceRegistry(
                new ServiceRegistry.ServiceRegistryCallbacks() {
                    public void serviceChanged(ServiceEvent event,
                                               Dictionary oldProps) {
                        m_dispatcher.fireServiceEvent(event, oldProps, null);
                    }
                });
        public final Map<Long, Bundle> m_bundles = new HashMap<>();
        public final Map bundleConfig = new HashMap();
        public BundleContext m_context;

        public PojoSRInternals() {
            // Done here rather than at initialisation because of an IntelliJ bug where Rearrange Code places this before
            // m_reg initialisation, which creates an 'Illegal Forward Reference' at compile time.
            this.m_dispatcher = new EventDispatcher(m_reg);
        }
    }
}
