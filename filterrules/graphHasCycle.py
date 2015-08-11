# This filter accepts all instances where the graph interpretation has
# at least one cycle.
# These are very rare and I was told that they are wrong.
# Remark: We assume that the instance has an interpretation called
#         "graph", which is an object of the 
#         de.up.ling.irtg.graph.GraphAlgebra

INTERPRETATION_NAME = "graph"

NODE_UNVISITED = 0  # has never been visited
NODE_VISITED   = 1  # has been visited - current location is a descendant
NODE_DONE      = 2  # not part of any cycles


def findCycle(visited, jdigraph, node):
    if visited[node] == NODE_VISITED:
        return True
    elif visited[node] == NODE_DONE:
        return False

    retval = False

    visited[node] = NODE_VISITED
    for edge in jdigraph.outgoingEdgesOf(node):
        child = edge.getTarget()
        retval = findCycle(visited, jdigraph, child)
        if retval:
            break
    visited[node] = NODE_DONE

    return retval

def filter(instance, interpretations):
    jdigraph = interpretations[INTERPRETATION_NAME].getGraph()
    visited = dict()
    for node in jdigraph.vertexSet():
        visited[node] = NODE_UNVISITED

    for node in jdigraph.vertexSet():
        if visited[node] == NODE_UNVISITED:
            if findCycle(visited, jdigraph, node):
                return True

    return False     # no cycle has been found
