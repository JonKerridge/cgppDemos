package demoApplications.mcpi
import jcsp.lang.*
import groovyJCSP.*
import jcsp.net2.*
import jcsp.net2.mobile.*
import jcsp.net2.tcpip.*
import gppClusterBuilder.*
class McPiHostLoader implements LoaderConstants{
static void main(String[] args) {
int nodes = Integer.parseInt(args[0]) // number of nodes in cluster excluding host
// create node and net input channel used by NodeLoaders
def nodeAddress = new TCPIPNodeAddress(2000)
Node.getInstance().init(nodeAddress)
String hostIP = nodeAddress.getIpAddress()
println "Host $hostIP running with $nodes nodes"
NetChannelInput fromNodes = NetChannel.numberedNet2One(1 )
// wait for all the nodes to send their IP addresses
// create a List for the IPs
List <String> nodeIPs = []
// create list of individual net output channel to each node
List < NetChannelOutput> toNodes = []
for ( n in 0 ..< nodes) {
String nodeIP = (fromNodes.read() as String)
nodeIPs << nodeIP
toNodes << NetChannel.one2net(
new TCPIPNodeAddress(nodeIP,1000),
1,
new CodeLoadingChannelFilter.FilterTX())  // must be code loading because node process will be sent
}
// can now start timing as from now on the interactions are contiguous and
// do not rely on Nodes being started from command line
long initialTime = System.currentTimeMillis()
// acknowledge receipt of NodeIPs to nNodes
for ( n in 0 ..< nodes){
toNodes[n].write(acknowledgeNodeIPRead)
}
// now send the built Node process to each Node - name modified by builder
//@nodeProcess
for ( n in 0 ..< nodes){
toNodes[n].write(new McPiNodeProcess(
hostIP: hostIP,
nodeIP: nodeIPs[n],
toHostLocation: fromNodes.getLocation()
)
)
}
// now read acknowledgements from Nodes
for ( n in 0 ..< nodes){
assert  fromNodes.read() == nodeProcessRead :
"Failed to read Node Process read acknowledgement from ${nodeIPs[n]}"
}
// tell nodes to start their processes
for ( n in 0 ..< nodes){
toNodes[n].write(startNodeProcess)
}
println "Host running application process"
long processStart = System.currentTimeMillis()
// builder modifies the name of the host process
//@hostProcess
new PAR([new McPiHostProcess(
hostIP: hostIP,
nodeIPs: nodeIPs,
nodes2host: fromNodes,
host2nodes: toNodes
)]).run()
 
long processEnd = System.currentTimeMillis()
println "Host Node: Load Phase= ${processStart - initialTime} " +
"Processing Phase = ${processEnd - processStart} " +
"Total time = ${processEnd - initialTime}"
}
}
