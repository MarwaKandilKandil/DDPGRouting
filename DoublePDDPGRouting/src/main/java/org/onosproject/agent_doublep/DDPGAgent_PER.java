/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.agent_doublep;

import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.layers.AbstractLayer;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossFunction;
import deepnetts.net.loss.LossType;
import deepnetts.util.Tensor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author marwa
 */
public class DDPGAgent_PER extends DDPGAgent {
        private final int BUFFER_SIZE = (int)10000 ;      //5000 1e5//replay buffer size
    private final int BATCH_SIZE = 64;            //64 //128 minibatch size 128
    private final float GAMMA = (float) 0.90;             //discount factor
    private final float TAU = (float)  1e-3;               // for soft update of target parameters
    private final float LR_ACTOR =(float) 1e-4;          // learning rate of the actor
    private final float LR_CRITIC = (float)1e-3;         //learning rate of the critic
    private final float WEIGHT_DECAY = (float)0.0;         //L2 weight decay
    private final float NOISE_DECAY = (float) 0.99;
    private final float PER_ALPHA = 0.6f;                   // importance sampling exponent
    private final float PER_BETA = 0.4f;                    // prioritization exponent
    private int max_t = 10;

    private PriorityReplayBuffer memory ;            //Priority replay buffer
    private Experiance[] experiences = new Experiance[BATCH_SIZE];//experiances array to store minibatches
    private OUNoise noise;                   // noise generator for exploration
    private float noise_decay;
    
    private FeedForwardNetwork critic_local;  //critic local network
    private FeedForwardNetwork critic_target; //critic target network
    private int[] critic_units = {128,256};
     
    private FeedForwardNetwork actor_local;   // actor local network
    private FeedForwardNetwork actor_target;  //actor target network
    private int[] actor_units = {128,256};
    
    private int state_size;//4*num_links
    private int action_size;//num_links*2 (2 directions)
    private float current_beta;
    private float alpha;
    private Random r;

    private FileWriter myWriter;
    
    /**
     * @param state_size (int): dimension of each state -- dimension of 4*number of nodes*number of nodes [0]:load , [1]:latency , [2]:plr , [3]:lur , [4]:flow mod
     * @param action_size (int): dimension of each action -- dimension of number of links [weights on each link]
     */
public DDPGAgent_PER(int state_size, int action_size){
	super();
        writeToMyLog ("Agent Getting READY...\n");
        this.state_size = state_size;
        this.action_size = action_size;
        this.current_beta = PER_BETA;
        this.alpha = PER_ALPHA;
        writeToMyLog ("Agent state size: "+state_size+" action size: "+action_size+"\n");
        this.r = new Random();

        r.setSeed(123);
       // Actor Network (w/ Target Network)
        actor_local = configActor(state_size, action_size, actor_units[0] , actor_units[1], LR_ACTOR);
        actor_target= configActor(state_size, action_size, actor_units[0] , actor_units[1], LR_ACTOR);
        writeToMyLog ("Agent Done with config actor network...\n");
        // Critic Network (w/ Target Network)
        critic_local = configCritic(state_size, action_size, critic_units[0] , critic_units[1], LR_CRITIC);
        critic_target= configCritic(state_size, action_size, critic_units[0] , critic_units[1], LR_CRITIC);
        writeToMyLog ("Agent Done with config critic network...\n");

        //Noise processor
        this.noise = new OUNoise(action_size,0.05,0.15,0.2,r);
        writeToMyLog ("Agent noise size: "+noise.get_state_links_size()+"\n");
        writeToMyLog ("Agent noise: "+noise.toString()+"\n");
        this.noise_decay = NOISE_DECAY;
        writeToMyLog ("Agent Done with Noise generator...\n");

        //Replay Memory
        memory = new PriorityReplayBuffer(action_size,BUFFER_SIZE,BATCH_SIZE,alpha,r);
        writeToMyLog ("Agent Done with reply memory...\n");

        writeToMyLog ("Agent READY...\n");
        
    }

