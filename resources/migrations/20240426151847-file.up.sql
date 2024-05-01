--;;
create table file
(
    file_id   integer primary key asc,
    project_id    integer,
    filename      text,
    foreign key(project_id) references project(project_id)
);
