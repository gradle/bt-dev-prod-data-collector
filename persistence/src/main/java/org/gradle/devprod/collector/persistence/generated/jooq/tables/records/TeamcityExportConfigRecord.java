/*
 * This file is generated by jOOQ.
 */
package org.gradle.devprod.collector.persistence.generated.jooq.tables.records;


import java.time.OffsetDateTime;

import org.gradle.devprod.collector.persistence.generated.jooq.tables.TeamcityExportConfig;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TeamcityExportConfigRecord extends UpdatableRecordImpl<TeamcityExportConfigRecord> implements Record2<String, OffsetDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.teamcity_export_config.project_id</code>.
     */
    public void setProjectId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.teamcity_export_config.project_id</code>.
     */
    public String getProjectId() {
        return (String) get(0);
    }

    /**
     * Setter for
     * <code>public.teamcity_export_config.latest_finished_build_timestamp</code>.
     */
    public void setLatestFinishedBuildTimestamp(OffsetDateTime value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>public.teamcity_export_config.latest_finished_build_timestamp</code>.
     */
    public OffsetDateTime getLatestFinishedBuildTimestamp() {
        return (OffsetDateTime) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, OffsetDateTime> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, OffsetDateTime> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return TeamcityExportConfig.TEAMCITY_EXPORT_CONFIG.PROJECT_ID;
    }

    @Override
    public Field<OffsetDateTime> field2() {
        return TeamcityExportConfig.TEAMCITY_EXPORT_CONFIG.LATEST_FINISHED_BUILD_TIMESTAMP;
    }

    @Override
    public String component1() {
        return getProjectId();
    }

    @Override
    public OffsetDateTime component2() {
        return getLatestFinishedBuildTimestamp();
    }

    @Override
    public String value1() {
        return getProjectId();
    }

    @Override
    public OffsetDateTime value2() {
        return getLatestFinishedBuildTimestamp();
    }

    @Override
    public TeamcityExportConfigRecord value1(String value) {
        setProjectId(value);
        return this;
    }

    @Override
    public TeamcityExportConfigRecord value2(OffsetDateTime value) {
        setLatestFinishedBuildTimestamp(value);
        return this;
    }

    @Override
    public TeamcityExportConfigRecord values(String value1, OffsetDateTime value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TeamcityExportConfigRecord
     */
    public TeamcityExportConfigRecord() {
        super(TeamcityExportConfig.TEAMCITY_EXPORT_CONFIG);
    }

    /**
     * Create a detached, initialised TeamcityExportConfigRecord
     */
    public TeamcityExportConfigRecord(String projectId, OffsetDateTime latestFinishedBuildTimestamp) {
        super(TeamcityExportConfig.TEAMCITY_EXPORT_CONFIG);

        setProjectId(projectId);
        setLatestFinishedBuildTimestamp(latestFinishedBuildTimestamp);
        resetChangedOnNotNull();
    }
}
