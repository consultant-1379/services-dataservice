/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.eniq.events.server.services;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.distocraft.dc5000.repository.cache.GroupTypeDef;
import com.ericsson.eniq.events.server.common.Group;
import com.ericsson.eniq.events.server.common.GroupHashId;
import com.ericsson.eniq.events.server.common.UserPreferencesType;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.query.QueryParameter;
import com.ericsson.eniq.events.server.query.resultsettransformers.ResultSetTransformer;

/**
 * Data service interface.
 * 
 * @since Mar 2010
 */
@Local
public interface DataService {
    /**
     * Generate the UI MetaData for LiveSearch HandMakes.
     * 
     * @param query
     *        The SQ LQuery to execute.
     * @param servicesUrl
     *        Services Base URI
     * @param handsetPath
     *        REST path for Handset resource
     * @return UI Metadata JSON with can be used again to search based on a specific handset make
     */
    String getHandsetMakesUIMetadata(final String query, final String servicesUrl, final String handsetPath);

    /**
     * Get LiveLoad/Search results.
     * 
     * @param query
     *        The SQL Query to execute
     * @param liveLoadType
     *        The Node type i.e. BSC, CELL or APN. If set, the livesearch type in the returned JSON is set to this, of not set 'data' is used.
     * @param callbackId
     *        The UI callback ID, must be set.
     * @param pagingIndex
     *        start index in result
     * @param pagingLimit
     *        maximum number of rows requested for this result
     * @return ExtGWT LiveSearch JSON results.
     */
    String getLiveLoad(final String query, final String liveLoadType, final String callbackId, String pagingLimit, String pagingIndex);

    /**
     * Get LiveLoad/Search results.
     * 
     * @param query
     *        The SQL Query to execute
     * @param liveLoadType
     *        The Node type i.e. BSC, CELL or APN. If set, the livesearch type in the returned JSON is set to this, of not set 'data' is used.
     * @param callbackId
     *        The UI callback ID, must be set.
     * @param pagingIndex
     *        start index in result
     * @param pagingLimit
     *        maximum number of rows requested for this result
     * @return ExtGWT LiveSearch JSON results.
     */
    String getLiveLoadForAPN(final String query, final Map<String, QueryParameter> queryParams, final String liveLoadType, final String callbackId,
                             String pagingLimit, String pagingIndex);

    /**
     * Get a version of the Group Definitions to be used in the templates.
     * 
     * @return Map of Group names to Group object.
     */
    Map<String, Group> getGroupsForTemplates();

    /**
     * Used to get the group definition in the Group Management resource.
     * 
     * @param groupDefName
     *        The Group Type name e.g. APN, IMSI or TAC
     * @return the Group Definition
     */
    GroupTypeDef getGroupDefinition(final String groupDefName);

    /**
     * The query verifies if the included TAC is a member of the group.
     * 
     * @param query
     *        The Query to execute to get the groups and their data
     * @return JSON encoded data result
     */
    boolean isTacGroupMember(final String query);

    /**
     * Get the Group Management Data.
     * 
     * @param query
     *        The Query to execute to get the groups and their data
     * @return JSON encoded result
     */
    String getGroupData(final String query);

    String getGroupDataMultipleValues(final String query);

    /**
     * Gets the json data without time info.
     * 
     * @param query
     *        the query
     * @return the JSON data without time info
     */
    String getJSONDataWithoutTimeInfo(final String query);

    /**
     * Grid data variant.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     * @param mapRequestParameters
     *        the map request parameters
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded result
     */
    String getGridData(final String requestID, final String query, final Map<String, QueryParameter> mapRequestParameters, final String timeColumn,
                       final String tzOffset);

    /**
     * Grid data variant.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query with no parameters
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded result
     */
    String getGridData(final String requestID, final String query, final String timeColumn, final String tzOffset);

    /**
     * Grid data variant.
     * 
     * @param query
     *        SQL query with no parameters
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded result
     */
    String getGridDataAsCSV(final String query, final String timeColumn, final String tzOffset);

