# CS6380_Project2

Team Member: yxc210017 Yu-Ching Chang | nxv210002 Naga Sri Harsha Vadrevu | sms170005 Sayana Sabu

Step 1:
Connect to the DC machines

Step 2:
Upload whole folder to the DC machine
scp -r local_folder_name yxc210017@dc02.utdallas.edu: remote_folder_name

Example: scp -r CS6380_Project2 yxc210017@dc02.utdallas.edu:cs6380_project

Step 3:
Go into the folder, in one of the machine, run below command to compile java files

javac -d . Node.java ReadFile.java Mesge.java Server.java SynchGHS.java

Step 4:
In each machine, run below command, input nodeUID according to the config file.

java node.Node nodeUID