--;;
create table fragment
(
    fragment_id    string primary key,
    file_id    integer,
    fragment       text,
    page           integer,
    foreign key(file_id) references file(file_id)
);
