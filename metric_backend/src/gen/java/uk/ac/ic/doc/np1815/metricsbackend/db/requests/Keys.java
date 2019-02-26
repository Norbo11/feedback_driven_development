/*
 * This file is generated by jOOQ.
 */
package uk.ac.ic.doc.np1815.metricsbackend.db.requests;


import javax.annotation.Generated;

import org.jooq.UniqueKey;
import org.jooq.impl.Internal;

import uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.Profile;
import uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.ProfileLines;
import uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.records.ProfileLinesRecord;
import uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.records.ProfileRecord;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>requests</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ProfileRecord> PK_PROFILE = UniqueKeys0.PK_PROFILE;
    public static final UniqueKey<ProfileLinesRecord> PK_PROFILE_LINES = UniqueKeys0.PK_PROFILE_LINES;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 {
        public static final UniqueKey<ProfileRecord> PK_PROFILE = Internal.createUniqueKey(Profile.PROFILE, "pk_profile", Profile.PROFILE.ID);
        public static final UniqueKey<ProfileLinesRecord> PK_PROFILE_LINES = Internal.createUniqueKey(ProfileLines.PROFILE_LINES, "pk_profile_lines", ProfileLines.PROFILE_LINES.ID);
    }
}