/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.env_doublep;

import org.onosproject.agent_doublep.Action;
import org.onosproject.agent_doublep.State;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.onosproject.kpath_doublep.Edge;
import org.onosproject.kpath_doublep.Graph;
import org.onosproject.kpath_doublep.KSP;
import org.onosproject.kpath_doublep.Path;
import org.onosproject.net.topology.TopologyGraph;
import org.onosproject.util_doublep.Plot;

/**
 *
 * @author marwa
 */
public class SDNRouting extends Environment {

    private KSP ksp = new KSP();;
    private Graph topology = new Graph();;
    private float[] link_weights;
    private int[][] active_links;
    private State state;
    private int num_links;

    public int count = 0;
    private int steps_beyond_done = -1;
    private Random random = new Random();
    private float reward = 0.0f;
    private FileWriter myWriter;

    public SDNRouting(int num_links,int num_nodes, TopologyGraph g, float[][] weights, float[][] links_latency, float[][] links_packet_loss_ratio, float[][] links_utilization_ratio) {
        super(4*num_nodes*num_nodes, num_links);//actionSize in continuous is inf
        //error
        random.setSeed(123);
        this.num_links = num_links; //number of links in topology
        writeToMyLog("NUM LINKS "+num_links+ " NUM NODES "+num_nodes+"\n");
        //topology setup
        writeToMyLog("Edges size "+g.getEdges().size()+"\n");
        //this.topology = ksp.setupNSFnet();
        for (org.onlab.graph.Edge edge : g.getEdges()) {
            //edge index
            String src = edge.src().toString().substring(edge.src().toString().indexOf(':') + 1);
            src = Integer.parseInt(src.substring(src.length()-2),16) + "";
            String dst = edge.dst().toString().substring(edge.dst().toString().indexOf(':') + 1);
            dst = Integer.parseInt(dst.substring(dst.length()-2) + "",16)+"";
            //writeToMyLog("SDNEnv -- Constructor: Source is " + src + " Destination is " + dst + "\n");
            //writeToMyLog("SDNEnv -- Constructor: weight is " + weights[Integer.parseInt(src)][Integer.parseInt(dst)] + "\n");//-1

            topology.addEdge(src, dst, (double)weights[Integer.parseInt(src)][Integer.parseInt(dst)]);//-1
            topology.addEdge(dst, src, (double)weights[Integer.parseInt(dst)][Integer.parseInt(src)]);//-1
        }

        List<Edge> edges = topology.getEdgeList();

        //active links setup
        this.active_links = new int [num_nodes+1][num_nodes+1];
        for (int i = 0; i < edges.size(); i++) {
            int fromNode = Integer.parseInt(edges.get(i).getFromNode());
            int toNode = Integer.parseInt(edges.get(i).getToNode());
          //  writeToMyLog(i+" Edge "+fromNode +" , "+toNode+"\n");
            active_links[fromNode][toNode] = 1;
        }
        writeToMyLog("Active Links \n ");
        for (int i = 0; i < active_links.length; i++) {
            for (int j = 0; j < active_links.length; j++) {
                writeToMyLog(active_links[i][j]+" , ");
            }
            writeToMyLog(" \n ");
        }

        //weight setup
        writeToMyLog("Links Weights \n ");
        this.link_weights = new float[num_links];//num_links
        writeToMyLog("Links Weights size "+num_links+" edges size "+edges.size()+"\n ");
        for (int i = 0; i < link_weights.length; i++) {
            link_weights[i] = (float) edges.get(i).getWeight();
            writeToMyLog(link_weights[i]+" , ");
        }
        writeToMyLog(" \n ");
        this.state = new State(num_links, num_nodes,weights,links_latency,links_packet_loss_ratio,links_utilization_ratio);//inital random state
        this.state.set_links_load2(link_weights,active_links);//changing load to match link weights
    }

    /**
     * reading new network state, currently we do it randomly
     *
     * @return
     */
    @Override
    public Observation reset(float [][] lo,float [][] la,float [][] pr,float [][] lr , double[] pwr) {
        //this.state.setRandomState();//random state
        this.state.setNetworkState(lo,la,pr,lr);//reading network state
       //error
        this.link_weights = this.state.get_links_load2(active_links,num_links);//getting the random weight
        this.count = 0;
        this.steps_beyond_done = -1;
        return new Observation(this.state, get_reward(pwr),false);//1.0f
    }

