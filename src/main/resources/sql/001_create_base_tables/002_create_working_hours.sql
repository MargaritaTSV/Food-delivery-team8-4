CREATE TABLE IF NOT EXISTS working_hours (
   id BIGSERIAL PRIMARY KEY,
   monday VARCHAR(50),
   tuesday VARCHAR(50),
   wednesday VARCHAR(50),
   thursday VARCHAR(50),
   friday VARCHAR(50),
   saturday VARCHAR(50),
   sunday VARCHAR(50)
);