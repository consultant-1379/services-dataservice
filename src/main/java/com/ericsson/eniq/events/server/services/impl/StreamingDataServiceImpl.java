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
package com.ericsson.eniq.events.server.services.impl;

import static com.ericsson.eniq.events.server.logging.performance.ServicesPerformanceThreadLocalHolder.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import javax.ejb.*;

import com.ericsson.eniq.events.server.common.exception.ServiceException;
import com.ericsson.eniq.events.server.datasource.DBConnectionManager;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy;
import com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicyFactory;
import com.ericsson.eniq.events.server.logging.ServicesLogger;
import com.ericsson.eniq.events.server.query.*;
import com.ericsson.eniq.events.server.services.StreamingDataService;

@Stateless
//@TransactionManagement(TransactionManagementType.BEAN)
@Local(StreamingDataService.class)
public class StreamingDataServiceImpl implements StreamingDataService {

    private static final String ENIQ_DIR = "eniq";

    private static final String BACKUP_DIR = "backup";

    private static final String CSV_EXPORT_DIR = "csv_export";

    private static final String EXTENSION = ".csv";

    private static final String SYSTEM_COLUMN_PREFIX = "SYS_COL_";

    private static final String TEMP_EXTRACT_APPEND_ON = "SET TEMPORARY OPTION Temp_Extract_Append = ON;";

    private static final String TEMP_EXTRACT_QUOTES_ON = "SET TEMPORARY OPTION Temp_Extract_Quotes = ON; ";

    private static final String TEMP_EXTRACT_QUOTE = "SET TEMPORARY OPTION Temp_Extract_Quote = '\"' ;";

    // csv file name added in template for queries with insert
    // which will be replaced by the file name created dynamically in services code 
    private static final String CSV_FILE_NAME = "csvFileName";

    private static final String TEMP_EXTRACT_NAME1_OFF = "SET TEMPORARY OPTION Temp_Extract_Name1 = '';";

    private final List<Integer> index = new ArrayList();

    @EJB
    private DBConnectionManager dbConnectionManager;

    @EJB
    private LoadBalancingPolicyFactory loadBalancingPolicyFactory;

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.eniq.events.server.services.CSVDataService#streamDataAsCsv(java.lang.String, java.lang.String, java.lang.String)
     */

    private void streamDataForAnyStringTransformer(String query, final Map<String, QueryParameter> parameters,
                                                   final LoadBalancingPolicy loadBalancingPolicy, final OutputStream out) {
        NamedParameterStatement pstmt = null;
        Connection conn = null;
        File outputFile = null;
        try {
            setQueryExecutionStartTime(Calendar.getInstance().getTimeInMillis());

            outputFile = getExportFile();

            conn = this.dbConnectionManager.getCSVConnection(loadBalancingPolicy);

            //create the temporary options for CSV Export.
            final String tempExtractName1 = "SET TEMPORARY OPTION Temp_Extract_Name1 = '" + outputFile.getAbsolutePath() + "';";

            /*
             * TEMP_EXTRACT_NAME1 to be set in template itself for query templates with temporary tables usage in template: #if($csv == true) SET
             * TEMPORARY OPTION Temp_Extract_Name1 = 'csvFileName' ; #end
             */

            final boolean useTemplateTempOptionFlag = query.contains(CSV_FILE_NAME);

            if (useTemplateTempOptionFlag) {
                /*
                 * intially setting the temp extract option to OFF state to make sure insert queries with local temp table run .
                 */
                query = TEMP_EXTRACT_NAME1_OFF + "\n" + query.replaceAll(CSV_FILE_NAME, outputFile.getAbsolutePath());
            } else {
                query = tempExtractName1 + "\n" + query;
            }

            //Temporary option to embed the data in double quotes
            query = TEMP_EXTRACT_QUOTES_ON + "\n" + TEMP_EXTRACT_QUOTE + "\n" + "\n" + query;
            SQLQueryLogger.detailed(Level.FINE, getClass().getName(), "getData", query, parameters);

            pstmt = getPreparedStatement(query, parameters, conn);
            pstmt.executeQuery();

            //Once the query has completed it's possible to get the column headers and start streaming the data to the client.
            streamCSVToBrowser(outputFile, pstmt.getResultSet().getMetaData(), out);
            outputFile.delete();

            if (outputFile.exists()) {
                ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", "Could not delete: " + outputFile.getAbsolutePath());
            }

        } catch (final Exception e) {
            throw new ServiceException(e);
        } finally {

            //Close the Statement...
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", e);
                }
            }

