package com.ericsson.eniq.events.server.services.cache;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * A class to store and manage a cache of Radio Network KPI data
 *
 * @author ericker
 */

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.WRITE)
public class RadioNetworkKPICache {
    // KPI Cache
    // TODO: Replace "String" defining node with an object which has a type and an id (so only one node level cache)
    private final Map<Calendar, Map<String, Map<String, Double>>> cellKPIData = new HashMap<Calendar, Map<String, Map<String, Double>>>();

    private final Map<Calendar, Map<String, Map<String, Double>>> controllerKPIData = new HashMap<Calendar, Map<String, Map<String, Double>>>();

    private final Map<Calendar, Map<String, Double>> networkKPIData = new HashMap<Calendar, Map<String, Double>>();

    /**
     * Clears all KPI data from the cache
     */
    public void clearCache() {
        cellKPIData.clear();
        controllerKPIData.clear();
        networkKPIData.clear();
    }

    /**
     * Adds cell KPI data to the cache, overwriting previous values if they exist
     *
     * @param date  The date for which these KPIs are valid
     * @param value The KPI data
     */
    public void putCellKPI(final Calendar date, final Map<String, Map<String, Double>> value) {
        cellKPIData.put(date, value);
    }

    /**
     * Adds controller KPI data to the cache, overwriting previous values if they exist
     *
     * @param date  The date for which these KPIs are valid
     * @param value The KPI data
     */
    public void putControllerKPI(final Calendar date, final Map<String, Map<String, Double>> value) {
        controllerKPIData.put(date, value);
    }

    /**
     * Adds network level KPI data to the cache, overwriting previous values if they exist
     *
     * @param date  The date for which the KPIs are valid
     * @param value The KPI data
     */
    public void putNetworkKPI(final Calendar date, final Map<String, Double> value) {
        networkKPIData.put(date, value);
    }

    /**
     * Searches the cache for the specified date and cell name, returning a map of
     * KPI names to their values
     *
     * @param date A calendar object defining which date we are searching for
     * @param cell A string representing the cell that is being searched
     * @return If a cache is found a map of KPI names to values is returned, if
     *         a cache is found for the date but not the cell name, an empty map is returned,
     *         if no cache is found for that day then a null reference is returned.
     */
    public Map<String, Double> getCellKPI(final Calendar date, final String cell) {
        // TODO: Make these generic
        if (cellKPIData.containsKey(date)) {
            final Map<String, Map<String, Double>> values = cellKPIData.get(date);
            if (values.containsKey(cell)) {
                // We have a cache! Return it
                return values.get(cell);
            }
            // No cell by this name is found in the cache
            return new HashMap<String, Double>();
        }
        // We have no cache for this date
        return null;
    }

    /**
     * Searches the cache for the specified date and controller name, returning a map of
     * KPI names to their values
     *
     * @param date       A calendar object defining which date we are searching for
     * @param controller A string representing the controller that is being searched
     * @return If a cache is found a map of KPI names to values is returned, if
     *         a cache is found for the date but not the cell name, an empty map is returned,
     *         if no cache is found for that day then a null reference is returned.
     */
    public Map<String, Double> getControllerKPI(final Calendar date, final String controller) {
        if (controllerKPIData.containsKey(date)) {
            final Map<String, Map<String, Double>> values = controllerKPIData.get(date);
            if (values.containsKey(controller)) {
                // We have a cache! Return it
                return values.get(controller);
            }
            // No cell by this name is found in the cache
            return new HashMap<String, Double>();
        }
        // We have no cache for this date
        return null;
    }

    /**
     * Searches the cache for the specified date, returning a map of KPI names to their values.
     *
     * @param date A calendar object defining which date we are searching for
     * @return If no cache is found, returns null, if a cache is found then a map of KPI names to
     *         their values is returned
     */
    @Lock(LockType.READ)
    public Map<String, Double> getNetworkKPI(final Calendar date) {
        return networkKPIData.get(date);
    }
}
