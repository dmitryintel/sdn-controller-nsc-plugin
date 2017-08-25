/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.controller.nsc.api;

import static java.util.Collections.singletonMap;
import static org.osc.sdk.controller.Constants.PLUGIN_NAME;
import static org.osc.sdk.controller.Constants.QUERY_PORT_INFO;
import static org.osc.sdk.controller.Constants.SUPPORT_FAILURE_POLICY;
import static org.osc.sdk.controller.Constants.SUPPORT_OFFBOX_REDIRECTION;
import static org.osc.sdk.controller.Constants.SUPPORT_PORT_GROUP;
import static org.osc.sdk.controller.Constants.SUPPORT_SFC;
import static org.osc.sdk.controller.Constants.USE_PROVIDER_CREDS;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_PASSWORD;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_URL;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_USER;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.osc.sdk.controller.FlowInfo;
import org.osc.sdk.controller.FlowPortInfo;
import org.osc.sdk.controller.Status;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.api.SdnRedirectionApi;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

@Component(configurationPid = "com.intel.nsc.SdnController",
        property = {
                PLUGIN_NAME + "=NSC",
                SUPPORT_OFFBOX_REDIRECTION + ":Boolean=false",
                SUPPORT_SFC + ":Boolean=false",
                SUPPORT_FAILURE_POLICY + ":Boolean=false",
                USE_PROVIDER_CREDS + ":Boolean=true",
                QUERY_PORT_INFO + ":Boolean=false",
                SUPPORT_PORT_GROUP + ":Boolean=false"},
        service={SdnControllerApi.class},
        immediate=true
        )
public class NeutronSdnControllerApi implements SdnControllerApi {

    @Reference(target="(osgi.local.enabled=true)")
    private TransactionControl txControl;

    @Reference(target="(osgi.unit.name=nsc-mgr)")
    private EntityManagerFactoryBuilder builder;

    @Reference(target="(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;

    @Reference(target="(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;

    private EntityManager em;

    Logger log = Logger.getLogger(NeutronSdnControllerApi.class);

    private final static String VERSION = "0.1";
    private final static String NAME = "NSC";

    private static final String DB_URL_PREFIX = "jdbc:h2:./nscPlugin_";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "admin123";

   
    public NeutronSdnControllerApi() {
    }
    
    @Activate
    public void activate(BundleContext context) {
    	// TODO
    	System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>>>>>>>>>>>> Activating " + getClass().getSimpleName());
    }

    @Override
    public Status getStatus(VirtualizationConnectorElement vc, String region) throws Exception {
        // TODO: Future. We should not rely on list ports instead we should send a valid status
        // based on is SDN controller ready to serve
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(vc))) {
//            neutronApi.test();
//        }
 
        return new Status(NAME, VERSION, true);
    }

    @Override
    public SdnRedirectionApi createRedirectionApi(VirtualizationConnectorElement vc, String region) {

    	if (vc == null || vc.getName() == null || vc.getName().length() == 0) {
    		throw new IllegalArgumentException("Non-null VC with non-empty name required!");
    	}

    	Properties props = new Properties();

    	props.setProperty(JDBC_URL, DB_URL_PREFIX + vc.getControllerIpAddress());        
    	props.setProperty(JDBC_USER, DB_USER);
    	props.setProperty(JDBC_PASSWORD, DB_PASSWORD);

    	DataSource ds = null;
    	try {
    		ds = this.jdbcFactory.createDataSource(props);
    	} catch (SQLException e) {
    		log.error(e);
    		return null; 
    	}

    	this.em = this.resourceFactory.getProviderFor(this.builder,
    			singletonMap("javax.persistence.nonJtaDataSource", (Object)ds), null)
    			.getResource(this.txControl);

    	return new NeutronSdnRedirectionApi(vc, region, txControl, em);
    }

    @Override
    public HashMap<String, FlowPortInfo> queryPortInfo(VirtualizationConnectorElement vc, String region,
                                                       HashMap<String, FlowInfo> portsQuery) throws Exception {
        throw new NotImplementedException("NSC SDN Controller does not support flow based query");
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }

}
