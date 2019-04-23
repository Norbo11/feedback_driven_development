package np1815.feedback.metricsbackend.persistance;

import np1815.feedback.metricsbackend.api.impl.DslContextFactory;
import np1815.feedback.metricsbackend.model.FileException;
import np1815.feedback.metricsbackend.model.FilePerformance;
import org.jooq.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static np1815.feedback.metricsbackend.db.requests.Tables.APPLICATION;
import static np1815.feedback.metricsbackend.db.requests.Tables.EXCEPTION_FRAMES;
import static np1815.feedback.metricsbackend.db.requests.tables.Exception.EXCEPTION;
import static np1815.feedback.metricsbackend.db.requests.tables.Profile.PROFILE;
import static np1815.feedback.metricsbackend.db.requests.tables.ProfileLines.PROFILE_LINES;
import static org.jooq.impl.DSL.avg;

public class JooqMetricsBackendOperations implements MetricsBackendOperations {

    private final DslContextFactory dslContextFactory;

    public JooqMetricsBackendOperations(DslContextFactory dslContextFactory) {
        this.dslContextFactory = dslContextFactory;
    }

    @Override
    public void addProfileLine(int profileId, String filePath, int lineNumber, int numberOfSamples, long sampleTime) {
        dslContextFactory.create().insertInto(PROFILE_LINES)
            .columns(PROFILE_LINES.PROFILE_ID,
                PROFILE_LINES.FILE_NAME,
                PROFILE_LINES.SAMPLES,
                PROFILE_LINES.LINE_NUMBER,
                PROFILE_LINES.SAMPLE_TIME
            )
            .values(profileId,
                filePath,
                numberOfSamples,
                lineNumber, // Pyflame starts counting lines from 1, IntelliJ counts from 0, want DB to be consistent with IntelliJ
                sampleTime
            )
            .execute();
    }

    @Override
    public int addProfile(String applicationName, String version, Timestamp startTime, Timestamp endTime, long duration) {
        return dslContextFactory.create().insertInto(PROFILE)
            .columns(
                PROFILE.APPLICATION_NAME,
                PROFILE.VERSION,
                PROFILE.DURATION,
                PROFILE.START_TIMESTAMP,
                PROFILE.END_TIMESTAMP
            )
            .values(
                applicationName,
                version,
                duration,
                startTime,
                endTime
            )
            .returningResult(PROFILE.ID)
            .fetchOne()
            .get(PROFILE.ID);
    }

    @Override
    public Map<Integer, FilePerformance> getGlobalAveragePerLine(String applicationName, String version, String filename) {
        return dslContextFactory.create()
            .select(PROFILE_LINES.LINE_NUMBER,
                avg(PROFILE_LINES.SAMPLE_TIME).as("avg")
            )
            .from(PROFILE_LINES)
            .join(PROFILE).on(PROFILE.ID.eq(PROFILE_LINES.PROFILE_ID))
            .where(PROFILE_LINES.FILE_NAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .groupBy(PROFILE_LINES.FILE_NAME, PROFILE_LINES.LINE_NUMBER)
            .fetch()
            .intoMap(PROFILE_LINES.LINE_NUMBER, r -> new FilePerformance()
                .globalAverage(r.get("avg", Double.class))
                .status(FilePerformance.StatusEnum.PROFILED)
            );
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
    public int addException(int profileId, String exceptionType, String message) {
        return dslContextFactory.create()
            .insertInto(EXCEPTION)
            .columns( EXCEPTION.PROFILE_ID, EXCEPTION.EXCEPTION_TYPE, EXCEPTION.EXCEPTION_MESSAGE)
            .values(profileId, exceptionType, message)
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
    public Map<Integer, List<FileException>> getExceptionsForLine(String applicationName, String version, String filename) {
        return dslContextFactory.create()
            .select(
                EXCEPTION_FRAMES.LINE_NUMBER,
                PROFILE.END_TIMESTAMP,
                EXCEPTION.EXCEPTION_TYPE,
                EXCEPTION.EXCEPTION_MESSAGE
            )
            .from(EXCEPTION_FRAMES)
            .join(EXCEPTION).on(EXCEPTION_FRAMES.EXCEPTION_ID.eq(EXCEPTION.ID))
            .join(PROFILE).on(EXCEPTION.PROFILE_ID.eq(PROFILE.ID))
            .where(EXCEPTION_FRAMES.FILENAME.eq(filename))
            .and(PROFILE.APPLICATION_NAME.eq(applicationName))
            .and(PROFILE.VERSION.eq(version))
            .fetchGroups(EXCEPTION_FRAMES.LINE_NUMBER, r -> new FileException()
                .exceptionTime(r.get(PROFILE.END_TIMESTAMP).toLocalDateTime())
                .exceptionType(r.get(EXCEPTION.EXCEPTION_TYPE))
                .exceptionMessage(r.get(EXCEPTION.EXCEPTION_MESSAGE))
            );
    }
}
