alter table build
    add build_cache_load_failure boolean;

update build
   set build_cache_load_failure = false;

alter table build
    alter column build_cache_load_failure set not null;

alter table build
    add build_cache_store_failure boolean;

update build
   set build_cache_store_failure = false;

alter table build
    alter column build_cache_store_failure set not null;
