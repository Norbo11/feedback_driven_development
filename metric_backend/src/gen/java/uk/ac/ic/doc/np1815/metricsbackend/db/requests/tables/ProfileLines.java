/*
 * This file is generated by jOOQ.
 */
package uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables;


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
import uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.records.ProfileLinesRecord;


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
public class ProfileLines extends TableImpl<ProfileLinesRecord> {

    private static final long serialVersionUID = -177916353;

    /**
     * The reference instance of <code>requests.profile_lines</code>
     */
    public static final ProfileLines PROFILE_LINES = new ProfileLines();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ProfileLinesRecord> getRecordType() {
        return ProfileLinesRecord.class;
    }

    /**
     * The column <code>requests.profile_lines.id</code>.
     */
    public final TableField<ProfileLinesRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('requests.profile_lines_id_seq'::regclass)", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>requests.profile_lines.profile_id</code>.
     */
    public final TableField<ProfileLinesRecord, Integer> PROFILE_ID = createField("profile_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>requests.profile_lines.file_name</code>.
     */
    public final TableField<ProfileLinesRecord, String> FILE_NAME = createField("file_name", org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>requests.profile_lines.samples</code>.
     */
    public final TableField<ProfileLinesRecord, Integer> SAMPLES = createField("samples", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>requests.profile_lines</code> table reference
     */
    public ProfileLines() {
        this(DSL.name("profile_lines"), null);
    }

    /**
     * Create an aliased <code>requests.profile_lines</code> table reference
     */
    public ProfileLines(String alias) {
        this(DSL.name(alias), PROFILE_LINES);
    }

    /**
     * Create an aliased <code>requests.profile_lines</code> table reference
     */
    public ProfileLines(Name alias) {
        this(alias, PROFILE_LINES);
    }

    private ProfileLines(Name alias, Table<ProfileLinesRecord> aliased) {
        this(alias, aliased, null);
    }

    private ProfileLines(Name alias, Table<ProfileLinesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> ProfileLines(Table<O> child, ForeignKey<O, ProfileLinesRecord> key) {
        super(child, key, PROFILE_LINES);
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
        return Arrays.<Index>asList(Indexes.PK_PROFILE_LINES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ProfileLinesRecord, Integer> getIdentity() {
        return Keys.IDENTITY_PROFILE_LINES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ProfileLinesRecord> getPrimaryKey() {
        return Keys.PK_PROFILE_LINES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ProfileLinesRecord>> getKeys() {
        return Arrays.<UniqueKey<ProfileLinesRecord>>asList(Keys.PK_PROFILE_LINES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileLines as(String alias) {
        return new ProfileLines(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileLines as(Name alias) {
        return new ProfileLines(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ProfileLines rename(String name) {
        return new ProfileLines(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ProfileLines rename(Name name) {
        return new ProfileLines(name, null);
    }
}
