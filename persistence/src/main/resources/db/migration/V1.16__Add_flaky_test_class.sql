create table flaky_test_class
(
    build_id        varchar(255) not null,
    flaky_test_fqcn varchar(1024) not null,
    primary key (build_id, flaky_test_fqcn)
);
