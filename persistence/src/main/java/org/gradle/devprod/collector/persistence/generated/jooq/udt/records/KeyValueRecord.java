/*
 * This file is generated by jOOQ.
 */
package org.gradle.devprod.collector.persistence.generated.jooq.udt.records;


import org.gradle.devprod.collector.persistence.generated.jooq.udt.KeyValue;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UDTRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class KeyValueRecord extends UDTRecordImpl<KeyValueRecord> implements Record2<String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.key_value.key</code>.
     */
    public void setKey(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.key_value.key</code>.
     */
    public String getKey() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.key_value.value</code>.
     */
    public void setValue(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.key_value.value</code>.
     */
    public String getValue() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return KeyValue.KEY;
    }

    @Override
    public Field<String> field2() {
        return KeyValue.VALUE;
    }

    @Override
    public String component1() {
        return getKey();
    }

    @Override
    public String component2() {
        return getValue();
    }

    @Override
    public String value1() {
        return getKey();
    }

    @Override
    public String value2() {
        return getValue();
    }

    @Override
    public KeyValueRecord value1(String value) {
        setKey(value);
        return this;
    }

    @Override
    public KeyValueRecord value2(String value) {
        setValue(value);
        return this;
    }

    @Override
    public KeyValueRecord values(String value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached KeyValueRecord
     */
    public KeyValueRecord() {
        super(KeyValue.KEY_VALUE);
    }

    /**
     * Create a detached, initialised KeyValueRecord
     */
    public KeyValueRecord(String key, String value) {
        super(KeyValue.KEY_VALUE);

        setKey(key);
        setValue(value);
        resetChangedOnNotNull();
    }
}
