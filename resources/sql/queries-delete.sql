-- :name delete-question :execute
delete from question where question_id = :question_id;

-- :name delete-section :execute
delete from section where section_id = :section_id;
