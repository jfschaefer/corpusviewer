# This filter accepts all instances where the graph interpretation has
# at least one node with indegree 2 or higher.
# The other graphs are mostly trees and often less interesting when
# testing different graph layout algorithms
# Remark: We assume that the instance has an interpretation called
#         "graph", which is an object of the 
#         de.up.ling.irtg.graph.GraphAlgebra

#minimal maximal indegree ;
MIN_MAX_INDEGREE    = 2
INTERPRETATION_NAME = "graph"

def getMaxIndegree(jgraph):
    indegrees = dict()
    for node in jgraph.vertexSet():
        indegrees[node] = 0
    for edge in jgraph.edgeSet():
        indegrees[edge.getTarget()] += 1
    return max(indegrees.values())

def filter(instance, interpretations):
    m = getMaxIndegree(interpretations[INTERPRETATION_NAME].getGraph())
    return m >= MIN_MAX_INDEGREE