    /**
     *  Return the current exponent β based on its schedul. Linearly anneal β from its initial value β0 to 1, at the end of learning.
     * @param t Current time step in the episode
     * @returnCurrent exponent beta
     */
    public float get_beta(int t){
    float frac = (float)t/max_t;
    float f_frac = frac<1.0f? frac:1.0f;
    current_beta = (float) (PER_BETA + f_frac * (1. - PER_BETA));
    return current_beta;
    }

    /**
     * Return the TD error calculated between Q_targets and Q_expected
     * @param Q_targets
     * @param Q_expected
     * @return
     */
    public float[] get_TD_error(float[] Q_targets , float[] Q_expected){
        float[] td_error = new float[Q_targets.length];
        for(int i=0;i<td_error.length;i++){
            td_error[i] = Q_targets[i] - Q_expected[i];
        }
        return td_error;
    }

    public float[] get_TD_error2(float[][] Q_targets , float[][] Q_expected,int exps_size){
        float[] td_error = new float[exps_size];// bec Q value is 1 for each experiance
        for (int i = 0; i < td_error.length; i++) {
            td_error[i] = Q_targets[i][0] - Q_expected[i][0];
        }

        return td_error;
    }

    public void reset(){
        this.noise.reset();
    }
    private FeedForwardNetwork configActor(int state_size, int action_size, int fc1_units , int fc2_units, float lr){
        // create instance of feed forward neural network using its builder
        FeedForwardNetwork neuralNet = FeedForwardNetwork.builder()
                .addInputLayer(state_size)
                .addFullyConnectedLayer(fc1_units, ActivationType.RELU)
                .addFullyConnectedLayer(fc2_units, ActivationType.RELU)
                .addOutputLayer(action_size, ActivationType.RELU)
                .lossFunction(LossType.MEAN_SQUARED_ERROR)
                .randomSeed(123)
                .build();
                 // set training settings
        neuralNet.getTrainer().setMaxError(0.2f)
                              .setLearningRate(lr);
        writeToMyLog ("Actor network ...\n "+ neuralNet.toString()+"\n");
        return neuralNet;
    }
    