    @Override
    public Observation step(Action action,float [][] la,float [][] pr,float [][] lr, double[] pwr) {
        float[] weights = action.get_links();
        List<Edge> edges = topology.getEdgeList();
        writeToMyLog("Weights: \n");
        //weight setup
        this.link_weights = weights;
        for (int i = 0; i < link_weights.length; i++) {
            edges.get(i).setWeight(weights[i]);
            writeToMyLog(weights[i]+" , ");

        }
        this.topology = new Graph();
        this.topology.addEdges(edges);
        writeToMyLog("\n"+topology.toString());
        //error
        writeToMyLog("Set Links Load\n");
        //this.state.set_links_load2(weights,active_links);//changing load to match link weights
        this.state.set_links_load(action.get_links_weights());

        //this.state.setRandomState();//random state as if we are reading from network statistics
        this.state.setNetworkState(state.get_links_load(),la,pr,lr);//reading new network state
       // writeToMyLog("New State  "+ state.toString()+"\n");
        float reward = get_reward(pwr);
        writeToMyLog("New Reward  "+ reward+"\n");

        return new Observation(state,reward,true);//1.0f//1.0f

    }
/* //stillneed work
    public float get_reward() {
        int NumNodes = topology.getNodes().size();
        for (int src = 1; src < NumNodes + 1; src++) {
            for (int dest = 1; dest < NumNodes + 1; dest++) {
                if (src != dest) {
                    List<Path> p = ksp.ksp(topology, src + "", dest + "", 1);//k-shortest path
                    // System.out.println("k) cost: [path]");
                     //for (Path pp : p) {
                        //System.out.println(++n + ") " + pp+" Total cost "+ pp.getTotalCost());
                     //}
                }
            }
        }
    }*/


    /**
     * The total reward of the environment
     * @return
     */
    /*
    public float get_reward() {

        // Total Latency
        double cost = 0.0;
        double[] latencies_list = get_links_latency();
        for (int i = 0; i < latencies_list.length; i++) {
            cost += Math.pow(latencies_list[i], 2);
        }
        double sqrt_latency = Math.sqrt(cost / latencies_list.length);
        //Minimum Link Util

        double[] util_list = get_links_plr();
        double util_cost = util_list[0];
        for (int i = 0; i < util_list.length; i++) {
            if(util_list[i] < util_cost){
                util_cost = util_list[i];
            }
        }
        util_cost = Math.pow(util_cost, 2);
        double sqrt_util = Math.sqrt(util_cost / util_list.length);
        //Average PLR
        double plr_cost = 0.0;
        double[] plr_list = get_links_lur();
        for (int i = 0; i < plr_list.length; i++) {
            plr_cost += plr_list[i];
        }
        plr_cost = Math.pow(plr_cost/plr_list.length, 2);
        double sqrt_plr = Math.sqrt(plr_cost / plr_list.length);

        double total_r = -1*(sqrt_latency + sqrt_util + sqrt_plr);

       // writeToMyLog("Env-- get_reward: "+total_r+"\n");
        return (float)total_r;//multiply by -1 as we try to minimize  latency but Q-learning maximizes reward
    }*/

    /**
     * The total reward of the environment
     * @return
     */
    public float get_reward(double[] pwr_list) {

        // Total Latency
        double cost = 0.0;
        double[] latencies_list = get_links_latency();
       /* for (int i = 0; i < latencies_list.length; i++) {
            cost += latencies_list[i];
        }*/
       cost = normalization(latencies_list);

        double sqrt_latency = cost*0.33;
        writeToMyLog("Env-- get_reward: sqrt_latency "+sqrt_latency+"\n");
        //Minimum Link Util
        double util_cost = 0.0;
        double[] util_list = get_links_plr();
       /* double util_min = util_list[0];
        for (int i = 0; i < util_list.length; i++) {
            if(util_list[i] < util_min){
                util_min = util_list[i];
            }
        }*/
        util_cost = normalization(util_list);
        double sqrt_util = util_cost*0.33;
        writeToMyLog("Env-- get_reward: before if sqrt_util "+sqrt_util+"\n");
        Double d = sqrt_util;
        if(d.isNaN()){
            sqrt_util = 0.0;
        }
        writeToMyLog("Env-- get_reward: after if sqrt_util "+sqrt_util+"\n");
        //power
        double pwr_cost = normalization(pwr_list)*0.33;
        Double d2 = pwr_cost;
        if(d2.isNaN()){
            pwr_cost = 0.0;
        }
        writeToMyLog("Env-- get_reward: after if pwr_cost "+pwr_cost+"\n");
        //Average PLR
        double plr_cost = 0.0;
        double[] plr_list = get_links_lur();
        /*for (int i = 0; i < plr_list.length; i++) {
            plr_cost += plr_list[i];
        }*/
        plr_cost = normalization(plr_list);
        double sqrt_plr = plr_cost*0.33 ;
        writeToMyLog("Env-- get_reward: plr_cost "+plr_cost+"\n");
       // double total_r = -1*(sqrt_latency + sqrt_util + sqrt_plr);
        double total_r = -1*(sqrt_latency + pwr_cost + sqrt_plr);

        writeToMyLog("Env-- get_reward: "+total_r+"\n");
        return (float)total_r;//multiply by -1 as we try to minimize  latency but Q-learning maximizes reward
    }

