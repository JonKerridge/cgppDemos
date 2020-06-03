package demoApplications.mandelbrot
import jcsp.lang.*
import groovyJCSP.*
import jcsp.net2.*
import jcsp.net2.mobile.*
import jcsp.net2.tcpip.*
import gppClusterBuilder.*
 
import groovyParallelPatterns.DataDetails
import groovyParallelPatterns.ResultDetails
import groovyParallelPatterns.cluster.connectors.NodeRequestingFanAny
import groovyParallelPatterns.cluster.connectors.NodeRequestingSeqCastList
import groovyParallelPatterns.cluster.connectors.OneNodeRequestedList
import groovyParallelPatterns.connectors.reducers.AnyFanOne
import groovyParallelPatterns.connectors.reducers.ListFanOne
import groovyParallelPatterns.connectors.reducers.ListMergeOne
import groovyParallelPatterns.functionals.groups.AnyGroupAny
import groovyParallelPatterns.functionals.groups.ListGroupList
import groovyParallelPatterns.terminals.Collect
import groovyParallelPatterns.terminals.Emit
 
import demoApplications.mandelbrot.MandelbrotLine as ml
import demoApplications.mandelbrot.MandelbrotLineCollect as mlc
import demoApplications.mandelbrot.SerializedMandelbrotLine as sml
 
class MandelbrotHostProcess implements CSProcess, LoaderConstants{
String hostIP
List <String> nodeIPs
NetChannelInput nodes2host
List <NetChannelOutput> host2nodes
 
@Override
void run() {


// number of workers on each node
int cores = 4
// number of clusters
int clusters = 1
 
int nodes_Number = nodeIPs.size()
// create basic process connections for host
for ( n in 0 ..< nodes_Number) {
// wait for all nodes to start
assert nodes2host.read() == nodeProcessInitiation :
"Node ${nodeIPs[n]} failed to initialise node process"
// create host2nodes channels - already have node IPs
}
long initialTime = System.currentTimeMillis()
// send application channel data to nodes - inserted by Builder - also those at host
List inputVCNs   // each node gets a list of input VCNs
//@inputVCNs
//host NodeInput Insert

inputVCNs = [ [200] ]

 
for ( n in 0 ..< nodes_Number) host2nodes[n].write(inputVCNs[n])
 
//@hostInputs
//host Input Insert

ChannelInputList emitRequestList = [] 
emitRequestList.append(NetChannel.numberedNet2One(100)) 
ChannelInput collectFromNodes = NetChannel.numberedNet2One(300) 

 
// now read acknowledgments
for ( n in 0 ..< nodes_Number){
assert nodes2host.read() == nodeApplicationInChannelsCreated :
"Node ${nodeIPs[n]} failed to create node to host link channels"
}
// each node gets a list [IP, vcn] to which it is connected
List outputVCNs
//@outputVCNs
//host NodeOutput Insert

outputVCNs = [ [ [hostIP, 100], [hostIP, 300] ] ]

 
for ( n in 0 ..< nodes_Number) host2nodes[n].write(outputVCNs[n])
 
//@hostOutputs
//host Output Insert

ChannelOutputList emitResponseList = [] 
emitResponseList.append(NetChannel.one2net(new TCPIPNodeAddress(nodeIPs[0], 1000), 200)) 

 
// now read acknowledgments
for ( n in 0 ..< nodes_Number){
assert nodes2host.read() == nodeApplicationOutChannelsCreated :
"Node ${nodeIPs[n]} failed to create node to host link channels"
}
// all the net application channels have been created
long processStart = System.currentTimeMillis()
// now start the process - inserted by builder
//@hostProcess
//host Process Channel Insert

//application variables
int width = 350
int height = 200
int maxIterations = 100
double pixelDelta = 0.01
 
def emitDetails = new DataDetails(dName: ml.getName(),
dInitMethod: ml.init,
dInitData: [width, height, pixelDelta, maxIterations],
dCreateMethod: ml.create
)
 
def chan1 = Channel.one2one()


 
def resultDetails = new ResultDetails(rName: mlc.getName(),
rInitMethod: mlc.init,
rCollectMethod: mlc.collector,
rFinaliseMethod: mlc.finalise )
 
def chan2 = Channel.one2one()


//host ProcessPar Insert

def emit = new Emit (
    // input channel not required
    output: chan1.out(),
    eDetails: emitDetails
    )
 
def onrl = new OneNodeRequestedList(
    input: chan1.in(),
    request: emitRequestList,
    response: emitResponseList,
    )
 


def afo2 = new AnyFanOne(
    inputAny: collectFromNodes,
    output: chan2.out(),
    sources: clusters
    )
 
def collector = new Collect(
    input: chan2.in(),
    // no output channel required
    rDetails: resultDetails
    )
 



new PAR([emit , onrl , afo2 , collector ]).run()

 
 
long processEnd = System.currentTimeMillis()
println "Times           Load Process"
List times = [ 
    ["Host          ", (processStart - initialTime), (processEnd - processStart)] 
 ]
for ( n in 0 ..< nodes_Number){
times << (List)(nodes2host.read() )
}
times.each {println "$it"}
}
}
