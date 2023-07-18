create table precondition_test
(
    -- The build id, references the build table
    build_id      varchar(255)  not null,
    -- The class name where the precondition test comes from (e.g. LocalPreconditionTest, RemotePreconditionTest)
    class_name    varchar(1024) not null,
    -- Comma separated list of preconditions (e.g. "precondition1,precondition2")
    preconditions text[],
    -- PASSED/FAILED/SKIPPED
    outcome      varchar(255)  not null,

    -- Primary key definition
    primary key (build_id, class_name, preconditions),
    -- We declare build_id as a foreign key. With cascading enabled,
    -- it will be automatically cleaned up, when the build is deleted
    foreign key (build_id)
        references build on delete cascade,
    -- We expect that we always have at least one precondition
    constraint non_empty_array_constraint
        check (cardinality(preconditions) > 0)
)