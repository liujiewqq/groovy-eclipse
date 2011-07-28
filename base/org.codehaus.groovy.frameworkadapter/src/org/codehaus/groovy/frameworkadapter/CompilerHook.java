/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.frameworkadapter;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.internal.baseadaptor.StateManager;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.DisabledInfo;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The compiler hook is in charge of determining what 
 * the compiler level is for this eclipse instance.
 * 
 * As a command line argument, or in the eclipse.ini file, 
 * specify -Dgroovy.compiler.level=18 or
 * -Dgroovy.compiler.level=17 to force the compiler level one
 * way or the other.
 * @author Andrew Eisenberg
 * @created Jul 13, 2011
 */
@SuppressWarnings("deprecation")
public class CompilerHook implements HookConfigurator, AdaptorHook {
    enum SpecifiedVersion { 
        _16(6), _17(7), _18(8), _19(9), _20(0), UNSPECIFIED(-1); 
        final int minorVersion;
        SpecifiedVersion(int minorVersion) {
            this.minorVersion = minorVersion;
        }
        
        static SpecifiedVersion find() {
            String compilerLevel = FrameworkProperties.getProperty("groovy.compiler.level");
            
            if (compilerLevel == null) {
                return UNSPECIFIED;
            }
            
            if ("16".equals(compilerLevel) || "1.6".equals(compilerLevel)) {
                return _16;
            }
            if ("17".equals(compilerLevel) || "1.7".equals(compilerLevel)) {
                return _17;
            }
            if ("18".equals(compilerLevel) || "1.8".equals(compilerLevel)) {
                return _18;
            }
            if ("19".equals(compilerLevel) || "1.9".equals(compilerLevel)) {
                return _19;
            }
            if ("20".equals(compilerLevel) || "2.0".equals(compilerLevel)) {
                return _20;
            }
            
            // this is an error prevent startup
            throw new IllegalArgumentException("Invalid Groovy compiler level specified: " + compilerLevel + 
                    "\nMust be one of 16, 1.6, 17, 1.7, 18, 1.8, 19, 1.9, 20, or 2.0");
        }
    }
    private static final String GROOVY_PLUGIN_ID = "org.codehaus.groovy";
    
    private final SpecifiedVersion version;
    
    private BaseAdaptor adapter;
    
    private boolean versionFound = false;

    public CompilerHook() {
        version = SpecifiedVersion.find();
    }
    
    public void initialize(BaseAdaptor adaptor) {
        this.adapter = adaptor;
    }

    
    public void frameworkStart(BundleContext context) throws BundleException {
        if (version == SpecifiedVersion.UNSPECIFIED) {
            return;
        }
        
        System.out.println("Starting Groovy-Eclipse compiler hook.  Specified compiler level: " + version);
        
        // ServiceReference is parameterized in 3.7, not 3.6
        final ServiceReference serviceReference = context
                .getServiceReference(PackageAdmin.class.getName());
        final PackageAdmin packageAdmin = (PackageAdmin) context
                .getService(serviceReference);
        
        State state = ((StateManager) adapter.getPlatformAdmin()).getSystemState();
        BundleDescription[] disabledBundles = state.getDisabledBundles();
        List<Bundle> bundlesToRefresh = new ArrayList<Bundle>();
        for (BundleDescription bundle : disabledBundles) {
            if (bundle.getSymbolicName().equals(GROOVY_PLUGIN_ID)) {
                handleBundle(bundle, state, context);
                bundlesToRefresh.add(getBundle(bundle, context));
            }
        }
        
        BundleDescription[] bundles = state.getBundles(GROOVY_PLUGIN_ID);
        for (BundleDescription bundle : bundles) {
            handleBundle(bundle, state, context);
            bundlesToRefresh.add(getBundle(bundle, context));
        }
        
        checkVersionFound(bundlesToRefresh);
        
        Bundle[] allBundles = bundlesToRefresh.toArray(new Bundle[0]);

        state.resolve(bundles);
        state.resolve(disabledBundles);
        
        packageAdmin.refreshPackages(allBundles);
    }

    /**
     * @param bundle
     * @return
     */
    private Bundle getBundle(BundleDescription bundle, BundleContext context) {
        return context.getBundle(bundle.getBundleId());
    }


    private void checkVersionFound(List<Bundle> bundlesToRefresh) {
        if (!versionFound) {
            throw new IllegalArgumentException("Specified version of Groovy not found. Available versions are: " + toVersionNumbers(bundlesToRefresh));
        }
    }

    private String toVersionNumbers(List<Bundle> bundlesToRefresh) {
        StringBuilder sb = new StringBuilder();
        for (Bundle bundle : bundlesToRefresh) {
            sb.append(bundle.getVersion() + "\n");
        }
        return sb.toString();
    }

    private void handleBundle(BundleDescription bundle, State state, BundleContext context) throws BundleException {
        if (bundle.getVersion().getMinor() == version.minorVersion) {
            getBundle(bundle, context).start();
            adapter.getPlatformAdmin().removeDisabledInfo(createDisabledInfo(state, bundle));
            versionFound = true;
        } else {
            getBundle(bundle, context).stop();
            adapter.getPlatformAdmin().addDisabledInfo(createDisabledInfo(state, bundle));
        }
    }

    private static DisabledInfo createDisabledInfo(State state, BundleDescription bundle) {
        DisabledInfo info = new DisabledInfo(
                "org.eclipse.pde.ui",
                "Disabled via PDE", bundle);
        return info;
    }

    public void addHooks(HookRegistry hookRegistry) {
        hookRegistry.addAdaptorHook(this);
    }

    public void frameworkStop(BundleContext context) throws BundleException { }
    public void frameworkStopping(BundleContext context) { }
    public void addProperties(Properties properties) { }
    public URLConnection mapLocationToURLConnection(String location)
            throws IOException {
        return null;
    }
    public void handleRuntimeError(Throwable error) { }
    public FrameworkLog createFrameworkLog() {
        return null;
    }
}
