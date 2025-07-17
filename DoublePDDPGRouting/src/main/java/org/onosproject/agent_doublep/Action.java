/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.agent_doublep;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.onosproject.kpath_doublep.Edge;
import org.onosproject.kpath_doublep.Path;

/**
 *
 * @author marwa
 */
public class Action {
    private State state;
    private int num_nodes;
    private int[][] network_active_links;//2d array of active links numbered from 1 to n 
    private float[] links;//1-2,3-1, 
    private float[][] weights;
    
    public Action(State state,float[] w,int[][] activelinks){
        this.num_nodes = activelinks.length-1;
        this.state = state;
        network_active_links = activelinks ;
        this.weights = new float[num_nodes+1][num_nodes+1]; //weights of each edge in the topology
        this.links= w;
        //coppying weights in a 1D float array to a 2D float array
        /**
         * w = [0.23 1.45 1.55]
         * network_active_links =>
         *      1  2  3  4
         *  --------------
         *  1 | 0  1  0  0
         *  2 | 1  0  1  0
         *  3 | 0  1  0  1
         *  4 | 0  0  1  0
         *
         * weights =>
         *      1     2     3   4
         *  ---------------------
         *  1 | 0    0.23  0    0
         *  2 | 0.23  0   1.45  0
         *  3 | 0    1.45  0   1.55
         *  4 | 0     0   1.55  0
         */
        int k = 0;
        for(int i=0; i<num_nodes+1 ; i++){
            for(int j=0; j<num_nodes+1 ; j++){
                if(network_active_links[i][j] == 1 && k<w.length && weights[i][j] == 0){
                    weights[i][j] = w[k];
                    weights[j][i] = w[k];
                    k++;
                }
            }
        }
    }
    
    public Action(State state,int num_nodes){
        this.num_nodes = num_nodes;
        this.state = state;
        this.weights = new float[num_nodes+1][num_nodes+1]; //weights of each edge in the topology
        
    }
    public Action(State state,float[][] weights,int num_links,int[][] activelinks){
        this.state = state;
        this.weights = weights; //weights of each edge in the topology
        this.num_nodes = weights.length-1;
        //setting up 1D links from 2D weights
        links= new float[num_links];
        int k = 0;
        network_active_links = activelinks ;
        for(int i=0; i<num_nodes+1 ; i++){
            for(int j=0; j<num_nodes+1 ; j++){
                if(network_active_links[i][j] == 1 && k<links.length && weights[i][j] != 0){
                    links[k] = weights[i][j]  ;
                    k++;
                }
            }
        }
    }



     public float[] get_links() {
        return links;
    }
    public float[][] get_links_weights() {
        return weights;
    }

    public void set_links_weights(float[][] w) {
        this.weights = w;
    }
    public void set_rand_const_links_weights(Random rand,int[][] activelinks,int num_links,int isRand) {
        for(int i=0; i< weights.length ; i++){
            for(int j=0; j< weights[i].length ; j++){
                if(activelinks[i][j] == 1){
                    float w;
                    if(isRand == 1){
                        w = rand.nextFloat() * 100 +1;
                    }else{
                        w = 1.0f;
                    }

                weights[i][j] = w;
                weights[j][i] = w;
                }
            }
        }
        this.num_nodes = weights.length-1;
        //setting up 1D links from 2D weights
        links= new float[num_links];
        int k = 0;
        network_active_links = activelinks ;

        for(int i=0; i<num_nodes+1 ; i++){
            for(int j=0; j<num_nodes+1 ; j++){
                if(network_active_links[i][j] == 1 && k<links.length && weights[i][j] != 0){
                    links[k] = weights[i][j]  ;
                    k++;
                }
            }
        }

    }



    public State getState() {
        return state;
    }

    public void setState(State state) {
       this.state= state;
    }

    public float clamp(float val, float min, float max) {
    return Math.max(min, Math.min(max, val));
}
    public float[][] clamp_weights(float min, float max) {
        for(int i = 0;i<weights.length;i++){
            for(int j = 0;j<weights.length;j++){
            weights[i][j] = clamp(weights[i][j],min,max);
            }
        }
    return weights;
}
    public String print_weights(){
        String weights_str = "\n";
        for(int i = 0;i<weights.length;i++) {
            for (int j = 0; j < weights.length; j++) {
                weights_str += weights[i][j] + " , ";
            }
            weights_str+="\n";
        }
        weights_str+="\n";
        return weights_str;
    }

    public String print_links(){
        String weights_str = "\n";
        for(int i = 0;i<links.length;i++) {

                weights_str += links[i] + " , ";

        }
        weights_str+="\n";
        return weights_str;
    }
//need to print array content

    @Override
    public String toString() {
        return "Action{" +" State = " + state.toString() +" \n Links ="+print_links()+" \n Links weights = " + print_weights()+ "}";
    }
}
