/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.exclusivetacs;

import static com.ericsson.eniq.events.server.common.ApplicationConstants.*;

import java.util.*;

import javax.ejb.*;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;

import com.distocraft.dc5000.repository.cache.GroupTypeDef;
import com.distocraft.dc5000.repository.cache.GroupTypeKeyDef;
import com.ericsson.eniq.events.server.services.DataService;
import com.ericsson.eniq.events.server.templates.mappingengine.TemplateMappingEngine;
import com.ericsson.eniq.events.server.templates.utils.TemplateUtils;
import com.ericsson.eniq.events.server.utils.QueryUtils;
import com.ericsson.eniq.events.server.utils.RequestParametersWrapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Class responsible for some of the business logic around exclusive TAC handling See
 * http://atrclin2.athtem.eei.ericsson.se/wiki/index.php/ENIQ_Events_Services_Design_Rules#Design_Rules_for_Exclusive_TAC_handling for the rules
 * governing this class
 *
 * @author EEMECOY
 */
@Stateless
@Local
public class ExclusiveTACHandler {

    @EJB
    private TemplateUtils templateUtils;

    @EJB
    private DataService dataService;

    @EJB
    private TemplateMappingEngine templateMappingEngine;

    public boolean queryIsExclusiveTacRelated(final String groupName, final String tac) {
        if (isExclusiveTacGroup(groupName)) {
            return true;
        }
        if (isTacInExclusiveTacGroup(tac)) {
            return true;
        }
        return false;
    }

    public boolean isExclusiveTacGroup(final String groupName) {
        return EXCLUSIVE_TAC_GROUP_NAME.equalsIgnoreCase(groupName);
    }

    /**
     * is this TAC a member of the EXCLUSIVE_TAC group?
     *
     * @param tac
     * @return true if the TAC is a member of the EXCLUSIVE_TAC group, false otherwise
     */
    public boolean isTacInExclusiveTacGroup(final String tac) {

        if (StringUtils.isBlank(tac)) {
            return false;
        }

        final String drillType = null;
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        requestParameters.add(GROUP_NAME_PARAM, EXCLUSIVE_TAC_GROUP_NAME);
        requestParameters.add(TYPE_PARAM, TYPE_TAC);
        final Map<String, Object> templateParametersForGroup = new HashMap<String, Object>();
        addTACGroupInfoToTemplateParameters(templateParametersForGroup);
        templateParametersForGroup.put(GROUP_NAME_PARAM, EXCLUSIVE_TAC_GROUP_NAME);
        templateParametersForGroup.put(TAC_PARAM, tac);
        final String query = this.templateUtils.getQueryFromTemplate(templateMappingEngine.getTemplate(URI_PATH_GROUP, requestParameters, drillType),
                templateParametersForGroup);
        return this.dataService.isTacGroupMember(query);
    }

    private void addTACGroupInfoToTemplateParameters(final Map<String, Object> templateParams) {
        final GroupTypeDef groupDefinition = dataService.getGroupDefinition(TAC_PARAM_UPPER_CASE);
        final List<String> keys = new ArrayList<String>();
        keys.add(GroupTypeDef.KEY_NAME_GROUP_NAME);
        for (final GroupTypeKeyDef dataKey : groupDefinition.getDataKeys()) {
            keys.add(dataKey.getKeyName());
        }
        templateParams.put(GROUP_KEYS, keys);
        templateParams.put(GROUP_TABLE, groupDefinition.getTableName());
    }

    public void setQueryUtils(final QueryUtils queryUtils) {

    }

    /**
     * @param templateUtils
     *        the templateUtils to set
     */
    public void setTemplateUtils(final TemplateUtils templateUtils) {
        this.templateUtils = templateUtils;
    }

    /**
     * @param dataService
     *        the dataService to set
     */
    public void setDataService(final DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * @param templateMappingEngine
     *        the templateMappingEngine to set
     */
    public void setTemplateMappingEngine(final TemplateMappingEngine templateMappingEngine) {
        this.templateMappingEngine = templateMappingEngine;
    }

    public boolean queryIsExclusiveTacRelated(final MultivaluedMap<String, String> requestParameters) {
        final RequestParametersWrapper requestParametersWrapper = new RequestParametersWrapper(requestParameters);
        return queryIsExclusiveTacRelated(requestParametersWrapper.getGroupName(), requestParametersWrapper.getTacParamFromNodeOrTacParam());
    }

    /**
     * In most cases we should exclude all events that are related to a TAC that is in the EXCLUSIVE_TAC group. The exceptions are: 1. It is a
     * Terminal Group event analysis and the group name is EXCLUSIVE_TAC or 2. A TAC is explicitly requested from the UI. This is necessary to cater
     * for drilldowns from point 1.
     *
     * @param requestParameters
     *        request parameters passed from UI
     * @boolean true if template should apply tac exclusion, false otherwise
     * @deprecated This logic is used to populate a boolean in the SGEH event analysis templates which controls the flow of the template logic. This
     *             approach should not be used in velocity templates - there should be no business logic in templates. Additionally, this method
     *             doesn't look correct, it should consider the case where the TAC parameter is passed to the services layer with the NODE parameter
     *             rather than the TAC parameter.
     */
    @Deprecated
    public boolean shouldUseTACExclusionInSGEHEventAnalysisQuery(final MultivaluedMap<String, String> requestParameters) {
        boolean useTACExclusion = true;
        if (new RequestParametersWrapper(requestParameters).requestContainsTACParam()) {
            useTACExclusion = false;
        }
        if (requestContainsExclusiveTacGroup(requestParameters)) {
            useTACExclusion = false;
        }
        if (new RequestParametersWrapper(requestParameters).getTacParamFromNodeOrTacParam() != null) {
            useTACExclusion = false;
        }

        return useTACExclusion;

    }

    /**
     * Check to see if the requestParameters contains a groupname parameter with the value EXCLUSIVE_TAC. If it exists, we return true.
     *
     * @param requestParameter
     * @return true if there is a groupname parameter with value EXCLUSIVE_TAC. false otherwise
     */

    private boolean requestContainsExclusiveTacGroup(final MultivaluedMap<String, String> requestParameter) {
        if (requestParameter.containsKey(GROUP_NAME_PARAM)) {
            final String groupNameParam = requestParameter.getFirst(GROUP_NAME_PARAM);
            if (EXCLUSIVE_TAC_GROUP_NAME.equalsIgnoreCase(groupNameParam)) {
                return true;
            }
        }
        return false;
    }

}
