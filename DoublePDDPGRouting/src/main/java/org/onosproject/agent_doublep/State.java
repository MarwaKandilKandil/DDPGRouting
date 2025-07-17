/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.agent_doublep;

import java.util.List;
import java.util.Random;
import org.onosproject.kpath_doublep.Path;

/**
 *
 * @author marwa
 */
public class State {

    private int num_links;
    private int num_nodes;
    private float[][][] network_state; //3D array that carries state of each link interms of load, latency, plr, lur and flow mod rate
    /** Network state
     *          1                  2                     3                  4
     * 1 [-----------------] [lo,lat,plr,lur,fr] [lo,lat,plr,lur,fr] [lo,lat,plr,lur,fr]
     * 2 [lo,lat,plr,lur,fr] [-----------------] [lo,lat,plr,lur,fr] [lo,lat,plr,lur,fr]
     * 3 [lo,lat,plr,lur,fr] [lo,lat,plr,lur,fr] [-----------------] [lo,lat,plr,lur,fr]
     * 4 [lo,lat,plr,lur,fr] [lo,lat,plr,lur,fr] [lo,lat,plr,lur,fr] [-----------------]
     */
    private Random random = new Random(123);

    public State(int num_links, int num_nodes, float[][] links_load, float[][] links_latency, float[][] links_packet_loss_ratio, float[][] links_utilization_ratio) {
        this.num_links = num_links;
        this.num_nodes = num_nodes;
        this.network_state = new float[4][num_nodes+1][num_nodes+1];
        this.network_state[0] = links_load;
        this.network_state[1] = links_latency;
        this.network_state[2] = links_packet_loss_ratio;
        this.network_state[3] = links_utilization_ratio;
        //this.network_state[4] = flow_mod_rate;
    }

    public State(int num_nodes) {
        this.num_nodes = num_nodes;
        this.network_state = new float[4][num_nodes][num_nodes];
        this.setRandomState();
    }

    public float[][] get_links_load() {
        return network_state[0];
    }
    public float[] get_links_load2(int[][] network_active_links, int num_links) {
        float[] w = new float[num_links];
        int k = 0;
        for(int i=0; i<num_nodes ; i++){
            for(int j=0; j<num_nodes ; j++){
                if(network_active_links[i][j] == 1 && k<w.length && network_state[0][i][j] != 0){
                    w[k] = network_state[0][i][j]  ;
                    //network_state[0][j][i] = w[k];
                    k++;
                }
            }

        }
        return w;
    }


    public float[][] get_links_latency() {
        return network_state[1];
    }

    public float[][] get_links_plr() {
        return network_state[2];
    }

    public float[][] get_links_lur() {
        return network_state[3];
    }
/*
    public float[][] get_links_fmr() {
        return network_state[4];
    }*/

    public void set_links_load(float[][] load) {
        network_state[0] = load;
    }

    public void set_links_load2(float[] w,int[][] network_active_links) {
        //float[][] load = network_state[0] ;
        int k = 0;
        for(int i=0; i<num_nodes+1 ; i++){
            for(int j=0; j<num_nodes+1 ; j++){
                if(network_active_links[i][j] == 1 && k<w.length && network_state[0][i][j] == 0){
                    network_state[0][i][j] = w[k];
                    network_state[0][j][i] = w[k];
                    k++;
                }
            }
        }
       // network_state[0] = load;
    }

    public void set_links_latency(float[][] latency) {
        network_state[1] = latency;
    }

    public void set_links_plr(float[][] plr) {
        network_state[2] = plr;
    }

    public void set_links_lur(float[][] lur) {
        network_state[3] = lur;
    }
/*
    public void set_links_fmr(float[][] fmr) {
        network_state[4] = fmr;
    }*/

    public float[] get_flat_state() {
        float[] flate_network_state = new float[(num_nodes+1) * (num_nodes+1) * 4];
        for (int i = 0; i < network_state.length; i++) {//walk on each row
            for (int j = 0; j < network_state[i].length; j++) {
                for (int k = 0; k < network_state[i][j].length; k++) {
//error
                    //flate_network_state[(i*network_state[i].length)+j] = network_state[i][j][k];//j * network_state[i].length) + k
                    flate_network_state[(i * network_state[i].length * network_state[i][j].length) + (j * network_state[i][j].length) + k] = network_state[i][j][k];

                }
            }
        }
        for (int i = 0; i < flate_network_state.length; i++) {
            System.out.print(flate_network_state[i] + " , ");
        }
        return flate_network_state;
    }

    /**
     * Setting a random network state //values should be changed
     */
    public void setRandomState() {
        this.setRandom(0, 100f, 2500f);//load //weight
        this.setRandom(1, 1f, 10f);//latency
        this.setRandom(2, 0.1f, 1.0f);//plr
        this.setRandom(3, 0.1f, 1.0f);//lur
        //this.setRandom(4, 1, 50);//fmr
    }

    public void setRandom(int variable, float min, float max) {

        for (int j = 0; j < network_state[variable].length; j++) {
            for (int k = 0; k < network_state[variable][j].length; k++) {
                float state_values = random.nextFloat() * min - max;
                network_state[variable][j][k] = state_values;

            }
        }

    }


    public void setNetworkState(float [][] lo,float [][] la,float [][] pr,float [][] lr) {
        this.setNetwork(0, lo);//load //weight
        this.setNetwork(1, la);//latency
        this.setNetwork(2, pr);//plr
        this.setNetwork(3, lr);//lur
        //this.setRandom(4, 1, 50);//fmr
    }

    public void setNetwork(int variable, float [][] s) {

        for (int j = 0; j < network_state[variable].length; j++) {
            for (int k = 0; k < network_state[variable][j].length; k++) {

                network_state[variable][j][k] = (float)s[j][k];

            }
        }

    }



    public String print_stat(int stat , float[][] net_stat){
        String stat_string ="\n ";
        if(stat == 0){
            stat_string += "Load\n";
        }else if(stat == 1){
            stat_string += "Latency\n";
        }else if(stat == 2){
            stat_string += "Packet Loss Ratio\n";
        }else if(stat == 3){
            stat_string += "Link Utilization Ratio\n";
        }/*else if(stat == 4){
            stat_string += "Flow Mod Rate\n";
        }*/

        for(int i = 0; i< net_stat.length ;i++){
            for(int j = 0; j< net_stat[i].length ;j++) {
                stat_string += net_stat[i][j] + " ";
            }
        }
        stat_string+="\n---------------------------------\n";
        return stat_string;
    }

    public String print_all_stat (){
        String all_stat = "";
        for(int i = 0 ;i<network_state.length;i++){
            all_stat+= print_stat(i , network_state[i]);
        }
        return all_stat;
    }
//needto print arrays

    @Override
    public String toString() {
        return "State{" + print_all_stat() + "}";
    }
}
