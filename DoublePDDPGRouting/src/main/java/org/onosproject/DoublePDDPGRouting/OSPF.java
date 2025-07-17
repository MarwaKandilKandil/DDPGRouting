package org.onosproject.DoublePDDPGRouting;

import org.onlab.graph.Edge;
import org.onosproject.kpath_doublep.Graph;
import org.onosproject.kpath_doublep.KSP;
import org.onosproject.kpath_doublep.Path;
import org.onosproject.net.topology.TopologyGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OSPF{
    Graph graph;                                 //Topology graph
    KSP ksp;                                     //K-shortest path algorithm
    private FileWriter myWriter;                 //Log file
    int numEdges ;                               // number of edges
    int numNodes;                                //Number of Nodes
    int k_paths;
    boolean find_path;
    int[] myPathstr;
    int[] myRevPathstr;
    List<Path> all_paths = new ArrayList<Path>();
//This class should contain the shorest path first algorithm , it should take matrix and generate the path or multipaths
//Will be similar to Assignment10 in CPE567
    public OSPF(TopologyGraph g, double[][] weights,int k_paths) {
        graph = new Graph();
        ksp = new KSP();
        numEdges = 0;
        numNodes = 0;
        this.k_paths = k_paths;
        all_paths = new ArrayList<Path>();
        int i = 1;
        for (Edge edge : g.getEdges())
        {
            //edge index
            String src = edge.src().toString().substring(edge.src().toString().indexOf(':')  +  1);
            src = Integer.parseInt(src.substring(src.length()-2),16)+"";//src.charAt(src.length()-1)+""
            String dst = edge.dst().toString().substring(edge.dst().toString().indexOf(':')  +  1);
            dst = Integer.parseInt(dst.substring(dst.length()-2)+"",16)+"";//dst.charAt(dst.length()-1)
           // writeToMyLog (i+" OSPF -- Constructor: Source is "+ src +" Destination is "+ dst +" Weight "+ weights[Integer.parseInt(src)][Integer.parseInt(dst)]+"\n");
           // writeToMyLog ("OSPF -- Constructor: Source indx "+ Integer.parseInt(src) +" Destination indx "+ Integer.parseInt(dst) +"\n");

           // writeToMyLog ("OSPF -- Constructor: weight is "+ weights[Integer.parseInt(src)][Integer.parseInt(dst)] +"\n");//-1

            graph.addEdge(src,dst,weights[Integer.parseInt(src)][Integer.parseInt(dst)]);//-1
            graph.addEdge(dst,src,weights[Integer.parseInt(dst)][Integer.parseInt(src)]);//-1
            i+=2;
        }
        
        numNodes = g.getVertexes().size();
        numEdges = g.getEdges().size();

        writeToMyLog ("OSPF -- Constructor: Number of edges is "+ numEdges +" Number of Nodes is "+ numNodes +"\n");
        print_weights(weights);
    }

    //writeToMyLog : to print log message in log txt file (me)
    private void writeToMyLog (String str){
        try {
            myWriter = new FileWriter("/home/morakan/MK_Files/DPLogs/myOSPFlog.txt ",true);
            myWriter.write(str);
            myWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    //finds K shortest paths
    public List<Path> findPath(int src, int dst, int k_paths){
        List<Path> p = ksp.ksp(graph, src + "", dst + "", k_paths);//k-shortest path
        if(!p.isEmpty()){
            find_path = true;
        }else{
            find_path = false;
        }
        writeToMyLog ("OSPF -- findPath: find_path "+find_path+"\n");
        //printing paths (only for testing)
        writeToMyLog ("OSPF -- findPath: k) cost: [path]  P size"+p.size()+"\n");
        int n = 0;
        for (Path pp : p) {
            writeToMyLog (++n + ") " + pp+" Total cost "+ pp.getTotalCost()+" \n");
        }
        all_paths.add(p.get(k_paths-1));
        return p;
    }
    //saves nodes in path as int[]
    public void setPathStr(List<Path> paths){
    	writeToMyLog ("\n Starting with myPathstr\n");
    	List<String> nodes  = paths.get(k_paths-1).getNodes();
        myPathstr = new int[nodes.size()];
        myRevPathstr = new int[nodes.size()];
        writeToMyLog ("OSPF -- setPathStr: myPathstr size "+ myPathstr.length+"\n");
        writeToMyLog ("OSPF -- setPathStr: nodes "+ nodes.toString()+" size "+ nodes.size()+"\n");
        writeToMyLog ("OSPF -- setPathStr: path   ,   Reverse path\n");
        int j = nodes.size() - 1;
        for (int i = 0; i < nodes.size(); i++) {
            myPathstr[i] = Integer.parseInt(nodes.get(i));
            myRevPathstr[i] = Integer.parseInt(nodes.get(j));
            j--;
            writeToMyLog ("OSPF -- setPathStr: "+ myPathstr[i]+" , "+myRevPathstr[i]+"\n");
            
        }
        writeToMyLog ("\n Done with myPathstr\n");
        

    }
    //saves reverse nodes in path as int[]
    /*public void setReversePathStr(List<Path> paths){
    	writeToMyLog ("\n Starting with myRevPathstr\n");
    	List<String> nodes  = paths.get(k_paths-1).getNodes();
        myRevPathstr = new int[nodes.size()];
        
        for (int i = nodes.size(); i >= 0; i--) {
            myRevPathstr[nodes.size()-i] = Integer.parseInt(nodes.get(i));
            writeToMyLog ("OSPF -- setPathStr: Rev path "+myRevPathstr[nodes.size()-i]);
        }
	writeToMyLog ("\n");
    }*/

    public boolean getFindPath(){ return find_path;}
    public int[] getPathstr(){ return myPathstr;}
    public int[] getRevPathstr(){ return myRevPathstr;}
    public List<Path> getAllPaths(){ return all_paths;}
    public void start(int src, int dst){
        writeToMyLog("OSPF -- start: Staring to look for paths....\n");
        writeToMyLog("OSPF -- start: Staring to look for "+src+" , "+dst+"\n");
        List<Path> paths = findPath(src,dst,k_paths);
        setPathStr(paths);
        //setReversePathStr(paths);
        writeToMyLog("OSPF -- start: path is....\n");
        for (int p:myPathstr) {
            writeToMyLog(p+" -> ");
        }
        writeToMyLog(" \n");
        writeToMyLog("OSPF -- start: Reverse path is....\n");
        for (int p:myRevPathstr) {
            writeToMyLog(p+" -> ");
        }
        writeToMyLog(" \n");
    }
    private void print_weights(double[][] w)
    {
        String str="\n";
        //me
        writeToMyLog("OSPF -- Constructor: weights matrix  "+ w.length+"\n");

        for(int i=0; i<w.length; i++) {
            for (int j = 0; j < w.length; j++)
                str += String.valueOf(w[i][j])+"    ";
            str += "\n";
        }
        //me
        writeToMyLog(str+"\n");

    }
    public List<org.onosproject.kpath_doublep.Edge> getEdges(){
        return graph.getEdgeList();
    }
    public String [] getNodes(){
        Set<String> s = graph.getNodeLabels();
        String[] ts= new String[s.size()];
        s.toArray(ts);
        for(int i=0;i<ts.length;i++){
            writeToMyLog("OSPF -- getNodes: "+ ts[i]+"\n");

        }
        return ts;
    }
}
