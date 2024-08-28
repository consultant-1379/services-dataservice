/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.exclusivetacs;

import static com.ericsson.eniq.events.server.common.ApplicationConstants.*;
import static com.ericsson.eniq.events.server.test.common.ApplicationTestConstants.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author EEMECOY
 *
 */
public class ExclusiveTACHandlerTest {

    private ExclusiveTACHandler exclusiveTACHandler;

    @Before
    public void setup() {
        exclusiveTACHandler = new ExclusiveTACHandler();
    }

    @Test
    public void testShouldUseTACExclusionInSGEHEventAnalysisQuery_IsTrueIfARegularTacGroupIsSpecified() {
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        requestParameters.putSingle(GROUP_NAME_PARAM, SAMPLE_TAC_GROUP);
        assertThat(exclusiveTACHandler.shouldUseTACExclusionInSGEHEventAnalysisQuery(requestParameters), is(true));
    }

    @Test
    public void testShouldUseTACExclusionInSGEHEventAnalysisQuery_IsFalseIfTheExclusiveTacGroupIsSpecified() {
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        requestParameters.putSingle(GROUP_NAME_PARAM, EXCLUSIVE_TAC_GROUP_NAME);
        assertThat(exclusiveTACHandler.shouldUseTACExclusionInSGEHEventAnalysisQuery(requestParameters), is(false));
    }

    @Test
    public void testShouldUseTACExclusionInSGEHEventAnalysisQuery_IsTrueIfTheTacParamIsntIncluded() {
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        assertThat(exclusiveTACHandler.shouldUseTACExclusionInSGEHEventAnalysisQuery(requestParameters), is(true));
    }

    @Test
    public void testShouldUseTACExclusionInSGEHEventAnalysisQuery_IsFalseIfTheTacParamIsIncluded() {
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        requestParameters.putSingle(TAC_PARAM, SAMPLE_TAC_TO_STRING);
        assertThat(exclusiveTACHandler.shouldUseTACExclusionInSGEHEventAnalysisQuery(requestParameters), is(false));
    }

    @Test
    public void testIsExclusiveTACGroupIsTrueForExclusiveTacGroup() {
        assertThat(exclusiveTACHandler.isExclusiveTacGroup(EXCLUSIVE_TAC_GROUP), is(true));
    }

    @Test
    public void testIsExclusiveTACGroupIsTrueForExclusiveTacGroup_lowerCase() {
        assertThat(exclusiveTACHandler.isExclusiveTacGroup("exclusive_tac"), is(true));
    }

    @Test
    public void testIsExclusiveTACGroupIsTrueForNormalTacGroup() {
        assertThat(exclusiveTACHandler.isExclusiveTacGroup(SAMPLE_TAC_GROUP), is(false));
    }

}