    /**
     * Grid data variant.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query to be prepared
     * @param queryParameters
     *        named parameter map
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param loadBalancingPolicy
     *        load balancing policy to use when determining data source connection
     * @return JSON encoded result
     */
    String getGridData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters, final String timeColumn,
                       final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy);

    /**
     * Gets the grid data as csv.
     * 
     * @param query
     *        the query
     * @param mapRequestParameters
     *        the map request parameters
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @return the grid data as csv
     */
    String getGridDataAsCSV(final String query, final Map<String, QueryParameter> mapRequestParameters, final String timeColumn, final String tzOffset);

    /**
     * Gets the grid data as csv.
     * 
     * @param query
     *        the query
     * @param mapRequestParameters
     *        the map request parameters
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param loadBalancingPolicy
     *        the load balancing policy
     * @return the grid data as csv
     */
    String getGridDataAsCSV(final String query, final Map<String, QueryParameter> mapRequestParameters, final String timeColumn,
                            final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy);

    /**
     * Get data for charting.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query to be prepared
     * @param queryParameters
     *        named parameter map
     * @param xaxis
     *        indicate the number of column will be the xaxis
     * @param secondYaxis
     *        indicate the number column will be the second yaxis
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded data result
     * @see QueryParameter
     */
    String getChartData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters, final String xaxis,
                        final String secondYaxis, final String timeColumn, final String tzOffset);

    /**
     * Get data for charting.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query to be prepared
     * @param queryParameters
     *        named parameter map
     * @param xaxis
     *        indicate the number of column will be the xaxis
     * @param secondYaxis
     *        indicate the number column will be the second yaxis
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @return JSON encoded data result
     * @see QueryParameter
     */
    String getChartData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters, final String xaxis,
                        final String secondYaxis, final String timeColumn, final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse);

    /**
     * Get data for charting the Groups Most Frequent Signalling Graph. It needs a specific method because the transformer needs to tell the JSONUtils
     * to calculate the Y_AXIS_MAX and MIN
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query to be prepared
     * @param queryParameters
     *        named parameter map
     * @param xaxis
     *        indicate the number of column will be the xaxis
     * @param secondYaxis
     *        indicate the number column will be the second yaxis
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded data result
     * @see QueryParameter
     */
    String getGroupsMostFreqSignalChartData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                            final String timeColumn, final String tzOffset);

    /**
     * Executes a query and transforms the result to a string representation.
     * <p/>
     * TODO: this should replace all the other getXXData methods - use transformers instead to influence the output format.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        the query
     * @param parameters
     *        the parameters
     * @param transformer
     *        - transforms result set to string representation (CSV, JSON etc.)
     * @return string representing the result set
     */
    String getData(final String requestID, final String query, final Map<String, QueryParameter> parameters,
                   final ResultSetTransformer<String> transformer);

    /**
     * Gets the sampling chart data.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        the query
     * @param queryParameters
     *        the query parameters
     * @param chartDateTime
     *        the chart date time
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param xaxis
     *        the xaxis
     * @param secondYaxis
     *        the second yaxis
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @return the sampling chart data
     */
    String getSamplingChartData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                final String[] chartDateTime, final String xaxis, final String secondYaxis, final String timeColumn,
                                final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse);

    /**
     * Gets the sampling chart data. Combines the results for the list of queries into one json strong.
     * 
     * @param requestID
     *        for cancelling
     * @param queries
     *        the list of SQL queries to execute
     * @param queryParameters
     *        the query parameters
     * @param chartDateTime
     *        the chart date time
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param xaxis
     *        the xaxis
     * @param secondYaxis
     *        the second yaxis
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @return the sampling chart data
     */
    String getSamplingChartData(final String requestID, final List<String> query, final Map<String, QueryParameter> queryParameters,
                                final String[] chartDateTime, final String xaxis, final String secondYaxis, final String timeColumn,
                                final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse);

    /**
     * Gets the sampling chart data with sum calculator, which is only used for integer sum
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        the query
     * @param queryParameters
     *        the query parameters
     * @param chartDateTime
     *        the chart date time
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param xaxis
     *        the xaxis
     * @param secondYaxis
     *        the second yaxis
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @return the sampling chart data
     */
    String getSamplingChartDataWithSumCalculatorForInteger(final String requestID, final String query,
                                                           final Map<String, QueryParameter> queryParameters, final String[] chartDateTime,
                                                           final String xaxis, final String secondYaxis, final String timeColumn,
                                                           final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse);

    /**
     * Gets the sampling chart data with sum calculator, which is only used for integer sum
     * 
     * @param requestID
     *        for cancelling
     * @param queries
     *        the list of SQL queries to execute
     * @param queryParameters
     *        the query parameters
     * @param chartDateTime
     *        the chart date time
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param xaxis
     *        the xaxis
     * @param secondYaxis
     *        the second yaxis
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @return the sampling chart data
     */
    String getSamplingChartDataWithSumCalculatorForInteger(final String requestID, final List<String> query,
                                                           final Map<String, QueryParameter> queryParameters, final String[] chartDateTime,
                                                           final String xaxis, final String secondYaxis, final String timeColumn,
                                                           final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse);

    /**
     * Get data for subBI - busy data (hour or day). The idea is to manipulate result set when there is no event for particular day/hour in the
     * database
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query to be prepared
     * @param queryParameters
     *        named parameter map
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @param busyKey
     *        the key to differentiate between busy hour and busy day
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded data result
     * @see QueryParameter
     */
    String getSubBIBusyData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                            final LoadBalancingPolicy loadBalancingPolicyToUse, final String busyKey, final String tzOffset);

    /**
     * Get data for subBI - busy day data. The idea is to manipulate result set when there is no event for particular day in the database
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query to be prepared
     * @param queryParameters
     *        named parameter map
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded data result
     * @see QueryParameter
     */
    String getSubBIBusyDayGridData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                   final String timeColumn, final LoadBalancingPolicy loadBalancingPolicyToUse, final String tzOffset);

    /**
     * Get the RAT values and descriptions as specified in the database
     * 
     * @param query
     *        query to run against database
     * @return Map of RAT integer values, and corresponding RAT description eg 0->GSM
     */
    Map<String, String> getRATValuesAndDescriptions(String query);

    /**
     * Modified variant of standard "getGridData" for TR HN63122. Executes a query and transforms the result to a string representation, and extracts
     * the relevant Sub Cause Code Help text from a concatenated list of all Sub Cause Code Help texts returned by the DB, based on Cause Code ID.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        SQL query to be prepared
     * @param mapRequestParameters
     *        the map request parameters
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @return JSON encoded result
     */
    String getGridDataForCauseCode(final String requestID, String query, Map<String, QueryParameter> mapRequestParameters, String timeColumn,
                                   String tzOffset);

    /**
     * Get a version of the Group Definitions to be used in the templates.
     * 
     * @return Map of Group names to Group object with Hash ID.
     */
    Map<String, GroupHashId> getGroupsForTemplatesForHashId();

    /**
     * @param requestId
     *        for cancelling request
     * @param query
     *        to be executed
     * @param queryParameters
     * @param timeColumnIndexes
     *        list of column needs to be converted to local time
     * @param tzOffset
     * @param loadBalancingPolicy
     * @return
     */
    String getGridData(String requestId, String query, Map<String, QueryParameter> queryParameters, List<Integer> timeColumnIndexes, String tzOffset,
                       LoadBalancingPolicy loadBalancingPolicy);

    /**
     * This method will append row data from the list of result set generated for list of queries passes as parameter. The queries should generate
     * result set with identical column order and data type
     * 
     * @param requestId
     *        for cancelling request
     * @param list
     *        of queries to be executed
     * @param queryParameters
     * @param timeColumnIndexes
     *        list of column needs to be converted to local time
     * @param tzOffset
     * @param loadBalancingPolicy
     * @return
     */
    String getGridDataWithAppendedRows(String requestId, List<String> queries, Map<String, QueryParameter> queryParameters,
                                       List<Integer> timeColumnIndexes, String tzOffset, LoadBalancingPolicy loadBalancingPolicy);

    /**
     * Fetch the tech pack license numbers from the repdb database
     * 
     * @param query
     *        the SQL query to run
     */
    Map<String, List<String>> getTechPackLicenseNumbers(final String query);

    /**
     * @param requestID
     *        request ID for query
     * @param query
     *        the SQL query to execute
     * @param queryParameters
     *        the queryParameters to input into the SQL query
     * @param tzOffset
     *        time zone offset provided by user
     * @param loadBalancingPolicy
     *        the load balancing policy to use when selecting a database connection for use
     * @return the JSON result for the query
     */
    String getGridData(String requestId, String query, Map<String, QueryParameter> queryParameters, String tzOffset,
                       LoadBalancingPolicy loadBalancingPolicy);

    /**
     * This method will append row data from the list of result set generated for list of queries passes as parameter. The queries should generate
     * result set with identical column order and data type
     * 
     * @param requestId
     *        for cancelling request
     * @param list
     *        of queries to be executed
     * @param queryParameters
     * @param xaxis
     *        indicate the number of column will be the xaxis
     * @param secondYaxis
     *        indicate the number column will be the second yaxis
     * @param timeColumn
     *        the timezone column
     * @param timeColumnIndexes
     *        list of column needs to be converted to local time
     * @param tzOffset
     * @param loadBalancingPolicy
     * @return
     */
    String getChartDataWithAppendedRows(String requestId, List<String> query, Map<String, QueryParameter> queryParameters, String xaxis,
                                        String secondYaxis, String timeColumn, String tzOffset, LoadBalancingPolicy loadBalancingPolicy);

    /**
     * This method will append row data from the list of result set generated for list of queries passes as parameter. The queries should generate
     * result set with identical column order and data type
     * 
     * @param requestId
     *        for cancelling request
     * @param list
     *        of queries to be executed
     * @param queryParameters
     * @param xaxis
     *        indicate the number of column will be the xaxis
     * @param secondYaxis
     *        indicate the number column will be the second yaxis
     * @param timeColumn
     *        the timezone column
     * @param timeColumnIndexes
     *        list of column needs to be converted to local time
     * @param tzOffset
     * @param loadBalancingPolicy
     * @return
     */
    String getSubBIBusyDataWithAppendedRows(String requestID, List<String> query, Map<String, QueryParameter> queryParameters,
                                            LoadBalancingPolicy loadBalancingPolicy, String busyKey, String tzOffset);

    /**
     * This method will append row data from the list of result set generated for list of queries passes as parameter. The queries should generate
     * result set with identical column order and data type
     * 
     * @param requestId
     *        for cancelling request
     * @param list
     *        of queries to be executed
     * @param queryParameters
     * @param xaxis
     *        indicate the number of column will be the xaxis
     * @param secondYaxis
     *        indicate the number column will be the second yaxis
     * @param tzOffset
     * @param loadBalancingPolicy
     * @return
     */
    String getChartDataWithAppendedRowsRoaming(String requestId, List<String> query, Map<String, QueryParameter> queryParameters, String xaxis,
                                               String secondYaxis, String tzOffset, LoadBalancingPolicy loadBalancingPolicy);

    /**
     * Fetch data The standard transformer is used (ie the transformer originally called the grid transformer)
     * 
     * @param requestId
     *        request id for cancelling request
     * @param query
     *        SQL query to execute
     * @param queryParameters
     *        parameters for SQL query
     * @param tzOffset
     * @param loadBalancingPolicy
     * @return
     */
    String getData(String requestId, String query, Map<String, QueryParameter> queryParameters, String tzOffset,
                   LoadBalancingPolicy loadBalancingPolicy);

    /**
     * Stores user preferences
     * 
     * @param query
     *        insert/update query
     * @param queryParams
     *        parameters for SQL insert/update statement
     */
    void updateUserPreferences(final String query, final Map<String, QueryParameter> queryParams);

    /**
     * Gets user preferences
     * 
     * @param query
     *        SQL query
     * @param queryParams
     *        parameters for SQL query
     * @return user preferences or null if no data found
     */
    UserPreferencesType getUserPreferences(String query, Map<String, QueryParameter> queryParams);

    /**
     * Fetch the raw tables by querying the raw time range
     * 
     * @param query
     *        query to run
     * @param queryParameters
     *        query parameters
     * 
     * @return list of raw tables to use
     */
    List<String> getRawTables(String query, Map<String, QueryParameter> queryParameters);

    /**
     * Fetch the RAB thresholds from the Thresholds table in the repdb database
     * 
     * @param query
     *        SQL query to run
     * @return json formatted list of RAB threshholds
     */
    String getRABThresholds(String query);

    /**
     * Fetch data with resultSetTransformerFactory
     * 
     * @param query
     *        SQL query to execute
     * @param parameters
     *        parameters for SQL query
     * @param resultSetTransformerFactory
     * @return
     */
    String getData(final String query, final Map<String, QueryParameter> parameters, final ResultSetTransformer<String> resultSetTransformerFactory);

    /**
     * Gets the chart data with chartTitle and timeTickInterval in chart header.
     * 
     * @param requestID
     *        for cancelling
     * @param query
     *        the query
     * @param queryParameters
     *        the query parameters
     * @param chartDateTime
     *        the chart date time
     * @param timeColumn
     *        the timezone column
     * @param tzOffset
     *        the timezone offset
     * @param xaxis
     *        the xaxis
     * @param secondYaxis
     *        the second yaxis
     * @param loadBalancingPolicyToUse
     *        the load balancing policy to use
     * @param chartTitle
     * @param timeInterval
     *        interval between two time tick on xaxis
     * @return the sampling chart data
     */
    String getChartWithTitleAndTimeIntervalData(String requestId, String query, Map<String, QueryParameter> mapRequestParameters,
                                                String[] dataVolumeDateTimeList, String xAxis, String secondYAxis, String timeColumn,
                                                String tzOffset, LoadBalancingPolicy loadBalancingPolicy, String chartTitle, int timeInterval);

}
