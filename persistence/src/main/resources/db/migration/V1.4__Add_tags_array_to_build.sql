alter table build
    add tags varchar(255) [];

update build
set tags = (select array_agg(tag_name) from tags t where t.build_id = build.build_id)
where tags is null;

drop table tags;