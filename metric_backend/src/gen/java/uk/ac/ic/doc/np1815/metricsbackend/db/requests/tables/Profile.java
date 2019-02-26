/*
 * This file is generated by jOOQ.
 */
package uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import uk.ac.ic.doc.np1815.metricsbackend.db.requests.Indexes;
import uk.ac.ic.doc.np1815.metricsbackend.db.requests.Keys;
import uk.ac.ic.doc.np1815.metricsbackend.db.requests.Requests;
import uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.records.ProfileRecord;


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
public class Profile extends TableImpl<ProfileRecord> {

    private static final long serialVersionUID = 1692000110;

    /**
     * The reference instance of <code>requests.profile</code>
     */
    public static final Profile PROFILE = new Profile();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ProfileRecord> getRecordType() {
        return ProfileRecord.class;
    }

    /**
     * The column <code>requests.profile.id</code>.
     */
    public final TableField<ProfileRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('requests.profile_id_seq'::regclass)", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>requests.profile.duration</code>.
     */
    public final TableField<ProfileRecord, Double> DURATION = createField("duration", org.jooq.impl.SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>requests.profile.start_timestamp</code>.
     */
    public final TableField<ProfileRecord, Timestamp> START_TIMESTAMP = createField("start_timestamp", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>requests.profile.end_timestamp</code>.
     */
    public final TableField<ProfileRecord, Timestamp> END_TIMESTAMP = createField("end_timestamp", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * Create a <code>requests.profile</code> table reference
     */
    public Profile() {
        this(DSL.name("profile"), null);
    }

    /**
     * Create an aliased <code>requests.profile</code> table reference
     */
    public Profile(String alias) {
        this(DSL.name(alias), PROFILE);
    }

    /**
     * Create an aliased <code>requests.profile</code> table reference
     */
    public Profile(Name alias) {
        this(alias, PROFILE);
    }

    private Profile(Name alias, Table<ProfileRecord> aliased) {
        this(alias, aliased, null);
    }

    private Profile(Name alias, Table<ProfileRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Profile(Table<O> child, ForeignKey<O, ProfileRecord> key) {
        super(child, key, PROFILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Requests.REQUESTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PK_PROFILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ProfileRecord, Integer> getIdentity() {
        return Keys.IDENTITY_PROFILE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ProfileRecord> getPrimaryKey() {
        return Keys.PK_PROFILE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ProfileRecord>> getKeys() {
        return Arrays.<UniqueKey<ProfileRecord>>asList(Keys.PK_PROFILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile as(String alias) {
        return new Profile(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile as(Name alias) {
        return new Profile(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Profile rename(String name) {
        return new Profile(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Profile rename(Name name) {
        return new Profile(name, null);
    }
}
