create table task_trends
(
    build_id         varchar(255) not null,
    project_id       varchar      not null,
    task_path        varchar      not null,
    build_start      timestamp with time zone    not null,
    task_duration_ms integer,
    status           varchar
);

alter table task_trends
    add constraint task_trends_pk
        primary key (build_id, task_path);



