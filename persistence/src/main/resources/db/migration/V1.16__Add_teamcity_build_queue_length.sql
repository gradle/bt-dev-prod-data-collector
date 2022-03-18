create table teamcity_build_queue_length
(
    id     int                      not null primary key,
    time   timestamp with time zone not null,
    length int                      not null
);

create index teamcity_build_queue_length_time on teamcity_build_queue_length (time);
