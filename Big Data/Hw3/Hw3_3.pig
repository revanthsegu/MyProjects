A = load '/HW3/join/input/NASA_HTTP.txt' USING PigStorage('\t');          
B = load '/HW3/join/input/HOST_COUNTRY.txt' USING PigStorage('\t'); 
D = COGROUP A BY $0,B by $0;
store D into '/home/rxs115330/output33';