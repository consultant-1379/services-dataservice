/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.query.QueryParameter;

/**
 * A data service to stream the data as a CSV file to an output stream
 * 
 * @author ericker
 * @since 2010
 *
 */
@Local
public interface StreamingDataService {

    /**
     * Streams the data returned from the given query to the output stream as CSV
     * 
     * @param query SQL query to be prepared
     * @param queryParameters named parameter map
     * @param timeColumn the timezone column
     * @param tzOffset the timezone offset
     * @param loadBalancingPolicy load balancing policy to use when determining data source connection
     * @param out the output stream to stream to
     */
    void streamDataAsCsv(final String query, final Map<String, QueryParameter> queryParameters,
            final String timeColumn, final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy,
            OutputStream out);

    /**
     * Streams the data returned from the given query to the output stream as CSV
     * 
     * @param query SQL query to be prepared
     * @param timeColumn the timezone column
     * @param tzOffset the timezone offset
     * @param out the output stream to stream to
     */
    void streamDataAsCsv(String query, String timeColumn, String tzOffset, OutputStream out);

    /**
     * Modified variant of streamDataAsCsv.
     * Streams the data returned from the given query to the output stream as CSV, and extracts
     * the Sub Cause Code Help Text, based on Cause Code ID, for all rows.
     * Added for TR HN63122.
     * 
     * @param query SQL query to be prepared
     * @param queryParameters named parameter map
     * @param timeColumn the timezone column
     * @param tzOffset the timezone offset
     * @param loadBalancingPolicy load balancing policy to use when determining data source connection
     * @param out the output stream to stream to
     */
    void streamDataAsCsvForCauseCode(String query, Map<String, QueryParameter> queryParameters, String timeColumn,
            String tzOffset, LoadBalancingPolicy loadBalancingPolicy, OutputStream out);

    /**
     * Streams the data returned from the given query to the output stream as CSV
     * 
     * @param query SQL query to be prepared
     * @param queryParameters named parameter map
     * @param timeColumnIndexes the time columns to be converted to local time 
     * @param tzOffset the timezone offset
     * @param loadBalancingPolicy load balancing policy to use when determining data source connection
     * @param out the output stream to stream to
     */
    void streamDataAsCsv(String query, Map<String, QueryParameter> mapRequestParametersForHashId,
            List<Integer> timeColumnIndexes, String tzOffset, LoadBalancingPolicy loadBalancingPolicy,
            OutputStream outputStream);

    /**
     * Streams the data returned from the given query to the output stream as CSV
     * 
     * @param queries SQL queries to be prepared and executed
     * @param queryParameters named parameter map
     * @param timeColumnIndexes the time columns to be converted to local time 
     * @param tzOffset the timezone offset
     * @param loadBalancingPolicy load balancing policy to use when determining data source connection
     * @param out the output stream to stream to
     */
    void streamDataAsCsv(List<String> queries, Map<String, QueryParameter> queryParameters,
            List<Integer> timeColumnIndexes, String tzOffset, LoadBalancingPolicy loadBalancingPolicy, OutputStream out);
}
