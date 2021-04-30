create type key_value as (
                             key varchar,
                             value varchar
                         );

alter table build
    add custom_values key_value [];
