
A = load '/HW3/join/input/NASA_HTTP.txt' USING PigStorage('\t');          
B = load '/HW3/join/input/HOST_COUNTRY.txt' USING PigStorage('\t');       
C = load '/HW3/join/input/COUNTRY_NAME.txt' USING PigStorage('\t'); 
D = COGROUP A BY $0,B by $0;
E = FOREACH D GENERATE FLATTEN($1), FLATTEN($2),$0;
F = FOREACH E GENERATE $0,$1,$3;
G = COGROUP F by $2, C by $0;
H = FOREACH G GENERATE FLATTEN($1), FLATTEN($2),$0;
I = FOREACH H GENERATE $0 ,$1 ,$2,$4;
store I into '/home/rxs115330/output34';  