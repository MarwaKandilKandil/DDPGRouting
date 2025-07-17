/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.agent_doublep;

/**
 *
 * @author marwa
 */
public class Experiance {
     private State state;
    private Action action; //DDPG must have continous action space
    private float Reward;
    private State nextState;
    //private int NextActionMask[] ;
    private boolean done;

    // Initialize Replay memory
    public  Experiance(State state , Action action , float reward , State nextState , boolean done){
        this.state = state;
        this.action = action;
        Reward = reward;
        this.nextState = nextState;
        //NextActionMask = nextActionMask ;
        this.done = done;
    }

    public Action getAction(){
        return this.action;
    }
    public float getReward(){
        return Reward;
    }
    public State getState(){
        return state;
    }
    public State getNextState(){
        return nextState;
    }
    /*public int[] getNextActionMask(){
        return NextActionMask;
    }*/
    public void setAction(Action action){
        this.action = action;
    }
    public void setReward(float reward){
        Reward = reward;
    }
    public void setState(State state){
        this.state=state;
    }
    public void setNextState(State nextState){
        this.nextState = nextState;
    }
    /*public void setNextActionMask(int[]  mask){
        NextActionMask = mask;
    }*/
    public boolean getDone(){
        return done;
    }
    public void setDone(boolean done){
        this.done= done;
    }
    public String toString() {
        String str = " State "+ state.toString()+"\n Action "+ this.action.toString()+
                "\n Reward "+ Reward+"\n Next_State "+ nextState.toString();
        return str;
    }
}