        private FeedForwardNetwork configCritic(int state_size, int action_size, int fc1_units , int fc2_units, float lr){
        // create instance of feed forward neural network using its builder
        //FeedForwardNetwork
        FeedForwardNetwork neuralNet = FeedForwardNetwork.builder()
                .addInputLayer(state_size + action_size)
                .addFullyConnectedLayer(fc1_units, ActivationType.RELU)
                .addFullyConnectedLayer(fc2_units, ActivationType.RELU)
                .addOutputLayer(1, ActivationType.RELU)
                .lossFunction(LossType.MEAN_SQUARED_ERROR)
                .randomSeed(123)
                .build();
         // set training settings
        neuralNet.getTrainer().setMaxError(0.2f)
                              .setLearningRate(lr);
            writeToMyLog ("Critic network ...\n "+ neuralNet.toString()+"\n");
        return neuralNet;
    }
    public FeedForwardNetwork softUpdate(FeedForwardNetwork local, FeedForwardNetwork target){
        writeToMyLog ("Agent softUpdate...\n");
    /*INDArray critic_param = critic_local.params();
        critic_target.setParams(critic_param.mul(TAU).add(critic_param.mul(1-TAU)));
        INDArray actor_param = actor_local.params();
        actor_target.setParams(actor_param.mul(TAU).add(actor_param.mul(1-TAU)));*/

        List<AbstractLayer> local_param = local.getLayers();
        List<AbstractLayer> target_param = target.getLayers();
        writeToMyLog ("DDPGAgent -- softupdtate(): local_layers size = "+local_param.size()+"target_layers size = "+target_param.size()+"\n");
        float lr = target.getTrainer().getLearningRate();
        //writeToMyLog("\nDDPGAgent -- softupdtate(): New weights  = ");
        for(int i = 1 ; i<local_param.size();i++){
            Tensor weights_local = local_param.get(i).getWeights();
            Tensor weights_target = target_param.get(i).getWeights();
            //writeToMyLog ("DDPGAgent -- softupdtate(): local_layers size = "+local_param.get(i).getWeights().toString()+"\n");
            //writeToMyLog ("DDPGAgent -- softupdtate(): target_layers size = "+weights_target.size()+"\n");

            float[] w_local = weights_local.getValues();
            //writeToMyLog("\nDDPGAgent -- softupdtate(): weights_local  = "+printFloat(w_local));
           // writeToMyLog("\nDDPGAgent -- softupdtate(): weights_local  = "+w_local.length);

            float[] w_target = weights_target.getValues();
            //writeToMyLog("\nDDPGAgent -- softupdtate(): weights_target  = "+w_target.length);

            weights_local.multiply(TAU);
            weights_target.multiply(1-TAU);
            Tensor theta_param = weights_local;
            theta_param.add(weights_target);
            
            //target_param.get(i).setWeights(theta_param);
            target.getLayers().get(i).setWeights(theta_param);
           // writeToMyLog("i : "+theta_param.toString()+"\n");
        }
        /*
        // rebuild the network 
        target = FeedForwardNetwork.builder()
                .addLayer(target_param.get(0))//input layer
                .addLayer(target_param.get(1))//fully connected 
                .addLayer(target_param.get(2))//fully connected
                .addLayer(target_param.get(3))//output layer 
                .lossFunction(LossType.MEAN_SQUARED_ERROR)
                .randomSeed(123)
                .build();
        
        target.getTrainer().setMaxError(0.2f)
                              .setLearningRate(lr);
        */
        //for log
        List<AbstractLayer> target_param_2 = target.getLayers();
        //writeToMyLog("\nDDPGAgent -- softupdtate(): target weights  = ");
        for(int i = 1 ; i<local_param.size();i++){
            Tensor weights_target = target_param.get(i).getWeights();

            float[] w_target = weights_target.getValues();
           // writeToMyLog("\nDDPGAgent -- softupdtate(): weights_target  = "+ w_target.length +"\n");
           // writeToMyLog("i : "+weights_target.toString()+"\n");
        }
        
        return target; 
    }


    
     /**
     * Save experience in replay memory, and use random sample from buffer to learn.
     * @param state
     * @param action
     * @param reward
     * @param next_state
     * @param done
     */
    public void step(State state, Action action, float reward, State next_state, boolean done, int t){
        writeToMyLog ("Agent step...\n");
        // Save experience
        memory.addReplay(state,action,reward,next_state,done);
        writeToMyLog ("Agent step: addReplay..."+memory.printMem()+"\n");
        // Learn, if enough samples are available in memory
        if (memory.memory_length()>= BATCH_SIZE){
            experiences = memory.getMiniBatch(BATCH_SIZE);
            writeToMyLog ("Agent step: getMiniBatch...\n");
            //printExp ();
            //this.learn(experiences, GAMMA,t);
            this.learn2(experiences, GAMMA,t);
            writeToMyLog ("Agent step: learn...\n");
        }
    }
    
    public Action act(State state , boolean add_noise, int[][] active_links){
        writeToMyLog ("Agent act...\n");
        float[] state_inputs = state.get_flat_state();
        //writeToMyLog ("Agent act: state flattened:"+state_inputs.length+"\n");
        //writeToMyLog ("Agent act: state flattened:"+printFloat(state_inputs)+"\n\n");

        float[] predicted_action = actor_local.predict(state_inputs);
        //writeToMyLog ("Agent act: predicted_action:"+predicted_action.length+"\n");
        //writeToMyLog ("Agent act: predicted_action:"+printFloat(predicted_action)+"\n\n");

        if(add_noise){
            float[] noise_sample = noise.sample();//should be same size as actions
            //writeToMyLog("\nAgent act: Noise sample size = "+noise_sample.length+"\n");
            //writeToMyLog ("Agent act: noise_sample:"+printFloat(noise_sample)+"\n\n");
            for(int i = 0 ; i<predicted_action.length ;i++){
                predicted_action[i]+= noise_decay *noise_sample[i];
            }
            noise_decay*=noise_decay;
        }
        Action action = new Action(state,predicted_action,active_links);
        action.clamp_weights(0, 50);//0 as links arenot connected
       // writeToMyLog ("Agent act: action "+action.toString()+"\n");
        return action;
    }

