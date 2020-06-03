package demoApplications.mcpi

import groovyParallelPatterns.DataClass


class SerializedMCpiData extends DataClass {
  int iterations = 0
  int within = 0
  static int instance = 0
  static String withinOp = "getWithin"
  int instanceNumber



  /**
   * Calculates for each iteration an x and y random value 0.0 <= v < 1.0
   * Then determines if the sum of the squares of x and y are <= 1.0 and adds
   * 1 to within if so.
   *
   * @return completedOK
   */
  int getWithin(List d){
    def rng = new Random()
    float x, y
    for ( i in 1 ..iterations){
      x = rng.nextFloat()
      y = rng.nextFloat()
      if ( ((x*x) + (y*y)) <= 1.0 ) within = within + 1
    }
    return completedOK
  }

  @Override
  SerializedMCpiData clone() {
    SerializedMCpiData newClone = new SerializedMCpiData()
    newClone.iterations = this.iterations
    newClone.within = this.within
    newClone.instanceNumber = this.instance
    instance++
//    println "cloned ${newClone.toString()}"
    return newClone
  }

  String toString (){
    String s = "SerializedMCpiData: $instanceNumber, $iterations, $within "
    return s
  }

}
