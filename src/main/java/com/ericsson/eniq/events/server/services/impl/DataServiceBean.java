/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.eniq.events.server.services.impl;

import static com.ericsson.eniq.events.server.common.ApplicationConstants.*;

import java.util.*;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.distocraft.dc5000.repository.cache.GroupTypeDef;
import com.ericsson.eniq.events.server.common.*;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory;
import com.ericsson.eniq.events.server.query.DataServiceQueryExecutor;
import com.ericsson.eniq.events.server.query.QueryParameter;
import com.ericsson.eniq.events.server.query.resultsettransformers.ResultSetTransformer;
import com.ericsson.eniq.events.server.query.resultsettransformers.ResultSetTransformerFactory;
import com.ericsson.eniq.events.server.services.DataService;
import com.ericsson.eniq.events.server.utils.*;

/**
 * Common data access layer for event data services.
 * 
 * @since Mar 2010
 */
@Stateless
//@TransactionManagement(TransactionManagementType.BEAN)
@Local(DataService.class)
public class DataServiceBean implements DataService {

    @EJB
    private LoadBalancingPolicyFactory loadBalancingPolicyFactory;

    @EJB
    //used by some of the JSON transformers
    private RATDescriptionMappingUtils ratDescriptionMappingUtils;

    @EJB
    private DataServiceQueryExecutor dataServiceQueryExecutor;

    @EJB
    private GroupDataService groupDataService;

    /**
     * Get a version of the Group Definitions to be used in the templates.
     * 
     * @return Map of Group names to Group object.
     */
    @Override
    public Map<String, Group> getGroupsForTemplates() {
        return groupDataService.getGroupsForTemplates();
    }

    /**
     * Get a version of the Group Definitions to be used in the templates.
     * @return Map of Group names to Group object for Hash ID.
     */
    @Override
    public Map<String, GroupHashId> getGroupsForTemplatesForHashId() {
        return groupDataService.getGroupsForTemplatesForHashId();

    }

    /**
     * Used to get the group definition in the Group Management resource
     * @param groupDefType
     *        The Group Type name e.g. APN, IMSI or TAC
     * @return the Group Definition
     */
    @Override
    public GroupTypeDef getGroupDefinition(final String groupDefType) {
        return groupDataService.getGroupDefinition(groupDefType);
    }

