create table precondition_test
(
    -- The build id, references the build table
    build_id   varchar(255)  not null,
    -- The class name where the precondition test comes from (e.g. LocalPreconditionTest, RemotePreconditionTest)
    class_name varchar(1024) not null,
    -- Comma separated list of preconditions (e.g. "precondition1,precondition2")
    preconditions text not null,
    -- True if the test was skipped, false otherwise
    skipped boolean not null,
    -- True if the test failed, false otherwise
    failed boolean not null,
    primary key (build_id, class_name, preconditions),
    foreign key (build_id) references build
)