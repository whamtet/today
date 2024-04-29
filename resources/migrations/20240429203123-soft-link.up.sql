--;;
create table soft_link
(
    file_id   integer,
    q         text,
    foreign key(file_id) references file(file_id)
);
