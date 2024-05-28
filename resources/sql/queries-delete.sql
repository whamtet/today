-- :name delete-question :execute
delete from question where question_id = :question_id;

-- :name delete-section :execute
delete from section where section_id = :section_id;

-- :name delete-file :returning-execute
delete from file where file_id = :file_id
returning dir;
