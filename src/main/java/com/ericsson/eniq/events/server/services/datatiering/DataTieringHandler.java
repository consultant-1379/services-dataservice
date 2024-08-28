/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.eniq.events.server.services.datatiering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedMap;

import com.ericsson.eniq.events.server.common.TechPackRepresentation;
import com.ericsson.eniq.events.server.utils.DateTimeUtils;
import com.ericsson.eniq.events.server.utils.FormattedDateTimeRange;
import com.ericsson.eniq.events.server.utils.config.ApplicationConfigManager;



import static com.ericsson.eniq.events.server.common.ApplicationConstants.KEY_DATA_TIERED_DELAY;
import static com.ericsson.eniq.events.server.common.ApplicationConstants.TRUE;
import static com.ericsson.eniq.events.server.common.TechPackData.EVENT_E_LTE;
import static com.ericsson.eniq.events.server.common.TechPackData.EVENT_E_RAN_CFA;
import static com.ericsson.eniq.events.server.common.TechPackData.EVENT_E_SGEH;
import static com.ericsson.eniq.events.server.common.TechPackData.EVENT_E_USER_PLANE;

@Stateless
@Local
public class DataTieringHandler {

    @EJB
    private ApplicationConfigManager applicationConfigManager;

    static List<String> applicableTechpacks = new ArrayList<String>();

    static {
        applicableTechpacks.add(EVENT_E_SGEH);
        applicableTechpacks.add(EVENT_E_LTE);
        applicableTechpacks.add(EVENT_E_RAN_CFA);
        applicableTechpacks.add(EVENT_E_USER_PLANE);
    }

    /**
     * This method will return true if queries <=2 hours where service(implementing GenericService) class
     * is data tiering applicable.
     *
     * @param formattedDateTimeRange
     * @param isDataTieredService
     * @param licensedTechPacks
     *
     * @return true/false
     */
    public boolean useDataTieringView(final FormattedDateTimeRange formattedDateTimeRange,
            final boolean isDataTieredService, final Collection<TechPackRepresentation> techPackList) {
        return DateTimeUtils.rawEventRangeForTimeSeriesChartWihtoutOneMinAggregate(formattedDateTimeRange.getRangeInMinutes()) && isDataTieredService && isDataTieringTechPack(techPackList);
    }

    /**
     * This method will return true if we need to apply delays on times of queries
     * to align with data loading time between error raw and 15min success tables.
     * There are TWO possible use cases of this.
     * <p/>
     * A. queries <=2 hours where service(implementing GenericService) class is data tiering applicable.
     * B. drilldown from where we already applied delays
     * <p/>
     * Please also see #DataTieredDateTimeRange.java class how to adjust timerange in case datatiering considered
     *
     * @param formattedDateTimeRange
     * @param isDataTieredService
     * @param licensedTechPacks
     * @param parameters             from URL
     *
     * @return true/false
     */
    public boolean appplyLatencyForDataTiering(final FormattedDateTimeRange formattedDateTimeRange,
            final boolean isDataTieredService, final List<String> licensedTechPacks,
            final MultivaluedMap<String, String> parameters) {
        if (isDataTieredService) {
            return DateTimeUtils.rawEventRangeForTimeSeriesChartWihtoutOneMinAggregate(formattedDateTimeRange.getRangeInMinutes()) && isDataTieringTechPack(licensedTechPacks);
        }

        final String isDrilledDownFromDataTieredWindow = parameters.getFirst(KEY_DATA_TIERED_DELAY);
        if ((isDrilledDownFromDataTieredWindow != null) && isDrilledDownFromDataTieredWindow.equalsIgnoreCase(TRUE)) {
            return DateTimeUtils.rawEventRangeForTimeSeriesChartWihtoutOneMinAggregate(formattedDateTimeRange.getRangeInMinutes());
        }

        return false;

    }

    private boolean isDataTieringTechPack(final List<String> licensedTechPacks) {
        for (final String techPack : licensedTechPacks) {
            if (applicableTechpacks.contains(techPack)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDataTieringTechPack(final Collection<TechPackRepresentation> techPackList) {
        for (final TechPackRepresentation techPack : techPackList) {
            if (applicableTechpacks.contains(techPack.getName())) {
                return true;
            }
        }
        return false;
    }

    public void setApplicationConfigManager(ApplicationConfigManager applicationConfigManager) {
        this.applicationConfigManager = applicationConfigManager;
    }
}
