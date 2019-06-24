SELECT
  requests.profile.id,
  requests.profile.version,
  requests.profile_lines.file_name,
  requests.profile_lines.line_number,
  requests.profile_lines.sample_time
FROM
  requests.profile
JOIN
  requests.profile_lines ON profile.id = profile_lines.profile_id


select
  "requests"."profile_lines"."line_number",
  avg("requests"."profile_lines"."sample_time") as "avg",
  count(requests.profile.id) as "count"
from "requests"."profile_lines"
join "requests"."profile"
  on "requests"."profile"."id" = "requests"."profile_lines"."profile_id"
where "requests"."profile_lines"."file_name" = 'playground_application/controllers/default_controller.py'
         and "requests"."profile"."application_name" = 'playground_application'
--          and "requests"."profile"."version" = '8cdae6d945d82af46a40e90e86c294c9a9b44b47'
group by "requests"."profile_lines"."file_name", "requests"."profile_lines"."line_number"


select
--   time_bucket('1 second', start_timestamp) as one_min,
  version,
  file_name,
  start_timestamp,
  "requests"."profile_lines"."line_number",
  "requests"."profile_lines"."sample_time"
from "requests"."profile_lines"
     join "requests"."profile"
     on "requests"."profile"."start_timestamp" = "requests"."profile_lines"."profile_start_timestamp"
-- where "requests"."profile_lines"."file_name" = 'playground_application/controllers/default_controller.py'
  and "requests"."profile"."application_name" = 'ride_service'
-- group by "requests"."profile_lines"."file_name", "requests"."profile_lines"."line_number", one_min
order by start_timestamp desc;

select
  requests.logging_lines.line_number,
  requests.logging_lines.filename,
  requests.logging_lines.level,
  requests.logging_lines.logger,
  requests.logging_lines.message
from "requests"."logging_lines"
   join "requests"."profile"
   on "requests"."profile"."start_timestamp" = "requests"."logging_lines"."profile_start_timestamp"
-- where "requests"."logging_lines"."filename" = 'playground_application/controllers/uber_controller.py'
  and "requests"."profile"."application_name" = 'uber_service';

-- Get all versions of an application and where they were released
select distinct on (version)
  requests.profile.start_timestamp
  start_timestamp,
  version
from "requests"."profile_lines"
   join "requests"."profile"
   on "requests"."profile"."start_timestamp" = "requests"."profile_lines"."profile_start_timestamp"
where "requests"."profile"."application_name" = 'ride_service'
and requests.profile.version = 'bd2e3f9b6b21bbd3a8a3f8bad7b93290878814ee'
order by version, start_timestamp ASC;

select distinct on ("requests"."profile"."version")
    "requests"."profile"."start_timestamp",
    "requests"."profile"."end_timestamp",
    "requests"."profile"."duration"
from "requests"."profile"
    left join "requests"."profile_lines" on "requests"."profile"."start_timestamp" = "requests"."profile_lines"."profile_start_timestamp"
    left join "requests"."logging_lines" on "requests"."profile"."start_timestamp" = "requests"."logging_lines"."profile_start_timestamp"
    left join "requests"."exception" on "requests"."profile"."start_timestamp" = "requests"."exception"."profile_start_timestamp"
    left join "requests"."exception_frames" on "requests"."exception"."id" = "requests"."exception_frames"."exception_id"
where ("requests"."profile"."application_name" = 'ride_service'
           and "requests"."profile"."version" = 'bd2e3f9b6b21bbd3a8a3f8bad7b93290878814ee'
           and ("requests"."profile_lines"."line_number" = 107)
                    or "requests"."logging_lines"."line_number" = 107
                    or "requests"."exception_frames"."line_number" = 107)
order by "requests"."profile"."version", start_timestamp ASC;

select "requests"."exception_frames"."line_number",
   count("requests"."exception".profile_start_timestamp) as "exception_count"
from "requests"."profile"
    join "requests".exception on profile.start_timestamp = exception.profile_start_timestamp
    join "requests".exception_frames on exception.id = exception_frames.exception_id
where ("requests"."exception_frames"."filename" = 'playground_application/controllers/uber_controller.py'
   and "requests"."profile"."application_name" = 'ride_service'
   and "requests"."profile"."version" = 'b0da819a5f3039a1631e43655413147c8238b64b')
group by "requests"."exception_frames"."filename", "requests"."exception_frames"."line_number"
