/*
 * This file is generated by jOOQ.
 */
package org.gradle.devprod.collector.persistence.generated.jooq.tables;


import org.gradle.devprod.collector.persistence.generated.jooq.Keys;
import org.gradle.devprod.collector.persistence.generated.jooq.Public;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.TaskTrendsRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row6;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TaskTrends extends TableImpl<TaskTrendsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.task_trends</code>
     */
    public static final TaskTrends TASK_TRENDS = new TaskTrends();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TaskTrendsRecord> getRecordType() {
        return TaskTrendsRecord.class;
    }

    /**
     * The column <code>public.task_trends.build_id</code>.
     */
    public final TableField<TaskTrendsRecord, String> BUILD_ID = createField(DSL.name("build_id"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.task_trends.project_id</code>.
     */
    public final TableField<TaskTrendsRecord, String> PROJECT_ID = createField(DSL.name("project_id"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.task_trends.task_path</code>.
     */
    public final TableField<TaskTrendsRecord, String> TASK_PATH = createField(DSL.name("task_path"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.task_trends.build_start</code>.
     */
    public final TableField<TaskTrendsRecord, OffsetDateTime> BUILD_START = createField(DSL.name("build_start"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false), this, "");

    /**
     * The column <code>public.task_trends.task_duration_ms</code>.
     */
    public final TableField<TaskTrendsRecord, Integer> TASK_DURATION_MS = createField(DSL.name("task_duration_ms"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.task_trends.status</code>.
     */
    public final TableField<TaskTrendsRecord, String> STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR, this, "");

    private TaskTrends(Name alias, Table<TaskTrendsRecord> aliased) {
        this(alias, aliased, null);
    }

    private TaskTrends(Name alias, Table<TaskTrendsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.task_trends</code> table reference
     */
    public TaskTrends(String alias) {
        this(DSL.name(alias), TASK_TRENDS);
    }

    /**
     * Create an aliased <code>public.task_trends</code> table reference
     */
    public TaskTrends(Name alias) {
        this(alias, TASK_TRENDS);
    }

    /**
     * Create a <code>public.task_trends</code> table reference
     */
    public TaskTrends() {
        this(DSL.name("task_trends"), null);
    }

    public <O extends Record> TaskTrends(Table<O> child, ForeignKey<O, TaskTrendsRecord> key) {
        super(child, key, TASK_TRENDS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<TaskTrendsRecord> getPrimaryKey() {
        return Keys.TASK_TRENDS_PK;
    }

    @Override
    public List<UniqueKey<TaskTrendsRecord>> getKeys() {
        return Arrays.<UniqueKey<TaskTrendsRecord>>asList(Keys.TASK_TRENDS_PK);
    }

    @Override
    public TaskTrends as(String alias) {
        return new TaskTrends(DSL.name(alias), this);
    }

    @Override
    public TaskTrends as(Name alias) {
        return new TaskTrends(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TaskTrends rename(String name) {
        return new TaskTrends(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TaskTrends rename(Name name) {
        return new TaskTrends(name, null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<String, String, String, OffsetDateTime, Integer, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }
}
