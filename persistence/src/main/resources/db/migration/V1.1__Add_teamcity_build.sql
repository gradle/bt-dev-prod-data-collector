create table teamcity_build
(
    build_id varchar(255) not null
        constraint teamcity_build_pk
            primary key,
    configuration varchar(255) not null,
    queued timestamp with time zone not null,
    started timestamp with time zone,
    finished timestamp with time zone,
    state varchar(255) not null,
    status varchar(255) not null,
    status_text varchar(1024),
    branch varchar(255) not null,
    git_commit_id varchar(255) not null
);
