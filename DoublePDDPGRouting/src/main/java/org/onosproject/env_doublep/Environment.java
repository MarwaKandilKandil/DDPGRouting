/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.env_doublep;

import org.onosproject.agent_doublep.Action;

/**
 *
 * @author marwa
 */
public abstract  class Environment {
    private int actionSize;//number of links in topo in DDPG actions must be continuous
    private int stateSize;//number of QoS parameters
    //private double[][] state_space;
    //float action;



    public Environment( int stateSize, int actionSize) {
        this.actionSize = actionSize;
        this.stateSize = stateSize;
        //this.state_space = state_space;
    }

    /**
     * reset the environment
     * @return
     */
    public abstract Observation reset(float [][] lo,float [][] la,float [][] pr,float [][] lr,double[] pwr);

    /**
     * Used for continuous action spaces
     * @param action
     * @return
     */
    public abstract Observation step(Action action,float [][] la,float [][] pr,float [][] lr,double[] pwr);
    /**
     * Used for discrete  action spaces
     * @param action
     * @return
     */
    public abstract Observation step(int action);
    
    public int getStateSize() {
        return stateSize;
    }
    public int getActionSize() {
        return this.actionSize;
    }
    /*public double[][] getStateSpace() {
        return this.state_space;
    }*/

    /**
     * Used for discrete action spaces
     * @return
     */
    public int[] getAvailableActions() {
        int[] availableActions = new int[actionSize];
        for (int i=0;i<availableActions.length;i++) {
            availableActions[i] = 1;
        }
        return availableActions;
    }
}
