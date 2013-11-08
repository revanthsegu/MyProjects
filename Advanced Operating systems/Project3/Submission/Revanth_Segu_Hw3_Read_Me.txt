
CS6378 - Advanced Operating System
Homework/Assignment# 3

The program expects three input parameters as command line arguments. First argument is path of commands file which has the commands to be executed (The file which has instructions to be done). Second parameter is network configuration file which has the ips of the nodes in the network and the port numbers to be used. Third parameter is the process id. The program execution will not be started until all the connections are established.

An example of configuration file(for 5 systems) is given below:

5
net37.utdallas.edu
net38.utdallas.edu
net39.utdallas.edu
net40.utdallas.edu
net41.utdallas.edu

8000
8001
8002
8003
8004
8005
8006
8007
8008
8009



The current process will connect to the processes connect using ports shown above. In the above example five processes are given. The first process with 0 process id hosts server to remaining four using first four ports (8000,8001,8002,8003). The second process with process id 1 will connect to process 0 as client on 8000 and hosts server for next three processes that are being added into network in next three ports (8004,8005,8006). Similarly all process will connect and establish network connections.


Process id starts from 0 and runs through n-1.The program expects the processes to added into network in order 0,1,2 ... n-1.



The program assumes nodes in system first 0 to NS process as server nodes last NC processess are treated as clients.



The program can be executed by the following commands

1.	javac ReplicaConsistency.java
2.	java ReplicaConsistency <input file> <config file> <process id>

at process 0 the command is
java ReplicaConsistency input.txt config.txt 0

at process i the command is
java ReplicaConsistency input.txt config.txt i


	The output messages are shown on the console.
	

Q4. Yes, All replicas of the object go through exactly the same sequence of updates.
Q5. As Hold time increases percentage of failed requests increase.
