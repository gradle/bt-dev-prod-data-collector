alter table build
    add successful boolean;

update build
   set successful = true;

alter table build alter column successful set not null;
