/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 * http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */

package org.fusesource.fabric.bridge.zk.internal;

import org.apache.activemq.pool.AmqJNDIPooledConnectionFactory;
import org.fusesource.fabric.api.FabricService;
import org.fusesource.fabric.bridge.internal.AbstractConnectorTestSupport;
import org.junit.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import javax.jms.ConnectionFactory;
import java.util.Hashtable;

/**
 * @author Dhiraj Bokde
 */
public class ZkManagedGatewayServiceFactoryTest extends AbstractConnectorTestSupport {

    private static final String CONNECTION_FACTORY_CLASS_NAME = ConnectionFactory.class.getName();
    private static final String DESTINATION_RESOLVER_CLASS_NAME = DestinationResolver.class.getName();
    private static final String REMOTE_FACTORY_FILTER = "(" + Constants.SERVICE_PID + "=remoteCF" + ")";

    private static ClassPathXmlApplicationContext applicationContextZkServer;
    // TODO modify test to connect to bridge and verify that messages can be exchanged
//    private static ClassPathXmlApplicationContext bridgeContext;

    private static FabricService fabricService;
    private static BundleContext bundleContext;
    private static final String TEST_PID = "test-gateway";
    private static final String SERVICE_PROPERTY = "service";

    private ZkManagedGatewayServiceFactory serviceFactory;

    @BeforeClass
    public static void setUpClass() {
        applicationContextZkServer = new ClassPathXmlApplicationContext("test-zkserver.xml");

        fabricService = applicationContextZkServer.getBean(FabricService.class);

        bundleContext = new MockBundleContext() {

            AmqJNDIPooledConnectionFactory localConnectionFactory = new AmqJNDIPooledConnectionFactory(TEST_LOCAL_BROKER_URL);
            AmqJNDIPooledConnectionFactory remoteConnectionFactory = new AmqJNDIPooledConnectionFactory(TEST_REMOTE_BROKER_URL);

            @Override
            public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                Hashtable<String, Object> properties = new Hashtable<String, Object>();
                if (CONNECTION_FACTORY_CLASS_NAME.equals(clazz)) {
                    if (REMOTE_FACTORY_FILTER.equals(filter)) {
                        properties.put(SERVICE_PROPERTY, remoteConnectionFactory);
                    } else  {
                        return null;
                    }
                } else if (DESTINATION_RESOLVER_CLASS_NAME.equals(clazz)) {
                    properties.put(SERVICE_PROPERTY, new DynamicDestinationResolver());
                } else {
                    return null;
                }
                return new ServiceReference[] { new MockServiceReference(null, properties, null) };
            }

            @Override
            public Object getService(ServiceReference reference) {
                return reference.getProperty(SERVICE_PROPERTY);
            }
        };

        // setup test bridge
//        bridgeContext = new ClassPathXmlApplicationContext("test-zkbridge-context.xml");
    }

    @AfterClass
    public static void tearDownClass() {
//        bridgeContext.destroy();
        applicationContextZkServer.destroy();
    }

    @Before
    public void setUp() {
        serviceFactory = new ZkManagedGatewayServiceFactory();
        serviceFactory.setFabricService(fabricService);
        serviceFactory.setBundleContext(bundleContext);
    }

    @After
    public void tearDown() throws Exception {
        serviceFactory.destroy();
        serviceFactory = null;
    }

    @Test
    public void testInit() throws Exception {
        serviceFactory.init();
    }

    @Test
    public void testUpdatedBrokerUrlBridge() throws Exception {
        // start
        serviceFactory.init();

        // create a simple broker URL based gateway
        Hashtable<String, String> properties = getDefaultConfig();
        properties.put("localBroker.brokerUrl", TEST_REMOTE_BROKER_URL);
        properties.put("exportedBroker.brokerUrl", TEST_REMOTE_BROKER_URL);

        serviceFactory.updated(TEST_PID, properties);

        // TODO assert that the gateway was started
    }

    @Test
    public void testUpdatedRefs() throws Exception {
        // start
        serviceFactory.init();

        // create a simple broker URL based gateway
        Hashtable<String, String> properties = getDefaultConfig();
        properties.put("localBroker.connectionFactoryRef", "remoteCF");
        properties.put("exportedBroker.connectionFactoryRef", "remoteCF");

        properties.put("localBroker.destinationResolverRef", "localResolver");
        properties.put("exportedBroker.destinationResolverRef", "localResolver");

        serviceFactory.updated(TEST_PID, properties);

        // TODO assert that the gateway was started
    }

    private Hashtable<String, String> getDefaultConfig() {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("versionName", "base");
        properties.put("profileName", "test-gateway");
        properties.put("inboundDestinationsRef", "upstream");
        properties.put("outboundDestinationsRef", "downstream");
        return properties;
    }

    @Test
    public void testDeleted() throws Exception {
        testUpdatedBrokerUrlBridge();

        serviceFactory.deleted(TEST_PID);
    }

}
