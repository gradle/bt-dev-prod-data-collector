create table precondition_probing
(
    -- The set of preconditions
    preconditions  text[] not null,
    -- The name of the machine (e.g. dev100.gradle.org)
    host           varchar(255)             not null,
    -- The name of the test class (e.g. org.gradle.test.precondition.LocalPreconditionProbingTests)
    test_class     varchar(255)             not null,
    -- The name of the gradle task running the test (e.g. :precondition-tester:embeddedIntegrationTest)
    test_task      varchar(1024)            not null,

    last_executed  timestamp with time zone not null,
    last_skipped   timestamp with time zone default null,
    last_failed    timestamp with time zone default null,
    last_succeeded timestamp with time zone default null,

    constraint pk_precondition_probing
        primary key (preconditions, host, test_class, test_task)
);