/*
 * This file is generated by jOOQ.
 */
package uk.ac.ic.doc.np1815.jooq.metrics;


import javax.annotation.Generated;

import org.jooq.UniqueKey;
import org.jooq.impl.Internal;

import uk.ac.ic.doc.np1815.jooq.metrics.tables.Performance;
import uk.ac.ic.doc.np1815.jooq.metrics.tables.records.PerformanceRecord;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>metrics</code> schema.
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

    public static final UniqueKey<PerformanceRecord> PK_PERFORMANCE = UniqueKeys0.PK_PERFORMANCE;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 {
        public static final UniqueKey<PerformanceRecord> PK_PERFORMANCE = Internal.createUniqueKey(Performance.PERFORMANCE, "pk_performance", Performance.PERFORMANCE.FILE_NAME);
    }
}
