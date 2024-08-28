/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.utils;

import static com.ericsson.eniq.events.server.common.ApplicationConfigConstants.*;
import static com.ericsson.eniq.events.server.common.ApplicationConstants.*;

import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedMap;

import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory;

/**
 * Class to handle the decision logic around selecting a load balancing policy to use for a particular query
 * 
 * @author eemecoy
 *
 */
@Stateless
@Local
public class LoadBalancingPolicyService {

    @Resource(name = ENIQ_EVENT_PROPERTIES)
    private Properties eniqEventsProperties;

    static final String ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES = "ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES";

    @EJB
    private LoadBalancingPolicyFactory loadBalancingPolicyFactory;

    /**
     * Fetch the appropriate LoadBalancingPolicy If the property
     * ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES is set to false, then no load
      * balancing policy is used
      * 
     * Otherwise this is determined based on the type of query - if the query is
     * IMSI based, then a specific IMSILoadBalancingPolicy is returned Otherwise
     * the default policy (round robin) is returned.
      *
     * @param requestParameters
     *          requestParameters from URL
      * @return the load balancing policy
      */
    public LoadBalancingPolicy getLoadBalancingPolicy(final MultivaluedMap<String, String> requestParameters) {

        final boolean useLoadBalancingPolicies = Boolean.valueOf((String) eniqEventsProperties
                .get(ENIQ_EVENTS_USE_LOAD_BALANCING_POLICIES));

        if (!useLoadBalancingPolicies) {
            return loadBalancingPolicyFactory.getNoLoadBalancingPolicy();
        }

        final String imsi = requestParameters.getFirst(IMSI_PARAM);
        if (imsi == null) {
            return loadBalancingPolicyFactory.getRoundRobinLoadBalancingPolicy();
        }
        return loadBalancingPolicyFactory.getImsiLoadBalancingPolicy(imsi);
    }

    /**
     * exposed for unit test.
     *
    * @param eniqEventsProperties
    *          the eniqEventsProperties to set
    */
    public void setEniqEventsProperties(final Properties eniqEventsProperties) {
        this.eniqEventsProperties = eniqEventsProperties;
    }

    /**
     * Sets the load balancing policy factory.
     *
    * @param loadBalancingPolicyFactory
    *          the load balancing policy factory
    */
    public void setLoadBalancingPolicyFactory(final LoadBalancingPolicyFactory loadBalancingPolicyFactory) {
        this.loadBalancingPolicyFactory = loadBalancingPolicyFactory;
    }

}
