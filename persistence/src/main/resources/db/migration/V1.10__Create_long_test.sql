-- Correct the primary key and foreign key name for long_test table
alter table long_test drop
    constraint tags_build_fk;
alter table long_test drop
    constraint tags_pk;
alter table long_test
    add constraint long_test_build_fk
        foreign key (build_id) references build;
alter table long_test
    add constraint long_test_pk
        primary key (build_id, class_name);