    /**
     * Update policy and value parameters using given batch of experience array.
     * Q_targets = r + γ * critic_target(next_state, actor_target(next_state))
     * where:
     *      actor_target(state) -> action
     *      critic_target(state, action) -> Q-value
     * @param exps array of (s, a, r, s', done)
     * @param gamma discount factor
     * @param t current time step of the episode
     */
    public void learn(Experiance[] exps , double gamma, int t){
        writeToMyLog ("Agent learn...\n");

        //getting experiances
        State[] states = this.get_states(exps);
        State[] next_states = this.get_next_states(exps);
        Action[] actions = this.get_actions(exps);
        float[] rewards = this.get_rewards(exps);
        int[] dones = this.get_dones(exps);
        float totalTrainingLoss_critic = (float)0.0;
        //-------------------update critic---------------------//
        //Get predicted next_actions and Qvalues from target models
         for(int i = 0 ; i<exps.length ;i++){
            float[] actions_next = actor_target.predict(next_states[i].get_flat_state());//if action size is 1 the output is one, if action size > 1 then output = action size, need to change action from float to float[]
             writeToMyLog ("Agent learn: actions_next... "+printFloat(actions_next)+"\n");
            float[] next_states_actions = getStatesAction(next_states[i],actions_next);
             //writeToMyLog ("Agent learn: next_states_actions... "+printFloat(next_states_actions)+"\n");
            float[] Q_target_next = critic_target.predict(next_states_actions);//array to keep track of Q_targets size is 1
             //writeToMyLog ("Agent learn: Q_target_next... "+printFloat(Q_target_next)+"\n");
             // Compute Q targets for current states (y_i)
            for(int j = 0 ; j<Q_target_next.length;j++){
                Q_target_next[j] = rewards[i] + (GAMMA*Q_target_next[j]*(1-dones[i]));
                //writeToMyLog ("Agent learn: Q_target_next "+i+" : "+Q_target_next[j] +"\n");
            }
             writeToMyLog ("Agent learn: Q_target_next ... "+printFloat(Q_target_next)+"\n");
            //Get expected actions and Qvalues from target models
            float[] actions_curr = actor_local.predict(states[i].get_flat_state());
             writeToMyLog ("Agent learn: actions_curr... "+printFloat(actions_curr)+"\n");

             float[] states_actions = getStatesAction(states[i],actions_curr);//should get states at action a
             writeToMyLog ("Agent learn: states_actions ... "+printFloat(states_actions)+"\n");
             float[] Q_expected_next = critic_local.predict(states_actions);//array to keep track of Q_expected size is 1
             writeToMyLog ("Agent learn: Q_expected_next ... "+printFloat(Q_expected_next)+"\n");

             //Compute importance-sampling weight wj
             float f_currbeta = get_beta(t);
             float[] weights =new float[]{memory.get_weights2(f_currbeta,i)};//.get_weights(f_currbeta);

             //Compute TD-error δj
             float[] TD_errors = get_TD_error(Q_target_next,Q_expected_next);
             //Update transition priority pj
             //memory.update_priorities(TD_errors);
             memory.update_priorities2(TD_errors,i);
             ////STILL NEEDS WORK /////
             // Minimize the loss and fit network
            LossFunction lossFunction = critic_local.getLossFunction();
             //writeToMyLog ("Agent learn: lossFunction ... "+lossFunction.toString()+"\n");
             //Do forward pass, but don't clear the input activations in each layers - we need those set so we can calculate
            // gradients based on them
            critic_local.setInput(states_actions);
             //critic_local.forward();
            //float[] error= lossFunction.addPatternError(Q_expected_next, Q_target_next);//critic_local.getOutput()
             float[] error = weighted_mse_loss(Q_expected_next, Q_target_next,weights);
             writeToMyLog ("Agent learn: error ... "+printFloat(error)+"\n");
             critic_local.setOutputError(error);
             critic_local.predict(states_actions);
             /*for(int h = 1 ;h<critic_local.getLayers().size();h++){
                 critic_local.getLayers().get(i).backward();
                 writeToMyLog ("Agent learn: Layer "+i+" working\n");
             }*/
            //Update the gradient: apply learning rate, momentum, etc
            //This modifies the Gradient object in-place
             //critic_local.backward();
             writeToMyLog ("Agent learn: critic backward ...\n");
            critic_local.applyWeightChanges();
             writeToMyLog ("Agent learn: critic applyWeightChanges ...\n");
            totalTrainingLoss_critic = lossFunction.getTotal();
             //writeToMyLog("\nDDPGAgent -- learn () :  TrainError: " + totalTrainingLoss_critic+"\n");
        }
        //-------------------update Actor---------------------//
        // Compute actor loss
        for(int i = 0 ; i<exps.length ;i++){

            float[] actions_pred_local = actor_local.predict(states[i].get_flat_state());
            writeToMyLog ("Agent learn: actions_pred_local ... "+printFloat(actions_pred_local)+"\n");
            float[] states_action_pred_local = getStatesAction(states[i],actions_pred_local);
            writeToMyLog ("Agent learn: states_action_pred_local ... "+printFloat(states_action_pred_local)+"\n");
            float[] actor_loss= critic_local.predict(states_action_pred_local);
            writeToMyLog ("Agent learn: actor_loss ... "+printFloat(actor_loss)+"\n");
            actor_local.setInput(states[i].get_flat_state());
            for(int j = 0 ; j<actor_loss.length ; j++){
                actor_loss[j] *= -1.0;//gradient ascend (finding max Q)
            }
            //Minimize the loss
            actor_local.setOutputError(actor_loss);
            //actor_local.backward();
            actor_local.predict(states[i].get_flat_state());
            //writeToMyLog ("Agent learn: actor backward ...\n");
            actor_local.applyWeightChanges();
            //writeToMyLog ("Agent learn: actor applyWeightChanges ...\n");
        }
        critic_target = softUpdate(critic_local,critic_target);
        writeToMyLog ("Agent learn: critic softUpdate ...\n");
        actor_target = softUpdate(actor_local,actor_target);
        writeToMyLog ("Agent learn: actor softUpdate ...\n");
        
    }

