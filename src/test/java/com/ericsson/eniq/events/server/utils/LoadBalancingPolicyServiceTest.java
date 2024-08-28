/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.utils;

import static com.ericsson.eniq.events.server.common.ApplicationConstants.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Properties;

import javax.ws.rs.core.MultivaluedMap;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.events.server.datasource.loadbalancing.IMSILoadBalancingPolicy;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory;
import com.ericsson.eniq.events.server.datasource.loadbalancing.NoLoadBalancingPolicy;
import com.ericsson.eniq.events.server.datasource.loadbalancing.RoundRobinLoadBalancingPolicy;
import com.ericsson.eniq.events.server.test.common.BaseJMockUnitTest;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author eemecoy
 *
 */
public class LoadBalancingPolicyServiceTest extends BaseJMockUnitTest {

    private LoadBalancingPolicyService objToTest;

    LoadBalancingPolicyFactory mockedLoadBalancingPolicyFactory;

    Properties mockedEniqEventsProperties;

    RoundRobinLoadBalancingPolicy roundRobinLoadBalancingPolicy;

    @Before
    public void setup() {
        mockedEniqEventsProperties = mockery.mock(Properties.class);
        mockedLoadBalancingPolicyFactory = mockery.mock(LoadBalancingPolicyFactory.class);
        roundRobinLoadBalancingPolicy = mockery.mock(RoundRobinLoadBalancingPolicy.class);

        objToTest = new LoadBalancingPolicyService();
        objToTest.setEniqEventsProperties(mockedEniqEventsProperties);
        objToTest.setLoadBalancingPolicyFactory(mockedLoadBalancingPolicyFactory);

        allowGetsOnLoadBalancingPolicyFactory();
    }

    private void allowGetsOnLoadBalancingPolicyFactory() {
        mockery.checking(new Expectations() {
            {
                allowing(mockedLoadBalancingPolicyFactory).getRoundRobinLoadBalancingPolicy();
                will(returnValue(roundRobinLoadBalancingPolicy));
            }
        });

    }

    @Test
    public void testGetLoadBalancingPolicyWhenItsTurnedOff() {
        setProperty(LoadBalancingPolicyService.ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES, "false");
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        final LoadBalancingPolicy mockedNoLoadBalancingPolicy = expectGetOnFactoryForNoLoadBalancingPolicy();
        final LoadBalancingPolicy result = objToTest.getLoadBalancingPolicy(requestParameters);
        assertThat(result, is(mockedNoLoadBalancingPolicy));
    }

    private LoadBalancingPolicy expectGetOnFactoryForNoLoadBalancingPolicy() {

        final LoadBalancingPolicy noLoadBalancingPolicy = mockery.mock(NoLoadBalancingPolicy.class);
        mockery.checking(new Expectations() {
            {
                one(mockedLoadBalancingPolicyFactory).getNoLoadBalancingPolicy();
                will(returnValue(noLoadBalancingPolicy));

            }
        });
        return noLoadBalancingPolicy;
    }

    @Test
    public void testGetLoadBalancingPolicyForIMSIGroupsIsDefaultPolicy() {
        setProperty(LoadBalancingPolicyService.ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES, "true");
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        requestParameters.putSingle(TYPE_PARAM, TYPE_IMSI);

        final LoadBalancingPolicy result = objToTest.getLoadBalancingPolicy(requestParameters);
        assertThat(result, is((LoadBalancingPolicy) roundRobinLoadBalancingPolicy));
    }

    private void setProperty(final String propertyName, final String propertyValue) {
        mockery.checking(new Expectations() {
            {
                one(mockedEniqEventsProperties).get(propertyName);
                will(returnValue(propertyValue));

            }
        });

    }

    @Test
    public void testGetLoadBalancingPolicyForPlainQuery() {
        setProperty(LoadBalancingPolicyService.ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES, "true");
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        requestParameters.putSingle(TYPE_PARAM, TYPE_APN);

        final LoadBalancingPolicy result = objToTest.getLoadBalancingPolicy(requestParameters);
        assertThat(result, is((LoadBalancingPolicy) roundRobinLoadBalancingPolicy));
    }

    @Test
    public void testGetLoadBalancingPolicyForIMSI() {
        setProperty(LoadBalancingPolicyService.ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES, "true");
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        requestParameters.putSingle(TYPE_PARAM, TYPE_IMSI);
        final String imsi = "208020019901503";
        requestParameters.putSingle(IMSI_PARAM, imsi);
        final LoadBalancingPolicy mockedImsiLoadBalancingPolicy = expectGetForIMSILoadBalancingPolicy(imsi);
        final LoadBalancingPolicy result = objToTest.getLoadBalancingPolicy(requestParameters);
        assertThat(result, is(mockedImsiLoadBalancingPolicy));
    }

    private IMSILoadBalancingPolicy expectGetForIMSILoadBalancingPolicy(final String imsi) {
        final IMSILoadBalancingPolicy mockedImsiLoadBalancingPolicy = mockery.mock(IMSILoadBalancingPolicy.class);
        mockery.checking(new Expectations() {
            {
                one(mockedLoadBalancingPolicyFactory).getImsiLoadBalancingPolicy(imsi);
                will(returnValue(mockedImsiLoadBalancingPolicy));

            }
        });
        return mockedImsiLoadBalancingPolicy;
    }

}
