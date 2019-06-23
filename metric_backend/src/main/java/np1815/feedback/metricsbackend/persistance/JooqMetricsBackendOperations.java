package np1815.feedback.metricsbackend.persistance;

import com.google.common.collect.Sets;
import np1815.feedback.metricsbackend.api.impl.DslContextFactory;
import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.metricsbackend.persistance.models.LineGlobalPerformance;
import org.jooq.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static np1815.feedback.metricsbackend.db.requests.Tables.*;
import static np1815.feedback.metricsbackend.db.requests.tables.Exception.EXCEPTION;
import static np1815.feedback.metricsbackend.db.requests.tables.Profile.PROFILE;
import static np1815.feedback.metricsbackend.db.requests.tables.ProfileLines.PROFILE_LINES;
import static org.jooq.impl.DSL.*;

public class JooqMetricsBackendOperations implements MetricsBackendOperations {

    private final DslContextFactory dslContextFactory;

    public JooqMetricsBackendOperations(DslContextFactory dslContextFactory) {
        this.dslContextFactory = dslContextFactory;
    }

    @Override
    public void addProfileLine(LocalDateTime profileStartTimestamp, String filePath, int lineNumber, int numberOfSamples, long sampleTime, String functionName) {
        dslContextFactory.create().insertInto(PROFILE_LINES)
            .columns(PROFILE_LINES.PROFILE_START_TIMESTAMP,
                PROFILE_LINES.FILE_NAME,
                PROFILE_LINES.SAMPLES,
                PROFILE_LINES.LINE_NUMBER,
                PROFILE_LINES.SAMPLE_TIME,
                PROFILE_LINES.FUNCTION_NAME
            )
            .values(profileStartTimestamp,
                filePath,
                numberOfSamples,
                lineNumber,
                sampleTime,
                functionName
            )
            .execute();
    }

    @Override
    public LocalDateTime addProfile(String applicationName, String version, LocalDateTime startTime, LocalDateTime endTime, String urlRule, long duration) {
        return dslContextFactory.create().insertInto(PROFILE)
            .columns(
                PROFILE.APPLICATION_NAME,
                PROFILE.VERSION,
                PROFILE.DURATION,
                PROFILE.START_TIMESTAMP,
                PROFILE.END_TIMESTAMP,
                PROFILE.URL_RULE
            )
            .values(
                applicationName,
                version,
                duration,
                startTime,
                endTime,
                urlRule
            )
            .returningResult(PROFILE.START_TIMESTAMP)
            .fetchOne()
            .get(PROFILE.START_TIMESTAMP);
    }

    @Override
    public Set<String> getApplicationVersions(String applicationName) {
        Result<Record1<String>> result = dslContextFactory.create()
            .select(PROFILE.VERSION)
            .from(PROFILE)
            .where(PROFILE.APPLICATION_NAME.eq(applicationName))
            .fetch();

        return result.intoSet(PROFILE.VERSION);
    }

    @Override
    public void addApplicationIfDoesntExist(String applicationName) {
        if (dslContextFactory.create().fetchExists(APPLICATION, APPLICATION.NAME.eq(applicationName))) {
            return;
        }

        dslContextFactory.create()
            .insertInto(APPLICATION)
            .columns(APPLICATION.NAME)
            .values(applicationName)
            .execute();
    }

    @Override
    public int addException(LocalDateTime profileStartTimestamp, String exceptionType, String message) {
        return dslContextFactory.create()
            .insertInto(EXCEPTION)
            .columns(EXCEPTION.PROFILE_START_TIMESTAMP, EXCEPTION.EXCEPTION_TYPE, EXCEPTION.EXCEPTION_MESSAGE)
            .values(profileStartTimestamp, exceptionType, message)
            .returning(EXCEPTION.ID)
            .fetchOne()
            .get(EXCEPTION.ID);
    }

