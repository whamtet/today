create table section
(
    section_id    integer primary key asc,
    project_id    integer,
    section       text,
    foreign key(project_id) references project(project_id)
);
--;;
create table question
(
    question_id   integer primary key asc,
    project_id    integer,
    section_id    integer,
    question      text,
    editor        text,
    foreign key(project_id) references project(project_id),
    foreign key(section_id) references section(section_id)
);
