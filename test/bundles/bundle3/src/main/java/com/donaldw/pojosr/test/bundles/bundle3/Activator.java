package com.donaldw.pojosr.test.bundles.bundle3;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author donald-w
 */
public class Activator implements BundleActivator {
    private Logger logger = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Bundle 3");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Bundle 3");
    }
}