            //Close the Connection...
            if (conn != null) {
                try {
                    conn.close();
                } catch (final SQLException e) {
                    ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", e);
                }
            }

            //Close the OutputStream...
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", e);
                }
            }

            //Do final cleanup...
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }

            setQueryExecutionEndTime(Calendar.getInstance().getTimeInMillis());
            releaseAllResources();
        }
    }

    private void streamDataForAnyStringTransformer(final List<String> queries, final Map<String, QueryParameter> parameters,
                                                   final LoadBalancingPolicy loadBalancingPolicy, final OutputStream out) {

        NamedParameterStatement pstmt = null;
        File outputFile = null;
        Connection conn = null;

        try {
            setQueryExecutionStartTime(Calendar.getInstance().getTimeInMillis());
            outputFile = getExportFile();

            //create the temporary options for CSV Export.
            final String tempExtractName1 = "SET TEMPORARY OPTION Temp_Extract_Name1 = '" + outputFile.getAbsolutePath() + "';";

            ResultSetMetaData metaData = null;
            for (String query : queries) {
                conn = this.dbConnectionManager.getCSVConnection(loadBalancingPolicy);

                query = tempExtractName1 + "\n" + TEMP_EXTRACT_APPEND_ON + "\n" + query;

                //Temporary option to embed the data in double quotes
                query = TEMP_EXTRACT_QUOTES_ON + "\n" + TEMP_EXTRACT_QUOTE + "\n" + "\n" + query;
                ServicesLogger.detailed(Level.FINE, getClass().getName(), "streamDataForAnyStringTransformer", query, parameters);

                pstmt = getPreparedStatement(query, parameters, conn);
                pstmt.executeQuery();
                metaData = pstmt.getResultSet().getMetaData();
            }

            streamCSVToBrowser(outputFile, metaData, out);
            outputFile.delete();

            if (outputFile.exists()) {
                ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", "Could not delete: " + outputFile.getAbsolutePath());
            }
        } catch (final Exception e) {
            throw new ServiceException(e);
        } finally {

            //Close the Statement...
            if (pstmt != null) {
                try {
                    while (pstmt.getMoreResults()) {
                        pstmt.getResultSet().close();
                    }
                } catch (final SQLException e) {
                    ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", e);
                }
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", e);
                }
            }

            //Close the Connection...
            if (conn != null) {
                try {
                    conn.close();
                } catch (final SQLException e) {
                    ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", e);
                }
            }

            //Close the OutputStream...
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    ServicesLogger.warn(getClass().getName(), "streamDataForAnyStringTransformer", e);
                }
            }

            //Do final cleanup...
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }

            setQueryExecutionEndTime(Calendar.getInstance().getTimeInMillis());
            releaseAllResources();
        }
    }

    private void streamCSVToBrowser(final File file, final ResultSetMetaData metaData, final OutputStream out) throws SQLException, IOException {

        BufferedReader fileReader = null;
        //setup a reader...
        if (file.exists()) {
            String line = "";
            //Setup a reader for the file. This call will block until the query starts to write into the file.
            fileReader = new BufferedReader(new FileReader(file));

            //Write the column names to the CSV.
            out.write(getColumnNames(metaData).getBytes());
            final byte val1 = 13;
            final byte val2 = 10;
            final byte[] newLine = { val1, val2 };
            out.write(newLine);
            out.flush();

            //Read from the fileReader...
            StringBuilder sb = new StringBuilder();
            while ((line = fileReader.readLine()) != null) {
                if (!index.isEmpty()) {
                    final String REGEX = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
                    final String[] values = line.split(REGEX);
                    for (int i = 0; i < values.length; i++) {
                        if (index.contains(i + 1)) {
                            continue;
                        }
                        sb.append(values[i]).append(",");

                    }
                    line = sb.toString();
                }
                out.write(line.getBytes());
                out.write(newLine);
                out.flush();
                sb.setLength(0);
            }
            //close the reader...
            fileReader.close();
        }
        fileReader = null; //OK for GC.
    }

    private String getColumnNames(final ResultSetMetaData metaData) throws SQLException {
        final int columnCount = metaData.getColumnCount();
        final StringBuilder stringBuilder = new StringBuilder();
        index.clear();
        for (int i = 1; i < columnCount + 1; i++) {
            if (metaData.getColumnLabel(i).startsWith(SYSTEM_COLUMN_PREFIX)) {
                index.add(i);
                continue;
            }
            stringBuilder.append(metaData.getColumnLabel(i));
            stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    /*
     * Create the file used for export. This file is saved to the shared location /eniq/backup/csv_export/
     */
    private File getExportFile() throws Exception {
        //get the file name from the current time in ms...
        final StringBuilder fileName = new StringBuilder(System.getProperty("file.separator"));
        fileName.append(ENIQ_DIR);
        fileName.append(System.getProperty("file.separator"));
        fileName.append(BACKUP_DIR);
        fileName.append(System.getProperty("file.separator"));
        fileName.append(CSV_EXPORT_DIR);

        final File directory = new File(fileName.toString());

        //create the csv_export directory if it's not already created...
        if (directory.exists()) {
            fileName.append(System.getProperty("file.separator"));
            fileName.append(Calendar.getInstance().getTimeInMillis());
            fileName.append(EXTENSION);
            return new File(fileName.toString());
        }

        //create the directory...
        if (directory.mkdir()) {
            fileName.append(System.getProperty("file.separator"));
            fileName.append(Calendar.getInstance().getTimeInMillis());
            fileName.append(EXTENSION);
            return new File(fileName.toString());
        }

        throw new Exception("Could not create directory - " + fileName.toString());
    }

    NamedParameterStatement getPreparedStatement(final String query, final Map<String, QueryParameter> parameters, final Connection conn)
            throws SQLException {
        return QueryParameter.setParameters(new NamedParameterStatement(conn, query), parameters);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.eniq.events.server.services.CSVDataService#streamDataAsCsv(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void streamDataAsCsv(final String query, final Map<String, QueryParameter> queryParameters, final String timeColumn,
                                final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy, final OutputStream out) {
        streamDataForAnyStringTransformer(query, queryParameters, loadBalancingPolicy, out);
    }

    @Override
    public void streamDataAsCsv(final String query, final Map<String, QueryParameter> queryParameters, final List<Integer> timeColumnIndexes,
                                final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy, final OutputStream out) {
        streamDataForAnyStringTransformer(query, queryParameters, loadBalancingPolicy, out);
    }

    @Override
    public void streamDataAsCsv(final List<String> queries, final Map<String, QueryParameter> queryParameters, final List<Integer> timeColumnIndexes,
                                final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy, final OutputStream out) {
        streamDataForAnyStringTransformer(queries, queryParameters, loadBalancingPolicy, out);
    }

    /*
     * @see com.ericsson.eniq.events.server.services.StreamingDataService#streamDataAsCsvForCauseCode(java.lang.String, java.util.map,
     * java.lang.String, java.lang.String, com.ericsson.eniq.events.server.datasource.loadbalancing.LoadBalancingPolicy, java.io.OutputStream)
     */
    @Override
    public void streamDataAsCsvForCauseCode(final String query, final Map<String, QueryParameter> queryParameters, final String timeColumn,
                                            final String tzOffset, final LoadBalancingPolicy loadBalancingPolicy, final OutputStream out) {
        streamDataForAnyStringTransformer(query, queryParameters, loadBalancingPolicy, out);
    }

    @Override
    public void streamDataAsCsv(final String query, final String timeColumn, final String tzOffset, final OutputStream out) {
        this.streamDataAsCsv(query, null, timeColumn, tzOffset, loadBalancingPolicyFactory.getDefaultLoadBalancingPolicy(), out);
    }

    /**
     * exposed to help get class under test.
     * 
     * @param dbConnectionManager
     *        the dbConnectionManager to set
     */
    public void setDbConnectionManager(final DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    /**
     * exposed for tests (set from DataServiceBaseTestCase-context.xml)
     * 
     * @param loadBalancingPolicyFactory
     *        the loadBalancingPolicyFactory to set
     */
    public void setLoadBalancingPolicyFactory(final LoadBalancingPolicyFactory loadBalancingPolicyFactory) {
        this.loadBalancingPolicyFactory = loadBalancingPolicyFactory;
    }

}
