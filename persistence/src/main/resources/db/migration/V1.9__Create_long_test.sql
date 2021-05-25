-- Represents a long running test class
create table long_test
(
    build_id varchar(255) not null
        constraint tags_build_fk
            references build,
    class_name varchar(1024) not null,
    duration_ms bigint not null,
    constraint tags_pk
        primary key (build_id, class_name)
);

