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
		s11 = self.addSwitch('s11', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.11.1')
		s12 = self.addSwitch('s12', cls = OVSSwitch , protocols = "OpenFlow13", ip = '10.0.12.1')

		
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

		
		self.addLink(s1,s2,delay='1ms',bw = 1e6, max_queue_size=30)
		
		self.addLink(s2,s5,delay='1ms',bw = 1e6, max_queue_size=30)
		self.addLink(s2,s6,delay='1ms',bw = 1e6, max_queue_size=30)
		self.addLink(s2,s12,delay='1ms',bw = 1e6, max_queue_size=30)
		
		self.addLink(s3,s6,delay='1ms',bw = 1e6, max_queue_size=30)
		self.addLink(s3,s9,delay='1ms',bw = 1e6, max_queue_size=30)
		
		self.addLink(s4,s7,delay='1ms',bw = 1e6, max_queue_size=30)
		self.addLink(s4,s10,delay='1ms',bw = 1e6, max_queue_size=30)
		self.addLink(s4,s11,delay='1ms',bw = 1e6, max_queue_size=30)
		
		self.addLink(s5,s7,delay='1ms',bw = 1e6, max_queue_size=30)
		self.addLink(s5,s8,delay='1ms',bw = 1e6, max_queue_size=30)
		
		self.addLink(s6,s7,delay='1ms',bw = 1e6, max_queue_size=30)
		
		self.addLink(s8,s10,delay='1ms',bw = 1e6, max_queue_size=30)

		self.addLink(s9,s12,delay='1ms',bw = 1e6, max_queue_size=30)
		
		self.addLink(s10,s11,delay='1ms',bw = 1e6, max_queue_size=30)			

def run():
	topo = Project()
	net = Mininet(topo = topo , controller = None )
	c1 = net.addController('c1' , controller = RemoteController , ip = '127.0.0.1')
	c1.start()
	net.start()
	#net.pingAll()
	timeTotal = 1260#4000#10000
	loadLevel = 8
	bw = 9750.0#9.75
	time.sleep(5)
	print("starting traffic for training")
	for k in range(10):
		print("Traffic round ",k)
		for i in range(30*loadLevel):#10
			generate_flow(net,bw,timeTotal,loadLevel)
        loadLevel = loadLevel + 2
		time.sleep(timeTotal)
	
	
		
	print("Stopping traffic for training")
	#src_drop,dst_drop = getRandomPair(net)
	net.configLinkStatus("s8","s10",'down')
	print("----Link Dropped---- ","s8","  ","s10")
	timeTotal = 300
	for k in range(10):
		print("Traffic round ",k)
		for i in range(20*loadLevel):#10
			generate_flow(net,bw,timeTotal,loadLevel)
        loadLevel = loadLevel + 2
		time.sleep(timeTotal)
	
	print("starting traffic for testing")
	timeTotal = 500#600
	loadLevel = 2 
	for ld in range(10):
		traffic_ld = loadLevel*10
		print("Traffic Load ",traffic_ld," % ")
		for i in range(3*loadLevel):#10
			generate_flow(net,bw,timeTotal,loadLevel)
	
		time.sleep(timeTotal)
		loadLevel = loadLevel + 2
	print("Stopping traffic for testing")
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