    private double[] min_max(double[] arr){
        double[] min_max = new double[]{arr[0],arr[0]};
        for(int i=0;i<arr.length;i++){
            if(arr[i]<min_max[0]){
                min_max[0]=arr[i];
            }
            if(arr[i]>min_max[1]){
                min_max[1]=arr[i];
            }
        }
        return min_max;
    }

    private double normalization(double[] arr){

        double[] min_max = min_max(arr);
        double min = min_max[0], max = min_max[1],cost =0.0;
        for (int i = 0; i < arr.length; i++) {
            cost += (arr[i] - min)/(max-min);
        }
        return cost;
    }

    
    public double[] get_links_latency() {
        int s = 0;
        float[][] latencies = state.get_links_latency();
        double[] lat = new double[num_links*2];//1-2-4
        for (int i = 0; i < active_links.length; i++) {
            for (int j = 0; j < active_links.length; j++) {

                if(active_links[i][j]== 1) {
                    lat[s] = latencies[i][j];
                    s++;
                }
            }
            }
        return lat;
    }
    public double[] get_links_plr() {
        int s = 0;
        float[][] packetLoss = state.get_links_plr();
        double[] plr = new double[num_links*2];//1-2-4
        for (int i = 0; i < active_links.length; i++) {
            for (int j = 0; j < active_links.length; j++) {

                if(active_links[i][j]== 1) {
                    plr[s] = packetLoss[i][j];
                    s++;
                }
            }
        }
        return plr;
    }
    public double[] get_links_lur() {
        int s = 0;
        float[][] utilization = state.get_links_lur();
        double[] lur = new double[num_links*2];//1-2-4
        for (int i = 0; i < active_links.length; i++) {
            for (int j = 0; j < active_links.length; j++) {

                if(active_links[i][j]== 1) {
                    lur[s] = utilization[i][j];
                    s++;
                }
            }
        }
        return lur;
    }


    @Override
    public Observation step(int action) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[][] get_active_links(){
        return active_links;
    }
    public int get_num_links(){
        return num_links;
    }
    public State getState(){return state;}

    /**
     * to print log message in log txt file (me)
     *
     * @param str
     */
    private void writeToMyLog(String str) {
        try {
            myWriter = new FileWriter("/home/morakan/MK_Files/DPLogs/mySDNEnvlog.txt ", true);
            myWriter.write(str);
            myWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
    /**
     * Used to plot training data graph
     * @param num_epd
     * @param avg_rewards
     */
    public void plotTrainingData(int num_epd,int window_size , double[] avg_rewards){
        double[][] dataPoints = new double[num_epd/window_size][2];
        for(int i = 0 ; i< dataPoints.length ; i++){
            dataPoints[i] = new double[]{(i+1.0)*10.0,avg_rewards[i]};
        }
        // Plot.scatter(dataPoints, "Average reward VS Epochs");
        Plot.line(dataPoints, "Average reward VS Epochs","Epochs","Avg. Reward");

    }

    /**
     * Used to plot training data graph
     * @param avg_rewards
     */
    public void plotTrainingData2( ArrayList<Double> avg_rewards){
        double[][] dataPoints = new double[avg_rewards.size()][2];
        for(int i = 0 ; i< dataPoints.length ; i++){
            dataPoints[i] = new double[]{(i+1.0)*100.0,avg_rewards.get(i)};
        }
        // Plot.scatter(dataPoints, "Average reward VS Epochs");
        Plot.line(dataPoints, "Average reward VS Epochs","Epochs","Rewards");

    }

}
