/*
 * Copyright 2014-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.DoublePDDPGRouting;

import org.onlab.graph.Edge;//RezmoRouting
import org.onlab.packet.*;
import org.onlab.util.KryoNamespace;
import org.onlab.util.Tools;
import org.onosproject.agent_doublep.*;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.env_doublep.Observation;
import org.onosproject.env_doublep.SDNRouting;
import org.onosproject.kpath_doublep.Path;
import org.onosproject.net.packet.*;
import org.onosproject.net.*;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.device.DeviceService;//by RezmoRouting
import org.onosproject.net.device.PortStatistics;//by RezmoRouting
import org.onosproject.net.link.LinkService; //by RezmoRouting
import org.onosproject.net.statistic.StatisticService; //by RezmoRouting

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Timer;//by RezmoRouting
import java.util.TimerTask;//by RezmoRouting

import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.topology.*;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.MultiValuedTimestamp;
import org.onosproject.store.service.StorageService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import java.util.*;
import java.net.InetAddress;//me
import java.net.UnknownHostException;//me

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.FLOW_PRIORITY;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.FLOW_PRIORITY_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.FLOW_TIMEOUT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.FLOW_TIMEOUT_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.IGNORE_IPV4_MCAST_PACKETS;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.IGNORE_IPV4_MCAST_PACKETS_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.IPV6_FORWARDING;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.IPV6_FORWARDING_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_DST_MAC_ONLY;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_DST_MAC_ONLY_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_ICMP_FIELDS;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_ICMP_FIELDS_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV4_ADDRESS;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV4_ADDRESS_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV4_DSCP;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV4_DSCP_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV6_ADDRESS;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV6_ADDRESS_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV6_FLOW_LABEL;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_IPV6_FLOW_LABEL_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_TCP_UDP_PORTS;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_TCP_UDP_PORTS_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_VLAN_ID;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.MATCH_VLAN_ID_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.PACKET_OUT_OFPP_TABLE;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.PACKET_OUT_OFPP_TABLE_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.PACKET_OUT_ONLY;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.PACKET_OUT_ONLY_DEFAULT;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.RECORD_METRICS;
import static org.onosproject.DoublePDDPGRouting.OsgiPropertyConstants.RECORD_METRICS_DEFAULT;
import static org.slf4j.LoggerFactory.getLogger;

import javax.visrec.ml.data.DataSet;
import javax.visrec.ml.eval.EvaluationMetrics;

import deepnetts.net.FeedForwardNetwork;
import deepnetts.data.DataSets;
import deepnetts.eval.Evaluators;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossType;
import deepnetts.net.train.BackpropagationTrainer;
import deepnetts.util.Tensor;
import org.onosproject.util_doublep.CsvFile;
import org.onosproject.util_doublep.Plot;


/**
 * Sample reactive forwarding application.
 */
@Component(
        immediate = true,
        service = DoublePDDPGRouting.class,
        property = {
                PACKET_OUT_ONLY + ":Boolean=" + PACKET_OUT_ONLY_DEFAULT,
                PACKET_OUT_OFPP_TABLE + ":Boolean=" + PACKET_OUT_OFPP_TABLE_DEFAULT,
                FLOW_TIMEOUT + ":Integer=" + FLOW_TIMEOUT_DEFAULT,
                FLOW_PRIORITY + ":Integer=" + FLOW_PRIORITY_DEFAULT,
                IPV6_FORWARDING + ":Boolean=" + IPV6_FORWARDING_DEFAULT,
                MATCH_DST_MAC_ONLY + ":Boolean=" + MATCH_DST_MAC_ONLY_DEFAULT,
                MATCH_VLAN_ID + ":Boolean=" + MATCH_VLAN_ID_DEFAULT,
                MATCH_IPV4_ADDRESS + ":Boolean=" + MATCH_IPV4_ADDRESS_DEFAULT,
                MATCH_IPV4_DSCP + ":Boolean=" + MATCH_IPV4_DSCP_DEFAULT,
                MATCH_IPV6_ADDRESS + ":Boolean=" + MATCH_IPV6_ADDRESS_DEFAULT,
                MATCH_IPV6_FLOW_LABEL + ":Boolean=" + MATCH_IPV6_FLOW_LABEL_DEFAULT,
                MATCH_TCP_UDP_PORTS + ":Boolean=" + MATCH_TCP_UDP_PORTS_DEFAULT,
                MATCH_ICMP_FIELDS + ":Boolean=" + MATCH_ICMP_FIELDS_DEFAULT,
                IGNORE_IPV4_MCAST_PACKETS + ":Boolean=" + IGNORE_IPV4_MCAST_PACKETS_DEFAULT,
                RECORD_METRICS + ":Boolean=" + RECORD_METRICS_DEFAULT
        }
)

