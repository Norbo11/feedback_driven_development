/*
 * This file is generated by jOOQ.
 */
package uk.ac.ic.doc.np1815.jooq;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import uk.ac.ic.doc.np1815.jooq.tables.Performance;


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
public class Metrics extends SchemaImpl {

    private static final long serialVersionUID = -1250650710;

    /**
     * The reference instance of <code>metrics</code>
     */
    public static final Metrics METRICS = new Metrics();

    /**
     * The table <code>metrics.performance</code>.
     */
    public final Performance PERFORMANCE = uk.ac.ic.doc.np1815.jooq.tables.Performance.PERFORMANCE;

    /**
     * No further instances allowed
     */
    private Metrics() {
        super("metrics", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Performance.PERFORMANCE);
    }
}
