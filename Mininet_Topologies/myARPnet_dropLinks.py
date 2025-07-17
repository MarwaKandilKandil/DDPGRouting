#!/usr/bin/env puthon

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.cli import CLI
from mininet.node import RemoteController , OVSSwitch
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from functools import partial
from thread import start_new_thread
import time
import random
#MUST ACTIVATE OPENFLOW and FWD applications in ONOS

# (h1)--S1---S2--(h2)
#      \    /
#        S3--(h3)

class Project( Topo):
	def build(self, **_opts):
		#add hosts
		h1 = self.addHost('h1', ip='10.0.0.1')
		h2 = self.addHost('h2', ip='10.0.0.2')
		h3 = self.addHost('h3', ip='10.0.0.3')
		h4 = self.addHost('h4', ip='10.0.0.4')
		h5 = self.addHost('h5', ip='10.0.0.5')
		h6 = self.addHost('h6', ip='10.0.0.6')
		h7 = self.addHost('h7', ip='10.0.0.7')
		h8 = self.addHost('h8', ip='10.0.0.8')
		h9 = self.addHost('h9', ip='10.0.0.9')
		h10 = self.addHost('h10', ip='10.0.0.10')
		h11 = self.addHost('h11', ip='10.0.0.11')
		h12 = self.addHost('h12', ip='10.0.0.12')
		h13 = self.addHost('h13', ip='10.0.0.13')
		h14 = self.addHost('h14', ip='10.0.0.14')
		h15 = self.addHost('h15', ip='10.0.0.15')
		h16 = self.addHost('h16', ip='10.0.0.16')
		h17 = self.addHost('h17', ip='10.0.0.17')
		h18 = self.addHost('h18', ip='10.0.0.18')
		h19 = self.addHost('h19', ip='10.0.0.19')
		h20 = self.addHost('h20', ip='10.0.0.20')
		
		#add switches
		s1 = self.addSwitch('s1', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.1')
		s2 = self.addSwitch('s2', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.2.1')
		s3 = self.addSwitch('s3', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.3.1')
		s4 = self.addSwitch('s4', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.4.1')
		s5 = self.addSwitch('s5', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.5.1')
		
		s6 = self.addSwitch('s6', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.6.1')
		s7 = self.addSwitch('s7', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.7.1')
		s8 = self.addSwitch('s8', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.8.1')
		s9 = self.addSwitch('s9', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.9.1')
		s10 = self.addSwitch('s10', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.10.1')
		
		s11 = self.addSwitch('s11', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.2')
		s12 = self.addSwitch('s12', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.3')
		s13 = self.addSwitch('s13', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.4')
		s14 = self.addSwitch('s14', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.5')
		s15 = self.addSwitch('s15', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.6')
		
		s16 = self.addSwitch('s16', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.7')
		s17 = self.addSwitch('s17', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.8')
		s18 = self.addSwitch('s18', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.9')
		s19 = self.addSwitch('s19', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.1.10')
		s20 = self.addSwitch('s20', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.2.1')
		
		#add links
		self.addLink(h1,s1)
		self.addLink(h2,s2)
		self.addLink(h3,s3)
		self.addLink(h4,s4)
		self.addLink(h5,s5)
		
		self.addLink(h6,s6)
		self.addLink(h7,s7)
		self.addLink(h8,s8)
		self.addLink(h9,s9)
		self.addLink(h10,s10)
		self.addLink(h11,s11)
		self.addLink(h12,s12)
		self.addLink(h13,s13)
		self.addLink(h14,s14)
		self.addLink(h15,s15)
		self.addLink(h16,s16)
		self.addLink(h17,s17)
		self.addLink(h18,s18)
		self.addLink(h19,s19)
		self.addLink(h20,s20)
		
		
		self.addLink(s1,s2,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s2,s1,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s1,s3,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s3,s1,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s1,s6,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s6,s1,delay='10ms', bw = 10, max_queue_size=30)

		
		self.addLink(s2,s4,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s4,s2,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s2,s5,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s5,s2,delay='12ms', bw = 10, max_queue_size=30)
		
		self.addLink(s3,s7,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s7,s3,delay='12ms', bw = 10, max_queue_size=30)
		
		self.addLink(s3,s8,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s8,s3,delay='12ms', bw = 10, max_queue_size=30)
		
		self.addLink(s4,s5,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s5,s4,delay='14ms', bw = 10, max_queue_size=30)
		
		self.addLink(s4,s11,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s11,s4,delay='14ms', bw = 10, max_queue_size=30)
		
		self.addLink(s5,s6,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s6,s5,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s6,s7,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s7,s6,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s6,s8,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s8,s6,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s7,s10,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s10,s7,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s8,s9,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s9,s8,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s8,s10,delay='10ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s10,s8,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s8,s12,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s12,s8,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s9,s10,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s10,s9,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s9,s11,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s11,s9,delay='10ms', bw = 10, max_queue_size=30)
		
		
		self.addLink(s10,s19,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s19,s10,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s11,s13,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s13,s11,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s12,s14,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s14,s12,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s12,s18,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s18,s12,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s13,s15,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s15,s13,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s13,s17,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s17,s13,delay='10ms', bw = 10, max_queue_size=30)
		
		
		self.addLink(s14,s15,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s15,s14,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s14,s18,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s18,s14,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s15,s16,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s16,s15,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s16,s18,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s18,s16,delay='10ms', bw = 10, max_queue_size=30)
		
		self.addLink(s16,s19,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s19,s16,delay='10ms', bw = 10, max_queue_size=30)
		
		
		self.addLink(s17,s19,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s19,s17,delay='10ms', bw = 10, max_queue_size=30)
		
		
		self.addLink(s17,s20,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s20,s17,delay='10ms', bw = 10, max_queue_size=30)
		
		
		self.addLink(s19,s20,delay='1ms', bw = 1e6, max_queue_size=30)
		#self.addLink(s20,s19,delay='10ms', bw = 10, max_queue_size=30)
		
			

def run():
	topo = Project()
	net = Mininet(topo = topo , controller = None )
	c1 = net.addController('c1' , controller = RemoteController , ip = '127.0.0.1')
	c1.start()
	net.start()
	#net.pingAll()
	timeTotal = 1980#4000
	loadLevel = 8 
	bw = 9750.0#9.75
	time.sleep(5)
	print("starting traffic for training")
	for k in range(10):
		print("Traffic round ",k)
		for i in range(30*loadLevel):
			generate_flow(net,bw,timeTotal,loadLevel)
        loadLevel = loadLevel + 2
		time.sleep(timeTotal)
	
	
		
	print("Stopping traffic for training")
	#src_drop,dst_drop = getRandomPair(net)
	net.configLinkStatus("s4","s5",'down')
	print("----Link Dropped---- ","s4","  ","s5")
	timeTotal = 300
	for k in range(10):
		print("Traffic round ",k)
		for i in range(20*loadLevel):#10
			generate_flow(net,bw,timeTotal,loadLevel)
        loadLevel = loadLevel + 2
		time.sleep(timeTotal)
	
	print("starting traffic for testing")
	timeTotal = 800#600
	loadLevel = 2 
	for ld in range(5):
		traffic_ld = loadLevel*10
		print("Traffic Load ",traffic_ld," % ")
		for i in range(6*loadLevel):#10
			generate_flow(net,bw,timeTotal,loadLevel)
	
		time.sleep(timeTotal)
		loadLevel = loadLevel + 2
		
	#print("Stopping traffic for testing")
	
	net.pingAll()
	CLI(net)
	net.stop()
	

def startIperf(host1 , host2 , amount , timeTotal , loadLevel):
	print("(Re)startingiperf3 -- loadLevel: {}".format(loadLevel))
	command1 = "iperf3 -s -p 5566 -i 1 > iperf_{}.txt &".format(host2.name)
	host2.cmd(command1)
	bw = float(amount) * (float(loadLevel)/float(10))
	print("Host {} to Host {} Bw: {}".format(host1.name,host2.name,bw))
	command = "iperf3 -c {} -u -p 5566 -t {} -b {}M > iperf_{}.txt &".format(host2.IP(), timeTotal , bw,host1.name)
	host1.cmd(command)
	
def getRandomPair(net):
	hosts = net.hosts
	# select random src and dst
    	end_points = random.sample(hosts, 2)
    	src = net.get(str(end_points[0]))
    	dst = net.get(str(end_points[1]))
    	print("Random hosts src: {} dst:{}".format(src.name,dst.name))
    	return src,dst
    	
def generate_flow(net,bw,timeTotal,loadLevel):
	src,dst = getRandomPair(net)
	startIperf(src,dst,bw,timeTotal,loadLevel)
	
setLogLevel('info')
run()	
