/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.agent_doublep;

import java.util.Random;

/**
 *
 * @author marwa
 */
public class OUNoise {
    private int size; // number of links
    private double sigma;
    private double theta;
    private double mu;
    private float[] state_links;
    private float[] muu ;
    Random r;

    public OUNoise(int size, double mu, double theta, double sigma,Random r){
        this.size = size;
        this.sigma = sigma;
        this.theta = theta;
        this.muu = new float[this.size];
        for(int f = 0 ;f<muu.length ;f++){
            muu[f] = (float)(mu);
        }
        this.r = r;
        reset();
    }

    /**
     * Reset the internal state (= noise) to mean (mu).
     */
    public void reset(){
        //state_path = muu ;
         this.state_links = new float[size];//this.muu.length]
        for(int f = 0 ;f<muu.length ;f++){
            state_links[f] = muu[f];
        }
    }

    public float[] sample(){
        float[] x = new float[size];
        for(int f = 0 ;f<muu.length ;f++){
            x[f] = (float)((muu[f]-state_links[f])*theta);
        }
        float[] dx_noise = new float[size];
        for(int i=0 ; i< size ;i++){
            dx_noise[i] =(float) (r.nextFloat()*sigma)+x[i];
        }
        for(int f = 0 ;f<size ;f++){
            state_links[f] += dx_noise[f];
        }
        return state_links;
    }

    public int get_state_links_size(){
        return state_links.length;
    }
    public String toString(){
        String s="";
        for(int f=0;f<state_links.length;f++){
            s+=state_links[f]+" , ";
        }
        s+= "\n";
        return s;
    }
}
