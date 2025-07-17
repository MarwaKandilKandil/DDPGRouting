package org.onosproject.DoublePDDPGRouting;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLGenerator {
    String[] nodeIds;
    String[] coordinatesX;
    String[] coordinatesY;

    String[] linkIds;
    String[] linkSources;
    String[] linkTargets;

    String[] demandIds ;
    String[] demandSources ;
    String[] demandTargets ;
    String[] demandValues ;
    String demand_values = "20.0";
    Map<String, Integer> nodeDictionary = new HashMap<>();
    FileWriter myWriter;
    //read the toplogy using the methods
    //apply the links on network and check the results
    //add flows and run
    //read results for ablien
    //read results for nsfnet
    //read results for arpnet
    public XMLGenerator() {
        try {
            generateXml();
            writeToMyLog("XML file generated successfully.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateXml() throws IOException {
        StringBuilder xmlContent = new StringBuilder();
        xmlContent.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
        xmlContent.append("<network xmlns=\"http://sndlib.zib.de/network\" version=\"1.0\">\n");

        writeMeta(xmlContent);
        setup_ablien();
        writeNetworkStructure(xmlContent);

        xmlContent.append("</network>");

        try (FileWriter fileWriter = new FileWriter("/home/morakan/eclipse-workspace/EclipseJavaTransportExamples/network.xml")) {
            fileWriter.write(xmlContent.toString());
        }
    }

    public void writeMeta(StringBuilder xmlContent) {
        xmlContent.append(" <meta>\n");
        xmlContent.append("  <granularity>6month</granularity>\n");
        xmlContent.append("  <time>2004</time>\n");
        xmlContent.append("  <unit>MBITPERSEC</unit>\n");
        xmlContent.append("  <origin>Yin Zhang, http://userweb.cs.utexas.edu/~yzhang/research/AbileneTM, scaled directed peak matrix, period:6 month, total demand 3Tbps</origin>\n");
        xmlContent.append(" </meta>\n");
    }

    public void writeNetworkStructure(StringBuilder xmlContent) {
        xmlContent.append(" <networkStructure>\n");
        writeNodes(xmlContent);
        writeLinks(xmlContent);
        writeDemands(xmlContent);
        xmlContent.append(" </networkStructure>\n");
    }

    public void writeNodes(StringBuilder xmlContent) {
        xmlContent.append("  <nodes coordinatesType=\"geographical\">\n");
        /*
        String[] nodeIds = {"ATLAM5", "ATLAng", "CHINng", "DNVRng", "HSTNng", "IPLSng", "KSCYng", "LOSAng", "NYCMng", "SNVAng", "STTLng", "WASHng"};
        String[] coordinatesX = {"-84.3833", "-85.5", "-87.6167", "-105.0", "-95.517364", "-86.159535", "-96.596704", "-118.25", "-73.9667", "-122.02553", "-122.3", "-77.026842"};
        String[] coordinatesY = {"33.75", "34.5", "41.8333", "40.75", "29.770031", "39.780622", "38.961694", "34.05", "40.7833", "37.38575", "47.6", "38.897303"};
        */
        for (int i = 0; i < nodeIds.length; i++) {
            xmlContent.append("   <node id=\"").append(nodeIds[i]).append("\">\n");
            xmlContent.append("    <coordinates>\n");
            xmlContent.append("     <x>").append(coordinatesX[i]).append("</x>\n");
            xmlContent.append("     <y>").append(coordinatesY[i]).append("</y>\n");
            xmlContent.append("    </coordinates>\n");
            xmlContent.append("   </node>\n");
        }

        xmlContent.append("  </nodes>\n");
    }

    public void writeLinks(StringBuilder xmlContent) {
        xmlContent.append("  <links>\n");
        /*
        String[] linkIds = {"ATLAM5_ATLAng", "ATLAng_HSTNng", "ATLAng_IPLSng", "ATLAng_WASHng", "CHINng_IPLSng", "CHINng_NYCMng", "DNVRng_KSCYng", "DNVRng_SNVAng", "DNVRng_STTLng", "HSTNng_KSCYng", "HSTNng_LOSAng", "IPLSng_KSCYng", "LOSAng_SNVAng", "NYCMng_WASHng", "SNVAng_STTLng"};
        String[] linkSources = {"ATLAng", "HSTNng", "IPLSng", "WASHng", "IPLSng", "NYCMng", "KSCYng", "SNVAng", "STTLng", "KSCYng", "LOSAng", "IPLSng", "SNVAng", "WASHng", "STTLng"};
        String[] linkTargets = {"ATLAM5", "ATLAng", "ATLAng", "ATLAng", "CHINng", "CHINng", "DNVRng", "DNVRng", "DNVRng", "HSTNng", "LOSAng", "KSCYng", "SNVAng", "NYCMng", "SNVAng"};
        */
        for (int i = 0; i < linkIds.length; i++) {
            xmlContent.append("   <link id=\"").append(linkIds[i]).append("\">\n");
            xmlContent.append("    <source>").append(linkSources[i]).append("</source>\n");
            xmlContent.append("    <target>").append(linkTargets[i]).append("</target>\n");
            xmlContent.append("    <preInstalledModule>\n");
           // xmlContent.append("     <capacity>9920.0</capacity>\n");
            xmlContent.append("     <capacity>").append(9920.0).append("</capacity>\n");//link capacity of10MB
            xmlContent.append("     <cost>0.0</cost>\n");
            xmlContent.append("    </preInstalledModule>\n");
            xmlContent.append("    <additionalModules>\n");
            xmlContent.append("     <addModule>\n");
           // xmlContent.append("      <capacity>40000.0</capacity>\n");
            xmlContent.append("     <capacity>").append(40000.0).append("</capacity>\n");//link load of 50%
            xmlContent.append("      <cost>").append((i + 1) * 100.0).append("</cost>\n");
            xmlContent.append("     </addModule>\n");
            xmlContent.append("    </additionalModules>\n");
            xmlContent.append("   </link>\n");
        }

        xmlContent.append("  </links>\n");
    }
//Demands=flows
    public void writeDemands(StringBuilder xmlContent) {
        xmlContent.append("  <demands>\n");

       /* String[] demandIds = {"IPLSng_STTLng", "CHINng_ATLAM5", "HSTNng_STTLng", "LOSAng_KSCYng", "LOSAng_NYCMng", "HSTNng_LOSAng", "IPLSng_CHINng", "LOSAng_DNVRng", "LOSAng_SNVAng", "DNVRng_ATLAM5"};
        String[] demandSources = {"IPLSng", "CHINng", "HSTNng", "LOSAng", "LOSAng", "HSTNng", "IPLSng", "LOSAng", "LOSAng", "DNVRng"};
        String[] demandTargets = {"STTLng", "ATLAM5", "STTLng", "KSCYng", "NYCMng", "LOSAng", "CHINng", "DNVRng", "SNVAng", "ATLAM5"};
        String[] demandValues = {"5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0"};
        */
        for (int i = 0; i < demandIds.length; i++) {
            xmlContent.append("   <demand id=\"").append(demandIds[i]).append("\">\n");
            xmlContent.append("    <source>").append(demandSources[i]).append("</source>\n");
            xmlContent.append("    <target>").append(demandTargets[i]).append("</target>\n");
            xmlContent.append("    <demandValue>").append(demand_values+"").append("</demandValue>\n"); //demandValues[i]
            xmlContent.append("   </demand>\n");
        }

        xmlContent.append("  </demands>\n");
    }
    public void setup_arpnet(){
        //nodes
        nodeIds = new String[]{"n1", "n2", "n3", "n4", "n5",
                                "n6", "n7", "n8", "n9", "n10",
                                "n11", "n12", "n13", "n14", "n15",
                                "n16", "n17", "n18", "n19", "n20"};
        coordinatesX = new String[]{"-84.3833", "-85.5", "-87.6167", "-105.0", "-95.517364",
                                    "-86.159535", "-96.596704", "-118.25", "-73.9667", "-122.02553",
                                    "-122.3", "-40.026842", "17.026842", "-7.026842", "-27.026842",
                                    "-17.026842", "-57.026842", "-67.026842", "-72.026842", "-7.026842"};
        coordinatesY = new String[]{"33.75", "34.5", "41.8333", "40.75", "29.770031",
                                    "39.780622", "38.961694", "34.05", "40.7833", "37.38575",
                                    "47.6", "38.897303", "38.897303", "20.897303", "18.897303",
                                    "18.897303", "58.897303", "28.897303", "40.897303", "3.897303"};
        //links
        linkIds = new String[]{"n1_n2", "n1_n3", "n2_n4", "n3_n7", "n3_n8",
                                "n4_n5", "n4_n11", "n2_n5", "n1_n6", "n6_n7",
                                "n5_n6", "n8_n9", "n7_n10", "n9_n10", "n8_n10",
                                "n11_n13", "n13_n17", "n9_n11", "n17_n20", "n19_n20",
                                "n17_n19", "n10_n19", "n20_n14", "n14_n15", "n15_n16",
                                "n16_n18", "n14_n18", "n14_n12", "n18_n12", "n19_n16",
                                "n8_n12", "n13_n15"};

        // Separate demand sources and targets
        linkSources = new String[linkIds.length];
        linkTargets = new String[linkIds.length];

        for (int i = 0; i < linkIds.length; i++) {
            String[] nodes = linkIds[i].split("_");
            linkSources[i] = nodes[0];
            linkTargets[i] = nodes[1];
        }

        // number of flows and link loads of 50%
        demandIds = new String[]{"n1_n20", "n19_n20", "n2_n5", "n4_n5", "n12_n18",
                                "n6_n10", "n7_n10", "n17_n15", "n4_n11", "n19_n3",
                                "n3_n5", "n3_n13", "n16_n4", "n6_n16", "n6_n18",
                                "n13_n19", "n18_n20", "n10_n11", "n15_n20", "n11_n1"};
        // Separate demand sources and targets
        demandSources = new String[demandIds.length];
        demandTargets = new String[demandIds.length];

        for (int i = 0; i < demandIds.length; i++) {
            String[] nodes = demandIds[i].split("_");
            demandSources[i] = nodes[0];
            demandTargets[i] = nodes[1];
        }
        demandValues = new String[]{"5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0"};
        writeToMyLog("Building node dictionary\n");

        for(int i=1;i<=nodeIds.length;i++){
            writeToMyLog(nodeIds[i-1]+" - ");
            nodeDictionary.put(nodeIds[i-1], i);
        }
        writeToMyLog("\n Done with nodes dictionary\n");
    }

    public void setup_nsfnet(){
        //nodes
        nodeIds = new String[]{"n1", "n2", "n3", "n4", "n5", "n6", "n7",
                                "n8", "n9", "n10", "n11", "n12", "n13", "n14"};
        coordinatesX = new String[]{"-84.3833", "-85.5", "-87.6167", "-105.0",
                                    "-95.517364", "-86.159535", "-96.596704", "-118.25", "-73.9667",
                                    "-122.02553", "-122.3", "-40.026842", "17.026842", "-7.026842"};
        coordinatesY = new String[]{"33.75", "34.5", "41.8333", "40.75", "29.770031", "39.780622", "38.961694", "34.05", "40.7833", "37.38575", "47.6", "38.897303", "38.897303", "20.897303"};
        //links
        linkIds = new String[]{"n1_n2", "n1_n8", "n1_n3", "n2_n3", "n2_n4", "n3_n6", "n4_n5", "n4_n11", "n5_n6", "n5_n7",
                "n7_n8", "n7_n10", "n8_n9", "n9_n12", "n9_n13", "n10_n9", "n11_n12", "n11_n13", "n14_n13",
                "n12_n14", "n6_n14", "n6_n10"};
        /*linkSources = new String[]{"n1", "n1", "n1", "n2", "n2", "n3", "n4", "n4", "n5", "n5",
                                    "n7", "n7", "n8", "n9", "n9", "n10", "n11", "n11",
                                    "n14", "n12", "n6", "n6"};

        linkTargets = new String[]{"n2", "n8", "n3", "n3", "n4", "n6", "n5", "n11", "n6", "n7",
                                    "n8", "n10", "n9", "n12", "n13", "n9", "n12", "n13", "n13",
                                    "n14", "n14", "n10"};*/
        // Separate demand sources and targets
        linkSources = new String[linkIds.length];
        linkTargets = new String[linkIds.length];

        for (int i = 0; i < linkIds.length; i++) {
            String[] nodes = linkIds[i].split("_");
            linkSources[i] = nodes[0];
            linkTargets[i] = nodes[1];
        }

        // number of flows and link loads of 50%
        demandIds = new String[]{"n1_n14", "n13_n14", "n2_n5", "n4_n5", "n8_n14", "n6_n10", "n7_n10", "n11_n9", "n4_n11",
                                "n9_n3", "n3_n5", "n3_n13", "n6_n4", "n6_n12", "n6_n8", "n13_n3", "n8_n2", "n10_n11", "n14_n10",
                                 "n11_n1"};
        // Separate demand sources and targets
        demandSources = new String[demandIds.length];
        demandTargets = new String[demandIds.length];

        for (int i = 0; i < demandIds.length; i++) {
            String[] nodes = demandIds[i].split("_");
            demandSources[i] = nodes[0];
            demandTargets[i] = nodes[1];
        }
        demandValues = new String[]{"5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0"};
        writeToMyLog("Building node dictionary\n");

        for(int i=1;i<=nodeIds.length;i++){
            writeToMyLog(nodeIds[i-1]+" - ");
            nodeDictionary.put(nodeIds[i-1], i);
        }
        writeToMyLog("\n Done with nodes dictionary\n");
    }

    /**
     * Adding the nodes names, coordinates, links, and the node dictionary
     * In the routing app, check if 7 nodes,then setup the xml file
     * the dictionary will help with the installation of paths
     */
    public void setup_ablien(){
        //nodes
        nodeIds = new String[]{"ATLAM5", "ATLAng", "CHINng", "DNVRng", "HSTNng", "IPLSng", "KSCYng", "LOSAng", "NYCMng", "SNVAng", "STTLng", "WASHng"};
        coordinatesX = new String[]{"-84.3833", "-85.5", "-87.6167", "-105.0", "-95.517364", "-86.159535", "-96.596704", "-118.25", "-73.9667", "-122.02553", "-122.3", "-77.026842"};
        coordinatesY = new String[]{"33.75", "34.5", "41.8333", "40.75", "29.770031", "39.780622", "38.961694", "34.05", "40.7833", "37.38575", "47.6", "38.897303"};
        //links
        linkIds = new String[]{"ATLAM5_ATLAng", "ATLAng_HSTNng", "ATLAng_IPLSng", "ATLAng_WASHng", "CHINng_IPLSng", "CHINng_NYCMng", "DNVRng_KSCYng", "DNVRng_SNVAng", "DNVRng_STTLng", "HSTNng_KSCYng", "HSTNng_LOSAng", "IPLSng_KSCYng", "LOSAng_SNVAng", "NYCMng_WASHng", "SNVAng_STTLng"};
        linkSources = new String[]{"ATLAng", "HSTNng", "IPLSng", "WASHng", "IPLSng", "NYCMng", "KSCYng", "SNVAng", "STTLng", "KSCYng", "LOSAng", "IPLSng", "SNVAng", "WASHng", "STTLng"};
        linkTargets = new String[]{"ATLAM5", "ATLAng", "ATLAng", "ATLAng", "CHINng", "CHINng", "DNVRng", "DNVRng", "DNVRng", "HSTNng", "LOSAng", "KSCYng", "SNVAng", "NYCMng", "SNVAng"};

        // number of flows and link loads of 50%
        demandIds = new String[]{"IPLSng_STTLng", "CHINng_ATLAM5", "HSTNng_STTLng", "LOSAng_KSCYng", "LOSAng_NYCMng",
                                "HSTNng_LOSAng", "IPLSng_CHINng", "LOSAng_DNVRng", "LOSAng_SNVAng", "DNVRng_ATLAM5",
                                 "HSTNng_ATLAM5","ATLAM5_KSCYng", "CHINng_DNVRng", "KSCYng_NYCMng", "HSTNng_IPLSng",
                                "DNVRng_IPLSng", "WASHng_NYCMng", "KSCYng_WASHng", "SNVAng_NYCMng", "WASHng_DNVRng"};
        //demandSources = new String[]{"IPLSng", "CHINng", "HSTNng", "LOSAng", "LOSAng", "HSTNng", "IPLSng", "LOSAng", "LOSAng", "DNVRng"};
        //demandTargets = new String[]{"STTLng", "ATLAM5", "STTLng", "KSCYng", "NYCMng", "LOSAng", "CHINng", "DNVRng", "SNVAng", "ATLAM5"};
        // Separate demand sources and targets
        demandSources = new String[demandIds.length];
        demandTargets = new String[demandIds.length];

        for (int i = 0; i < demandIds.length; i++) {
            String[] nodes = demandIds[i].split("_");
            demandSources[i] = nodes[0];
            demandTargets[i] = nodes[1];
        }

        demandValues = new String[]{"45.0", "57.0", "47.0", "27.0", "110.0",
                                    "111.0", "72.0", "89.0", "122.0", "136.0",
                                    "80.0", "63.0", "31.0", "38.0", "98.0",
                                    "57.0", "114.0", "45.0", "54.0", "126.0"};
        writeToMyLog("Building node dictionary\n");

        for(int i=1;i<=nodeIds.length;i++){
            writeToMyLog(nodeIds[i-1]+" - ");
            nodeDictionary.put(nodeIds[i-1], i);
        }
        writeToMyLog("\n Done with nodes dictionary\n");
    }

    public List<int[]> read_gams_output(){
        //gams output
        String filePath = "/home/morakan/MK_Files/DPLogs/network_paths.txt";
        // List to store arrays of nodes
        List<String[]> nodesList_names = new ArrayList<>();
        List<int[]> nodesList = new ArrayList<>();
        try {
            // Read the file using BufferedReader
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            // Read each line from the file
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line by "-"
                String[] nodes = line.split("-");
                //saving the corresponding nodes number
                int[] nodes_num= new int[nodes.length];
                writeToMyLog("\n");
                for(int s=0;s<nodes.length;s++){
                    writeToMyLog("node string "+ s+ " "+nodes[s]+"\n");
                    nodes_num[s]=nodeDictionary.get(nodes[s]);
                    writeToMyLog("node num "+ s+ " "+nodeDictionary.get(nodes[s])+"\n");
                }

                // Add the array of nodes to the list
                nodesList_names.add(nodes);
                nodesList.add(nodes_num);
            }

            // Close the reader
            reader.close();

            // Print the content of the list
            for (String[] nodes : nodesList_names) {
                writeToMyLog("Nodes: " + String.join(", ", nodes)+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodesList;
    }
    public void setup_ablien2(int[][] topo_links){
        nodeIds = new String[7];//{"ATLAM5", "ATLAng", "CHINng", "DNVRng", "HSTNng", "IPLSng", "KSCYng", "LOSAng", "NYCMng", "SNVAng", "STTLng", "WASHng"};


        coordinatesX = new String[]{"-84.3833", "-85.5", "-87.6167", "-105.0", "-95.517364", "-86.159535", "-96.596704", "-118.25", "-73.9667", "-122.02553", "-122.3", "-77.026842"};
        coordinatesY = new String[]{"33.75", "34.5", "41.8333", "40.75", "29.770031", "39.780622", "38.961694", "34.05", "40.7833", "37.38575", "47.6", "38.897303"};

        demandIds = new String[topo_links.length];//{"IPLSng_STTLng", "CHINng_ATLAM5", "HSTNng_STTLng", "LOSAng_KSCYng", "LOSAng_NYCMng", "HSTNng_LOSAng", "IPLSng_CHINng", "LOSAng_DNVRng", "LOSAng_SNVAng", "DNVRng_ATLAM5"};
        demandSources = new String[topo_links.length];//{"IPLSng", "CHINng", "HSTNng", "LOSAng", "LOSAng", "HSTNng", "IPLSng", "LOSAng", "LOSAng", "DNVRng"};
        demandTargets = new String[]{"STTLng", "ATLAM5", "STTLng", "KSCYng", "NYCMng", "LOSAng", "CHINng", "DNVRng", "SNVAng", "ATLAM5"};
        for (int i = 0; i < topo_links.length; i++) {
            int src = topo_links[i][0];
            int dst = topo_links[i][1];
        }
        demandValues = new String[]{"5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0", "5.0"};

    }
    //writeToMyLog : to print log message in log txt file (me)
    private void writeToMyLog(String str) {
        try {
            myWriter = new FileWriter("/home/morakan/MK_Files/DPLogs/myXMLlog.txt ", true);
            myWriter.write(str);
            myWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}
