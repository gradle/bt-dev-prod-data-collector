alter table teamcity_build
    add buildscan_urls varchar(255) [],
    add composite boolean;

update teamcity_build set buildscan_urls=ARRAY[]::varchar[], composite= true;

alter table teamcity_build
    alter buildscan_urls set not null,
    alter composite set not null;