    /**
     * Update policy and value parameters using given batch of experience array.
     * Q_targets = r + γ * critic_target(next_state, actor_target(next_state))
     * where:
     *      actor_target(state) -> action
     *      critic_target(state, action) -> Q-value
     * @param exps array of (s, a, r, s', done)
     * @param gamma discount factor
     * @param t current time step of the episode
     */
    public void learn2(Experiance[] exps , double gamma, int t){
        writeToMyLog ("Agent learn...\n");

        //getting experiances
        State[] states = this.get_states(exps);
        State[] next_states = this.get_next_states(exps);
        Action[] actions = this.get_actions(exps);
        float[] rewards = this.get_rewards(exps);
        int[] dones = this.get_dones(exps);
        float totalTrainingLoss_critic = (float)0.0;
        float[][] Q_target_next = new float[exps.length][1];
        float[][] Q_expected_next= new float[exps.length][1];
        float[][] actions_curr = new float[exps.length][action_size];
        float[][] states_actions = new float[exps.length][state_size + action_size];
        float[] weights = new float[exps.length];
        //-------------------update critic---------------------//
        //Get predicted next_actions and Qvalues from target models
        for(int i = 0 ; i<exps.length ;i++){
            float[] actions_next = actor_target.predict(next_states[i].get_flat_state());//if action size is 1 the output is one, if action size > 1 then output = action size, need to change action from float to float[]
            //writeToMyLog ("Agent learn: actions_next... "+printFloat(actions_next)+"\n");
            float[] next_states_actions = getStatesAction(next_states[i],actions_next);
            //writeToMyLog ("Agent learn: next_states_actions... "+printFloat(next_states_actions)+"\n");
            Q_target_next[i] = critic_target.predict(next_states_actions);//array to keep track of Q_targets size is 1
            //writeToMyLog ("Agent learn: Q_target_next... "+printFloat(Q_target_next)+"\n");
            // Compute Q targets for current states (y_i)
            for(int j = 0 ; j<Q_target_next.length;j++){
                float target = rewards[i] + (GAMMA*Q_target_next[j][0]*(1-dones[i]));
                Q_target_next[j] = new float[]{target};
                //writeToMyLog ("Agent learn: Q_target_next "+i+" : "+Q_target_next[j] +"\n");
            }
           // writeToMyLog ("Agent learn: Q_target_next ... "+printFloat(Q_target_next[i])+"\n");
            //Get expected actions and Qvalues from target models
            actions_curr[i] = actor_local.predict(states[i].get_flat_state());
          //  writeToMyLog ("Agent learn: actions_curr... "+printFloat(actions_curr[i])+"\n");

            states_actions[i] = getStatesAction(states[i],actions_curr[i]);//should get states at action a
          //  writeToMyLog ("Agent learn: states_actions ... "+printFloat(states_actions[i])+"\n");
            Q_expected_next[i] = critic_local.predict(states_actions[i]);//array to keep track of Q_expected size is 1
           // writeToMyLog ("Agent learn: Q_expected_next ... "+printFloat(Q_expected_next[i])+"\n");
            //Compute importance-sampling weight wj
            float f_currbeta = get_beta(t);
            weights[i] =memory.get_weights2(f_currbeta,i);//.get_weights(f_currbeta);
        }

        //Compute TD-error δj
        float[] TD_errors = get_TD_error2(Q_target_next,Q_expected_next,exps.length);
        //Update transition priority pj
        //memory.update_priorities(TD_errors);
        memory.update_priorities2(TD_errors,exps.length);
        float[] error = weighted_mse_loss2(Q_expected_next, Q_target_next,weights);
       // writeToMyLog("Agent learn: error ... " + printFloat(error) + "\n");

        ////STILL NEEDS WORK ////
        for(int i = 0 ; i<exps.length ;i++) {
            // Minimize the loss and fit network
            //Do forward pass, but don't clear the input activations in each layers - we need those set so we can calculate
            // gradients based on them
            critic_local.setInput(states_actions[i]);
            critic_local.setOutputError(error);
            critic_local.predict(states_actions[i]);
            //Update the gradient: apply learning rate, momentum, etc
            //This modifies the Gradient object in-place
            writeToMyLog("Agent learn: critic backward ...\n");
            critic_local.applyWeightChanges();
            writeToMyLog("Agent learn: critic applyWeightChanges ...\n");
            //writeToMyLog("\nDDPGAgent -- learn () :  TrainError: " + totalTrainingLoss_critic+"\n");
        }
            //-------------------update Actor---------------------//
        // Compute actor loss
        for(int i = 0 ; i<exps.length ;i++){

            float[] actions_pred_local = actor_local.predict(states[i].get_flat_state());
          //  writeToMyLog ("Agent learn: actions_pred_local ... "+printFloat(actions_pred_local)+"\n");
            float[] states_action_pred_local = getStatesAction(states[i],actions_pred_local);
           // writeToMyLog ("Agent learn: states_action_pred_local ... "+printFloat(states_action_pred_local)+"\n");
            float[] actor_loss= critic_local.predict(states_action_pred_local);
         //   writeToMyLog ("Agent learn: actor_loss ... "+printFloat(actor_loss)+"\n");
            actor_local.setInput(states[i].get_flat_state());
            for(int j = 0 ; j<actor_loss.length ; j++){
                actor_loss[j] *= -1.0;//gradient ascend (finding max Q)
            }
            //Minimize the loss
            actor_local.setOutputError(actor_loss);
            //actor_local.backward();
            actor_local.predict(states[i].get_flat_state());
            //writeToMyLog ("Agent learn: actor backward ...\n");
            actor_local.applyWeightChanges();
            //writeToMyLog ("Agent learn: actor applyWeightChanges ...\n");
        }
        critic_target = softUpdate(critic_local,critic_target);
        writeToMyLog ("Agent learn: critic softUpdate ...\n");
        actor_target = softUpdate(actor_local,actor_target);
        writeToMyLog ("Agent learn: actor softUpdate ...\n");

    }

