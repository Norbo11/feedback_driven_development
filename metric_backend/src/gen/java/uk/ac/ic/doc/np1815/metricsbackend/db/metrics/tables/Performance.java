/*
 * This file is generated by jOOQ.
 */
package uk.ac.ic.doc.np1815.metricsbackend.db.metrics.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import uk.ac.ic.doc.np1815.metricsbackend.db.metrics.Indexes;
import uk.ac.ic.doc.np1815.metricsbackend.db.metrics.Keys;
import uk.ac.ic.doc.np1815.metricsbackend.db.metrics.Metrics;
import uk.ac.ic.doc.np1815.metricsbackend.db.metrics.tables.records.PerformanceRecord;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Performance extends TableImpl<PerformanceRecord> {

    private static final long serialVersionUID = 1515365623;

    /**
     * The reference instance of <code>metrics.performance</code>
     */
    public static final Performance PERFORMANCE = new Performance();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PerformanceRecord> getRecordType() {
        return PerformanceRecord.class;
    }

    /**
     * The column <code>metrics.performance.file_name</code>.
     */
    public final TableField<PerformanceRecord, String> FILE_NAME = createField("file_name", org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>metrics.performance.average_performance</code>.
     */
    public final TableField<PerformanceRecord, Double> AVERAGE_PERFORMANCE = createField("average_performance", org.jooq.impl.SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * Create a <code>metrics.performance</code> table reference
     */
    public Performance() {
        this(DSL.name("performance"), null);
    }

    /**
     * Create an aliased <code>metrics.performance</code> table reference
     */
    public Performance(String alias) {
        this(DSL.name(alias), PERFORMANCE);
    }

    /**
     * Create an aliased <code>metrics.performance</code> table reference
     */
    public Performance(Name alias) {
        this(alias, PERFORMANCE);
    }

    private Performance(Name alias, Table<PerformanceRecord> aliased) {
        this(alias, aliased, null);
    }

    private Performance(Name alias, Table<PerformanceRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Performance(Table<O> child, ForeignKey<O, PerformanceRecord> key) {
        super(child, key, PERFORMANCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Metrics.METRICS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PK_PERFORMANCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PerformanceRecord> getPrimaryKey() {
        return Keys.PK_PERFORMANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PerformanceRecord>> getKeys() {
        return Arrays.<UniqueKey<PerformanceRecord>>asList(Keys.PK_PERFORMANCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Performance as(String alias) {
        return new Performance(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Performance as(Name alias) {
        return new Performance(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Performance rename(String name) {
        return new Performance(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Performance rename(Name name) {
        return new Performance(name, null);
    }
}