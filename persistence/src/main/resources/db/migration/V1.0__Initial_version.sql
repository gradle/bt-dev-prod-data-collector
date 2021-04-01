create table build
(
    build_id varchar(255) not null
        constraint build_pk
            primary key,
    root_project varchar(255),
    path_to_first_test_task varchar(255),
    time_to_first_test_task bigint,
    build_start timestamp with time zone,
    build_finish timestamp with time zone,
    username varchar(255),
    host varchar(255)
);

create table tags
(
    build_id varchar(255) not null
        constraint tags_build_fk
            references build,
    tag_name varchar(255) not null,
    constraint tags_pk
        primary key (build_id, tag_name)
);

