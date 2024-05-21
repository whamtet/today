-- Place your queries here. Docs available https://www.hugsql.org/
-- :name upsert-user :returning-execute
insert into user (email, first_name, last_name, pic)
values (:email, :given_name, :family_name, :picture)
on conflict(email)
do update
set first_name = :given_name,
last_name = :family_name,
pic = :picture
returning user_id;

-- :name get-user-by-id :query :one
select * from user where user_id = :user_id;

-- :name create-project :returning-execute
insert into project (name) values (:project-name)
returning project_id;

-- :name update-project :execute
update project set name = :name where project_id = :project_id;

-- :name get-projects :query
select * from project;

-- :name get-project :query :one
select * from project;

-- :name get-project-by-name :query :one
select project_id from project where name = :new-project-name;

-- :name get-project-by-id :query :one
select name from project where project_id = :project_id;

-- :name get-question :query :one
select * from question where question_id = :question_id;
-- :name get-questions :query
select section.*, question, question_id from section
left outer join question on section.section_id = question.section_id
where section.project_id = :project_id;
-- :name get-questions-flat :query
select * from question where project_id = :project_id;

-- :name get-sections :query
select * from section where project_id = :project_id;

-- :name insert-section :execute
insert into section(project_id, section, ordering)
select :project_id, :section, count(*) * 2 from section where project_id = :project_id;

-- :name move-section :execute
update section set ordering = 2 * :mid - ordering where ordering = :mid - 1 or ordering = :mid + 1;

-- :name update-section :execute
update section set section = :section where section_id = :section_id;

-- :name get-question-text :query :one
select * from question where project_id = :project_id and question = :question;

-- :name insert-question :execute
insert into question (project_id, section_id, question, editor) values (:project_id, :section_id, :question, :editor);

-- :name update-question :execute
update question set question = :question where question_id = :question_id;

-- :name update-editor :returning-execute
update question set editor = :editor where question_id = :question_id
returning project_id;

-- :name insert-file :execute
insert into file (project_id, dir, ind, filename_original, pages)
values (:project_id, :dir, 0, :filename_original, :pages);

-- :name update-file :execute
update file set
ind = :index, filename_original = :filename_original, pages_old = pages, pages = :pages
where file_id = :file_id;

-- :name get-files :query
select * from file where project_id = :project_id

-- :name get-file :query :one
select * from file where file_id = :file_id;
