package com.ericsson.eniq.events.server.services.impl;

import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.ericsson.eniq.events.server.common.exception.ServiceException;
import com.ericsson.eniq.events.server.logging.ServicesLogger;
import com.ericsson.eniq.events.server.services.DataService;
import com.ericsson.eniq.events.server.templates.utils.TemplateUtils;
import com.ericsson.eniq.events.server.utils.RATDescriptionMappingUtils;

/**
 * Class which maintains a map of rat values to rat descriptions
 * This is retrieved from the database on start up, and stored for future use
 *
 * @author eavidat
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.WRITE)
public class RATDescriptionMappingsService {

    private static final String RAT_VALUES_AND_DESCRIPTIONS_VM = "topology/q_get_rat_values_and_descriptions.vm";

    @EJB
    private DataService dataService;

    @EJB
    private TemplateUtils templateUtils;

    @EJB
    protected RATDescriptionMappingUtils ratDescriptionMappingUtils;

    public static final int RETRY_WAIT_TIME = 5000;

    private static final int MAX_RETRY_COUNT = 15; // 75 seconds

    private int retryCount;

    @PostConstruct
    public void populateMapFromRATTableInDB() {
        retryCount = 0;
        while (ratDescriptionMappingUtils.getRatDescriptionMapping() == null) {
            try {
                final String query = this.templateUtils.getQueryFromTemplate(RAT_VALUES_AND_DESCRIPTIONS_VM);
                ratDescriptionMappingUtils.setRatMappings(this.dataService.getRATValuesAndDescriptions(query));
                ServicesLogger.detailed(Level.INFO, getClass().getName(), "populateMapFromRATTableInDB()",
                        "RAT Mappings populated from DB");
                return;
            } catch (final RuntimeException re) {
                ServicesLogger.detailed(Level.SEVERE, getClass().getName(), "populateMapFromRATTableInDB()",
                        "RAT Mappings NOT POPULATED from DB, reader not available.  Retrying in "
                                + (RETRY_WAIT_TIME / 1000) + " seconds... (" + (MAX_RETRY_COUNT - retryCount)
                                + " tries remaining");
                retryCount++;
                if (retryCount < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_WAIT_TIME);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    // We have exceeded our retry count so fail
                    throw new ServiceException(re);
                }
            }
        }
    }

    @PreDestroy
    public void clearRATMap() {
        retryCount = 0;
    }

    /**
     * provided for testing purposes
     *
     * @param templateUtils
     */
    public void setTemplateUtils(final TemplateUtils templateUtils) {
        this.templateUtils = templateUtils;
    }

    /**
     * provided for testing purposes
     *
     * @param dataService
     */
    public void setDataService(final DataService dataService) {
        this.dataService = dataService;
    }
}