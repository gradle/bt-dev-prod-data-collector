/*
 * This file is generated by jOOQ.
 */
package org.gradle.devprod.collector.persistence.generated.jooq.tables;


import java.util.Arrays;
import java.util.List;

import org.gradle.devprod.collector.persistence.generated.jooq.Keys;
import org.gradle.devprod.collector.persistence.generated.jooq.Public;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.LongTestRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class LongTest extends TableImpl<LongTestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.long_test</code>
     */
    public static final LongTest LONG_TEST = new LongTest();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<LongTestRecord> getRecordType() {
        return LongTestRecord.class;
    }

    /**
     * The column <code>public.long_test.build_id</code>.
     */
    public final TableField<LongTestRecord, String> BUILD_ID = createField(DSL.name("build_id"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.long_test.class_name</code>.
     */
    public final TableField<LongTestRecord, String> CLASS_NAME = createField(DSL.name("class_name"), SQLDataType.VARCHAR(1024).nullable(false), this, "");

    /**
     * The column <code>public.long_test.duration_ms</code>.
     */
    public final TableField<LongTestRecord, Long> DURATION_MS = createField(DSL.name("duration_ms"), SQLDataType.BIGINT.nullable(false), this, "");

    private LongTest(Name alias, Table<LongTestRecord> aliased) {
        this(alias, aliased, null);
    }

    private LongTest(Name alias, Table<LongTestRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.long_test</code> table reference
     */
    public LongTest(String alias) {
        this(DSL.name(alias), LONG_TEST);
    }

    /**
     * Create an aliased <code>public.long_test</code> table reference
     */
    public LongTest(Name alias) {
        this(alias, LONG_TEST);
    }

    /**
     * Create a <code>public.long_test</code> table reference
     */
    public LongTest() {
        this(DSL.name("long_test"), null);
    }

    public <O extends Record> LongTest(Table<O> child, ForeignKey<O, LongTestRecord> key) {
        super(child, key, LONG_TEST);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<LongTestRecord> getPrimaryKey() {
        return Keys.LONG_TEST_PK;
    }

    @Override
    public List<UniqueKey<LongTestRecord>> getKeys() {
        return Arrays.<UniqueKey<LongTestRecord>>asList(Keys.LONG_TEST_PK);
    }

    @Override
    public List<ForeignKey<LongTestRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<LongTestRecord, ?>>asList(Keys.LONG_TEST__LONG_TEST_BUILD_FK);
    }

    private transient Build _build;

    public Build build() {
        if (_build == null)
            _build = new Build(this, Keys.LONG_TEST__LONG_TEST_BUILD_FK);

        return _build;
    }

    @Override
    public LongTest as(String alias) {
        return new LongTest(DSL.name(alias), this);
    }

    @Override
    public LongTest as(Name alias) {
        return new LongTest(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public LongTest rename(String name) {
        return new LongTest(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public LongTest rename(Name name) {
        return new LongTest(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, Long> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
