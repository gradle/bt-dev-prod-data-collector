create table teamcity_export_config
(
    project_id  varchar(1024)  not null primary key,
    latest_finished_build_timestamp timestamptz
);
