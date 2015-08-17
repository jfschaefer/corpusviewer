# README

The corpusviewer is a tool to view corpora in the IRTG format (more about IRTGs at [http://www.ling.uni-potsdam.de/staff/koller?id=irtg-11]).

## Remarks on the usage
### Run the corpusviewer
You can run the corpusviewer by calling
```Bash
sh run.sh
```
This increases the memory size compared to a simple `sbt run`.

### The interpretations file
The interpretations file specifies which interpretation belongs to which algebra.
The following interpretations file defines the interpretations *string* and *graph*:
```
interpretation string : de.up.ling.irtg.algebra.StringAlgebra
interpretation graph : de.up.ling.irtg.algebra.graph.GraphAlgebra
```

# How to add visualizations for other algebras?

All visualizations inherit from `visualization.Displayable`.
Once you've implemented it, you can add it to the visualization factory.
You can see which visualization factory is used in `Configuration`.
By default, a `ConcreteVisualizationFactory` is used.
If you would like to see an example:
A very small implementation of a Displayable can be found at `visualization.NoVisualization`.

