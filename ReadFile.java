package node;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner; // Import the Scanner class to read text files

public class ReadFile {

  public static List<HashMap<String, List<String>>> readConfig() {

    Integer numOfNode = 0;
    Integer numOfNeighbor;
    String nodeUID;
    String hostName;
    String listeningPort;
    String neighbor;
    String smallerUID;
    String largerUID;
    
    HashMap<Integer, String> indexUIDMap = new HashMap<>();
    HashMap<String, List<String>> addressMap = new HashMap<>();
    HashMap<String, List<String>> neighborsMap = new HashMap<>();
    HashMap<String, List<String>> edgesMap = new HashMap<>();
    List<HashMap<String, List<String>>> infoMapList = new ArrayList<>();
    
    try {
      File myObj = new File("config.txt");
      Scanner myReader = new Scanner(myObj);
      Integer index = 0;
      Integer count = 0;

      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        if (data.charAt(0) == '#') {
            index = 0;
            count += 1;
        } else if (data.charAt(0) != '#' && count == 1) { // number of nodes in the system
            numOfNode = Integer.parseInt(data);
            index += 1;
        } else if (data.charAt(0) != '#' && count == 2) { // nodeUID hostName listeningPort
            nodeUID = data.split("\\s+")[0];
            hostName = data.split("\\s+")[1];
            listeningPort = data.split("\\s+")[2];
            indexUIDMap.put(index, nodeUID);
            addressMap.put(nodeUID, Arrays.asList(hostName, listeningPort));
            index += 1;
        } else if (data.charAt(0) != '#' && count == 3) { // space delimited list of neighbors for each node
            numOfNeighbor = data.split("\\s+").length;
            nodeUID = indexUIDMap.get(index);
            List<String> neighbors = new ArrayList<>();

            smallerUID = data.split("\\s+")[0].split(",")[0].replace("(", "");
            largerUID = data.split("\\s+")[0].split(",")[1].replace(")", "");;

            if (neighborsMap.get(smallerUID) == null){
                neighbors.add(largerUID);
                neighborsMap.put(smallerUID, neighbors);
            }else{
                neighborsMap.get(smallerUID).add(largerUID);
            }

            List<String> neighbors2 = new ArrayList<>();
            if (neighborsMap.get(largerUID) == null){
                neighbors2.add(smallerUID);
                neighborsMap.put(largerUID, neighbors2);
            }else{
                neighborsMap.get(largerUID).add(smallerUID);
            }

            List<String> uidList = new ArrayList<>();
            uidList.add(data.split("\\s+")[1]);
            edgesMap.put(data.split("\\s+")[0], uidList);

            index += 1;
        }
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }

    infoMapList.add(addressMap);
    infoMapList.add(neighborsMap);
    infoMapList.add(edgesMap);

    // System.out.println(numOfNode);
    // System.out.println(indexUIDMap);
    // System.out.println(addressMap);
    // System.out.println(neighborsMap);
    // System.out.println(edgesMap);
    // System.out.println(infoMapList);

    return infoMapList;
}

}