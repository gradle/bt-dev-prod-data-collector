alter table precondition_test
    add task_path varchar(1024) default '';

alter table precondition_test drop constraint precondition_test_pkey;

alter table precondition_test
    add primary key (build_id, class_name, preconditions, task_path);