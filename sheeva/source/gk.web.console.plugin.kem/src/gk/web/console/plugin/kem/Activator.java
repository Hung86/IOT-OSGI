/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gk.web.console.plugin.kem;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import gk.web.console.plugin.kem.adapter.realtimedata.AdapterRealTimeDataServlet;
import gk.web.console.plugin.kem.adapter.settings.AdapterSettingsServlet;
import gk.web.console.plugin.kem.device.settings.DeviceSettingsServlet;
import gk.web.console.plugin.kem.file.KEMLogServlet;
import gk.web.console.plugin.kem.gateway.coresettings.GatewayCoreSettingsServlet;
import gk.web.console.plugin.kem.gateway.networksettings.GatewayNetworkSettingsServlet;
import gk.web.console.plugin.kem.status.GatewayStatusServlet;

/**
 * Activator is the main starting class.
 */
public class Activator implements BundleActivator
{
    private List webConsolePlugins = new ArrayList();
	private KemObjectsServiceTracker ASTHandler = new KemObjectsServiceTracker();


    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public final void start(BundleContext context) throws Exception
    {
        if(webConsolePlugins.size() == 0)
        {
        	ASTHandler.activate(context);
        	//webConsolePlugins.add((WebConsolePlugin) new WebConsolePlugin().register(context));
    		webConsolePlugins.add( new KEMLogServlet().register(context));
    		//webConsolePlugins.add( new StatusServlet().register(context));
    		//webConsolePlugins.add( new BundlesServlet().register(context));
    		webConsolePlugins.add( new GatewayStatusServlet(ASTHandler).register(context));
    		webConsolePlugins.add( new AdapterSettingsServlet(ASTHandler).register(context));
    		webConsolePlugins.add( new AdapterRealTimeDataServlet(ASTHandler).register(context));
    		//webConsolePlugins.add( new DeviceConfigServlet(context).register(context));
    		webConsolePlugins.add( new GatewayNetworkSettingsServlet().register(context));
    		webConsolePlugins.add( new GatewayCoreSettingsServlet(ASTHandler).register(context));
    		webConsolePlugins.add( new DeviceSettingsServlet(ASTHandler).register(context));
    		//webConsolePlugins.add( new EnergetixSensorServlet(ASTHandler).register(context));
        }
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public final void stop(BundleContext context) throws Exception
    {
    	if(webConsolePlugins != null){
    		for(int i = 0;webConsolePlugins.size() > i;i++){
            	SimpleWebConsolePlugin plugin;
            	plugin = (SimpleWebConsolePlugin) webConsolePlugins.get(i);
            	plugin.unregister();
        	}
        	webConsolePlugins.clear();
        	webConsolePlugins = null;
        	ASTHandler.deactivate();
    	}
    }
}
