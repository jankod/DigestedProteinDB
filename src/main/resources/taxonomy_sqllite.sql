CREATE TABLE taxonomy
(
    taxId       INTEGER PRIMARY KEY,
    parentTaxId INTEGER,
    name        TEXT,
    rank        TEXT, -- remove after rankId is added
    divisionId  INTEGER,
    rankId      INTEGER REFERENCES ranks (rankId)
);

CREATE TABLE ranks
(
    rankId   INTEGER PRIMARY KEY,
    rankName TEXT UNIQUE
);

SELECT DISTINCT t."rank", COUNT(t."rank")
from taxonomy t
group by t."rank";


SELECT DISTINCT t.divisionId, COUNT(t.divisionId)
from taxonomy t
group by t.divisionId;


SELECT COUNT(*)
from taxonomy t;



ALTER TABLE taxonomy
    ADD COLUMN rankId INTEGER REFERENCES ranks (rankId);

INSERT INTO ranks (rankName)
SELECT DISTINCT rank
FROM taxonomy;

UPDATE taxonomy
SET rankId = (SELECT rankId FROM ranks WHERE ranks.rankName = taxonomy.rank);

ALTER TABLE taxonomy
    DROP COLUMN rank;
