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
  "requests"."profile_lines"."function_name",
  avg("requests"."profile_lines"."sample_time") as "avg",
  count(requests.profile.id) as "count"
from "requests"."profile_lines"
       join "requests"."profile"
            on "requests"."profile"."id" = "requests"."profile_lines"."profile_id"
where "requests"."profile_lines"."file_name" = 'playground_application/controllers/default_controller.py'
  and "requests"."profile"."application_name" = 'playground_application'
  --          and "requests"."profile"."version" = '8cdae6d945d82af46a40e90e86c294c9a9b44b47'
group by "requests"."profile_lines"."file_name", "requests"."profile_lines"."function_name"
