package demoApplications.mandelbrot
import jcsp.lang.*
import groovyJCSP.*
import jcsp.net2.*
import jcsp.net2.mobile.*
import jcsp.net2.tcpip.*
import gppClusterBuilder.*
class MandelbrotNodeLoader implements LoaderConstants{
static void main(String[] args) {
String hostIP = args[0] // host IP must be specified
// create this node
def nodeAddress = new TCPIPNodeAddress(1000)
Node.getInstance().init(nodeAddress)
String nodeIP = nodeAddress.getIpAddress()
println "Node $nodeIP running"
// create net input channel from host
NetChannelInput fromHost = NetChannel.numberedNet2One(1,
new CodeLoadingChannelFilter.FilterRX() )
def hostAddress = new TCPIPNodeAddress(hostIP, 2000)
// create host request output channel
def toHost = NetChannel.any2net(hostAddress, 1)
// send host the IP of this Node and get response
toHost.write(nodeIP)
assert fromHost.read() == acknowledgeNodeIPRead:
"Node Load - $nodeIP: expected acknowledgement during initialisation not received"
// read in and connect the Application Noide process from host
CSProcess nodeProcess = fromHost.read() as CSProcess
nodeProcess.connectFromHost(fromHost)
// acknowledge receipt of node process
toHost.write(nodeProcessRead)
// wait to receive start signal from host
assert fromHost.read() == startNodeProcess:
"Node Load - $nodeIP: expected start Node Process signal not received"
//  start node process
new PAR([nodeProcess]).run()
}
}
