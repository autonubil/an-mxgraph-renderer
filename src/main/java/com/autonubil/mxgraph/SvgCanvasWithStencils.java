package com.autonubil.mxgraph;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.shape.mxStencil;
import com.mxgraph.shape.mxStencilRegistry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;

public class SvgCanvasWithStencils extends mxSvgCanvas {

	/**
	 * Constructs a new SVG canvas for the specified dimension and scale.
	 */
	public SvgCanvasWithStencils()
	{
		this(null);
	}

	/**
	 * Constructs a new SVG canvas for the specified bounds, scale and
	 * background color.
	 */
	public SvgCanvasWithStencils(Document document)
	{
		super(document);
	}
	
	
	@Override 
	public Element drawShape(int x, int y, int w, int h, Map<String, Object> style) {
		
		String shape = mxUtils.getString(style, mxConstants.STYLE_SHAPE, "");
		Element background = null;
		if (shape.indexOf('.') > -1) {
			mxStencil stencil = mxStencilRegistry.getStencil(shape);
			if (stencil != null) {
				mxRectangle bounds = new mxRectangle(x, y, w, h);
				System.err.println(stencil);
				
				Element elt = stencil.getDescription();

				if (elt != null)
				{	
					// TODO: a lot!
					elt = (Element)elt.getElementsByTagName("connections").item(0);
					return elt;	 
				}
				
				
			}
			
			
		}
		
		return super.drawShape(x, y, w, h, style);
	}
	
}