    @Override
    public String getGridData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters, final String timeColumn,
                              final String tzOffset) {
        return this.getData(requestId, query, queryParameters, getJSONGridDataTransformer(queryParameters, timeColumn, tzOffset),
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getGridData(final String requestId, final String query, final String timeColumn, final String tzOffset) {
        return this.getData(requestId, query, null, getJSONGridDataTransformer(null, timeColumn, tzOffset),
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getGridDataAsCSV(final String query, final String timeColumn, final String tzOffset) {
        return this.getData(CANCEL_REQ_NOT_SUPPORTED, query, null, ResultSetTransformerFactory.getCSVTransformer(timeColumn, tzOffset),
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getGridData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters, final String timeColumn,
                              final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy) {
        return this
                .getData(requestId, query, queryParameters, getJSONGridDataTransformer(queryParameters, timeColumn, tzOffset), loadBalancingPolicy);
    }

    @Override
    public String getGridData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters,
                              final List<Integer> timeColumnIndexes, final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy) {
        return this.getData(requestId, query, queryParameters, getJSONGridDataTransformer(queryParameters, timeColumnIndexes, tzOffset),
                loadBalancingPolicy);
    }

    @Override
    public String getGridDataWithAppendedRows(final String requestId, final List<String> queries, final Map<String, QueryParameter> queryParameters,
                                              final List<Integer> timeColumnIndexes, final String tzOffset,
                                              final LoadBalancingPolicy loadBalancingPolicy) {
        return this.getData(requestId, queries, queryParameters,
                getJSONGridDataTransformerForAppendingRows(queryParameters, timeColumnIndexes, tzOffset), loadBalancingPolicy);
    }

    /**
     * Create the JSON Grid Data Transformer This uses the fields DATE_FROM and DATE_TO fields in the queryParameters, if queryParameters is not null
     * If queryParameters is null, then nulls are passed to the JSONGridDataTransformer for these date fields
     * @param queryParameters
     * @return
     */
    private ResultSetTransformer<String> getJSONGridDataTransformer(final Map<String, QueryParameter> queryParameters, final String timeColumn,
                                                                    final String tzOffset) {
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return ResultSetTransformerFactory.getJSONGridDataTransformer(dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), timeColumn, tzOffset);
    }

    /**
     * Create the JSON Grid Data Transformer This uses the fields DATE_FROM and DATE_TO fields in the queryParameters, if queryParameters is not null
     * If queryParameters is null, then nulls are passed to the JSONGridDataTransformer for these date fields
     * @param queryParameters
     * @param timeColumnIndexes
     *        list of column index to be converted to local time
     * @return
     */
    private ResultSetTransformer<String> getJSONGridDataTransformer(final Map<String, QueryParameter> queryParameters,
                                                                    final List<Integer> timeColumnIndexes, final String tzOffset) {
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return ResultSetTransformerFactory.getJSONGridDataTransformer(dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), timeColumnIndexes, tzOffset);
    }

    private ResultSetTransformer<String> getJSONGridDataTransformerForAppendingRows(final Map<String, QueryParameter> queryParameters,
                                                                                    final List<Integer> timeColumnIndexes, final String tzOffset) {
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return ResultSetTransformerFactory.getJSONGridDataTransformerForAppendingRows(dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO),
                timeColumnIndexes, tzOffset);
    }

    /**
     * exposed to help get class under unit test
     * @return
     * @throws NamingException
     */
    InitialContext createInitialContext() throws NamingException {
        return new InitialContext();
    }

    @Override
    public String getGridDataAsCSV(final String query, final Map<String, QueryParameter> queryParameters, final String timeColumn,
                                   final String tzOffset) {
        return this.getData(CANCEL_REQ_NOT_SUPPORTED, query, queryParameters, ResultSetTransformerFactory.getCSVTransformer(timeColumn, tzOffset),
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getGridDataAsCSV(final String query, final Map<String, QueryParameter> queryParameters, final String timeColumn,
                                   final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy) {
        return this.getData(CANCEL_REQ_NOT_SUPPORTED, query, queryParameters, ResultSetTransformerFactory.getCSVTransformer(timeColumn, tzOffset),
                loadBalancingPolicy);
    }

    @Override
    public String getChartData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters, final String xaxis,
                               final String secondYaxis, final String timeColumn, final String tzOffset) {
        return getChartData(requestId, query, queryParameters, xaxis, secondYaxis, timeColumn, tzOffset,
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getChartData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters, final String xaxis,
                               final String secondYaxis, final String timeColumn, final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy) {

        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return getData(requestId, query, queryParameters, ResultSetTransformerFactory.getJSONChartDataTransformer(xaxis, secondYaxis,
                dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), timeColumn, tzOffset), loadBalancingPolicy);
    }

    @Override
    public String getChartDataWithAppendedRows(final String requestId, final List<String> queries, final Map<String, QueryParameter> queryParameters,
                                               final String xaxis, final String secondYaxis, final String timeColumn, final String tzOffset,
                                               final LoadBalancingPolicy loadBalancingPolicy) {
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return getData(
                requestId,
                queries,
                queryParameters,
                ResultSetTransformerFactory.getJSONChartDataTransformerWithAppendedRows(xaxis, secondYaxis, dateTimes.get(DATE_FROM),
                        dateTimes.get(DATE_TO), timeColumn, tzOffset), loadBalancingPolicy);
    }

    @Override
    public String getGroupsMostFreqSignalChartData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters,
                                                   final String timeColumn, final String tzOffset) {
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return getData(requestId, query, queryParameters, ResultSetTransformerFactory.getGroupsMostFreqSignalJSONChartDataTransformer(
                dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), timeColumn, tzOffset), loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getData(final String requestId, final String query, final Map<String, QueryParameter> parameters,
                          final ResultSetTransformer<String> transformer) {
        return getData(requestId, query, parameters, transformer, loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    private String getData(final String requestID, final List<String> query, final Map<String, QueryParameter> parameters,
                           final ResultSetTransformer<String> transformer, final LoadBalancingPolicy loadBalancingPolicy) {
        //can safely cast to string here, as transformer is of type String
        return dataServiceQueryExecutor.getDataForMultipleQueries(requestID, query, parameters, transformer, loadBalancingPolicy);
    }

    /**
     * Returns result set as string
     * @param query
     *        SQL query to be prepared
     * @param parameters
     *        Query Parameters
     * @param transformer
     *        The Transformer used to convert the SQL ResultSet to a String
     * @param loadBalancingPolicy
     *        Load Balancing Policy
     * @return result set returned as CSV
     */
    private String getData(final String requestID, final String query, final Map<String, QueryParameter> parameters,
                           final ResultSetTransformer<String> transformer, final LoadBalancingPolicy loadBalancingPolicy) {
        //can safely cast to string here, as transformer is of type String
        return dataServiceQueryExecutor.getData(requestID, query, parameters, transformer, loadBalancingPolicy);
    }

    /**
     * Get the group data
     * @param query
     *        The Query to execute to get the groups and their data
     * @return JSON encoded data result
     */
    @Override
    public String getGroupData(final String query) {
        return getData(CANCEL_REQ_NOT_SUPPORTED, query, null, ResultSetTransformerFactory.getJSONNameListDataTransformer());
    }

    @Override
    public String getGroupDataMultipleValues(final String query) {
        return getData(CANCEL_REQ_NOT_SUPPORTED, query, null, ResultSetTransformerFactory.getJSONNameListMultipleValuesDataTransformer());
    }

    /**
     * The query verifies if the included TAC is a member of the group.
     * @param query
     *        The Query to execute to get the group and tac
     * @return true if the TAC is a member of the group, false otherwise
     */
    @Override
    public boolean isTacGroupMember(final String query) {
        return dataServiceQueryExecutor.getData(CANCEL_REQ_NOT_SUPPORTED, query, null,
                ResultSetTransformerFactory.getTransformerToCheckResultSizeIsOne(), loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getHandsetMakesUIMetadata(final String query, final String servicesUrl, final String handsetPath) {
        return getData(CANCEL_REQ_NOT_SUPPORTED, query, null, ResultSetTransformerFactory.getJSONMetadataUITransformer(servicesUrl, handsetPath));
    }

    @Override
    public String getLiveLoad(final String query, final String liveLoadType, final String callbackID, final String pagingLimit,
                              final String pagingIndex) {
        return getData(CANCEL_REQ_NOT_SUPPORTED, query, null, ResultSetTransformerFactory.getJSONLiveLoadTransformer(liveLoadType, callbackID,
                pagingLimit, pagingIndex, ratDescriptionMappingUtils));
    }

    @Override
    public String getLiveLoadForAPN(final String query, final Map<String, QueryParameter> queryParams, final String liveLoadType,
                                    final String callbackID, final String pagingLimit, final String pagingIndex) {
        return getData(CANCEL_REQ_NOT_SUPPORTED, query, queryParams, ResultSetTransformerFactory.getJSONLiveLoadTransformer(liveLoadType, callbackID,
                pagingLimit, pagingIndex, ratDescriptionMappingUtils));
    }

    /**
     * exposed for tests (set from DataServiceBaseTestCase-context.xml)
     * @param loadBalancingPolicyFactory
     *        the loadBalancingPolicyFactory to set
     */
    public void setLoadBalancingPolicyFactory(final LoadBalancingPolicyFactory loadBalancingPolicyFactory) {
        this.loadBalancingPolicyFactory = loadBalancingPolicyFactory;
    }

    /**
     * here for test reasons
     * @param ratDescriptionMappingUtils
     */
    public void setRatDescriptionMappingUtils(final RATDescriptionMappingUtils ratDescriptionMappingUtils) {
        this.ratDescriptionMappingUtils = ratDescriptionMappingUtils;
    }

    @Override
    public String getSamplingChartData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                       final String[] chartDateTime, final String xaxis, final String secondYaxis, final String timeColumn,
                                       final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse) {
        final List<String> queryList = new ArrayList<String>();
        queryList.add(query);
        return getSamplingChartData(requestID, queryList, queryParameters, chartDateTime, xaxis, secondYaxis, timeColumn, tzOffset,
                loadBalancingPolicyToUse);
    }

    @Override
    public String getSamplingChartData(final String requestID, final List<String> query, final Map<String, QueryParameter> queryParameters,
                                       final String[] chartDateTime, final String xaxis, final String secondYaxis, final String timeColumn,
                                       final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse) {
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return getData(requestID, query, queryParameters, ResultSetTransformerFactory.getJSONSamplingChartDataTransformer(xaxis, secondYaxis,
                chartDateTime, dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), timeColumn, tzOffset), loadBalancingPolicyToUse);
    }

    @Override
    public String getSamplingChartDataWithSumCalculatorForInteger(final String requestID, final String query,
                                                                  final Map<String, QueryParameter> queryParameters, final String[] chartDateTime,
                                                                  final String xaxis, final String secondYaxis, final String timeColumn,
                                                                  final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse) {
        final List<String> queryList = new ArrayList<String>();
        queryList.add(query);
        return getSamplingChartDataWithSumCalculatorForInteger(requestID, queryList, queryParameters, chartDateTime, xaxis, secondYaxis, timeColumn,
                tzOffset, loadBalancingPolicyToUse);
    }

    @Override
    public String getSamplingChartDataWithSumCalculatorForInteger(final String requestID, final List<String> query,
                                                                  final Map<String, QueryParameter> queryParameters, final String[] chartDateTime,
                                                                  final String xaxis, final String secondYaxis, final String timeColumn,
                                                                  final String tzOffset, final LoadBalancingPolicy loadBalancingPolicyToUse) {
        return getData(requestID, query, queryParameters, ResultSetTransformerFactory.getJSONSamplingChartDataTransformerWithCalculator(xaxis,
                secondYaxis, chartDateTime, timeColumn, tzOffset, CalculatorFactory.getSumCalcaulator()), loadBalancingPolicyToUse);
    }

    @Override
    public String getSubBIBusyData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                   final LoadBalancingPolicy loadBalancingPolicy, final String busyKey, final String tzOffset) {

        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return getData(requestID, query, queryParameters, ResultSetTransformerFactory.getJSONChartDataTransformerForSUBBIBusy(busyKey,
                dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), tzOffset), loadBalancingPolicy);
    }

    @Override
    public String getSubBIBusyDataWithAppendedRows(final String requestID, final List<String> queries,
                                                   final Map<String, QueryParameter> queryParameters, final LoadBalancingPolicy loadBalancingPolicy,
                                                   final String busyKey, final String tzOffset) {

        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);

        return getData(
                requestID,
                queries,
                queryParameters,
                ResultSetTransformerFactory.getJSONChartDataTransformerForSUBBIBusyWithAppendedRows(busyKey, dateTimes.get(DATE_FROM),
                        dateTimes.get(DATE_TO), tzOffset), loadBalancingPolicy);
    }

    @Override
    public String getSubBIBusyDayGridData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                          final String timeColumn, final LoadBalancingPolicy loadBalancingPolicy, final String tzOffset) {

        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return getData(requestID, query, queryParameters, ResultSetTransformerFactory.getJSONGridDataTransformerForSUBBIBusyDay(
                dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), timeColumn, tzOffset), loadBalancingPolicy);
    }

    @Override
    public Map<String, String> getRATValuesAndDescriptions(final String query) {
        final ResultSetTransformer<Map<String, String>> transformer = ResultSetTransformerFactory.getRATValuesTransformer();

        return dataServiceQueryExecutor.getData(CANCEL_REQ_NOT_SUPPORTED, query, null, transformer,
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    /*
     * @see com.ericsson.eniq.events.server.services.DataService#getGridDataForCauseCode(java.lang.String, java.util.Map, java.lang.String,
     * java.lang.String)
     */
    @Override
    public String getGridDataForCauseCode(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                          final String timeColumn, final String tzOffset) {
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        final ResultSetTransformer<String> transformer = ResultSetTransformerFactory.getCauseCodeHelpTextTransformer(dateTimes.get(DATE_FROM),
                dateTimes.get(DATE_TO), timeColumn, tzOffset);
        //can safely cast to string here, as transformer is of type String
        return dataServiceQueryExecutor.getData(requestID, query, queryParameters, transformer,
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    @Override
    public String getJSONDataWithoutTimeInfo(final String query) {
        return getData(CANCEL_REQ_NOT_SUPPORTED, query, null, ResultSetTransformerFactory.getJSONNameListDataTransformerWithoutTimeInfo());
    }

    public void setDataServiceQueryExecutor(final DataServiceQueryExecutor dataServiceQueryExecutor) {
        this.dataServiceQueryExecutor = dataServiceQueryExecutor;
    }

    public void setGroupDataService(final GroupDataService groupDataService) {
        this.groupDataService = groupDataService;
    }

    @Override
    public Map<String, List<String>> getTechPackLicenseNumbers(final String query) {
        return dataServiceQueryExecutor.getDataFromRepdb(CANCEL_REQ_NOT_SUPPORTED, query, null,
                ResultSetTransformerFactory.getTechPackLicenseNumbersTransformer());
    }

    @Override
    public String getGridData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters, final String tzOffset,
                              final LoadBalancingPolicy loadBalancingPolicy) {
        return this.getData(requestId, query, queryParameters, getJSONGridDataTransformer(queryParameters, (String) null, tzOffset),
                loadBalancingPolicy);
    }

    @Override
    public String getChartDataWithAppendedRowsRoaming(final String requestId, final List<String> query,
                                                      final Map<String, QueryParameter> queryParameters, final String xaxis,
                                                      final String secondYaxis, final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy) {

        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        final ResultSetTransformer<String> transformer = ResultSetTransformerFactory.getRoamingHelpTextTransformer(dateTimes.get(DATE_FROM),
                dateTimes.get(DATE_TO), tzOffset);

        return getData(requestId, query, queryParameters, transformer, loadBalancingPolicy);
    }

    @Override
    public String getData(final String requestId, final String query, final Map<String, QueryParameter> queryParameters, final String tzOffset,
                          final LoadBalancingPolicy loadBalancingPolicy) {
        return this.getData(requestId, query, queryParameters, getJSONGridDataTransformer(queryParameters, (String) null, tzOffset),
                loadBalancingPolicy);
    }

    @Override
    public void updateUserPreferences(final String query, final Map<String, QueryParameter> queryParams) {
        dataServiceQueryExecutor.updateDataInRepdb(CANCEL_REQ_NOT_SUPPORTED, query, queryParams);
    }

    @Override
    public UserPreferencesType getUserPreferences(final String query, final Map<String, QueryParameter> queryParams) {
        return dataServiceQueryExecutor.getDataFromRepdb(CANCEL_REQ_NOT_SUPPORTED, query, queryParams,
                ResultSetTransformerFactory.getUserPreferencesTransformer());
    }

    @Override
    public List<String> getRawTables(final String query, final Map<String, QueryParameter> parameters) {
        return dataServiceQueryExecutor.getData(CANCEL_REQ_NOT_SUPPORTED, query, parameters,
                ResultSetTransformerFactory.getRawTableNamesTransformer(), loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());

    }

    @Override
    public String getRABThresholds(final String query) {
        return dataServiceQueryExecutor.getDataFromRepdb(CANCEL_REQ_NOT_SUPPORTED, query, null,
                ResultSetTransformerFactory.getJSONNameListDataTransformerWithoutTimeInfo());
    }

    /**
     * GetData
     * @param query
     *        The Query to execute
     * @param parameters
     *        Query paramenters
     * @param resultSetTransformerFactory
     *        Result set factory to format resultSet data to the required format
     * @return Encoded data result based on resultSetTransformerFactory
     */
    @Override
    public String getData(final String query, final Map<String, QueryParameter> parameters,
                          final ResultSetTransformer<String> resultSetTransformerFactory) {
        return getData(CANCEL_REQ_NOT_SUPPORTED, query, parameters, resultSetTransformerFactory,
                loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy());
    }

    private Map<String, String> getDateTime(final Map<String, QueryParameter> queryParameters, final String tzOffset) {
        final Map<String, String> result = new HashMap<String, String>();
        if (queryParameters != null && queryParameters.get(DATE_FROM) != null && queryParameters.get(DATE_TO) != null) {
            result.put(DATE_FROM, (String) queryParameters.get(DATE_FROM).getValue());
            result.put(DATE_TO, (String) queryParameters.get(DATE_TO).getValue());
        }

        if (queryParameters != null && queryParameters.get(ISWEEKOVERRIDE) != null && getWeekOverrideFlag(queryParameters.get(ISWEEKOVERRIDE))) {
            return getAdjustedDateTime(result, tzOffset);
        }
        return result;
    }

    /**
     * Gets the chart data with chartTitle and timeTickInterval in chart header.
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
    @Override
    public String getChartWithTitleAndTimeIntervalData(final String requestID, final String query, final Map<String, QueryParameter> queryParameters,
                                                       final String[] chartDateTime, final String xaxis, final String secondYaxis,
                                                       final String timeColumn, final String tzOffset,
                                                       final LoadBalancingPolicy loadBalancingPolicyToUse, final String chartTitle,
                                                       final int timeInterval) {
        final List<String> queryList = new ArrayList<String>();
        queryList.add(query);
        final Map<String, String> dateTimes = getDateTime(queryParameters, tzOffset);
        return getData(
                requestID,
                query,
                queryParameters,
                ResultSetTransformerFactory.getJSONChartWithTitleAndTimeIntervalDataTransformer(xaxis, secondYaxis, chartDateTime,
                        dateTimes.get(DATE_FROM), dateTimes.get(DATE_TO), timeColumn, tzOffset, chartTitle, timeInterval), loadBalancingPolicyToUse);
    }

    /* This check getWeekOverrideFlag is to adjudge if there is manipulation of the weekly query from local time 
     * to UTC ( 1week or more ) when only RAW tables were involved and if so, changes the handling of JSON produced
     * in order to display correct date time for queries more than a week's duration
     */
    private boolean getWeekOverrideFlag(final QueryParameter queryParameter) {
        if (TRUE.equalsIgnoreCase(queryParameter.getValue().toString())){
            return true;
        }
        return false;
    }

    /* This change is required in order to compensate the adjustment made in JSONUtils.getJSONGridTimeRangeParameters and
     * DateTimeUtils.getTimeStampInMilliseconds which expects locl time for query duration >= 1 WEEK
     */
    private Map<String, String> getAdjustedDateTime(final Map<String, String> result,final String tzOffset) {
        if (result != null && result.get(DATE_FROM) != null && result.get(DATE_TO) != null) {
            result.put(DATE_FROM, DateTimeUtils.getLocalTime(result.get(DATE_FROM), tzOffset, DATE_TIME_FORMAT));
            result.put(DATE_TO, DateTimeUtils.getLocalTime(result.get(DATE_TO), tzOffset, DATE_TIME_FORMAT));
        }
        return result;
    }
}
