/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.impl;

import junit.framework.Assert;

/**
 * @author eemecoy
 *
 */
public class GroupDataServiceIntegrationTest {

    private final GroupDataService groupDataService = new GroupDataService();

    public void testUnknownGroup() {
        Assert.assertNull(groupDataService.getGroupDefinition("AAA"));
    }

}
