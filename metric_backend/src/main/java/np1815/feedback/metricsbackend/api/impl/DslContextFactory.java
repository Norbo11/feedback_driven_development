package np1815.feedback.metricsbackend.api.impl;

import org.apache.commons.dbcp.BasicDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;


public class DslContextFactory {
    private static Logger LOG = LoggerFactory.getLogger(DslContextFactory.class);

    private String userName = "metric_backend";
    private String password = "imperial";
    private String url = "jdbc:postgresql://cloud-vm-46-203.doc.ic.ac.uk:5432/feedback_driven_development";

    public DSLContext create() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);

        LOG.info(String.format("Creating a DB connection: %s", url));

        return DSL.using(dataSource, SQLDialect.POSTGRES_9_5);
    }
}
