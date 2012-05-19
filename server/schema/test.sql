insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId)
    select 37.418, -122.172, X.nodeId, null, 0, 1 from (
        select nodeId from nodes as N1 where N1.nextNode is null and N1.tourId = 1
        union
        select null from dual where not exists (
            select * from nodes as N3 where N3.nextNode is null and N3.tourId = 1
        )
    ) as X;update nodes set nextNode=LAST_INSERT_ID() where nodeId != LAST_INSERT_ID() and nextNode is null;

