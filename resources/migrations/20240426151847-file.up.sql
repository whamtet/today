--;;
create table file
(
    file_id   integer primary key asc,
    question_id    integer,
    filename      text,
    foreign key(question_id) references question(question_id)
);
