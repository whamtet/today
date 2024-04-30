--;;
create table question
(
    question_id   integer primary key asc,
    project_id    integer,
    question      text unique,
    foreign key(project_id) references project(project_id)
);
