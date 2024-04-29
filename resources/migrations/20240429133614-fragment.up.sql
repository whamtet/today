--;;
create table fragment
(
    fragment_id    string primary key,
    question_id    integer,
    fragment       text,
    page           integer,
    foreign key(question_id) references question(question_id)
);
