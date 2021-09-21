update build
set build_cache_load_failure = false
where build_cache_load_failure is null;

alter table build
    alter column build_cache_load_failure set not null;

update build
set build_cache_store_failure = false
where build_cache_store_failure is null;

alter table build
    alter column build_cache_store_failure set not null;
