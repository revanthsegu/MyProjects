A = load '/HW3/top10/input' USING PigStorage(',');
B = group A by ($0,$1); 
C = foreach B generate $0,COUNT($1);
D = ORDER C by $1 DESC;
E = LIMIT D 10;
store E into '/home/rxs115330/output31';