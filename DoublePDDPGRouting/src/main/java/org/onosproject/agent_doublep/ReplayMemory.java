/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.agent_doublep;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author marwa
 */
public class ReplayMemory {
     private List<Experiance> memory;
    //private Experiance experiance;
    private int BatchSize;
    private int bufferSize;
    private int action_size;
    private Random r;


    // Initialize Replay memory
    public  ReplayMemory( int action_size , int bufferSize , int BatchSize , Random r ){
         this.BatchSize = BatchSize;
         this.bufferSize = bufferSize;
         this.action_size = action_size;
         this.memory = new ArrayList<Experiance>();
         this.r = r;
    }

    /**
     * Add a new experience to memory
     * @param LastInput  last state
     * @param LastAction 
     * @param reward
     * @param NextInput  next state
     * @param NextActionMask
     */
    public void addReplay(State LastInput, Action LastAction, float reward , State NextInput , boolean NextActionMask){
        if( memory.size() >= bufferSize )
            memory.remove( r.nextInt(memory.size()) );

        memory.add(new Experiance(LastInput , LastAction , reward , NextInput , NextActionMask));
    }

    /**
     * Randomly sample a batch of experiences from memory.
     * @param BatchSize
     * @return
     */
    public Experiance[]  getMiniBatch(int BatchSize){
        int size = memory.size() < BatchSize ? memory.size() : BatchSize ;
        Experiance[] retVal = new Experiance[size];

        for(int i = 0 ; i < size ; i++){
            retVal[i] = memory.get(r.nextInt(memory.size()));
        }
        return retVal;

    }

    public int memory_length(){
        return memory.size();
    }

    public String printMem(){
        String mem = "Mem Size is "+ memory.size()+"\n";
        for(int i = 0 ; i < memory.size() ; i++){
            mem += memory.get(i)+" , ";
        }
        mem += "\n";
        return mem;
    }
}
