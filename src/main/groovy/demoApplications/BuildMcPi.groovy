package demoApplications

import gppClusterBuilder.CGPPbuilder


def build = new CGPPbuilder()
String rootPath = "./"  // as required for use in Intellij

build.runClusterBuilder(rootPath + "mcpi\\McPi")



