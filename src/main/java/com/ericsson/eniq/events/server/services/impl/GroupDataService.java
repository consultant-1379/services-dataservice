/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.impl;

import static com.ericsson.eniq.events.server.common.ApplicationConstants.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.ejb.*;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.repository.cache.*;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;
import com.ericsson.eniq.events.server.common.Group;
import com.ericsson.eniq.events.server.common.GroupHashId;
import com.ericsson.eniq.events.server.datasource.DBConnectionManager;
import com.ericsson.eniq.events.server.logging.ServicesLogger;
import com.ericsson.eniq.events.server.services.GroupException;

/**
 * Methods to fetch information on groups from the repdb database
 * 
 * @author eemecoy
 * 
 */
@Singleton
//@TransactionManagement(TransactionManagementType.BEAN)
@LocalBean
public class GroupDataService {

    private transient String activeTechpackVersion = null;

    @EJB
    private DBConnectionManager dbConnectionManager;

    @PostConstruct
    public void createGroupCache() {
        Connection dwhrepConn = null;
        try {
            dwhrepConn = dbConnectionManager.getDwhrepConnection();
            final RockFactory dwhrepRockFactory = new RockFactory(dwhrepConn);
            GroupTypesCache.init(dwhrepRockFactory);
            activeTechpackVersion = getActiveTechpackVersion(dwhrepRockFactory);
            ServicesLogger.detailed(getClass().getName(), "createGroupCache", "Group Cache Rebuilt against " + activeTechpackVersion);
        } catch (final SQLException e) {
            if (e.getNextException() == null) {
                throw new GroupException(e);
            }
            throw new GroupException(e.getNextException());
        } catch (final RockException e) {
            throw new GroupException(e);
        } finally {
            try {
                if (dwhrepConn != null) {
                    ServicesLogger.detailed(Level.FINE, getClass().getName(), "createGroupCache", "Closing dwhrepConn " + dwhrepConn);
                    dwhrepConn.close();
                    ServicesLogger.detailed(Level.FINE, getClass().getName(), "createGroupCache", "Is closed: " + dwhrepConn.isClosed());
                }
            } catch (final SQLException e) {
                ServicesLogger.warn(getClass().getName(), "createGroupCache", e);
            }
        }
    }

    /**
     * Get the Events active techpack version. Groups can be specific to a version.
     * 
     * @param dwhrep
     *            Connection to dwhrep db
     * @return The active versionId for the events tech pack, null if not events tech pack is active.
     * @throws RockException
     *             Connectrion errors
     * @throws SQLException
     *             SQL Errors
     */
    private String getActiveTechpackVersion(final RockFactory dwhrep) throws RockException, SQLException {
        final Tpactivation where = new Tpactivation(dwhrep);
        where.setTechpack_name(EVENT_E_GROUP_TPNAME);
        final TpactivationFactory fac = new TpactivationFactory(dwhrep, where);
        final List<Tpactivation> vList = fac.get();
        //there can be only one active version....
        return (vList.isEmpty() ? null : vList.get(0).getVersionid());
    }

    public Map<String, Group> getGroupsForTemplates() {
        final Map<String, Group> groupMap = new HashMap<String, Group>();
        //First get the active tech pack as groups are version specific
        if (activeTechpackVersion == null) {
            ServicesLogger.warn(getClass().getName(), "getGroupsForTemplates", "No Active Tech Pack could be found.");
            return groupMap;
        }
        if (!GroupTypesCache.areGroupsDefined(activeTechpackVersion)) {
            ServicesLogger.warn(getClass().getName(), "getGroupsForTemplates", "No Group Definitions defined in Tech Pack " + activeTechpackVersion);
            return groupMap;
        }
        final Map<String, GroupTypeDef> groupDefs = GroupTypesCache.getGrouptypesDef(activeTechpackVersion);
        for (final String groupTypeName : groupDefs.keySet()) {
            final GroupTypeDef aGroupDefinition = groupDefs.get(groupTypeName);
            final Group velocityGroupDefinition = getCreateVelocityGroup(aGroupDefinition);
            groupMap.put(aGroupDefinition.getGroupType(), velocityGroupDefinition);
        }
        return groupMap;
    }

    private Group getCreateVelocityGroup(final GroupTypeDef aGroupDefinition) {
        final List<String> dataKeyNames = getDataNames(aGroupDefinition);
        return new Group(aGroupDefinition.getGroupType(), aGroupDefinition.getTableName(), GroupTypeDef.KEY_NAME_GROUP_NAME, dataKeyNames);
    }

    private List<String> getDataNames(final GroupTypeDef aGroupDefinition) {
        // I'm still creating them in a loop......
        final List<String> dataKeyNames = new ArrayList<String>();
        for (final GroupTypeKeyDef groupKey : aGroupDefinition.getDataKeys()) {
            //Ignore HIERARCHY_2 for 3G
            if (!groupKey.getKeyName().equals(HIER2)) {
                dataKeyNames.add(groupKey.getKeyName());
            }
        }
        return dataKeyNames;
    }

    public GroupTypeDef getGroupDefinition(final String groupDefType) {
        if (activeTechpackVersion == null) {
            ServicesLogger.warn(getClass().getName(), "getGroupDefinition", "No Active Tech Pack could be found.");
            return null;
        }
        if (!GroupTypesCache.areGroupsDefined(activeTechpackVersion)) {
            ServicesLogger.warn(getClass().getName(), "getGroupDefinition", "No Group Definitions defined in Tech Pack " + activeTechpackVersion);
            return null;
        }
        final String upper = groupDefType.toUpperCase();
        if (!GroupTypesCache.isGroupMgtType(activeTechpackVersion, upper)) {
            ServicesLogger.warn(getClass().getName(), "getGroupDefinition", "No Group Definition called " + upper + " defined in Tech Pack "
                    + activeTechpackVersion);
            return null;
        }
        return GroupTypesCache.getGrouptypesDef(activeTechpackVersion).get(upper);
    }

    public Map<String, GroupHashId> getGroupsForTemplatesForHashId() {
        final Map<String, GroupHashId> groupMap = new HashMap<String, GroupHashId>();
        //First get the active tech pack as groups are version specific
        if (activeTechpackVersion == null) {
            ServicesLogger.warn(getClass().getName(), "getGroupsForTemplates", "No Active Tech Pack could be found.");
            return groupMap;
        }
        if (!GroupTypesCache.areGroupsDefined(activeTechpackVersion)) {
            ServicesLogger.warn(getClass().getName(), "getGroupsForTemplates", "No Group Definitions defined in Tech Pack " + activeTechpackVersion);
            return groupMap;
        }
        final Map<String, GroupTypeDef> groupDefs = GroupTypesCache.getGrouptypesDef(activeTechpackVersion);
        for (final String groupTypeName : groupDefs.keySet()) {
            final GroupTypeDef aGroupDefinition = groupDefs.get(groupTypeName);
            final GroupHashId velocityGroupDefinition = getCreateVelocityGroupForHashId(aGroupDefinition);
            groupMap.put(aGroupDefinition.getGroupType(), velocityGroupDefinition);
        }
        return groupMap;
    }

    private GroupHashId getCreateVelocityGroupForHashId(final GroupTypeDef aGroupDefinition) {
        final List<String> dataKeyNames = getDataNames(aGroupDefinition);
        return new GroupHashId(aGroupDefinition.getGroupType(), aGroupDefinition.getTableName(), GroupTypeDef.KEY_NAME_GROUP_NAME, dataKeyNames);
    }

    /**
     * @param dbConnectionManager
     *            the dbConnectionManager to set
     */
    public void setDbConnectionManager(final DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

}
