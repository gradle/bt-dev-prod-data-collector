-- The success rate of recent 10 pre tested commit builds, on same base branch
alter table teamcity_build
    add pre_tested_commit_success_rate double precision default null
