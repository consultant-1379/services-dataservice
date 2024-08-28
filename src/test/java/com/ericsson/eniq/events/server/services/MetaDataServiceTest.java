package com.ericsson.eniq.events.server.services;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.events.server.json.JSONArray;
import com.ericsson.eniq.events.server.json.JSONException;
import com.ericsson.eniq.events.server.json.JSONObject;

public class MetaDataServiceTest {

    private static final String TAB_OWNER = "tabOwner";

    private static final String SEARCH_FIELDS = "searchFields";

    private static final String TABS = "tabs";

    private static final String ID = "id";

    private static final String NAME = "name";

    private JSONObject uiMetaData;

    @Before
    public void setUp() throws Exception {
        this.uiMetaData = new JSONObject(readFileAsString("metadata/UIMetaData_test_only.json"));
        assertNotNull(this.uiMetaData);
    }

    @Test
    public void validateTabElements() throws Exception {
        final JSONArray tabs = (JSONArray) this.uiMetaData.get(TABS);
        assertEquals(4, tabs.length());

        JSONObject tab;

        tab = tabs.getJSONObject(0);
        assertEquals("NETWORK_TAB", tab.get(ID));
        assertEquals("Network", tab.get(NAME));

        tab = tabs.getJSONObject(1);
        assertEquals("TERMINAL_TAB", tab.get(ID));
        assertEquals("Terminal", tab.get(NAME));

        tab = tabs.getJSONObject(2);
        assertEquals("SUBSCRIBER_TAB", tab.get(ID));
        assertEquals("Subscriber", tab.get(NAME));

        tab = tabs.getJSONObject(3);
        assertEquals("RANKINGS_TAB", tab.get(ID));
        assertEquals("Rankings", tab.get(NAME));
    }

    @Test
    public void validateSearchFieldElements() throws Exception {
        final JSONArray searchFields = (JSONArray) this.uiMetaData.get(SEARCH_FIELDS);
        assertEquals(3, searchFields.length());

        JSONObject searchField;

        searchField = searchFields.getJSONObject(0);
        assertEquals("NETWORK_TAB", searchField.get(TAB_OWNER));

        searchField = searchFields.getJSONObject(1);
        assertEquals("TERMINAL_TAB", searchField.get(TAB_OWNER));

        searchField = searchFields.getJSONObject(2);
        assertEquals("SUBSCRIBER_TAB", searchField.get(TAB_OWNER));
    }

    @Test
    public void testGettingFieldsInMetaData() throws JSONException {

        // fetch grid objects
        final JSONArray grids = (JSONArray) uiMetaData.get("grids");
        assertNotNull(grids);

        // fetch one grid object
        final JSONObject grid = (JSONObject) grids.get(0);

        // get column array for grid
        final JSONArray columns = (JSONArray) grid.get("columns");

        // Examine a sample column
        JSONObject column;
        column = (JSONObject) columns.get(0);
        assertNotNull(column.getString("header"));
        assertNotNull(column.getString("width"));
        assertNotNull(column.getString("datatype"));
    }

    /** 
     * Picks up a file from the classpath
     */
    private String readFileAsString(final String filePath) throws java.io.IOException {
        final StringBuilder fileData = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader()
                .getResourceAsStream(filePath)));
        final char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            final String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

}
