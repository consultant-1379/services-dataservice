/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.core.MultivaluedMap;

import com.ericsson.eniq.events.server.logging.ServicesLogger;
import com.ericsson.eniq.events.server.services.DataService;
import com.ericsson.eniq.events.server.templates.mappingengine.TemplateMappingEngine;
import com.ericsson.eniq.events.server.templates.utils.TemplateUtils;
import com.ericsson.eniq.events.server.utils.techpacks.TechPackCXCMappingUtils;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Class responsible for reading and caching the CXC numbers for the tech packs.
 * The class reads the CXC numbers from the VERSIONING and TPACTIVATION tables in the repdb database, and
 * caches these for future look ups
 *
 * @author eemecoy
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.WRITE)
public class TechPackCXCMappingService {

    @EJB
    private DataService dataService;

    @EJB
    private TemplateUtils templateUtils;

    @EJB
    private TemplateMappingEngine templateMappingEngine;

    @EJB
    private TechPackCXCMappingUtils techPackCXCMappingUtils;

    private static final String TEMPLATE_PATH = "GET_TECH_PACK_LICENSE_NUMBERS";

    private Map<String, List<String>> techPackLicenseNumbers;

    @PostConstruct
    public void readTechPackLicenseNumbersFromDB() {
        final MultivaluedMap<String, String> requestParameters = new MultivaluedMapImpl();
        final String templateFile = templateMappingEngine.getTemplate(TEMPLATE_PATH, requestParameters, null);
        final String query = templateUtils.getQueryFromTemplate(templateFile);
        techPackLicenseNumbers = dataService.getTechPackLicenseNumbers(query);
        techPackCXCMappingUtils.setTechPackLicenseNumbers(techPackLicenseNumbers);
    }

    @PreDestroy
    public void clearTechPackLicenseNumbersFromDB() {
        techPackLicenseNumbers = null;
        techPackCXCMappingUtils.setTechPackLicenseNumbers(techPackLicenseNumbers);
    }

    public List<String> getTechPackCXCNumbers(final String techPackName) {
        final List<String> cxcNumbers = techPackLicenseNumbers.get(techPackName);
        if (cxcNumbers == null) {
            ServicesLogger.warn(this.getClass().toString(), "getTechPackCXCNumbers", "TechPackLicenseNumbers contains "
                    + techPackLicenseNumbers.size() + " entries");
            return new ArrayList<String>();
        }
        return cxcNumbers;
    }

    public void setDataService(final DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * @param templateUtils the templateUtils to set
     */
    public void setTemplateUtils(final TemplateUtils templateUtils) {
        this.templateUtils = templateUtils;
    }

    /**
     * @param templateMappingEngine the templateMappingEngine to set
     */
    public void setTemplateMappingEngine(final TemplateMappingEngine templateMappingEngine) {
        this.templateMappingEngine = templateMappingEngine;
    }

    /**
     * @param techPackCXCMappingUtils the techPackCXCMappingUtils to set
     */
    public void setTechPackCXCMappingUtils(final TechPackCXCMappingUtils techPackCXCMappingUtils) {
        this.techPackCXCMappingUtils = techPackCXCMappingUtils;
    }

}