    /**
     * Return the weighted mse loss to be used by Prioritized experience replay
     * @param targets
     * @param expected
     * @param weights
     * @return
     */
    public float[] weighted_mse_loss(float[] targets,float[] expected,float[] weights){
        float[] out = new float[targets.length];
        float[] newWeights=new float[targets.length];
        int j= weights.length;
        //expanding weights to match out array
        for(int i=0 ; i<newWeights.length;i++){
            if(i<weights.length){
                newWeights[i] = weights[i];
            }else{
                newWeights[i] = weights[weights.length-j];
                j--;
            }
        }
        //writeToMyLog ("Agent weighted_mse_loss: newWeights ... "+printFloat(newWeights)+"\n");

        //Calculating MSE
        for(int i=0 ; i<targets.length;i++){
            out[i] = (float) (Math.pow((expected[i] - targets[i]),2))*newWeights[i];
        }
        writeToMyLog ("Agent weighted_mse_loss: out ... "+printFloat(out)+"\n");

        float sum = 0.0f;
        for(int i=0 ; i<out.length;i++){
            sum +=out[i] ;
        }
        float[] loss = new float[1];
        loss[0] = (float)sum/out.length;
        return loss;
    }

    public float[] weighted_mse_loss2(float[][] targets,float[][] expected,float[] weights){
        float[] out = new float[targets.length];
        //Calculating MSE
        for(int i=0 ; i<targets.length;i++){
            out[i] = (float) (Math.pow((expected[i][0] - targets[i][0]),2))*weights[i];
        }
        //writeToMyLog ("Agent weighted_mse_loss: out ... "+printFloat(out)+"\n");

        float sum = 0.0f;
        for(int i=0 ; i<out.length;i++){
            sum +=out[i] ;
        }
        float[] loss = new float[1];
        loss[0] = (float)sum/out.length;
        writeToMyLog ("Agent weighted_mse_loss: loss ... "+loss[0]+"\n");
        return loss;
    }
    
