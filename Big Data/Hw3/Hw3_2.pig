A = load '/HW3/join/input/NASA_HTTP.txt' USING PigStorage('\t');          
B = load '/HW3/join/input/HOST_COUNTRY.txt' USING PigStorage('\t');       
C = load '/HW3/join/input/COUNTRY_NAME.txt' USING PigStorage('\t');       
D = JOIN A BY $0, B BY $0;
E = foreach D generate $0,$1,$3;
F = JOIN E BY $2, C BY $0;
G = foreach F generate $0,$1,$2,$4;
store G into '/home/rxs115330/output32'; 