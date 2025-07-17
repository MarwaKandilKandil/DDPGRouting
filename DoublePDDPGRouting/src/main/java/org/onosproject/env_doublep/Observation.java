/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.env_doublep;

import org.onosproject.agent_doublep.State;

/**
 *
 * @author marwa
 */
public class Observation {
    private State state;
    private float reward;
    private boolean isFinal;


    public Observation(State state, float reward,boolean done) {
        this.state = state;
        this.reward	= reward;
        this.isFinal = done;
    }

    public State getState() {
        return state;
    }

    public float getReward() {
        return reward;
    }
    public boolean isFinal() {
        return isFinal;
    }
    

    @Override
    public String toString() {
        String str = "State "+ state.toString()+"\nReward "+ reward;
        return str;
    }
}
