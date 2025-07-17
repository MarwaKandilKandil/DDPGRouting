package org.onosproject.agent_doublep;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PriorityReplayBuffer {
    private List<Experiance> memory;
    private List<Float> priorities;   // priority for each memory element
    private List<Integer> indexes;    // To keep track of highest priority index
    private int BatchSize;            // size of each training batch
    private int bufferSize;           // maximum size of buffer
    private int action_size;
    private float alpha;              //0~1 indicating how much prioritization is used
    private float cumulative_priorities;
    private float eps = 1e-6f;
    private float max_priority;
    private float[] all_weights = new float[BatchSize];
    private Random r;
    private FileWriter myWriter;

    // Initialize Replay memory
    public  PriorityReplayBuffer( int action_size , int bufferSize , int BatchSize ,float alpha, Random r ){
        this.BatchSize = BatchSize;
        this.bufferSize = bufferSize;
        this.action_size = action_size;
        this.memory = new ArrayList<Experiance>();
        this.r = r;

        this.alpha = Math.max(0.0f,alpha);                        //alpha >= 0
        this.priorities = new ArrayList<Float>();
        this.cumulative_priorities = 0.0f;
        this.indexes = new ArrayList<Integer>();
        this.max_priority = (float) Math.pow(1.0,(double)alpha); //max priority = 1
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
        writeToMyLog ("AddReplay : memory size "+memory.size()+"\n");
        if( memory.size() <= bufferSize ) {
            memory.add(new Experiance(LastInput , LastAction , reward , NextInput , NextActionMask));
           // writeToMyLog ("AddReplay : new memory added ...."+"\n");
        }
        //writeToMyLog ("AddReplay : memory size ...."+memory.size() +"\n");
        //exclude the value that will be discarded (first element)
        if( priorities.size() >= bufferSize ) {
            cumulative_priorities -= this.priorities.get(0);
            priorities.remove(0);
            memory.remove(0);
           // memory.remove(r.nextInt(memory.size()));
        }
        if(memory.size() == BatchSize){
            print_weights();
            all_weights = new float[BatchSize];
            writeToMyLog ("AddReplay : Resetting all weights\n");
        }
        //initialy include the max priority possible at the end of the priority array
        this.priorities.add(this.max_priority); // already used alpha
        writeToMyLog ("AddReplay : priorities size "+priorities.size()+ " new priority "+this.max_priority+"\n");
        // Add to the cumulative priorities abs(td_error)
        this.cumulative_priorities += this.priorities.get(priorities.size()-1);
        writeToMyLog ("AddReplay : cumulative_priorities "+cumulative_priorities+"\n");

    }

    /**
     * Randomly sample a batch of experiences from memory.
     * @param BatchSize
     * @return
     */
    public Experiance[]  getMiniBatch(int BatchSize){
        int size = memory.size() < BatchSize ? memory.size() : BatchSize ;//current memory size
        //writeToMyLog ("getMiniBatch : size "+ size+"\n");
        //calculating probabilities
        ArrayList<Float> na_prob= new ArrayList<Float>();
        //writeToMyLog ("getMiniBatch : na_prob \n");
        if(this.cumulative_priorities!= 0){
            for(int i=0;i<memory.size();i++){
                na_prob.add((float)priorities.get(i)/cumulative_priorities);
                //writeToMyLog (" "+ na_prob.get(i)+"\n");
            }
            //writeToMyLog (" \n");
        }
        //Random indexes based on probability distribution
        ArrayList<Integer> l_indexes = randomChoice(size,na_prob);
        indexes = l_indexes;
        //Sampling random experiences based on the indexes
        Experiance[] retVal = new Experiance[size];
        for(int i = 0 ; i < l_indexes.size() ; i++){
            writeToMyLog ("getMiniBatch : memory size "+ memory.size()+" Random index "+l_indexes.get(i)+"\n");
            retVal[i] = memory.get(l_indexes.get(i));
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
    public int findCeil(ArrayList<Float> arr, float r, int l, int h)
    {
        int mid;
        while (l < h)
        {
            mid = l + ((h - l) >> 1); // Same as mid = (l+h)/2
            if(r > arr.get(mid))
                l = mid + 1;
            else
                h = mid;
        }
        return (arr.get(l) >= r) ? l : -1;
    }


    /**
     * returns a  random number according to distribution array defined by freq[]
     * @param freq probability or frequency array
     * @return
     */
    public int myRand(ArrayList<Float>  freq)//int size,
    {
        // Create and fill prefix array
        int n = freq.size(),i;
        ArrayList<Float> prefix= new ArrayList<Float>();

       // prefix.add(0,freq.get(0));
        prefix.add(freq.get(0));
        for (i = 1; i < n; ++i)
            prefix.add(prefix.get(i - 1) + freq.get(i));

        // prefix[n-1] is sum of all frequencies.
        // Generate a random number with
        // value from 0 to this sum
        float r = (float)((Math.random()*(323567)) % prefix.get(n - 1));
        int indexc = findCeil(prefix, r, 0, n - 1);

        return indexc;
    }

    public ArrayList<Integer> randomChoice(int size,ArrayList<Float>  prob){
        ArrayList<Integer> l_indexes = new ArrayList<Integer>();
        for(int i=0;i<size;i++){
            int indx = myRand(prob);//size,prob
            writeToMyLog ("randomChoice : myRand "+ indx+"\n");
            l_indexes.add(indx);
            writeToMyLog ("randomChoice : rand index "+i+" is "+ l_indexes.get(i)+"\n");
        }
        return l_indexes;
    }

    /**
     * wi= ((N x P(i)) ^ -β)/max(wi)
     * @param f_priority
     * @param current_beta
     * @param max_weight
     * @param i_n
     * @return
     */
    public float calculate_w (float f_priority, float current_beta, float max_weight,int i_n){
    float w = (i_n* (f_priority/cumulative_priorities));
    w = (float)Math.pow(w,(-1.0*current_beta))/max_weight;
    writeToMyLog ("calculate_w : w "+w+"\n");
    return w;
    }
    public float min(){
        float min = priorities.get(0);
        for(int  i=0;i<priorities.size();i++){
            if(priorities.get(i)<min){
                min = priorities.get(i);
            }
        }
        return min;
    }
    public float max(){
        float max = priorities.get(0);
        for(int  i=0;i<priorities.size();i++){
            if(priorities.get(i)>max){
                max = priorities.get(i);
            }
        }
        return max;
    }

    /**
     * Return the importance sampling  weights of the current sample based on the beta passed
     *
     * @param current_beta :float. fully compensates for the non-uniform probabilities P(i) if β = 1
     * @return
     */
    public float[] get_weights(float current_beta){
        int i_n = memory.size();
        float max_weight = (float) Math.pow(((i_n * min() / cumulative_priorities)) , (-1*current_beta));
        writeToMyLog ("get_weights : max_weight "+max_weight+"\n");
        float[] weights = new float[memory.size()];
        writeToMyLog ("get_weights : weights \n");
        for(int i=0;i<indexes.size();i++){
            int ii = indexes.get(i);
            weights[ii]=(calculate_w(priorities.get(ii),current_beta,max_weight,i_n));
            writeToMyLog (" "+ weights[ii]);
        }
        writeToMyLog ("\n ");
        return weights;
    }

    public float get_weights2(float current_beta,int exp_i){
        int i_n = memory.size();
        float max_weight = (float) Math.pow(((i_n * min() / cumulative_priorities)) , (-1*current_beta));
        writeToMyLog ("get_weights : max_weight "+max_weight+"\n");
        float weights = (calculate_w(priorities.get(exp_i),current_beta,max_weight,i_n));
        writeToMyLog ("get_weights : weights"+ weights +"\n");

        all_weights[exp_i] = weights;
        return weights;
    }

    public void print_weights(){
        writeToMyLog ("get_weights : weights \n");
        for(int i=0; i < all_weights.length ; i++){
            writeToMyLog (" "+ all_weights[i]);
        }
        writeToMyLog ("\n ");
    }


    /**
     * Update priorities of sampled transitions inspiration: https://bit.ly/2PdNwU9
     * @param TDerrors TD-Errors of last samples
     */
    public void update_priorities(float[] TDerrors){//update each experiance separate
        if(TDerrors.length == indexes.size()){
            for(int i=0; i<indexes.size();i++){
                int ii = indexes.get(i);
                //removing old priorities
                cumulative_priorities -= priorities.get(ii);
                //transition priority: pi^α = (|δi| + ε)^α
                float p = (float)Math.pow((Math.abs(TDerrors[ii]) + eps) ,alpha);
                priorities.set(ii,p);
                //Update new priorities
                cumulative_priorities += priorities.get(ii);
            }
            max_priority= max();
            this.indexes = new ArrayList<Integer>();
        }
    }

    /**
     * Update priorities of sampled transitions inspiration: https://bit.ly/2PdNwU9
     * @param TDerrors TD-Errors of last samples
     */
    public void update_priorities2(float[] TDerrors, int exp_size){//update each experiance separate
        writeToMyLog ("Update_priorities \n");
        for(int i=0; i<exp_size;i++) {
            //removing old priorities
            cumulative_priorities -= priorities.get(i);
            //transition priority: pi^α = (|δi| + ε)^α
            float p = (float) Math.pow((Math.abs(TDerrors[i]) + eps), alpha);
            writeToMyLog (" "+ p);
            priorities.set(i, p);
            //Update new priorities
            cumulative_priorities += priorities.get(i);
        }
        writeToMyLog ("\n ");
            max_priority= max();
            this.indexes = new ArrayList<Integer>();

    }

    public void update_priorities2_double(float[] TDerrors, float[] r,  int exp_size){//update each experiance separate
        writeToMyLog ("Update_priorities \n");
        for(int i=0; i<exp_size;i++) {
            //removing old priorities
            cumulative_priorities -= priorities.get(i);
            float pr = TDerrors[i] + r[i] + eps;
            //transition priority: pi^α = (|δi| + ε)^α
            float p = (float) Math.pow((Math.abs(pr) + eps), alpha);
            writeToMyLog (" "+ p);
            priorities.set(i, p);
            //Update new priorities
            cumulative_priorities += priorities.get(i);
        }
        writeToMyLog ("\n ");
        max_priority= max();
        this.indexes = new ArrayList<Integer>();

    }



    public float get_max_priority(){return max_priority;}

    private void writeToMyLog (String str){
        try {
            myWriter = new FileWriter("/home/morakan/MK_Files/DPLogs/myBufferlog.txt ",true);
            myWriter.write(str);
            myWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}
