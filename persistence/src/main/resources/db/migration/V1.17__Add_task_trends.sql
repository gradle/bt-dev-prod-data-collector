create table task_trends
(
    build_id         varchar(255) not null,
    project_id       varchar      not null,
    task_path        varchar      not null,
    build_start      timestamp    not null,
    task_duration_ms int,
    status           varchar,
    cacheable        boolean
);


