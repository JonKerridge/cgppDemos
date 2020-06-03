package demoApplications.mcpi
import jcsp.lang.*
import groovyJCSP.*
import jcsp.net2.*
import jcsp.net2.mobile.*
import jcsp.net2.tcpip.*
import gppClusterBuilder.*
 
import groovyParallelPatterns.DataDetails
import groovyParallelPatterns.ResultDetails
import groovyParallelPatterns.cluster.connectors.NodeRequestingSeqCastList
import groovyParallelPatterns.cluster.connectors.OneNodeRequestedList
import groovyParallelPatterns.connectors.reducers.ListFanOne
import groovyParallelPatterns.connectors.reducers.ListMergeOne
import groovyParallelPatterns.functionals.groups.ListGroupList
import groovyParallelPatterns.terminals.Collect
import groovyParallelPatterns.terminals.Emit
 
class McPiNodeProcess implements CSProcess, Serializable, NodeConnection, LoaderConstants{
String nodeIP, hostIP
NetLocation toHostLocation
NetChannelInput fromHost
 
def connectFromHost (NetChannelInput fromHost){
this.fromHost = fromHost
}
 
@Override
void run() {


// number of workers on each node
int cores = 4
// number of clusters
int clusters = 1 //worker nodes
long initialTime = System.currentTimeMillis()
// create basic connections for node
NetChannelOutput node2host = NetChannel.one2net(toHostLocation as NetChannelLocation)
node2host.write(nodeProcessInitiation)
// read in application net input channel VCNs [ vcn, ... ]
List inputVCNs = fromHost.read() as List
//@inputVCNs
//node Input Insert

ChannelInput nodeFromEmit = NetChannel.numberedNet2One(inputVCNs[0])

 
// acknowledge creation of net input channels
node2host.write(nodeApplicationInChannelsCreated)
 
// read in application net output channel locations [ [ip, vcn], ... ]
List outputVCNs = fromHost.read()
//@outputVCNs
//node Output Insert

ChannelOutput node2emit = NetChannel.one2net(new TCPIPNodeAddress(outputVCNs[0][0], 2000), outputVCNs[0][1])
ChannelOutput node2collect = NetChannel.one2net(new TCPIPNodeAddress(outputVCNs[1][0], 2000), outputVCNs[1][1])

 
// acknowledge creation of net output channels
node2host.write(nodeApplicationOutChannelsCreated)
println "Node starting application process"
long processStart = System.currentTimeMillis()
// now start the process - inserted by builder
//@nodeProcess
//node Process Insert

 
def chan3 = Channel.one2oneArray(cores)
def chan3OutList = new ChannelOutputList(chan3)
def chan3InList = new ChannelInputList(chan3)
def chan4 = Channel.one2oneArray(cores)
def chan4OutList = new ChannelOutputList(chan4)
def chan4InList = new ChannelInputList(chan4)


def nrfl = new NodeRequestingSeqCastList(
    request: node2emit,
    response: nodeFromEmit,
    outList: chan3OutList )
 
def group = new ListGroupList(
    inputList: chan3InList,
    outputList: chan4OutList,
    workers: cores,
    function: SerializedMCpiData.withinOp
    )
 
def afo1 = new ListFanOne(
    inputList: chan4InList,
    output: node2collect,
    )
 



new PAR([nrfl , group , afo1 ]).run()

 
long processEnd = System.currentTimeMillis()
node2host.write([nodeIP, (processStart - initialTime), (processEnd - processStart)])
}
}
