/*
 * This file is generated by jOOQ.
 */
package org.gradle.devprod.collector.persistence.generated.jooq.tables.records;


import org.gradle.devprod.collector.persistence.generated.jooq.tables.TaskTrends;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TaskTrendsRecord extends UpdatableRecordImpl<TaskTrendsRecord> implements Record6<String, String, String, OffsetDateTime, Integer, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.task_trends.build_id</code>.
     */
    public void setBuildId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.task_trends.build_id</code>.
     */
    public String getBuildId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.task_trends.project_id</code>.
     */
    public void setProjectId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.task_trends.project_id</code>.
     */
    public String getProjectId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.task_trends.task_path</code>.
     */
    public void setTaskPath(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.task_trends.task_path</code>.
     */
    public String getTaskPath() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.task_trends.build_start</code>.
     */
    public void setBuildStart(OffsetDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.task_trends.build_start</code>.
     */
    public OffsetDateTime getBuildStart() {
        return (OffsetDateTime) get(3);
    }

    /**
     * Setter for <code>public.task_trends.task_duration_ms</code>.
     */
    public void setTaskDurationMs(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.task_trends.task_duration_ms</code>.
     */
    public Integer getTaskDurationMs() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>public.task_trends.status</code>.
     */
    public void setStatus(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.task_trends.status</code>.
     */
    public String getStatus() {
        return (String) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<String, String, String, OffsetDateTime, Integer, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<String, String, String, OffsetDateTime, Integer, String> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return TaskTrends.TASK_TRENDS.BUILD_ID;
    }

    @Override
    public Field<String> field2() {
        return TaskTrends.TASK_TRENDS.PROJECT_ID;
    }

    @Override
    public Field<String> field3() {
        return TaskTrends.TASK_TRENDS.TASK_PATH;
    }

    @Override
    public Field<OffsetDateTime> field4() {
        return TaskTrends.TASK_TRENDS.BUILD_START;
    }

    @Override
    public Field<Integer> field5() {
        return TaskTrends.TASK_TRENDS.TASK_DURATION_MS;
    }

    @Override
    public Field<String> field6() {
        return TaskTrends.TASK_TRENDS.STATUS;
    }

    @Override
    public String component1() {
        return getBuildId();
    }

    @Override
    public String component2() {
        return getProjectId();
    }

    @Override
    public String component3() {
        return getTaskPath();
    }

    @Override
    public OffsetDateTime component4() {
        return getBuildStart();
    }

    @Override
    public Integer component5() {
        return getTaskDurationMs();
    }

    @Override
    public String component6() {
        return getStatus();
    }

    @Override
    public String value1() {
        return getBuildId();
    }

    @Override
    public String value2() {
        return getProjectId();
    }

    @Override
    public String value3() {
        return getTaskPath();
    }

    @Override
    public OffsetDateTime value4() {
        return getBuildStart();
    }

    @Override
    public Integer value5() {
        return getTaskDurationMs();
    }

    @Override
    public String value6() {
        return getStatus();
    }

    @Override
    public TaskTrendsRecord value1(String value) {
        setBuildId(value);
        return this;
    }

    @Override
    public TaskTrendsRecord value2(String value) {
        setProjectId(value);
        return this;
    }

    @Override
    public TaskTrendsRecord value3(String value) {
        setTaskPath(value);
        return this;
    }

    @Override
    public TaskTrendsRecord value4(OffsetDateTime value) {
        setBuildStart(value);
        return this;
    }

    @Override
    public TaskTrendsRecord value5(Integer value) {
        setTaskDurationMs(value);
        return this;
    }

    @Override
    public TaskTrendsRecord value6(String value) {
        setStatus(value);
        return this;
    }

    @Override
    public TaskTrendsRecord values(String value1, String value2, String value3, OffsetDateTime value4, Integer value5, String value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TaskTrendsRecord
     */
    public TaskTrendsRecord() {
        super(TaskTrends.TASK_TRENDS);
    }

    /**
     * Create a detached, initialised TaskTrendsRecord
     */
    public TaskTrendsRecord(String buildId, String projectId, String taskPath, OffsetDateTime buildStart, Integer taskDurationMs, String status) {
        super(TaskTrends.TASK_TRENDS);

        setBuildId(buildId);
        setProjectId(projectId);
        setTaskPath(taskPath);
        setBuildStart(buildStart);
        setTaskDurationMs(taskDurationMs);
        setStatus(status);
    }
}
