# an-mxgraph-renderer

Renders draw.io graphs (mxFiles) to images or svg.

## Usage
````
java -jar an-mxgraph-renderer-0.1.0.jar --in test.xml --out result.svg --format svg
````

If *in* or *out* parameter is omitted stdin and stdout will be used.
The default output format is png.


## Limitations 
Stencils are currently NOT supported in SVG files  

## License

This project is licensed under the MIT License
