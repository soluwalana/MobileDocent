/*insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) values
        (37.418, -122.172, 4, 5, 0, 1);
    
update nodes set nextNode=LAST_INSERT_ID() where nodeId = 4;
update nodes set prevNode=LAST_INSERT_ID() where nodeId = 5;*/

/*insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) values
     (37.418, -122.172, null, 1, 0, 1);
    
update nodes set nextNode=LAST_INSERT_ID() where nodeId = null;
update nodes set prevNode=LAST_INSERT_ID() where nodeId = 1;*/

    /* DELETE */
update nodes as N1, (select nodeId, nextNode from nodes where nodeId = 12) as N2 set N1.nextNode=N2.nextNode
    where N1.nextNode = N2.nodeId;
update nodes as N1, (select nodeId, prevNode from nodes where nodeId = 12) as N2 set N1.prevNode=N2.prevNode
    where N1.prevNode = N2.nodeId;
delete from nodes where nodeId = 12;

/* Insert code */
insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) 
     select 37.418, -122.172, 1, X.nextNode, 0, 1 from (
        select nextNode from nodes as N1 where N1.nodeId = 1
     )  as X ;
update nodes as N1, nodes as N2
    set N2.prevNode = LAST_INSERT_ID()
    where N1.nodeId != N2.nodeId and N2.nodeId = N1.nextNode and N1.nodeId = 1;
    
update nodes set nextNode = LAST_INSERT_ID()
    where nodeId = 1;
    
/* Prepend */
insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId)
    select 37.418, -122.172, null, X.nodeId, 0, 1 from (
        select nodeId from nodes as N1 where N1.prevNode is null and N1.tourId = 1
        union
        select null from dual where not exists (
           select * from nodes as N2 where N2.prevNode is null and N2.tourId = 1
        )
    ) as X;
    
update nodes set prevNode = LAST_INSERT_ID() where nodeId != LAST_INSERT_ID() and prevNode is null;
    
/*Append */
    insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) 
        select ?, ?, X.nodeId, null, ?, ? from (
        select nodeId from nodes as N1 where N1.nextNode is null and N1.tourId = 1 
        union 
        select null from dual where not exists (
        select * from nodes as N2 where N2.nextNode is null and N2.tourId = 1
        )) as X; 
        update nodes set nextNode=LAST_INSERT_ID() where nodeId != LAST_INSERT_ID() and nextNode is null;         
        select * from nodes where nodeId=LAST_INSERT_ID();       
