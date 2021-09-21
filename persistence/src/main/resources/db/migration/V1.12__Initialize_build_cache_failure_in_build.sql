update build
set build_cache_load_failure = false,
    build_cache_store_failure = false
where build_cache_load_failure is null;

alter table build
    alter column build_cache_load_failure set not null,
    alter column build_cache_store_failure set not null;