    public State[] get_states(Experiance[] exps){
        State[] states = new State[exps.length];
        for(int i=0 ; i<states.length ;i++){
            states[i] = exps[i].getState();
        }
        return states;
    }
    
    public State[] get_next_states(Experiance[] exps){
        State[] next_states = new State[exps.length];
        for(int i=0 ; i<next_states.length ;i++){
            next_states[i] = exps[i].getNextState();
        }
        return next_states;
    }
    public Action[] get_actions(Experiance[] exps){
        Action[] actions = new Action[exps.length];
        for(int i=0 ; i<actions.length ;i++){
            actions[i] = exps[i].getAction();
        }
        return actions;
    }
    public float[] get_rewards(Experiance[] exps){
        float[] rewards = new float[exps.length];
        for(int i=0 ; i<rewards.length ;i++){
            rewards[i] = exps[i].getReward();
        }
        return rewards;
    }
    /**
     * Returns integer array of 0s and 1s where 1 is done and 0 not done
     * @param exps
     * @return 
     */
    public int[] get_dones(Experiance[] exps){
        int[] dones = new int[exps.length];
        for(int i=0 ; i<dones.length ;i++){
            
            dones[i] = exps[i].getDone()? 1:0;
        }
        return dones;
    }
    /**
     * Combining state and action arrays to be used in critic network
     * @param s input state
     * @param action input action
     * @return array combining state and action
     */
    public float[] getStatesAction(State s, float[] action){
        float[] state = s.get_flat_state();
        float[] state_action = new float[state.length + action.length];
        for(int i = 0 ;i< state_action.length ;i++){
            if(i<state.length){
                state_action[i] = state[i];
            }else if(i<action.length){
                state_action[i] = action[i];
            }
        }
        return state_action;
    }
    public void printExp (){
        writeToMyLog("Experiances Array \n");
        for(int i=0; i<experiences.length ; i++){
            writeToMyLog(experiences[i].toString() +" , ");
        }
        writeToMyLog("\n");
    }

    private void writeToMyLog (String str){
        try {
            myWriter = new FileWriter("/home/morakan/MK_Files/DPLogs/myAGENTlog.txt ",true);
            myWriter.write(str);
            myWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
    public String printFloat(float[] arr){
        String s="";
        for(int f=0;f<arr.length;f++){
            s+=arr[f]+" , ";
        }
        s+= "\n";
        return s;
    }
}
