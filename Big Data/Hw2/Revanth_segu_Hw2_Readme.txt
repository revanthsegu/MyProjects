The program can be executed by following the steps given below:

Part(i)

hadoop jar Hw2.1.jar MapperSideJoin <input_path_small_relation> <input_path_big_relation> <output_path>


hadoop jar Hw2.1.jar MapperSideJoin /HW2/input/HOST_COUNTRY.txt  /HW2/input/NASA_HTTP.txt /home/rxs115330/output1


Part(ii)

hadoop jar Hw1.2.jar ReduceSideJoin <input_path> <output_path>


hadoop jar Hw2.2.jar ReduceSideJoin /HW2/input /home/rxs115330/output2
 
 (input path has NASA_HTTP.txt and HOST_COUNTRY.txt )