    @Override
    public Integer addExceptionFrame(int exceptionId, String filename, Integer lineNumber, String functionName, Integer parentFrameId) {
        return dslContextFactory.create()
            .insertInto(EXCEPTION_FRAMES)
            .columns(
                EXCEPTION_FRAMES.EXCEPTION_ID,
                EXCEPTION_FRAMES.FILENAME,
                EXCEPTION_FRAMES.LINE_NUMBER,
                EXCEPTION_FRAMES.FUNCTION_NAME,
                EXCEPTION_FRAMES.PARENT_ID
            )
            .values(exceptionId, filename, lineNumber, functionName, parentFrameId)
            .returning(EXCEPTION_FRAMES.ID)
            .fetchOne()
            .get(EXCEPTION_FRAMES.ID);
    }

    @Override
    public Map<Integer, LineGlobalPerformance> getGlobalPerformanceForLines(String applicationName, String version, String filename) {
        return dslContextFactory.create()
            .select(PROFILE_LINES.LINE_NUMBER,
                avg(PROFILE_LINES.SAMPLE_TIME).as("avg")
            )
            .from(PROFILE_LINES)
            .join(PROFILE).on(PROFILE.START_TIMESTAMP.eq(PROFILE_LINES.PROFILE_START_TIMESTAMP))
            .where(PROFILE_LINES.FILE_NAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .groupBy(PROFILE_LINES.FILE_NAME, PROFILE_LINES.LINE_NUMBER)
            .fetch()
            .intoMap(PROFILE_LINES.LINE_NUMBER, r ->
                new LineGlobalPerformance(r.get("avg", Double.class), LinePerformance.StatusEnum.PROFILED)
            );
    }

    @Override
    public Map<Integer, List<LineException>> getExceptionsFeedbackForLines(String applicationName, String version, String filename) {
        return dslContextFactory.create()
            .select(
                PROFILE.START_TIMESTAMP,
                PROFILE.END_TIMESTAMP,
                EXCEPTION_FRAMES.LINE_NUMBER,
                EXCEPTION.EXCEPTION_TYPE,
                EXCEPTION.EXCEPTION_MESSAGE
            )
            .from(EXCEPTION_FRAMES)
            .join(EXCEPTION).on(EXCEPTION_FRAMES.EXCEPTION_ID.eq(EXCEPTION.ID))
            .join(PROFILE).on(EXCEPTION.PROFILE_START_TIMESTAMP.eq(PROFILE.START_TIMESTAMP))
            .where(EXCEPTION_FRAMES.FILENAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .fetchGroups(EXCEPTION_FRAMES.LINE_NUMBER, r -> new LineException()
                .profileStartTimestamp(r.get(PROFILE.START_TIMESTAMP))
                .exceptionTime(r.get(PROFILE.END_TIMESTAMP))
                .exceptionType(r.get(EXCEPTION.EXCEPTION_TYPE))
                .exceptionMessage(r.get(EXCEPTION.EXCEPTION_MESSAGE))
            );
    }

    @Override
    public Map<Integer, LineGeneral> getGeneralFeedbackForLines(String applicationName, String version, String filename) {
        Map<Integer, Record2<Integer, Integer>> profileCount = dslContextFactory.create()
            .select(PROFILE_LINES.LINE_NUMBER,
                count(PROFILE.START_TIMESTAMP).as("count")
            )
            .from(PROFILE_LINES)
            .join(PROFILE).on(PROFILE.START_TIMESTAMP.eq(PROFILE_LINES.PROFILE_START_TIMESTAMP))
            .where(PROFILE_LINES.FILE_NAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .groupBy(PROFILE_LINES.FILE_NAME, PROFILE_LINES.LINE_NUMBER)
            .fetch()
            .intoMap(PROFILE_LINES.LINE_NUMBER);

        Map<Integer, Record2<Integer, Integer>> exceptionCount = dslContextFactory.create()
            .select(EXCEPTION_FRAMES.LINE_NUMBER,
                count(EXCEPTION.ID).as("count")
            )
            .from(EXCEPTION_FRAMES)
            .join(EXCEPTION).on(EXCEPTION.ID.eq(EXCEPTION_FRAMES.EXCEPTION_ID))
            .join(PROFILE).on(EXCEPTION.PROFILE_START_TIMESTAMP.eq(PROFILE.START_TIMESTAMP))
            .where(EXCEPTION_FRAMES.FILENAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .groupBy(EXCEPTION_FRAMES.FILENAME, EXCEPTION_FRAMES.LINE_NUMBER)
            .fetch()
            .intoMap(EXCEPTION_FRAMES.LINE_NUMBER);

        Map<Integer, Record2<Integer, Integer>> logCount = dslContextFactory.create()
            .select(LOGGING_LINES.LINE_NUMBER,
                count(LOGGING_LINES.ID).as("count")
            )
            .from(LOGGING_LINES)
            .join(PROFILE).on(PROFILE.START_TIMESTAMP.eq(LOGGING_LINES.PROFILE_START_TIMESTAMP))
            .where(LOGGING_LINES.FILENAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .groupBy(LOGGING_LINES.FILENAME, LOGGING_LINES.LINE_NUMBER)
            .fetch()
            .intoMap(LOGGING_LINES.LINE_NUMBER);

        Map<Integer, LineGeneral> generalFeedback = new HashMap<>();

        Set<Integer> lines = Sets.union(logCount.keySet(), Sets.union(profileCount.keySet(), exceptionCount.keySet()));

        for (int line : lines) {
            LineGeneral general = new LineGeneral();
            general.setProfileCount(profileCount.containsKey(line) ? profileCount.get(line).get("count", Integer.class) : 0);
            general.setLoggingCount(logCount.containsKey(line) ? logCount.get(line).get("count", Integer.class) : 0);
            general.setExceptionCount(exceptionCount.containsKey(line) ? exceptionCount.get(line).get("count", Integer.class) : 0);
            generalFeedback.put(line, general);
        }

        return generalFeedback;
    }

    @Override
    public Request getFirstRequestForLine(String applicationName, String version, String filename, int line) {
        return dslContextFactory.create()
            .select(
                PROFILE.START_TIMESTAMP,
                PROFILE.END_TIMESTAMP,
                PROFILE.DURATION
            )
            .distinctOn(PROFILE.VERSION)
            .from(PROFILE)
            .leftJoin(PROFILE_LINES).on(PROFILE.START_TIMESTAMP.eq(PROFILE_LINES.PROFILE_START_TIMESTAMP))
            .leftJoin(LOGGING_LINES).on(PROFILE.START_TIMESTAMP.eq(LOGGING_LINES.PROFILE_START_TIMESTAMP))
            .leftJoin(EXCEPTION).on(PROFILE.START_TIMESTAMP.eq(EXCEPTION.PROFILE_START_TIMESTAMP))
            .leftJoin(EXCEPTION_FRAMES).on(EXCEPTION.ID.eq(EXCEPTION_FRAMES.EXCEPTION_ID))
            .where(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .and(PROFILE_LINES.LINE_NUMBER.eq(line)
                .or(LOGGING_LINES.LINE_NUMBER.eq(line)
                .or(EXCEPTION_FRAMES.LINE_NUMBER.eq(line)))
            )
            .fetchOneInto(Request.class);
    }

    @Override
    public Map<Integer, List<LineExecution>> getPerformanceHistoryForLines(String applicationName, String version, String filename) {
        return getPerformanceHistory(applicationName, version, filename, null);
    }

    @Override
    public Map<Integer, List<LineExecution>> getPerformanceHistoryForLines(String applicationName, String version, String filename, LocalDateTime since) {
        return getPerformanceHistory(applicationName, version, filename, since);
    }

    private Map<Integer, List<LineExecution>> getPerformanceHistory(String applicationName, String version, String filename, LocalDateTime since) {
        SelectConditionStep<Record3<LocalDateTime, Integer, Long>> query = dslContextFactory.create()
            .select(
                PROFILE.START_TIMESTAMP,
                PROFILE_LINES.LINE_NUMBER,
                PROFILE_LINES.SAMPLE_TIME
            )
            .from(PROFILE_LINES)
            .join(PROFILE).on(PROFILE.START_TIMESTAMP.eq(PROFILE_LINES.PROFILE_START_TIMESTAMP))
            .where(PROFILE_LINES.FILE_NAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version));

        if (since != null) {
            query = query.and(PROFILE.START_TIMESTAMP.greaterOrEqual(since));
        }

        return query
            .fetch()
            .intoGroups(PROFILE_LINES.LINE_NUMBER, r -> new LineExecution()
                .profileStartTimestamp(r.get(PROFILE.START_TIMESTAMP))
                .lineNumber(r.get(PROFILE_LINES.LINE_NUMBER))
                .sampleTime(r.get(PROFILE_LINES.SAMPLE_TIME))
            );
    }

    @Override
    public void addLoggingLine(LocalDateTime profileStartTimestamp, String filePath, int lineNumber, String logger, String level, String message, LocalDateTime timestamp) {
        dslContextFactory.create().insertInto(LOGGING_LINES)
            .columns(
                LOGGING_LINES.PROFILE_START_TIMESTAMP,
                LOGGING_LINES.LOG_TIMESTAMP,
                LOGGING_LINES.FILENAME,
                LOGGING_LINES.LINE_NUMBER,
                LOGGING_LINES.LOGGER,
                LOGGING_LINES.LEVEL,
                LOGGING_LINES.MESSAGE
            )
            .values(
                profileStartTimestamp,
                timestamp,
                filePath,
                lineNumber,
                logger,
                level,
                message
            )
            .execute();
    }

    @Override
    public Map<Integer, List<LogRecord>> getLoggingRecordsForLines(String applicationName, String version, String filename) {
        return dslContextFactory.create()
            .select(
                LOGGING_LINES.PROFILE_START_TIMESTAMP,
                LOGGING_LINES.LINE_NUMBER,
                LOGGING_LINES.LOG_TIMESTAMP,
                LOGGING_LINES.LEVEL,
                LOGGING_LINES.LOGGER,
                LOGGING_LINES.MESSAGE
            )
            .from(LOGGING_LINES)
            .join(PROFILE).on(LOGGING_LINES.PROFILE_START_TIMESTAMP.eq(PROFILE.START_TIMESTAMP))
            .where(LOGGING_LINES.FILENAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .fetchGroups(LOGGING_LINES.LINE_NUMBER, LogRecord.class);
    }

    @Override
    public void addRequestParam(LocalDateTime profileStartTimestamp, String value, String type, String name) {
        dslContextFactory.create()
            .insertInto(REQUEST_PARAMS)
            .columns(
                REQUEST_PARAMS.REQUEST_START_TIMESTAMP,
                REQUEST_PARAMS.NAME,
                REQUEST_PARAMS.VALUE,
                REQUEST_PARAMS.TYPE
            )
            .values(
                profileStartTimestamp,
                name,
                value,
                type
            )
            .execute();
    }

    @Override
    public List<Request> getRequests(String applicationName, String version) {
        DSLContext dslContext = dslContextFactory.create();
        List<Request> requests = dslContext
            .select(
                PROFILE.START_TIMESTAMP,
                PROFILE.END_TIMESTAMP,
                PROFILE.DURATION,
                PROFILE.URL_RULE
            ).from(PROFILE)
            .where(PROFILE.APPLICATION_NAME.eq(applicationName).and(PROFILE.VERSION.eq(version)))
            .fetchInto(Request.class);

        requests.forEach(r -> {
            List<NewRequestParam> requestParams = dslContext.select(
                REQUEST_PARAMS.NAME,
                REQUEST_PARAMS.VALUE,
                REQUEST_PARAMS.TYPE
            ).from(REQUEST_PARAMS).fetchInto(NewRequestParam.class);

            r.setRequestParams(requestParams);
        });

        return requests;
    }
}