public class DoublePDDPGRouting {
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)//by RezmoRouting
    protected DeviceService deviceService; //by RezmoRouting

    @Reference(cardinality = ReferenceCardinality.MANDATORY)//by RezmoRouting
    protected LinkService linkService; //by RezmoRouting

    @Reference(cardinality = ReferenceCardinality.MANDATORY)//by RezmoRouting
    protected StatisticService statisticService; //by RezmoRouting

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private Timer MyTimer;//rezmo
    private TimerTask MyTimerTask;//rezmo

    private ApplicationId appId;

    /**
     * Enable packet-out only forwarding; default is false.
     */
    private boolean packetOutOnly = PACKET_OUT_ONLY_DEFAULT;

    /**
     * Enable first packet forwarding using OFPP_TABLE port instead of PacketOut with actual port; default is false.
     */
    private boolean packetOutOfppTable = PACKET_OUT_OFPP_TABLE_DEFAULT;

    /**
     * Configure Flow Timeout for installed flow rules; default is 10 sec.
     */
    private int flowTimeout = FLOW_TIMEOUT_DEFAULT;

    /**
     * Configure Flow Priority for installed flow rules; default is 10.
     */
    private int flowPriority = FLOW_PRIORITY_DEFAULT;

    /**
     * Enable IPv6 forwarding; default is false.
     */
    private boolean ipv6Forwarding = IPV6_FORWARDING_DEFAULT;

    /**
     * Enable matching Dst Mac Only; default is false.
     */
    private boolean matchDstMacOnly = MATCH_DST_MAC_ONLY_DEFAULT;

    /**
     * Enable matching Vlan ID; default is false.
     */
    private boolean matchVlanId = MATCH_VLAN_ID_DEFAULT;

    /**
     * Enable matching IPv4 Addresses; default is false.
     */
    private boolean matchIpv4Address = MATCH_IPV4_ADDRESS_DEFAULT;

    /**
     * Enable matching IPv4 DSCP and ECN; default is false.
     */
    private boolean matchIpv4Dscp = MATCH_IPV4_DSCP_DEFAULT;

    /**
     * Enable matching IPv6 Addresses; default is false.
     */
    private boolean matchIpv6Address = MATCH_IPV6_ADDRESS_DEFAULT;

    /**
     * Enable matching IPv6 FlowLabel; default is false.
     */
    private boolean matchIpv6FlowLabel = MATCH_IPV6_FLOW_LABEL_DEFAULT;

    /**
     * Enable matching TCP/UDP ports; default is false.
     */
    private boolean matchTcpUdpPorts = MATCH_TCP_UDP_PORTS_DEFAULT;

    /**
     * Enable matching ICMPv4 and ICMPv6 fields; default is false.
     */
    private boolean matchIcmpFields = MATCH_ICMP_FIELDS_DEFAULT;

    /**
     * Ignore (do not forward) IPv4 multicast packets; default is false.
     */
    private boolean ignoreIPv4Multicast = IGNORE_IPV4_MCAST_PACKETS_DEFAULT;

    /**
     * Enable record metrics for reactive forwarding.
     */
    private boolean recordMetrics = RECORD_METRICS_DEFAULT;

    /////// For Log files ///////
    private int[][] Node_Matrix;
    private FileWriter myWriter;
    private FileWriter myWriter2;

    private double[][] weight_matrix; //me
    Random rand = new Random(123); //me

    /////// For the learning process ///////
    private ArrayList<Double> sampled_rewards = new ArrayList<Double>();
    private final int NUM_EPISODES = 10; //10 100 1000  //200               //number of training episodes
    private int WINDOW_SIZE = 1;   //1 10 100                        //Window size for sampled rewards
    private double[] avg_rewards = new double[NUM_EPISODES / WINDOW_SIZE];   //List of average rewards over 100 episodes
    private double best_avg_reward = Double.NEGATIVE_INFINITY;    //best Average reward
    private String[] epi = new String[NUM_EPISODES / WINDOW_SIZE];


    /////// Network Stats ///////
    //To calculate link latency, later on must use 2D array for latency and arrays for mac
    private double sent_time = Double.NEGATIVE_INFINITY;
    private double recv_time = Double.NEGATIVE_INFINITY;
    private MacAddress src_mac = MacAddress.BROADCAST;
    private MacAddress dst_mac = MacAddress.BROADCAST;
    private double latency = 0.0;//Double.NEGATIVE_INFINITY;
    private final double LINK_CAPACITY = 1.0e9;                //1GB //10MB
    private float[][] links_latency;                              //link delay array
    private float[][] links_plr;                                  //link packet loss ratio array
    private float[][] links_util;                                  //link utilization array
    private int[][] topo_links;
    //private String[] epi = new String[NUM_EPISODES / WINDOW_SIZE];
    private boolean prob_flag = true;

    private double[] num_flow_mod;
    private double[] num_packet_in;
    private double[] switches_pwr;
    private List<Path> previous_paths = new ArrayList<Path>();
    private List<Path> current_paths = new ArrayList<Path>();
    private double TB_new = 0.0 , TB_old = 0.0; //Transmitted bytes used for Throughput calculations

    DDPGAgent agent_DDPG;
    DDPGAgent_PER agent_PER;
    DDPGAgent_DoublePER agent_double;
    SDNRouting env;
    Observation env_obsrv;
    TopologyGraph g;
    float episode_score;
    float[][] fweights ;
    double[][] dweights ;

    ArrayList<Double> avg_link_lat = new ArrayList<Double>();
    ArrayList<Double> avg_e2e_lat = new ArrayList<Double>();
    ArrayList<Double> avg_plr = new ArrayList<Double>();
    ArrayList<Double> avg_agnt_time = new ArrayList<Double>();
    ArrayList<Double> avg_troughput = new ArrayList<Double>();
    ArrayList<Double> av_pwr = new ArrayList<Double>();


    @Activate
    public void activate(ComponentContext context) {
        KryoNamespace.Builder metricSerializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API)
                .register(MultiValuedTimestamp.class);
        cfgService.registerProperties(getClass());
        appId = coreService.registerApplication("org.onosproject.DoublePDDPGRouting");
        packetService.addProcessor(processor, PacketProcessor.director(2));
        readComponentConfiguration(context);
        requestIntercepts();

        Node_Matrix = new int[deviceService.getDeviceCount()][deviceService.getDeviceCount()];
        weight_matrix = new double[deviceService.getDeviceCount()+1][deviceService.getDeviceCount()+1];//new double[deviceService.getDeviceCount()][deviceService.getDeviceCount()]
        links_latency = new float[deviceService.getDeviceCount() + 1][deviceService.getDeviceCount() + 1];
        links_plr = new float[deviceService.getDeviceCount() + 1][deviceService.getDeviceCount() + 1];
        links_util = new float[deviceService.getDeviceCount() + 1][deviceService.getDeviceCount() + 1];
        num_packet_in = new double[deviceService.getDeviceCount() + 1];
        num_flow_mod = new double[deviceService.getDeviceCount() + 1];
        switches_pwr = new double[deviceService.getDeviceCount() + 1];
        topo_links = new int[linkService.getLinkCount()][2];

        //create_random_weights(weight_matrix);
        my_timer_task();
        
        try {
            myWriter = new FileWriter("/home/morakan/MK_Files/DPLogs/myDPRoutinglog.txt ");//me
            myWriter2 = new FileWriter("/home/morakan/MK_Files/DPLogs/myDPResults.txt ");//me
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {

            writeToMyLog("Module has been started!!!!! " + InetAddress.getLocalHost().getHostAddress() + " \n");
        } catch (UnknownHostException e) {

            writeToMyLog("UnknownHostException!!!!! \n");

        }
        //test_deepNetts();
        log.info("Started", appId.id());
        mystatistics();
        mytopolgyfinder();
        Show_Matrix(Node_Matrix);
        /*try{
            Socket socket=new Socket("localhost",2004);
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            DataInputStream din=new DataInputStream(socket.getInputStream());
            out.println("Start");
            out.flush();
            log.info("Sending to START Training");
            out.close();
            din.close();
            socket.close();
        }
        catch(Exception e){
            e.printStackTrace();}*/

        //install_arpnet();
        //install_nsfnet();
        install_abliene();
        log.info("I am Starting Env...");
        writeToMyLog("I am Starting Env..." + "\n");



        //agent = 0,        --> GAMS
        //agent = 1,rand =0 -->OSPF constant link weights
        //agent = 1,rand =1 -->OSPF random link weights
        //agent = 2,        -->DDPG Agent
        //agent = 3         -->DDPG_PER Agent
        //agent = 4         -->DDPG_double Agent
        Agent(0,0);
        log.info("I am done with Env...");
        writeToMyLog("I am done with Env..." + "\n");
        //ddpg_test()

        /*if(deviceService.getDeviceCount()==12) {
            log.info("*** STARTING XML ***");
            XMLGenerator xl_gen = new XMLGenerator();
            log.info("*** DONE XML *** Waiting for GAMS");
            wait(5000);//1sec
            log.info("*** DONE GAMS ***");
            List<int[]> nodesList = xl_gen.read_gams_output();
            install_gams_sol(nodesList);
            log.info("*** DONE with Paths ***");
            //results (1, 0);
        }*/
       // log.info("*** FINISHED GAMS ***");
    }

    @Deactivate
    public void deactivate() {
        cfgService.unregisterProperties(getClass(), false);
        withdrawIntercepts();
        flowRuleService.removeFlowRulesById(appId);
    }

    @Modified
    public void modified(ComponentContext context) {
        readComponentConfiguration(context);
        requestIntercepts();
    }




    public void test_deepNetts() {

        int inputsNum = 1;
        int outputsNum = 1;

        String csvFilename = "/home/morakan/onos/apps/DoublePDDPGRouting/src/main/java/org/onosproject/util_doublep/linear.csv"; // this file contains the training data

        // load and create data set from csv file
        DataSet dataSet = null;
        try {
            dataSet = DataSets.readCsv(csvFilename, inputsNum, outputsNum);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create neural network using network specific builder
        FeedForwardNetwork neuralNet = FeedForwardNetwork.builder()
                .addInputLayer(inputsNum)
                .addOutputLayer(outputsNum, ActivationType.LINEAR)
                .lossFunction(LossType.MEAN_SQUARED_ERROR)
                .build();

        BackpropagationTrainer trainer = neuralNet.getTrainer();
        trainer.setMaxError(0.002f)
                .setMaxEpochs(10000)
                .setLearningRate(0.01f);

        // train network using loaded data set
        neuralNet.train(dataSet);

        // test model with same data
        EvaluationMetrics em = Evaluators.evaluateRegressor(neuralNet, dataSet);
        writeToMyLog(em + "\n");

        // print out learned model
        float slope = neuralNet.getLayers().get(1).getWeights().get(0);
        float intercept = neuralNet.getLayers().get(1).getBiases()[0];

        writeToMyLog("Original function: y = 0.5 * x + 0.2\n");
        writeToMyLog("Estimated/learned function: y = " + slope + " * x + " + intercept + "\n");

        // perform prediction for some input value
        float[] predictedOutput = neuralNet.predict(new float[]{0.2f});
        writeToMyLog("Predicted output for 0.2 :" + Arrays.toString(predictedOutput) + "\n");

        try {
            plotTrainingData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // plot predictions for some random data
        plotPredictions(neuralNet);
    }

    public void plotPredictions(FeedForwardNetwork nnet) {
        double[][] data = new double[100][2];

        for (int i = 0; i < data.length; i++) {
            data[i][0] = 0.5 - Math.random();
            nnet.setInput(new Tensor(1, 1, new float[]{(float) data[i][0]}));
            data[i][1] = nnet.getOutput()[0];
        }

        Plot.scatter(data, "Neural Network Predictions");
    }

    public void plotTrainingData() throws IOException {
        double[][] dataPoints = CsvFile.read("/home/morakan/onos/apps/DoublePDDPGRouting/src/main/java/org/onosproject/util_doublep/linear.csv", 30);
        Plot.scatter(dataPoints, "Training data");
    }



    private class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {
            // Stop processing if the packet has been handled, since we
            // can't do any more to it.

            if (context.isHandled()) {
                return;
            }

            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();

            if (ethPkt == null) {
                return;
            }
            if (ethPkt.getSourceMAC().equals(src_mac) &&
                    ethPkt.getDestinationMAC().equals(dst_mac)) {
                //context.time()
                //recv_time = System.currentTimeMillis();
                recv_time = System.nanoTime();
                //latency = (recv_time - sent_time)/1000.0;//ms
                latency = (recv_time-sent_time)/10000.0;//ms

               // latency = latency/10.0;//ms//1000
                // writeToMyLog("packet processor -- probe packet is received: " + recv_time+"\n");
                log.info("probe packet is received: " + latency);
                //writeToMyLog("packet processor -- Link Latency: " + latency +"\n");
                //log.info("Link Latency: " + latency);
                prob_flag = false;


            }
            HostId id = HostId.hostId(ethPkt.getDestinationMAC(), VlanId.vlanId(ethPkt.getVlanID()));

            // Do not process LLDP MAC address in any way.
            if (id.mac().isLldp()) {
                return;
            }

            // Do not process IPv4 multicast packets, let mfwd handle them
            if (ignoreIPv4Multicast && ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                if (id.mac().isMulticast()) {
                    return;
                }
            }

            // Do we know who this is for? If not, flood and bail. USEFULL FOR ARP PACKETS
            Host dst = hostService.getHost(id);
            if (dst == null) {
                flood(context);
                return;
            }
        }
    }

    // Floods the specified packet if permissible.
    private void flood(PacketContext context) {
        if (topologyService.isBroadcastPoint(topologyService.currentTopology(),
                context.inPacket().receivedFrom())) {
            context.treatmentBuilder().setOutput(PortNumber.FLOOD);
            context.send();
        } else {
            context.block();
        }
    }

    /**
     * Request packet in via packet service.
     */
    private void requestIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);

        selector.matchEthType(Ethernet.TYPE_IPV6);
        if (ipv6Forwarding) {
            packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
        } else {
            packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        }
    }

    /**
     * Cancel request for packet in via packet service.
     */
    private void withdrawIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        selector.matchEthType(Ethernet.TYPE_IPV6);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
    }

    /**
     * Extracts properties from the component configuration context.
     *
     * @param context the component context
     */
    private void readComponentConfiguration(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        Boolean packetOutOnlyEnabled =
                Tools.isPropertyEnabled(properties, PACKET_OUT_ONLY);
        if (packetOutOnlyEnabled == null) {
            //me
            //writeToMyLog("Packet-out is not configured, "   +   "using current value of "+ packetOutOnly+"\n");
            //log.info("Packet-out is not configured, "   +   "using current value of {}", packetOutOnly);
        } else {
            packetOutOnly = packetOutOnlyEnabled;
            //me
            //String p_out = packetOutOnly ? "enabled" : "disabled";
            //writeToMyLog("Configured. Packet-out only forwarding is "+p_out+"\n");
            //log.info("Configured. Packet-out only forwarding is {}",packetOutOnly ? "enabled" : "disabled");
        }

        Boolean packetOutOfppTableEnabled =
                Tools.isPropertyEnabled(properties, PACKET_OUT_OFPP_TABLE);
        if (packetOutOfppTableEnabled == null) {
            
            //writeToMyLog("OFPP_TABLE port is not configured, "   +  "using current value of "+ packetOutOfppTable+"\n");
            //log.info("OFPP_TABLE port is not configured, "   +  "using current value of {}", packetOutOfppTable);
        } else {
            packetOutOfppTable = packetOutOfppTableEnabled;
           
            //String p_outTable = packetOutOfppTable ? "enabled" : "disabled";
            //writeToMyLog("Configured. Forwarding using OFPP_TABLE port is "+p_outTable+"\n");
            //log.info("Configured. Forwarding using OFPP_TABLE port is {}",packetOutOfppTable ? "enabled" : "disabled");
        }

        Boolean ipv6ForwardingEnabled =
                Tools.isPropertyEnabled(properties, IPV6_FORWARDING);
        if (ipv6ForwardingEnabled == null) {
            
            //writeToMyLog( "IPv6 forwarding is not configured, "   +  "using current value of "+ ipv6Forwarding+"\n");
            //log.info("IPv6 forwarding is not configured, "   +  "using current value of {}", ipv6Forwarding);
        } else {
            ipv6Forwarding = ipv6ForwardingEnabled;
            
            //String ipF = ipv6Forwarding ? "enabled" : "disabled";
            //writeToMyLog( "Configured. IPv6 forwarding is "+ipF+"\n");
            //log.info("Configured. IPv6 forwarding is {}",ipv6Forwarding ? "enabled" : "disabled");
        }

        Boolean matchDstMacOnlyEnabled =
                Tools.isPropertyEnabled(properties, MATCH_DST_MAC_ONLY);
        if (matchDstMacOnlyEnabled == null) {
            
            //writeToMyLog( "Match Dst MAC is not configured, "   +  "using current value of "+matchDstMacOnly+"\n");
            //log.info("Match Dst MAC is not configured, "   +  "using current value of {}", matchDstMacOnly);
        } else {
            matchDstMacOnly = matchDstMacOnlyEnabled;
            
            //String mMac = matchDstMacOnly ? "enabled" : "disabled";
            //writeToMyLog( "Configured. Match Dst MAC Only is "+mMac+"\n");
            //log.info("Configured. Match Dst MAC Only is {}",matchDstMacOnly ? "enabled" : "disabled");
        }

        Boolean matchVlanIdEnabled =
                Tools.isPropertyEnabled(properties, MATCH_VLAN_ID);
        if (matchVlanIdEnabled == null) {
            
            //writeToMyLog( "Matching Vlan ID is not configured, "   +  "using current value of "+ matchVlanId+"\n");
            //log.info("Matching Vlan ID is not configured, "   +  "using current value of {}", matchVlanId);
        } else {
            matchVlanId = matchVlanIdEnabled;
            
            //String mV = matchVlanId ? "enabled" : "disabled";
            //writeToMyLog( "Configured. Matching Vlan ID is "+ mV+"\n");
            //log.info("Configured. Matching Vlan ID is {}",matchVlanId ? "enabled" : "disabled");
        }

        Boolean matchIpv4AddressEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV4_ADDRESS);
        if (matchIpv4AddressEnabled == null) {
            
            //writeToMyLog( "Matching IPv4 Address is not configured, "   +  "using current value of "+ matchIpv4Address+"\n");
            //log.info("Matching IPv4 Address is not configured, "   +  "using current value of {}", matchIpv4Address);
        } else {
            matchIpv4Address = matchIpv4AddressEnabled;
            
            //String mIP = matchIpv4Address ? "enabled" : "disabled";
            //writeToMyLog( "Configured. Matching IPv4 Addresses is "+mIP+"\n");
            //log.info("Configured. Matching IPv4 Addresses is {}",matchIpv4Address ? "enabled" : "disabled");
        }

        Boolean matchIpv4DscpEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV4_DSCP);
        if (matchIpv4DscpEnabled == null) {
            
            //writeToMyLog( "Matching IPv4 DSCP and ECN is not configured, "   +  "using current value of "+ matchIpv4Dscp+"\n");
            //log.info("Matching IPv4 DSCP and ECN is not configured, "   +  "using current value of {}", matchIpv4Dscp);
        } else {
            matchIpv4Dscp = matchIpv4DscpEnabled;
            
            //String mIP = matchIpv4Dscp ? "enabled" : "disabled";
            //writeToMyLog( "Configured. Matching IPv4 DSCP and ECN is "+mIP+"\n");
            //log.info("Configured. Matching IPv4 DSCP and ECN is {}",matchIpv4Dscp ? "enabled" : "disabled");
        }

        Boolean matchIpv6AddressEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV6_ADDRESS);
        if (matchIpv6AddressEnabled == null) {
            
            //writeToMyLog("Matching IPv6 Address is not configured, "   +  "using current value of "+ matchIpv6Address +"\n");
            //log.info("Matching IPv6 Address is not configured, "   +  "using current value of {}", matchIpv6Address);
        } else {
            matchIpv6Address = matchIpv6AddressEnabled;
            
            //String mAddrs = matchIpv6Address ? "enabled" : "disabled";
            //writeToMyLog( "Configured. Matching IPv6 Addresses is "+ mAddrs+"\n");
            //log.info("Configured. Matching IPv6 Addresses is {}",matchIpv6Address ? "enabled" : "disabled");
        }

        Boolean matchIpv6FlowLabelEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV6_FLOW_LABEL);
        if (matchIpv6FlowLabelEnabled == null) {
            
            //writeToMyLog( "Matching IPv6 FlowLabel is not configured, "   +  "using current value of "+ matchIpv6FlowLabel+"\n");
            //log.info("Matching IPv6 FlowLabel is not configured, "   +  "using current value of {}", matchIpv6FlowLabel);
        } else {
            matchIpv6FlowLabel = matchIpv6FlowLabelEnabled;
            
            //String mIP = matchIpv6FlowLabel ? "enabled" : "disabled";
            //writeToMyLog("Configured. Matching IPv6 FlowLabel is "+ mIP +"\n");
            //log.info("Configured. Matching IPv6 FlowLabel is {}",matchIpv6FlowLabel ? "enabled" : "disabled");
        }

        Boolean matchTcpUdpPortsEnabled =
                Tools.isPropertyEnabled(properties, MATCH_TCP_UDP_PORTS);
        if (matchTcpUdpPortsEnabled == null) {
            
            //writeToMyLog("Matching TCP/UDP fields is not configured, "   +  "using current value of "+ matchTcpUdpPorts +"\n");
            //log.info("Matching TCP/UDP fields is not configured, "   +  "using current value of {}", matchTcpUdpPorts);
        } else {
            matchTcpUdpPorts = matchTcpUdpPortsEnabled;
            
            //String bool1 = matchTcpUdpPorts ? "enabled" : "disabled";
            //writeToMyLog("Configured. Matching TCP/UDP fields is "+ bool1 +"\n");
            //log.info("Configured. Matching TCP/UDP fields is {}",matchTcpUdpPorts ? "enabled" : "disabled");
        }

        Boolean matchIcmpFieldsEnabled =
                Tools.isPropertyEnabled(properties, MATCH_ICMP_FIELDS);
        if (matchIcmpFieldsEnabled == null) {
            
            //writeToMyLog( "Matching ICMP (v4 and v6) fields is not configured, "   +  "using current value of "+ matchIcmpFields+"\n");
            //log.info("Matching ICMP (v4 and v6) fields is not configured, "   +  "using current value of {}", matchIcmpFields);
        } else {
            matchIcmpFields = matchIcmpFieldsEnabled;
            
            //String bool2 = matchIcmpFields ? "enabled" : "disabled";
            //writeToMyLog("Configured. Matching ICMP (v4 and v6) fields is "+ bool2 +"\n");
            //log.info("Configured. Matching ICMP (v4 and v6) fields is {}",matchIcmpFields ? "enabled" : "disabled");
        }

        Boolean ignoreIpv4McastPacketsEnabled =
                Tools.isPropertyEnabled(properties, IGNORE_IPV4_MCAST_PACKETS);
        if (ignoreIpv4McastPacketsEnabled == null) {
            
            //writeToMyLog("Ignore IPv4 multi-cast packet is not configured, "   +  "using current value of "+ ignoreIPv4Multicast +"\n");
            log.info("Ignore IPv4 multi-cast packet is not configured, " + "using current value of {}", ignoreIPv4Multicast);
        } else {
            ignoreIPv4Multicast = ignoreIpv4McastPacketsEnabled;
            
            //String bool3 = ignoreIPv4Multicast ? "enabled" : "disabled";
            //writeToMyLog("Configured. Ignore IPv4 multicast packets is "+ bool3+"\n");
            //log.info("Configured. Ignore IPv4 multicast packets is {}",ignoreIPv4Multicast ? "enabled" : "disabled");
        }
        Boolean recordMetricsEnabled =
                Tools.isPropertyEnabled(properties, RECORD_METRICS);
        if (recordMetricsEnabled == null) {
            
            //writeToMyLog("IConfigured. Ignore record metrics  is "  + recordMetrics +  ", using current value of "+ recordMetrics+"\n");
            log.info("IConfigured. Ignore record metrics  is {} ," + "using current value of {}", recordMetrics);
        } else {
            recordMetrics = recordMetricsEnabled;
            
            //String bool4 = recordMetrics ? "enabled" : "disabled";
            //writeToMyLog("Configured. record metrics  is "+bool4+"\n");
            //log.info("Configured. record metrics  is {}",recordMetrics ? "enabled" : "disabled");
        }

        flowTimeout = Tools.getIntegerProperty(properties, FLOW_TIMEOUT, FLOW_TIMEOUT_DEFAULT);
        
        //writeToMyLog("Configured. Flow Timeout is configured to "+ flowTimeout +" seconds"+"\n");
        //log.info("Configured. Flow Timeout is configured to {} seconds", flowTimeout);

        flowPriority = Tools.getIntegerProperty(properties, FLOW_PRIORITY, FLOW_PRIORITY_DEFAULT);
        
        //writeToMyLog("Configured. Flow Priority is configured to "+ flowPriority+"\n");
        //log.info("Configured. Flow Priority is configured to {}", flowPriority);
    }

    /**
     * Packet processor responsible for forwarding packets along their paths.
     */

    private void my_timer_task() {
        this.MyTimer = new Timer();
        this.MyTimerTask = new TimerTask() {
            @Override
            public void run() {
                my_timer_task();
            }
        };
        MyTimer.schedule(MyTimerTask, 20000);
    }

    public double[][] Int2Double(int[][] m) {
        double[][] ret = new double[m.length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m.length; j++)
                ret[i][j] = Double.valueOf(m[i][j]);
        return ret;

    }

    //get the topology detected and represent it as a matrix of 0s and 1s called Node_matrix, 1s means connected
    private void mytopolgyfinder() {
        //me
        writeToMyLog("Topology Of network is as follows:" + "\n");
        log.info("Topology Of network is as follows:");
        for (int i = 0; i < Node_Matrix.length; i++)
            for (int j = 0; j < Node_Matrix.length; j++)
                Node_Matrix[i][j] = 0;
        Show_Matrix(Node_Matrix);
        //me
        writeToMyLog(topologyService.getGraph(topologyService.currentTopology()).toString() + "\n");
        log.info(topologyService.getGraph(topologyService.currentTopology()).toString());
        TopologyGraph g = topologyService.getGraph(topologyService.currentTopology());
        for (Edge edge : g.getEdges()) {
            //me
            writeToMyLog("Edge()=" + edge.dst() + " " + edge.src() + "\n");
            log.info("Edge()=" + edge.dst() + " " + edge.src());

        }
        for (Edge edge : g.getEdges()) {
            //me
            writeToMyLog("Edge()=" + Integer.parseInt(edge.dst().toString().substring(edge.dst().toString().indexOf(':') + 1), 16) + " " + Integer.parseInt(edge.src().toString().substring(edge.src().toString().indexOf(':') + 1), 16) + "\n");
            log.info("Edge()=" + Integer.parseInt(edge.dst().toString().substring(edge.dst().toString().indexOf(':') + 1), 16) + " " + Integer.parseInt(edge.src().toString().substring(edge.src().toString().indexOf(':') + 1), 16));

        }
        Iterable<Link> links = linkService.getActiveLinks();//getting all active links

        //  StatisticService statisticService=StatisticService.class);
        links.forEach((element) -> {
            // log.info( element.toString()  +  "  type=  "  +  element.type()  +   "  state= "  +  element.state()  +  "  src= "  +  element.src()  +  "  dst "  +  element.dst());
            //log.info( "Load(byte/sec)= "  +  statisticService.load(element).rate());
            //me
            writeToMyLog("Link Load(byte/sec)= " + statisticService.load(element).rate() + "\n");
            int p = Integer.parseInt(element.src().toString().substring(element.src().toString().indexOf(':') + 1, element.src().toString().indexOf('/')), 16);//source index
            int q = Integer.parseInt(element.dst().toString().substring(element.dst().toString().indexOf(':') + 1, element.dst().toString().indexOf('/')), 16);//dist index
            Long val = statisticService.load(element).rate();
            Node_Matrix[p - 1][q - 1] = 1 + val.intValue(); // I put 1+ for zero values
            double w = rand.nextDouble() * 100;
            weight_matrix[p][q] = w + 1.0;//weight_matrix[p - 1][q - 1] = w;
            weight_matrix[q][p] = w + 1.0;//weight_matrix[q - 1][p - 1] = w
        });

        //me
        int links_size = 0;
        for (Link i : links) {
            links_size++;
        }

        //me
        writeToMyLog("Number of Total links " + links_size + "\n");

    }

    //creating random weights for the topology
    private void create_random_weights(double[][] w) {
        writeToMyLog("Creating random weights " + w.length + "\n");
        for (int i = 0; i < w.length; i++) {
            for (int j = 0; j < w.length; j++) {
                w[i][j] = rand.nextDouble() * 100;
                w[j][i] = w[i][j];
            }
        }
    }

    private void Show_Matrix(int[][] m) {
        String str = "\n";
        //me
        writeToMyLog("I want to show a matrix " + m.length + "\n");
        log.info("I want to show a matrix " + m.length);
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++)
                str += String.valueOf(m[i][j]) + "    ";
            str += "\n";
        }
        //me
        writeToMyLog(str + "\n");
        log.info(str);
    }

    //to get port stats
    private void mystatistics() {
        Iterable<Device> devices = deviceService.getDevices();
        devices.forEach((element) -> {
            List<PortStatistics> portStatisticsList = deviceService.getPortDeltaStatistics(element.id());
            for (PortStatistics portdeltastat : portStatisticsList) {
                if (portdeltastat != null) {
                    log.info("portdeltastat bytes recieved " + portdeltastat.bytesReceived() + "  " + portdeltastat.bytesSent());

                    //me
                    //writeToMyLog("portdeltastat bytes recieved "   +   portdeltastat.bytesReceived()  +   "  "  +  portdeltastat.bytesSent()+"\n");
                } else {
                    //me
                    writeToMyLog("Unable to read portDeltaStats" + "\n");
                    log.info("Unable to read portDeltaStats");
                }
            }

        });

        devices.forEach((element) -> {
            Iterable<FlowEntry> flows = flowRuleService.getFlowEntries(element.id());

            for (FlowEntry fl : flows) {

                //me
                //writeToMyLog("flow of device"  +  element.id()   +  " is= "  +   fl.toString()+"\n");
                log.info("flow of device" + element.id() + " is= " + fl.toString());

            }

        });
        //me
        int total_flow_size = 0;
        //me
        for (Device d : devices) {

            Iterable<FlowEntry> flows = flowRuleService.getFlowEntries(d.id());

            for (FlowEntry fl : flows) {

                //me
                total_flow_size++;

            }
        }
        //me
        writeToMyLog("Number of active flows " + total_flow_size + "\n");

    }
    /////////////////////////// Training Algorithms /////////////////////////////

    /**
     * Used to setup, train and run the agent
     * Different agents can be applied
     * agent_type = 1 and isRand = 1 ---> Run Dikestra with random link weights
     * agent_type = 1 and isRand = 0 ---> Run OSPF with constant link weights
     * agent_type = 2                ---> Run DDPG Agent
     * agent_type = 3                ---> Run DDPG_PER Agent
     * agent_type = 4                ---> Run DDPG_Double Agent
     * @param agent_type type of agent to be trained and to run
     * @param isRand for OSPF with random link weights or constant link weights
     */
    public void Agent(int agent_type, int isRand) {
        g = topologyService.getGraph(topologyService.currentTopology());
        int num_links = g.getEdges().size();
        int num_nodes = g.getVertexes().size();
        //---Initial setup of weights---
        fweights = from_Double_to_float(weight_matrix);
        dweights = from_float_to_Double(fweights);
        install_ospf(g, dweights);
        writeToMyLog("Initial Paths setup done." + "\n");
        log.info("Initial Paths setup done.");
        //---Initial reading of statistics---
        for (int i = 0; i <= 3; i++) {
            get_stats();
        }

        //starting new envirnment with new topology and random weights
        env = new SDNRouting(num_links, num_nodes, g, fweights, links_latency, links_plr, links_util);
        int state_size = 4*num_nodes*num_nodes, action_size = num_links;
        //---Initial env state--- //SDNRouting env , DDPGAgent_PER agent
        Observation env_obsrv = env.reset(fweights, links_latency, links_plr, links_util,switches_pwr);
        State state = env.getState();
        writeToMyLog("Env initial state..." + "\n");
        log.info("Env initial state...");
        //---Initial score---
        episode_score = 0.0f;
        //---Agent---
        log.info("Agent type is "+ agent_type);
        if(agent_type == 0){
            log.info("GAMS Agent is ready...");
        }else if(agent_type ==1 && isRand==1){
            log.info("Random Agent is ready...");
        }else if(agent_type == 2){
            agent_DDPG = new DDPGAgent(state_size, action_size);
            writeToMyLog2("DDPG Agent is ready...\n");
            log.info("DDPG Agent is ready...");
        }
        else if(agent_type == 3){
            agent_PER = new DDPGAgent_PER(state_size, action_size);
            writeToMyLog2("DDPG_PER Agent is ready...\n");
            log.info("DDPG_PER Agent is ready...");
        }
        else if(agent_type == 4 ){
            agent_double = new DDPGAgent_DoublePER(state_size, action_size);
            writeToMyLog2("DDPG_Double Agent is ready...\n");
            log.info("DDPG_Double Agent is ready...");
        }

        writeToMyLog("Agent is ready... with state size "+ state_size+" ,action size "+action_size+ "\n");
        log.info("Agent is ready...");
        int edges_size = g.getEdges().size();
        if(agent_type !=0) {  //if not GAMS then start training and testing
            //---Initial learning---
            Train_Agent(agent_type, isRand);
            // state = env.getState();
            writeToMyLog("Initial Training is DONE \n");
            log.info("Initial Training is DONE...");
            //for(int ld = 20; ld <=100 ; ld += 20) {
            for (int i = 0; i < 10; i++) {//while(True)//40
                g = topologyService.getGraph(topologyService.currentTopology());
                int new_edges_size = g.getEdges().size();
                log.info("Number of Links " + new_edges_size);
                //---- Run learned model ----
                writeToMyLog2("RUNNING AGENT!!!! \n");
                log.info("RUNNING AGENT!!!!");
                Run_Agent(agent_type, isRand);
                // log.info("RUNNING AGENT!!!! run#"+i+" DONE");

            }
            // writeToMyLog2("########## LOAD OF "+ ld +" % ########## \n");
            writeToMyLog2("\n########## AVERAGE RESULTS ########## \n");
            writeToMyLog2("AVG Link Latency " + avg_value(avg_link_lat) + " ms \n");
            writeToMyLog2("AVG End-to-End Latency " + avg_value(avg_e2e_lat) + " ms \n");
            writeToMyLog2("AVG Link PLR " + avg_value(avg_plr) + " \n");
            writeToMyLog2("AVG Agent time " + avg_value(avg_agnt_time) + " ms \n");
            writeToMyLog2("AVG Throughput " + avg_value(avg_troughput) + " bps \n");
            writeToMyLog2("AVG Power " + avg_value(av_pwr) + " W \n");
            avg_link_lat.clear();//new ArrayList<Double>()
            avg_e2e_lat.clear();
            avg_plr.clear();
            avg_agnt_time.clear();
            avg_troughput.clear();
            av_pwr.clear();
            // }

            //----Running learned model-------
            for (int ld = 20; ld <= 100; ld += 20) {
                for (int i = 0; i < 20; i++) {//while(True)//40
                    g = topologyService.getGraph(topologyService.currentTopology());
                    int new_edges_size = g.getEdges().size();
                    log.info("Number of Links " + new_edges_size);
                    //---- Detect link failure ----
                    if (new_edges_size != edges_size) {
                        writeToMyLog("LINK FAILURE!!!!\n");
                        log.info("LINK FAILURE!!!!");
                        //---- Run on OSPF ----
                        writeToMyLog2("\n\n Results after Link Failure \n\n");
                        log.info("Results after Link Failure ");
                        for (int r = 0; r < 6; r++) {
                            results(agent_type, isRand);
                        }

                        writeToMyLog2("\n\n");
                        drop_links(4, 5);
                        //Action action = new Action(env_obsrv.getState(), fweights, env.get_num_links(),env.get_active_links());
                        //temp_OSPF (action,g);
                        //---- Retrain until convergence ----
                        log.info("Re-Training is STARTED...");

                        //epi = new String[NUM_EPISODES / WINDOW_SIZE];
                        //Re-Train Agent method that trains until convergence (still needs work)
                        double start_time = System.nanoTime();
                        ReTrain_Agent(agent_type, isRand);
                        double end_time = System.nanoTime();
                        writeToMyLog("Re-Training is DONE \n");
                        log.info("Re-Training is DONE...");
                        edges_size = new_edges_size;
                        double retraining_time = (end_time - start_time) / 1e6;
                        writeToMyLog2("\n\nResults after Re-Training \n");
                        writeToMyLog2("Re-training time " + retraining_time + "ms\n");
                        results(agent_type, isRand);

                    } else {
                        //---- Run learned model ----
                        writeToMyLog2("RUNNING AGENT!!!! \n");
                        log.info("RUNNING AGENT!!!! run#" + i + " LOAD " + ld + " %");
                        Run_Agent(agent_type, isRand);
                        // log.info("RUNNING AGENT!!!! run#"+i+" DONE");
                    }
                }
                writeToMyLog2("########## LOAD OF " + ld + " % ########## \n");
                writeToMyLog2("\n########## AVERAGE RESULTS ########## \n");
                writeToMyLog2("AVG Link Latency " + avg_value(avg_link_lat) + " ms \n");
                writeToMyLog2("AVG End-to-End Latency " + avg_value(avg_e2e_lat) + " ms \n");
                writeToMyLog2("AVG Link PLR " + avg_value(avg_plr) + " \n");
                writeToMyLog2("AVG Agent time " + avg_value(avg_agnt_time) + " ms \n");
                writeToMyLog2("AVG Throughput " + avg_value(avg_troughput) + " bps \n");
                writeToMyLog2("AVG Power " + avg_value(av_pwr) + " W \n");
                avg_link_lat.clear();//new ArrayList<Double>()
                avg_e2e_lat.clear();
                avg_plr.clear();
                avg_agnt_time.clear();
                avg_troughput.clear();
                av_pwr.clear();
            }
        }else{
            run_gams();
        }
    }
    public void Train_Agent (int agent_type,int isRand){
        //run agent
        //constantly detect topology
        //if link faild
        //restart training
        //stop when converging (isReTrain, then break if converge)
        State state = env.getState();
        num_packet_in = new double[deviceService.getDeviceCount() + 1];
        num_flow_mod = new double[deviceService.getDeviceCount() + 1];

        for (int episode = 1; episode < NUM_EPISODES; episode++) {
            log.info("EPISODE "+ episode);

            //---Read stats---
            get_stats();
            get_stats();
            //---Random start---
            env_obsrv = env.reset(fweights, links_latency, links_plr, links_util,switches_pwr);
            //writeToMyLog("Env Reset with new stats..." + "\n");
            //log.info("Env Ready Reset with new stats...");
            episode_score = 0.0f;
            if(agent_type == 2){
                agent_DDPG.reset();
                log.info("resetting DDPG agent");
            }
            else if(agent_type == 3){
                agent_PER.reset();
                log.info("resetting DDPG_PER agent");
            }
            else if(agent_type == 4){
                agent_double.reset();
                log.info("resetting DDPG_Double agent");
            }

            //---Intial state---
            state = env_obsrv.getState();
            Action action = new Action(env_obsrv.getState(), fweights, env.get_num_links(),env.get_active_links());
            for (int t = 1; t < 10; t++) {//t<1000
                log.info("EPISODE "+ episode+" -- Step "+t);
                //---ACTION---
                if(agent_type == 1){
                    action.set_rand_const_links_weights(rand, env.get_active_links(),env.get_num_links(),isRand);

                    //log.info("Random Action");
                }
                else if(agent_type == 2){
                    action = agent_DDPG.act(state, true, env.get_active_links());
                    //log.info("DDPG Action");
                }
                else if(agent_type == 3){
                    action = agent_PER.act(state, true, env.get_active_links());
                   // log.info("DDPG_PER Action");
                }
                else if(agent_type == 4){
                    action = agent_double.act(state, true, env.get_active_links());
                    // log.info("DDPG_Double Action");
                }
                //---Install new Paths---
                fweights = action.get_links_weights();
                dweights = from_float_to_Double(fweights);
                weight_matrix = dweights;
                //Copy previous paths
                copy_current_paths();
                //get newpaths using OSPF and Install new Paths
                install_ospf(g, dweights);
                //Calculate flow modifications
                calculate_flow_mod();
                wait(1000);//1sec
                //Read new stats after some traffic
                get_stats();
                get_stats();
                //---STEP---
                //preform a step with new weights
                env_obsrv = env.step(action, links_latency, links_plr, links_util,switches_pwr);
                //---NEXT STATE---
                State next_state = env_obsrv.getState();
                //---DONE---
                boolean done = env_obsrv.isFinal();
                //---REWARD---
                float reward = env_obsrv.getReward();
                //---Agent step---
               if(agent_type ==2){
                    agent_DDPG.step(state, action, reward, next_state, done);
                   //log.info("DDPG step");
                }
                else if(agent_type ==3){
                    agent_PER.step(state, action, reward, next_state, done,t);
                   //log.info("DDPG_PER step");
                }
                else if(agent_type ==4){
                    agent_double.step(state, action, reward, next_state, done,t);
                   //log.info("DDPG_Double step");
                }

                //---Add reward---
                episode_score += reward;
                //---update the current state---
                state = next_state;
                //wait(1000);//1sec

            }
            sampled_rewards.add((double) episode_score);
            log.info("EPISODE "+episode+"--- SAMPLED REWARD "+episode_score+"--- AVG REWARD "+Math.abs(avg_reward(sampled_rewards, episode)));
            //writeToMyLog("EPISODE "+episode+"--- SAMPLED REWARD "+episode_score+"--- AVG REWARD "+avg_r)
            if (episode % WINDOW_SIZE == 0) {
                //calculating average over 100 episodes
                // writeToMyLog("CALCULATING AVG REWARD... "+ "\n\n");
                //  log.info("CALCULATING AVG REWARD... ");
                double avg_r = Math.abs(avg_reward(sampled_rewards, episode));
                //  writeToMyLog("EPISODE "+i+"--- SAMPLED REWARD "+samp_reward+"--- AVG REWARD "+avg_r+ "\n\n");
                //  log.info("EPISODE "+i+"--- SAMPLED REWARD "+samp_reward+"--- AVG REWARD "+avg_r);
                epi[episode / WINDOW_SIZE] = "EPISODE " + episode + "--- SAMPLED REWARD " + episode_score + "--- AVG REWARD " + avg_r + "\n";
                writeToMyLog(epi[episode / WINDOW_SIZE]+"\n");
                //storing average reward
                avg_rewards[episode / WINDOW_SIZE] = avg_r;
                if (avg_r > best_avg_reward) {
                    best_avg_reward = avg_r;
                }
                //writeToMyLog("Episode "+i+" --- Best Avg reward "+best_avg_reward+ "\n");
                //log.info("Episode "+i+" --- Best Avg reward "+best_avg_reward);
            }
            //wait(1000);//1sec

        }
       /* try{
            Socket socket=new Socket("localhost",2004);
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            DataInputStream din=new DataInputStream(socket.getInputStream());
            out.println("Stop");
            out.flush();
            log.info("Sending to Stop Training");
            out.close();
            din.close();
            socket.close();
        }
        catch(Exception e){
            e.printStackTrace();}*/

        writeToMyLog2("Results \n");

        for(int i = 0 ;i<epi.length ;i+=1){//i+=20
            writeToMyLog2(epi[i]+"\n");
        }
        writeToMyLog2("Best average reward "+ best_avg_reward+"\n");
        results(agent_type,isRand);
        env.plotTrainingData(NUM_EPISODES,WINDOW_SIZE,avg_rewards);

    }
    public void Run_Agent (int agent_type,int isRand){
        if(agent_type==1){
            env_obsrv = env.reset(fweights, links_latency, links_plr, links_util,switches_pwr);
        }
        State state = env_obsrv.getState();
        //---ACTION---
        Action action = new Action(env_obsrv.getState(), fweights, env.get_num_links(),env.get_active_links());
        if(agent_type ==1){
            action.set_rand_const_links_weights(rand, env.get_active_links(),env.get_num_links(),isRand);
            log.info("Random Action");
        }
        else if(agent_type ==2){
            action = agent_DDPG.act(state, true, env.get_active_links());
            log.info("DDPG Action");
        }
        else if(agent_type ==3){
            action = agent_PER.act(state, true, env.get_active_links());
            log.info("DDPG_PER Action");
        }
        else if(agent_type ==4){
            action = agent_double.act(state, true, env.get_active_links());
            log.info("DDPG_Double Action");
        }
        //---Install new Paths---
        fweights = action.get_links_weights();
        dweights = from_float_to_Double(fweights);
        weight_matrix = dweights;
        //get newpaths using OSPF and Install new Paths

        //Copy previous paths
        copy_current_paths();
        //get newpaths using OSPF and Install new Paths
        install_ospf(g, dweights);
        //Calculate flow modifications
        calculate_flow_mod();

        //install_ospf(g, dweights);
        //log.info("Done OSPF");
        wait(1000);//1sec
        //Read new stats after some traffic
        get_stats();
       // log.info("Done stat");
        //---STEP---
        //preform a step with new weights
        env_obsrv = env.step(action, links_latency, links_plr, links_util,switches_pwr);
       // log.info("Done env step");
        //---NEXT STATE---
        State next_state = env_obsrv.getState();
        //---DONE---
        boolean done = env_obsrv.isFinal();
        //---REWARD---
        float reward = env_obsrv.getReward();
        //---Agent step---
        if(agent_type ==2){
            agent_DDPG.step(state, action, reward, next_state, done);
            //log.info("DDPG Step");
        }
        else if(agent_type ==3){
            agent_PER.step(state, action, reward, next_state, done,1);
            //log.info("DDPG_PER Step");
        }
        else if(agent_type ==4){
            agent_double.step(state, action, reward, next_state, done,1);
           // log.info("DDPG_Double Step");
        }

        double[] res = results(agent_type,isRand);
        avg_link_lat.add(res[0]);
        avg_e2e_lat.add(res[1]);
        avg_plr.add(res[2]);
        avg_agnt_time.add(res[3]);
        avg_troughput.add(res[4]);
        av_pwr.add(res[5]);
        //log.info("Done results");
    }
    public void drop_links(int src,int dst){
        weight_matrix[src][dst] = 0.0;//weight_matrix[p - 1][q - 1] = w;
        weight_matrix[dst][src] = 0.0;

        links_latency [src][dst] = 0.0f;//weight_matrix[p - 1][q - 1] = w;
        links_latency [dst][src] = 0.0f;

        links_plr [src][dst] = 0.0f;//weight_matrix[p - 1][q - 1] = w;
        links_plr [dst][src] = 0.0f;

        links_util [src][dst] = 0.0f;//weight_matrix[p - 1][q - 1] = w;
        links_util [dst][src] = 0.0f;

    }
    public void ReTrain_Agent (int agent_type,int isRand){
        //run agent
        //constantly detect topology
        //if link faild
        //restart training
        //stop when converging (isReTrain, then break if converge)
        State state = env.getState();
        episode_score = 0.0f;
        sampled_rewards.clear();
        boolean needsTraining = true;
        num_packet_in = new double[deviceService.getDeviceCount() + 1];
        num_flow_mod = new double[deviceService.getDeviceCount() + 1];
        double avg_r =  0.0;
        ArrayList<String> epi_reward = new ArrayList<String>();
        ArrayList<Double> avg_rwd= new ArrayList<Double>();

        for (int episode = 1; episode < NUM_EPISODES && needsTraining; episode++) {
            log.info("EPISODE "+ episode);

            //---Read stats---
            get_stats();
            //---Random start---
            env_obsrv = env.reset(fweights, links_latency, links_plr, links_util, switches_pwr);
            //writeToMyLog("Env Reset with new stats..." + "\n");
            //log.info("Env Ready Reset with new stats...");
            episode_score = 0.0f;
            if(agent_type == 2){
                agent_DDPG.reset();
                log.info("resetting DDPG agent");
            }
            else if(agent_type == 3){
                agent_PER.reset();
                log.info("resetting DDPG_PER agent");
            }
            else if(agent_type == 4){
                agent_double.reset();
                log.info("resetting DDPG_Double agent");
            }

            //---Intial state---
            state = env_obsrv.getState();
            Action action = new Action(env_obsrv.getState(), fweights, env.get_num_links(),env.get_active_links());
            for (int t = 1; t < 10; t++) {//t<1000
                log.info("EPISODE "+ episode+" -- Step "+t);
                //---ACTION---
                if(agent_type == 1){
                    action.set_rand_const_links_weights(rand, env.get_active_links(),env.get_num_links(),isRand);

                    log.info("Random Action");
                }
                else if(agent_type == 2){
                    action = agent_DDPG.act(state, true, env.get_active_links());
                    log.info("DDPG Action");
                }
                else if(agent_type == 3){
                    action = agent_PER.act(state, true, env.get_active_links());
                    log.info("DDPG_PER Action");
                }
                else if(agent_type == 4){
                    action = agent_double.act(state, true, env.get_active_links());
                    log.info("DDPG_Double Action");
                }
                //---Install new Paths---
                fweights = action.get_links_weights();
                dweights = from_float_to_Double(fweights);
                weight_matrix = dweights;
                //Copy previous paths
                copy_current_paths();
                //get newpaths using OSPF and Install new Paths
                install_ospf(g, dweights);
                //Calculate flow modifications
                calculate_flow_mod();
                wait(1000);//1sec
                //Read new stats after some traffic
                get_stats();
                //---STEP---
                //preform a step with new weights
                env_obsrv = env.step(action, links_latency, links_plr, links_util, switches_pwr);
                //---NEXT STATE---
                State next_state = env_obsrv.getState();
                //---DONE---
                boolean done = env_obsrv.isFinal();
                //---REWARD---
                float reward = env_obsrv.getReward();
                //---Agent step---
                if(agent_type ==2){
                    agent_DDPG.step(state, action, reward, next_state, done);
                    log.info("DDPG step");
                }
                else if(agent_type ==3){
                    agent_PER.step(state, action, reward, next_state, done,t);
                    log.info("DDPG_PER step");
                }
                else if(agent_type ==4){
                    agent_double.step(state, action, reward, next_state, done,t);
                    log.info("DDPG_Double step");
                }
                //---Add reward---
                episode_score += reward;
                //---update the current state---
                state = next_state;
            }
            sampled_rewards.add((double) episode_score);
            log.info("EPISODE "+episode+"--- SAMPLED REWARD "+episode_score+"--- AVG REWARD "+Math.abs(avg_reward(sampled_rewards, episode)));
            //writeToMyLog("EPISODE "+episode+"--- SAMPLED REWARD "+episode_score+"--- AVG REWARD "+avg_r)
            if (episode % WINDOW_SIZE == 0) {
                //calculating average over 100 episodes
                double prev_avg = avg_r;
                avg_r = Math.abs(avg_reward(sampled_rewards, episode));
                epi_reward.add( "EPISODE " + episode + "--- SAMPLED REWARD " + episode_score + "--- AVG REWARD " + avg_r + "\n");
                writeToMyLog(epi_reward.get(epi_reward.size()-1)+"\n");
                //storing average reward
                avg_rwd.add(avg_r);
                double delta_reward  = Math.abs(avg_r - prev_avg);
                if (delta_reward < 2 && avg_r > prev_avg){
                    needsTraining = false;
                    log.info("NeedsTraining = FALSE");
                }
            }
        }
        writeToMyLog2("\n ReTraining Results \n");

        for(int i = 0 ;i < epi_reward.size() ;i+=1){//i+=5
            writeToMyLog2(epi_reward.get(i)+"\n");
        }

        results(agent_type,isRand);
        env.plotTrainingData2(avg_rwd);

    }


    /**
     *
     * @param agent_type
     * @return double array [ 0: latency perlink , 1: end-to-end latency,
     *                        2: avg.plr         , 3: agent time,
     *                        4: throughput      , 5: power]
     */
    public double[] results (int agent_type, int isRand){
        double[] res = new double[6];
        res[0] = test_latency();res[1] = test_latency2(g ,weight_matrix);
        res[2] = test_avg_plr(env);

        double[] tt = test_time_Agent(agent_type,isRand) ;

        res[3] = tt[0];res[4] = tt[1];
        res[5] = avg_Power();
        writeToMyLog2("Average latency per link "+res[0]+" ms\n");
        writeToMyLog2("Average end-to-end latency "+res[1]+" ms \n");
        writeToMyLog2("Average Packet Loss ratio per link "+res[2]+"\n");
        writeToMyLog2("Average path generation time "+tt[0]+" ms"+" Throughput "+tt[1]+" bps \n");
        writeToMyLog2("Average Power consumed by network "+res[5]+" W\n\n");
        return res;
    }

    private double avg_value(ArrayList<Double> arr){
        double sum=0.0;
        for(double d:arr){
            sum+=d;
        }
        return sum/arr.size();
    }

    /**
     * Generates XML of the topology for GAMS to read it
     * Reads the results from GAMS
     * Install paths on ONOS
     */
    public void run_gams(){
        if(deviceService.getDeviceCount()==12) {
            log.info("*** STARTING XML ***");
            XMLGenerator xl_gen = new XMLGenerator();
            log.info("*** DONE XML *** Waiting for GAMS");
            wait(5000);//5sec
            log.info("*** DONE GAMS ***");
            List<int[]> nodesList = xl_gen.read_gams_output();
            install_gams_sol(nodesList);
            log.info("*** DONE with Paths ***");
            //results (1, 0);


            for (int i = 0; i < 10; i++) {//while(True)//40
                g = topologyService.getGraph(topologyService.currentTopology());
                int new_edges_size = g.getEdges().size();
                log.info("Number of Links " + new_edges_size);
                //---- Run learned model ----
                writeToMyLog2("RUNNING AGENT!!!! \n");
                log.info("RUNNING AGENT!!!!");
                double[] res = results(0,0);
                avg_link_lat.add(res[0]);
                avg_e2e_lat.add(res[1]);
                avg_plr.add(res[2]);
                avg_agnt_time.add(res[3]);
                avg_troughput.add(res[4]);
                av_pwr.add(res[5]);

            }
            // writeToMyLog2("########## LOAD OF "+ ld +" % ########## \n");
            writeToMyLog2("\n########## AVERAGE RESULTS ########## \n");
            writeToMyLog2("AVG Link Latency " + avg_value(avg_link_lat) + " ms \n");
            writeToMyLog2("AVG End-to-End Latency " + avg_value(avg_e2e_lat) + " ms \n");
            writeToMyLog2("AVG Link PLR " + avg_value(avg_plr) + " \n");
            writeToMyLog2("AVG Agent time " + avg_value(avg_agnt_time) + " ms \n");
            writeToMyLog2("AVG Throughput " + avg_value(avg_troughput) + " bps \n");
            writeToMyLog2("AVG Power " + avg_value(av_pwr) + " W \n");
            avg_link_lat.clear();//new ArrayList<Double>()
            avg_e2e_lat.clear();
            avg_plr.clear();
            avg_agnt_time.clear();
            avg_troughput.clear();
            av_pwr.clear();

        }
    }
