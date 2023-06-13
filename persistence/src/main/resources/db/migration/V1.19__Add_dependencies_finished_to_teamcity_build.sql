alter table teamcity_build
    add dependency_finished timestamp with time zone default null;
