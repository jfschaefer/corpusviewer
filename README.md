# THIS REPOSITORY HAS BEEN MOVED

You can find it at https://bitbucket.org/tclup/corpusviewer

&nbsp;

&nbsp;

# README

The corpusviewer is a tool to view corpora in the IRTG format (more about IRTGs at http://www.ling.uni-potsdam.de/staff/koller?id=irtg-11).

## Remarks on the usage

### Running the corpusviewer
You can run the corpusviewer by calling
```Bash
sh run.sh
```
This increases the memory size compared to a simple `sbt run`.

### Configuration
The configurations are stored in `corpusviewer.properties`.
You can either change it directly, or run `sh configure.sh`, which starts simple dialog for changing some of the important properties.
The most important property is probably `gesture_config_properties`, which sets the configuration file for the touch events.

### The interpretations file
The interpretations file specifies which interpretation belongs to which algebra.
The following interpretations file defines the interpretations *string* and *graph*:
```
interpretation string : de.up.ling.irtg.algebra.StringAlgebra
interpretation graph : de.up.ling.irtg.algebra.graph.GraphAlgebra
```

### Filter rules
The filter rules are written in *Jython*.
You have to provide a function `filter`, which takes two arguments: An instance and (for convenience) a map of the interpretations.
The `filter` function will be called for each instance - if it returns `true`, the instance will be loaded, otherwise it won't.
The instances are assessed in order, so you might want to maintain a state using global variables.
There are a few example filter rules in the repository.

## Improving performance
If you run the corpusviewer on a slow computer or you visualize very complex interpretations, you might notice
that the computations take too much time and the application gets less responsive or that the program takes a
lot of memory.
There are a couple of things you can do in order to improve the situation.

### Constant scaling function
Especially when you have big graphs in your preview, scrolling through the corpus can get very slow.
A lot of computational power is required for the repeated rescaling of the previews.
A simple way out is to use constant scaling instead.
Therefore you have to change the `preview_scaling_function` property in `corpusviewer.properties` to
`de.jfschaefer.corpusviewer.preview.ConstantScalingFunction`.

### Avoid big graphs in preview
Having really big graphs in the preview requires a significant amount of memory. Also, it can potentially slow down the scrolling speed.
The easiest solution is to disable graphs in the preview.
Another way out is skipping just the very large graphs in the preview.
Since the graphs get scaled down, you cannot see much in the preview anyway.
Therefore, you simply have to change the `preview_max_downscale` property in `corpusviewer.properties`.
The smaller the value, the more graphs are skipped.

### Reduce number of instances
The more instances you load, the more memory and preprocessing time you will need.
The very simple (and obvious) way out is to reduce the number of instances you load.
This can be achieved using the filter rules.
There is an example rule for only loading the first 100 instances, but you could as well load a random subset or every nth instance.


## How to add visualizations for other algebras?

All visualizations inherit from `visualization.Displayable`.
Once you've implemented it, you can add it to the visualization factory.
You can see which visualization factory is used in `Configuration`.
By default, a `ConcreteVisualizationFactory` is used.
If you would like to see an example:
A very small implementation of a Displayable can be found at `visualization.NoVisualization`.

