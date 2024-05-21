--;;
create table file
(
    file_id   integer primary key asc,
    project_id    integer,
    dir       text,
    ind     integer,
    filename_original text,
    pages     integer,
    pages_old integer,
    foreign key(project_id) references project(project_id)
);