////////////////////////// Helper methods ///////////////////////////////
    public float[][] from_Double_to_float(double[][] dweights) {
        float[][] fweights = new float[dweights.length][dweights[0].length];
        for (int i = 0; i < dweights.length; i++) {
            for (int j = 0; j < dweights[i].length; j++) {
                fweights[i][j] = (float) dweights[i][j];
            }
        }
        return fweights;
    }

    public double[][] from_float_to_Double(float[][] fweights) {
        double[][] dweights = new double[fweights.length][fweights[0].length];
       // writeToMyLog("From float to Double \n");

        for (int i = 0; i < fweights.length; i++) {
            for (int j = 0; j < fweights[i].length; j++) {
                dweights[i][j] = (double) fweights[i][j];
                //writeToMyLog(dweights[i][j]+", ");

            }
            //writeToMyLog(" \n");
        }
        //writeToMyLog(" \n");
        return dweights;
    }
//////////////////////// Performing OSPF /////////////////////////
    public void install_ospf(TopologyGraph g, double[][] weights) {
        OSPF ospf = new OSPF(g, weights, 1);
        String[] nodes = ospf.getNodes();
        //writeToMyLog("OSPF -- install_ospf: "+Arrays.toString(nodes)+" \n");
        //writeToMyLog("OSPF -- install_ospf: STARTING TO INSTALL PATHS \n");
        for (int i = 0; i < nodes.length; i++) {
            //writeToMyLog("OSPF -- install_ospf: " + nodes[i] + "\n");
            for (int j = 0; j < nodes.length; j++) {
                int src = Integer.parseInt(nodes[i]);
                int dst = Integer.parseInt(nodes[j]);
                if (src != dst) {
                    //writeToMyLog("OSPF -- install_ospf: INSTALL PATH..." + src + " and " + dst + " \n");
                    installPath3(src, dst, ospf);
                }
            }
        }
        current_paths = ospf.getAllPaths();
        //writeToMyLog("OSPF -- install_ospf: DONE \n");
        writeToMyLog("OSPF -- install_ospf: DONE \n");
    }

    public void install_arpnet() {
        TopologyGraph g = topologyService.getGraph(topologyService.currentTopology());
        OSPF ospf = new OSPF(g, weight_matrix, 1);
        int[] srcs = {1,1,1,2,2,3,3,4,4,5,6,6,7,8,8,8,9,9,10,11,12,12,13,13,14,14,15,16,16,17,17,19};
        int[] dsts = {2,3,6,4,5,7,8,5,11,6,7,8,10,9,10,12,10,11,19,13,14,18,15,17,15,18,16,18,19,19,20,20};
        //writeToMyLog("OSPF -- install_ospf: STARTING TO INSTALL PATHS \n");
        for (int i = 0; i < srcs.length; i++) {

                int src = srcs[i];
                int dst = dsts[i];
                installPath3(src, dst, ospf);
                installPath3(dst, src, ospf);

        }


        writeToMyLog("OSPF -- install_arpnet: DONE \n");
    }
    public void install_nsfnet() {
        TopologyGraph g = topologyService.getGraph(topologyService.currentTopology());
        OSPF ospf = new OSPF(g, weight_matrix, 1);
        int[] srcs = {1,1,1,2,2,3,4,4,5,5,6,6,7,8,9,9,9,11,11,12,14};
        int[] dsts = {2,3,8,4,3,6,5,11,7,6,13,10,8,9,14,12,10,12,14,13,13};
        //writeToMyLog("OSPF -- install_ospf: STARTING TO INSTALL PATHS \n");
        for (int i = 0; i < srcs.length; i++) {
            int src = srcs[i];
            int dst = dsts[i];
            if (src != dst) {
                //writeToMyLog("OSPF -- install_ospf: INSTALL PATH..." + src + " and " + dst + " \n");
                installPath3(src, dst, ospf);
            }
        }
        writeToMyLog("OSPF -- install_nsfnet: DONE \n");
    }

    public void install_abliene() {
        TopologyGraph g = topologyService.getGraph(topologyService.currentTopology());
        OSPF ospf = new OSPF(g, weight_matrix, 1);
        int[] srcs = {1,2,2,2,3,3,4,4,4,5,5,6,8,9,10};
        int[] dsts = {2,5,6,12,6,9,7,10,11,7,8,7,10,12,11};
        //writeToMyLog("OSPF -- install_ospf: STARTING TO INSTALL PATHS \n");
        for (int i = 0; i < srcs.length; i++) {
            int src = srcs[i];
            int dst = dsts[i];
            if (src != dst) {
                //writeToMyLog("OSPF -- install_ospf: INSTALL PATH..." + src + " and " + dst + " \n");
                installPath3(src, dst, ospf);
            }
        }
        writeToMyLog("OSPF -- install_abliene: DONE \n");
    }

    public void install_gams_sol(List<int[]> all_paths){
        TopologyGraph g = topologyService.getGraph(topologyService.currentTopology());
        OSPF ospf = new OSPF(g, weight_matrix, 1);

        writeToMyLog("OSPF -- install_ospf: STARTING TO INSTALL PATHS for GAMS\n");
        writeToMyLog("ALL PATHS size "+ all_paths.size()+"\n");
        for (int i = 0; i < all_paths.size(); i++) {
            writeToMyLog("patth size "+ all_paths.get(i).length);
            int[] path = all_paths.get(i);
            for (int j = 0; j < all_paths.get(i).length-1; j++){
                int src = path[j];
                int dst = path[j+1];
                installPath3(src, dst, ospf);
                installPath3(dst, src, ospf);
            }



        }
    }

    /**
     * Copying the current paths installed in flow tables to the previous_path list
     */
    private void copy_current_paths (){
        previous_paths.clear();
        for(int i = 0 ; i<current_paths.size();i++){
            previous_paths.add(current_paths.get(i));
        }

    }


    public double avg_reward(ArrayList<Double> rewards, int e) {
        double sum = 0.0;
        for (double d : rewards) {
            sum += d;
        }
        return (sum / (e * 1.0));//rewards.size()
    }
    public void wait(int t) {
        try {
            //writeToMyLog("Wait for "+t+"sec \n");
            Thread.sleep((long) t);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
///////////////////////// Installing Paths ///////////////////////////

    //find edge switches connected to the hosts
    private int findEdge(String node) {
        int nodeSW = -1;
        Set<Host> nodeHost = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(node)));//uses IP address to access host
        for (Host element : nodeHost) {
            if (element.ipAddresses().toString().contains(node))
                nodeSW = Integer.parseInt(element.location().deviceId().toString().substring(element.location().deviceId().toString().indexOf(':') + 1), 16);
        }
        return nodeSW;
    }



    /**
     * Install paths in flow tables of switches based on OSPF
     * @param srcSW number of source switch
     * @param dstSW number of destination switch
     * @param op OSPF algorithm
     */
    private void installPath3(int srcSW, int dstSW, OSPF op) {
        //find edge switches connected to the hosts

        //////// I stopped here 12/12/2021/////
        ////// Create weights matrix , find paths using OPSF , install flow rules///////
        if (srcSW != -1 && dstSW != -1) {
            String path = "\n";
            double[][] matrix = Int2Double(Node_Matrix);
            op.start(srcSW, dstSW);
            //writeToMyLog(" srcSW "+srcSW+" dstSW "+dstSW+"\n");
            if (op.getFindPath()) {
                int[] mypathstr = op.getPathstr();
                int[] revmypathstr = op.getRevPathstr();

                for (int i = 0; i < mypathstr.length; i++) {
                    path += String.valueOf((mypathstr[i])) + "->";

                }
                path += "\n";
                //log.info(path);
                //me
              // writeToMyLog(path + " ");

                List<Link> mypath = new LinkedList<>();
                List<Link> revmypath = new LinkedList<>();
                for (int i = 0; i < mypathstr.length - 1; i++) {
                    Iterable<Link> links = linkService.getActiveLinks();
                    //  StatisticService statisticService=StatisticService.class);
                    int finalI = i;
                    links.forEach((element) -> {
                        // log.info( element.toString()  +  "  type=  "  +  element.type()  +   "  state= "  +  element.state()  +  "  src= "  +  element.src()  +  "  dst "  +  element.dst());
                        //log.info( "Load(byte/sec)= "  +  statisticService.load(element).rate());
                        int p = Integer.parseInt(element.src().toString().substring(element.src().toString().indexOf(':') + 1, element.src().toString().indexOf('/')), 16);
                        int q = Integer.parseInt(element.dst().toString().substring(element.dst().toString().indexOf(':') + 1, element.dst().toString().indexOf('/')), 16);
                        if (p == mypathstr[finalI] && q == mypathstr[finalI + 1]) {
                            mypath.add(element);
                        }
                        if (p == revmypathstr[finalI] && q == revmypathstr[finalI + 1])
                            revmypath.add(element);
                    });
                }

                if (mypath != null && revmypath != null) {
                    String src_ip = "10.0.0." + srcSW;
                    String dst_ip = "10.0.0." + dstSW;
                   // writeToMyLog("IP addresses "+ src_ip+"  , "+ dst_ip+"\n");
                    //me
                    //writeToMyLog("My path is = " + mypath.toString() + "\n");
                    //writeToMyLog("Number of switches in path " + (mypath.size() + 1) + " Number of links " + mypath.size() + "\n\n");

                    //log.info("My path is = " + mypath.toString());

                    // install on intermediate nodes
                    mypath.forEach((element) -> {
                        installRule(element.src().deviceId(), element.src().port(), src_ip, dst_ip);
                    });
                    //install on the end egress nodes
                    Set<Host> dstHost1 = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(dst_ip)));
                    dstHost1.forEach((element) -> {
                        if (element.ipAddresses().toString().contains(dst_ip)) {
                            installRule(element.location().deviceId(), element.location().port(), src_ip, dst_ip);
                        }
                    });
                    //me
                   // writeToMyLog("My Reverse path is=" + revmypath.toString() + "\n");
                    //log.info("My Reverse path is=" + revmypath.toString());

                    // install on intermediate nodes
                    revmypath.forEach((element) -> {
                        installRule(element.src().deviceId(), element.src().port(), dst_ip, src_ip);
                    });
                    //install on the end egress nodes
                    Set<Host> srcHost2 = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(src_ip)));
                    srcHost2.forEach((element) -> {
                        if (element.ipAddresses().toString().contains(src_ip)) {
                            installRule(element.location().deviceId(), element.location().port(), dst_ip, src_ip);
                        }
                    });
                } else
                    //me
                    writeToMyLog("There is not a valid path" + "\n");
                //log.info("There is not a valid path");
            }
        } else
            //me
            writeToMyLog("Unable to find edge switches" + "\n");
            //log.info("Unable to find edge switches");
    }


    private void installRule(DeviceId devid, PortNumber portNumber, String src, String dst) {
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        Set<Host> dstHost = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(dst)));
        Set<Host> srcHost = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(src)));
        MacAddress srcmac = MacAddress.BROADCAST;
        MacAddress dstmac = MacAddress.BROADCAST;
        for (Host element : dstHost) {
            if (element.ipAddresses().toString().contains(dst))
                dstmac = element.mac();
        }

        for (Host element : srcHost) {
            if (element.ipAddresses().toString().contains(src))

                srcmac = element.mac();
        }
        selectorBuilder.matchEthSrc(srcmac).matchEthDst(dstmac);
        Ip4Prefix matchIp4SrcPrefix =
                Ip4Prefix.valueOf(IPv4.toIPv4Address(src),
                        Ip4Prefix.MAX_MASK_LENGTH);
        Ip4Prefix matchIp4DstPrefix =
                Ip4Prefix.valueOf(IPv4.toIPv4Address(dst),
                        Ip4Prefix.MAX_MASK_LENGTH);
        selectorBuilder.matchEthType(Ethernet.TYPE_IPV4);
        selectorBuilder.matchIPSrc(matchIp4SrcPrefix)
                .matchIPDst(matchIp4DstPrefix);
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(portNumber)
                .build();

        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(flowPriority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                // .makeTemporary(flowTimeout)
                .makePermanent()
                .add();
        flowObjectiveService.forward(devid, forwardingObjective);

    }



    ///////////////////////////// Packet Probing ////////////////////////
    /**
     * Sending prob packets to measure latency
     *
     * @param devid
     * @param portNumber
     * @param src
     * @param dst
     */
    private void send_prob(DeviceId devid, PortNumber portNumber, String src, String dst) {
        // writeToMyLog("send_prob -- Preparing prob packet...\n");
        // log.info("Preparing prob packet....");
        // Step 1: Preparing SRC and DST Mac address
        //  writeToMyLog("send_prob -- src "+src+" , dst "+dst+" \n");
        //  log.info("send_prob -- src "+src+ " , dst "+dst);
        Set<Host> dstHost = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(dst)));
        Set<Host> srcHost = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(src)));
        MacAddress srcmac = MacAddress.BROADCAST;
        MacAddress dstmac = MacAddress.BROADCAST;
        for (Host element : dstHost) {
            if (element.ipAddresses().toString().contains(dst))
                dstmac = element.mac();
                dst_mac = element.mac();
        }

        for (Host element : srcHost) {
            if (element.ipAddresses().toString().contains(src))

                srcmac = element.mac();
                src_mac = element.mac();
        }
        //   writeToMyLog("send_prob -- srcMAC "+srcmac+" , dstMAC "+dstmac+" \n");
        //   log.info("send_prob -- srcMAC "+srcmac+ " , dstMAC "+dstmac);
        // Step 2:Preparing IP payload
        IPv4 ip = new IPv4();
        ip.setDestinationAddress(dst);
        ip.setSourceAddress(src);
        UDP udp = new UDP();
        ip.setPayload(udp);
        //    writeToMyLog("send_prob -- ip src "+ip.getSourceAddress()+" , dst "+ip.getDestinationAddress()+" \n");
        //   log.info("send_prob -- ip src "+ip.getSourceAddress()+ " , dst "+ip.getDestinationAddress());
        // Step 3:Preparing ethernet
        Ethernet ethReply = new Ethernet();
        ethReply.setSourceMACAddress(srcmac);
        ethReply.setDestinationMACAddress(dstmac);
        ethReply.setEtherType(Ethernet.TYPE_IPV4);
        ethReply.setPayload(ip);

        //ConnectPoint targetPort = context.inPacket().receivedFrom();
        // Step 4:Preparing Traffic treatment
        TrafficTreatment t = DefaultTrafficTreatment.builder()
                .setOutput(PortNumber.TABLE).build();
        // Step 5:Preparing outbound Packet
        OutboundPacket o = new DefaultOutboundPacket(
                devid, t, ByteBuffer.wrap(ethReply.serialize()));

        // Step 6:Sending outbound Packet
        packetService.emit(o);

        //sent_time = System.currentTimeMillis();
        sent_time = System.nanoTime();
        wait(100);
        //   writeToMyLog("send_prob -- probe packet is sent "+sent_time+"\n");
        //   log.info("probe packet is sent: " + sent_time);
    }

    private void get_Mac(String src, String dst) {
        Set<Host> dstHost = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(dst)));
        Set<Host> srcHost = hostService.getHostsByIp(IpAddress.valueOf(IPv4.toIPv4Address(src)));

        for (Host element : dstHost) {
            if (element.ipAddresses().toString().contains(dst))
                dst_mac = element.mac();
        }

        for (Host element : srcHost) {
            if (element.ipAddresses().toString().contains(src))

                src_mac = element.mac();
        }

    }

    private void test_prob(int src, int dst) {
        //writeToMyLog("test_prob -- in side test prob\n");
        //log.info("test_prob -- in side test prob " );
        Iterable<Link> links = linkService.getActiveLinks();

        LinkedList<Link> myLink = new LinkedList<Link>();
        links.forEach((element) -> {
            int p = Integer.parseInt(element.src().toString().substring(element.src().toString().indexOf(':') + 1, element.src().toString().indexOf('/')), 16);
            int q = Integer.parseInt(element.dst().toString().substring(element.dst().toString().indexOf(':') + 1, element.dst().toString().indexOf('/')), 16);
            if (p == src && q == dst) {
                myLink.add(element);
            }
        });
        String src_ip = "10.0.0." + src;
        String dst_ip = "10.0.0." + dst;
        get_Mac(src_ip, dst_ip);

        myLink.forEach((element) -> {
            int p = Integer.parseInt(element.src().toString().substring(element.src().toString().indexOf(':') + 1, element.src().toString().indexOf('/')), 16);
            int q = Integer.parseInt(element.dst().toString().substring(element.dst().toString().indexOf(':') + 1, element.dst().toString().indexOf('/')), 16);
            if (p == src && q == dst) {
                send_prob(element.dst().deviceId(), element.dst().port(), src_ip, dst_ip);
            }
        });
        //installPath2(src_ip, dst_ip);
    }

    private void set_topo_links() {
        //  writeToMyLog("set_topo_links -- Topo_links\n");
        Iterable<Link> links = linkService.getActiveLinks();
        final int[] i = {0};
        LinkedList<Link> myLink = new LinkedList<Link>();
        links.forEach((element) -> {
            int p = Integer.parseInt(element.src().toString().substring(element.src().toString().indexOf(':') + 1, element.src().toString().indexOf('/')), 16);
            int q = Integer.parseInt(element.dst().toString().substring(element.dst().toString().indexOf(':') + 1, element.dst().toString().indexOf('/')), 16);
            topo_links[i[0]] = new int[]{p, q};
            i[0]++;
             //writeToMyLog("set_topo_links -- Topo_link [ "+p+" , "+q+" ]\n");

        });
    }


    private void test_plr(int src, int dst) {
        final String[] src_id = {""};
        final String[] dst_id = {""};
        final double[] src_p = {0.0};
        final double[] dst_p = {0.0};

        final double[] src_u = {0.0};
        final double[] dst_u = {0.0};
        Iterable<Link> links = linkService.getActiveLinks();

        links.forEach((element) -> {
            int p = Integer.parseInt(element.src().toString().substring(element.src().toString().indexOf(':') + 1, element.src().toString().indexOf('/')), 16);
            int q = Integer.parseInt(element.dst().toString().substring(element.dst().toString().indexOf(':') + 1, element.dst().toString().indexOf('/')), 16);
            if (p == src && q == dst) {
                src_id[0] = element.src().deviceId().toString();
                dst_id[0] = element.dst().deviceId().toString();
                // writeToMyLog("test_plr -- src_id " + src_id[0]+" dst_id " + dst_id[0]+ "\n");
            }
        });

        Iterable<Device> devices = deviceService.getDevices();
        devices.forEach((element) -> {

            // writeToMyLog("test_plr -- element_id " + element.id().toString()+ "\n");
            List<PortStatistics> portStatisticsList = deviceService.getPortDeltaStatistics(element.id());
            if (element.id().toString().equals(src_id[0])) {
                for (PortStatistics portdeltastat : portStatisticsList) {
                    if (portdeltastat != null) {
                        src_p[0] += (double) portdeltastat.packetsSent();
                        src_u[0] = (double) portdeltastat.bytesSent();

                        //writeToMyLog("test_plr -- packets sent " + src_p[0] +" bytes sent " + src_u[0] + "\n");
                        //log.info("test_plr -- packets sent " + src_p[0]+" bytes sent " + src_u[0]);

                    }
                }
            } else if (element.id().toString().equals(dst_id[0])) {
                for (PortStatistics portdeltastat : portStatisticsList) {
                    if (portdeltastat != null) {
                        dst_p[0] += (double) portdeltastat.packetsReceived();

                        dst_u[0] = (double) portdeltastat.bytesReceived();
                        //  writeToMyLog("test_plr -- packets dropped " + dst_p[0] + " bytes rev " + dst_u[0] + "\n");
                        //  log.info("test_plr -- packets dropped " + dst_p[0] + " bytes rev " + dst_u[0]);
                    }
                }
            }

        });
        links_plr[src][dst] = (float) Math.abs((src_p[0] - dst_p[0]) / (src_p[0] * 1.0+1.0));

                // links_util[src][dst] = LINK_CAPACITY - (src_u[0] + dst_u[0]);
        links_util[src][dst] = (float) ((src_u[0] + dst_u[0]) / LINK_CAPACITY);

        //  writeToMyLog("\ntest_plr -- Packet Loss Ratio for Link  ["+src+","+dst+"] is "+links_plr[src][dst]+ "\n\n");
        //  log.info("test_plr -- Packet Loss Ratio for Link  ["+src+","+dst+"] is "+links_plr[src][dst]);

        //  writeToMyLog("\ntest_plr -- Link Utilization for Link  ["+src+","+dst+"] is "+links_util[src][dst]+ "\n\n");
        // log.info("test_plr -- Link Utilization for Link  ["+src+","+dst+"] is "+links_util[src][dst]);
    }

    /**
     * calculates latency and plr over all links
     */
    private void get_stats() {
        set_topo_links(); //set links
       // log.info("Done Topo Links ");
        for (int i = 0; i < topo_links.length; i++) {
            int src = topo_links[i][0];
            int dst = topo_links[i][1];
            double s_time = System.currentTimeMillis();
            test_prob(src, dst);//send probs
           // log.info("Done prob ");
            //writeToMyLog("link "+src +" , "+dst+" : "+latency + "\n");
            //log.info("link "+src +" , "+dst+" : "+latency);

            if (latency == 0.0) {
                //writeToMyLog("Sending probs again \n");
                //log.info("Sending probs again ");
                //test_prob(src, dst);//send probs
                //wait(1000);
                //log.info("Before link "+src +" , "+dst+" : "+latency);
                double r_time = System.currentTimeMillis();
                latency = (r_time - s_time);
                log.info("After link "+src +" , "+dst+" : "+latency);

            }
            /*if (latency > 1000.0) {
                latency = 100.0;
            }*/
            links_latency[src][dst] = (float) latency;
            test_plr(src, dst);
            //log.info("Done plr ");
        }
        avg_Power();
        print_link_stat(links_latency,1);
        print_link_stat(links_plr,2);
        print_link_stat(links_util,3);
        print_pwr_stat(switches_pwr);
    }

    ///////////////////////////// Network Stats Calculations ////////////////////////
    private int[][] generate_random_pair(int num_nodes,int num_pairs){
        int[][] src_dst_pair = new int[num_pairs][2];
        int min = 1,max= num_nodes;
       // writeToMyLog2("Random pairs \n");
        for(int i=0 ;i<src_dst_pair.length ; i++){
            src_dst_pair[i][0] = min + (int)(Math.random() * ((max - min) + 1));
            src_dst_pair[i][1] = min + (int)(Math.random() * ((max - min) + 1));
            while(src_dst_pair[i][1] == src_dst_pair[i][0]){
                src_dst_pair[i][1] = min + (int)(Math.random() * ((max - min) + 1));
            }
            //writeToMyLog2("Pair ("+src_dst_pair[i][0]+" , "+src_dst_pair[i][1]+") ");
        }
        //writeToMyLog2("\n");
        return src_dst_pair;
    }

    private double test_latency() {
        double total_latency = 0.0;
        set_topo_links(); //set links

        for (int i = 0; i < topo_links.length; i++) {
            int src = topo_links[i][0];
            int dst = topo_links[i][1];
            double s_time = System.currentTimeMillis();
            test_prob(src, dst);//send probs
            //writeToMyLog("link "+src +" , "+dst+" : "+latency + "\n");
            //log.info("link "+src +" , "+dst+" : "+latency);
            if (latency == 0.0) {//Double.NEGATIVE_INFINITY
                //writeToMyLog("Sending probs again \n");
                //log.info("Sending probs again ");
               // test_prob(src, dst);//send probs
               // wait(1000);
                //log.info("Before link "+src +" , "+dst+" : "+latency);
                double r_time = System.currentTimeMillis();
                latency = (r_time - s_time);
                log.info("After link "+src +" , "+dst+" : "+latency);
            }
            /*if (latency > 1000.0) {
                latency = 100.0;
            }*/
            total_latency += latency;
        }
    return total_latency/topo_links.length;

    }

    private double test_latency2(TopologyGraph g ,double[][] weights  ) {
        double total_latency = 0.0;
        set_topo_links(); //set links
        //Set of src,dst pairs
        int[][] src_dst_pair = generate_random_pair(deviceService.getDeviceCount(),10);
        double[] total_lat = new double[src_dst_pair.length];
        OSPF ospf = new OSPF(g, weights, 1);

        for (int i = 0; i < src_dst_pair.length; i++) {
            int src = src_dst_pair[i][0];
            int dst = src_dst_pair[i][1];
            //path for each pair
            ospf.start(src, dst);
            int[] linkpath = ospf.getPathstr();
            //Latency for path
            for(int p=0 ; p<linkpath.length-1 ; p++){
                /*test_prob(linkpath[p], linkpath[p+1]);//send probs
               // writeToMyLog2("link "+linkpath[p] +" , "+linkpath[p+1]+" : "+latency + "\n");
                //log.info("link "+src +" , "+dst+" : "+latency);
                while (latency == Double.NEGATIVE_INFINITY) {
                    writeToMyLog("Sending probs again \n");
                    log.info("Sending probs again ");
                    test_prob(linkpath[p], linkpath[p+1]);//send probs
                }
                if (latency == 0.0) {
                    latency = 10.0;
                }*/

                double s_time = System.currentTimeMillis();
                test_prob(src, dst);//send probs
                //writeToMyLog("link "+src +" , "+dst+" : "+latency + "\n");
                //log.info("link "+src +" , "+dst+" : "+latency);
                if (latency == 0.0) {//Double.NEGATIVE_INFINITY
                    //writeToMyLog("Sending probs again \n");
                    //log.info("Sending probs again ");
                    // test_prob(src, dst);//send probs
                    // wait(1000);
                    //log.info("Before link "+src +" , "+dst+" : "+latency);
                    double r_time = System.currentTimeMillis();
                    latency = (r_time - s_time);
                    log.info("After link "+src +" , "+dst+" : "+latency);
                }
                /*if (latency > 1000.0) {
                    latency = 100.0;
                }*/
                total_latency += latency;
            }
            //saving end-to-end latency
            total_lat[i] = total_latency;
            total_latency =0.0;
        }
       // writeToMyLog2("Total end-to-end delays \n");
        //calculating average end-to-end latency
        total_latency =0.0;
        for(int i = 0 ; i<total_lat.length ; i++){
           // writeToMyLog2(total_lat[i]+" , ");
            total_latency += total_lat[i];
        }
        //writeToMyLog2(" \n");
        return total_latency/total_lat.length;
    }

    public double test_avg_plr(SDNRouting e) {
        double total_plr = 0.0;
        double[] plr_path = e.get_links_plr();
        for (int i = 0; i < plr_path.length; i++) {
            total_plr += plr_path[i];
        }
        double avg_plr = total_plr / plr_path.length;
        return avg_plr/1000.0;
    }

    public double[] test_time_Agent (int agent_type , int isRand){
        //TB_old = num_bytes_transmitted();
        //double test_start_time = System.nanoTime();
        State state;
        if(agent_type==0){
             state = env.getState();
        }else {
             state = env_obsrv.getState();
        }
        //---ACTION--- //env_obsrv.getState()
        Action action = new Action(state, fweights, env.get_num_links(),env.get_active_links());
        if(agent_type ==1 || agent_type ==0){
            action.set_rand_const_links_weights(rand, env.get_active_links(),env.get_num_links(),isRand);
        }else if(agent_type ==2){
            action = agent_DDPG.act(state, true, env.get_active_links());
           // log.info("DDPG Act");
        }
        else if(agent_type ==3){
            action = agent_PER.act(state, true, env.get_active_links());
           // log.info("DDPG_PER Act");
        }
        else if(agent_type ==4){
            action = agent_double.act(state, true, env.get_active_links());
          //  log.info("DDPG_Double Act");
        }
        //---Install new Paths---
        fweights = action.get_links_weights();
        dweights = from_float_to_Double(fweights);
        weight_matrix = dweights;
        //get newpaths using OSPF and Install new Paths

        install_ospf(g, dweights);
        wait(1000);//1sec
        //Read new stats after some traffic
        get_stats();
        //---STEP---
        //preform a step with new weights
        env_obsrv = env.step(action, links_latency, links_plr, links_util, switches_pwr);
        //---NEXT STATE---
        State next_state = env_obsrv.getState();
        //---DONE---
        boolean done = env_obsrv.isFinal();
        //---REWARD---
        float reward = env_obsrv.getReward();
        //---Agent step---
        double agent_start_time = System.nanoTime();
        if(agent_type ==2){
            agent_DDPG.step(state, action, reward, next_state, done);
            //log.info("DDPG Step");
        }
        else if(agent_type ==3){
            agent_PER.step(state, action, reward, next_state, done,1);
            //log.info("DDPG_PER Step");
        }
        else if(agent_type ==4){
            agent_double.step(state, action, reward, next_state, done,1);
           // log.info("DDPG_Double Step");
        }
        double test_end_time = System.nanoTime();
        //TB_new = num_bytes_recieved();
        double agent_time = Math.abs(((test_end_time - agent_start_time)/1.0e7)-1000);//ms
        //double test_time= (((test_end_time - test_start_time)/1.0e6)-1000)/1000.0;//s
        //agent_time = agent_time/1000.0;//s
        //double agent_throughput = Math.abs((TB_new - TB_old)*8)/test_time;
        double agent_throughput = Math.abs(num_bytes_recieved());

        return new double[]{agent_time ,agent_throughput} ;
    }




    /**
     * Calculates the number of flow modifications required
     */
    private void calculate_flow_mod(){
        for(int i = 0 ; i<current_paths.size();i++){//go through all paths
            Path currP= current_paths.get(i);
            Path prevP= previous_paths.get(i);
            if(!currP.equals(prevP)){ //if paths are different then flow modification took place
                //Calculate the number of flow modifications for current path
                List<String> currP_nodes = currP.getNodes();
                for(int c = 0 ; c<currP_nodes.size();c++){
                    int sw = Integer.parseInt(currP_nodes.get(c));//switch number
                    num_flow_mod[sw]++;//increment the number of modifications that took place
                    if(c!=0 && c!=currP_nodes.size()-1){// for packet_in only consider intermediate nodes
                        num_packet_in[sw]++;
                    }
                }
                //Calculate the number of flow modifications for previous path
                List<String> prevP_nodes = prevP.getNodes();
                for(int c = 0 ; c<prevP_nodes.size();c++){
                    int sw = Integer.parseInt(prevP_nodes.get(c));//switch number
                    num_flow_mod[sw]++;//increment the number of modifications that took place
                    if(c!=0 && c!=currP_nodes.size()-1){// for packet_in only consider intermediate nodes
                        num_packet_in[sw]++;
                    }
                }
            }
        }
    }
    private double num_bytes_transmitted(){

        final double[] src_b = {0};
        final int[] i = {0};

        Iterable<Device> devices = deviceService.getDevices();
        devices.forEach((element) -> {
            // writeToMyLog("test_plr -- element_id " + element.id().toString()+ "\n");
            List<PortStatistics> portStatisticsList = deviceService.getPortDeltaStatistics(element.id());

                for (PortStatistics portdeltastat : portStatisticsList) {
                    if (portdeltastat != null) {

                        src_b[0] += (double) portdeltastat.bytesSent();//+ portdeltastat.bytesReceived();

                        //writeToMyLog("test_plr -- packets sent " + src_p[0] +" bytes sent " + src_u[0] + "\n");
                        //log.info("test_plr -- packets sent " + src_p[0]+" bytes sent " + src_u[0]);
                        i[0]++;
                    }
                }


        });
        return src_b[0];//bytes//src_b[0]/i[0]
    }

    private double num_bytes_recieved(){

        //Keeping number of bytes sent by each port
        ArrayList<Double> dev_sent = new ArrayList<Double>();
        Iterable<Device> devices = deviceService.getDevices();
        devices.forEach((element) -> {
            // writeToMyLog("test_plr -- element_id " + element.id().toString()+ "\n");
            List<PortStatistics> portStatisticsList = deviceService.getPortDeltaStatistics(element.id());

            for (PortStatistics portdeltastat : portStatisticsList) {
                if (portdeltastat != null) {
                    dev_sent.add((double) portdeltastat.bytesSent());//+ portdeltastat.bytesReceived();
                }
            }
        });
        int time = 3;
        wait(time*1000);
        //Keeping number of bytes recieved by each port
        ArrayList<Double> dev_recv = new ArrayList<Double>();
        devices = deviceService.getDevices();
        devices.forEach((element) -> {
            // writeToMyLog("test_plr -- element_id " + element.id().toString()+ "\n");
            List<PortStatistics> portStatisticsList = deviceService.getPortDeltaStatistics(element.id());

            for (PortStatistics portdeltastat : portStatisticsList) {
                if (portdeltastat != null) {
                    dev_recv.add((double) portdeltastat.bytesReceived());//+ portdeltastat.bytesReceived();
                }
            }
        });
        //Total power for every single port
        double link_rate = 0.0;
        for(int i=0 ; i<dev_recv.size();i++) {
            //Calculating each port link rate
            link_rate += ((dev_recv.get(i) - dev_sent.get(i)) * 8.0) / (time * 1.0);
        }
        double throughput = link_rate/dev_recv.size();
        return throughput;//bytes//
    }
    ///////////////////////////// Power Calculations ////////////////////////
    /**
     * Calculating the total configuration power
     * 1- Calculating link rates for each port
     * 2- Using if statements to determine the consumed power
     * 3- Add all powers together
     * @return
     */
    private double total_link_rate_pwr(){
        //Keeping number of bytes sent by each port
        ArrayList<Double> dev_sent = new ArrayList<Double>();
        ArrayList<Double> dev_recv = new ArrayList<Double>();
        int time = 3;
        do {

            Iterable<Device> devices = deviceService.getDevices();
            devices.forEach((element) -> {
                // writeToMyLog("test_plr -- element_id " + element.id().toString()+ "\n");
                List<PortStatistics> portStatisticsList = deviceService.getPortDeltaStatistics(element.id());

                for (PortStatistics portdeltastat : portStatisticsList) {
                    if (portdeltastat != null) {
                        dev_sent.add((double) portdeltastat.bytesSent());//+ portdeltastat.bytesReceived();
                        //writeToMyLog("test_plr -- packets sent " + src_p[0] +" bytes sent " + src_u[0] + "\n");
                        //log.info("test_plr -- packets sent " + src_p[0]+" bytes sent " + src_u[0]);
                    }
                }


            });

            wait(time * 1000);
            //Keeping number of bytes recieved by each port

            devices = deviceService.getDevices();
            devices.forEach((element) -> {
                // writeToMyLog("test_plr -- element_id " + element.id().toString()+ "\n");
                List<PortStatistics> portStatisticsList = deviceService.getPortDeltaStatistics(element.id());

                for (PortStatistics portdeltastat : portStatisticsList) {
                    if (portdeltastat != null) {
                        dev_recv.add((double) portdeltastat.bytesReceived());//+ portdeltastat.bytesReceived();
                        //writeToMyLog("test_plr -- packets sent " + src_p[0] +" bytes sent " + src_u[0] + "\n");
                        //log.info("test_plr -- packets sent " + src_p[0]+" bytes sent " + src_u[0]);
                    }
                }
            });
        }while(dev_recv.size() !=dev_sent.size());
        //Total power for every single port
        double total_power = 0.0;
        for(int i=0 ; i<dev_recv.size();i++){
            //Calculating each port link rate
            double link_rate = ((dev_recv.get(i)-dev_sent.get(i))*8.0)/(time*1.0);
            //discrete power for each link rate
            if(link_rate >1e6 && link_rate<=100e6){
                total_power +=0.351;
            }else if(link_rate >100e6 && link_rate<=1e9){
                total_power +=0.697;
            }else if(link_rate>1e9){
                total_power += 2.60;
            }
        }
        return total_power;//bytes
    }
    /**
     * Switch power
     * P_switch = P_base + P_config +P_control
     * P_base = 48.7397 W (48739.7 mW) for OVS per switch
     * P_config = sum(ci * P_port)
     * ci = percentage of the line speed of the port relative to the maximum line speed
     * P_port = the power consumption of a port at full capacity measured in watt
     * P_port = 0 for OVS
     * P_control = (rate_packet_in * Energy_packet_in) + (rate_flow_mod * Energy_flow_mod)
     * Energy_packer_in = 775.53 uW/packet for OVS , Energy_flow_mod =1445.1309 uW/packet for OVS
     * @param rate_packet_in  number of packet_in per second
     * @param rate_flow_mod number of flow modifications per second
     * @return
     */
    private double switch_Power(double rate_packet_in, double rate_flow_mod) {

        double P_base = 48739.7;//mW
        double P_config = 0.0;

        double Energy_packet_in = 0.77553;//mWs per packet  //775.53W/packet
        double Energy_flow_mod = 1.44513;//mWs per packet  1445.1309W/packet
        double P_control = (rate_packet_in * Energy_packet_in ) + (rate_flow_mod * Energy_flow_mod );
        double P_switch = P_base + P_config + P_control;

        //rate_packet_in is from port stats
        //rate of flow modification must be calculated, how often flow is modified

        //packet_in rate 0f 1000 , flow_mod rate of 1005 with 1000000 packets with 50ms interval (ping command)
        //changing packet_in rate changes flow_mod rate

        return P_switch;//mW//P_switch//P_control


    }

    /**
     * Average power consumed for the whole network
     * Calculate power for each switch
     * @return
     */
    private double avg_Power() {

        double total_power = 0.0;
        //double avg_power = 0.0;
        for (int n = 1; n < num_flow_mod.length; n++) {
            switches_pwr [n] = switch_Power(num_packet_in[n], num_flow_mod[n]);
            total_power +=  switches_pwr [n];
        }
        total_power = (total_power/1000) + total_link_rate_pwr();//W
        //avg_power = total_power / (num_flow_mod.length-1);
        //AvgP_switch = sum(P_switch)/num_of_switches
        //return total_power/1000.0;
        return total_power;
    }







    //////////////////////////// Printing to Log files //////////////////
    private void print_flowMod (){
        writeToMyLog2("Flow Mod  ---- Packet_in\n");
        for (int n = 1; n < num_flow_mod.length; n++) {
            writeToMyLog2(num_flow_mod[n]+"  ---- "+ num_packet_in[n]+"\n");
        }
    }
    //writeToMyLog : to print log message in log txt file (me)
    private void writeToMyLog(String str) {
        try {
            myWriter = new FileWriter("/home/morakan/MK_Files/DPLogs/myDPRoutinglog.txt ", true);
            myWriter.write(str);
            myWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
    private void writeToMyLog2(String str) {
        try {
            myWriter2 = new FileWriter("/home/morakan/MK_Files/DPLogs/myDPResults.txt ", true);
            myWriter2.write(str);
            myWriter2.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
    private void print_link_stat(float[][] w, int l) {
        String str = "\n";
        
        if (l == 1) {
            writeToMyLog("\nprint_link_stat -- Links_latency: " + w.length + "\n");
        } else if (l == 2) {
            writeToMyLog("\nprint_link_stat -- Links_plr: " + w.length + "\n");
        } else {
            writeToMyLog("\nprint_link_stat -- Links_util: " + w.length + "\n");
        }


        for (int i = 0; i < w.length; i++) {
            for (int j = 0; j < w.length; j++)
                str += String.valueOf(w[i][j]) + "    ";
            str += "\n";
        }
        
        writeToMyLog(str + "\n");

    }

    private void print_pwr_stat(double[] pwr) {
        String str = "\n";
        
        writeToMyLog("\nprint_pwr_stat -- pwr: " + pwr.length + "\n");
        for (int i = 0; i < pwr.length; i++) {
                str += String.valueOf(pwr[i]) + "    ";
        }
        
        writeToMyLog(str + "\n");


    }


}


