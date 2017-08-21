package com.autonubil.mxgraph;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

public class Renderer {
	/**
	 * 
	 */
	public static String CHARSET_FOR_URL_ENCODING = "ISO-8859-1";

	/**
	 * 
	 */
	protected static final int IO_BUFFER_SIZE = 4 * 1024;

	public static void main(String[] args) throws ParseException {

		Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help").required(false).desc("shows this message").build());
		options.addOption(Option.builder("i").longOpt("in").numberOfArgs(1).optionalArg(false).required(false)
				.desc("source file").build());
		options.addOption(Option.builder("o").longOpt("out").numberOfArgs(1).optionalArg(false).required(false)
				.desc("target file").build());
		options.addOption(Option.builder("f").longOpt("format").numberOfArgs(1).optionalArg(false).required(false)
				.desc("format").build());

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdLine = parser.parse(options, args);

			if (cmdLine.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("MXGraph Renderer", options);
			} else {

				InputStream is = null;
				if (!cmdLine.hasOption("i")) {
					is = System.in;
				} else {
					String source = cmdLine.getOptionValue("i");
					try {
						is = new FileInputStream(new File(source));
					} catch (FileNotFoundException e) {
						System.err.println("Input file " + source + " not found");
						System.exit(1);
					}
				}

				OutputStream os = null;
				if (!cmdLine.hasOption("o")) {
					os = System.out;
				} else {
					String target = cmdLine.getOptionValue("o");
					try {
						os = new FileOutputStream(new File(target));
					} catch (FileNotFoundException e) {
						System.err.println("Target file " + target + " could not be created");
						System.exit(1);
					}
				}

				String format = cmdLine.getOptionValue("f", "svg");

				try {
					render(is, os, format);
				} catch (IOException e) {
					System.err.println("Failed to render drawing (" + e.getMessage() + ")");
					System.exit(1);
				}

			}
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			System.out.println("Try \"--help\" option for details.");
			System.exit(1);
		}
	}

	/**
	 * Applies a standard inflate algo to the input byte array
	 * @param binary the byte array to inflate
	 * @return the inflated String
	 * 
	 */
	public static String inflate(byte[] binary) throws IOException
	{
		StringBuilder result = new StringBuilder();
		InputStream in = new InflaterInputStream( new ByteArrayInputStream(binary), new Inflater(true));

		while (in.available() != 0) {
			byte[] buffer = new byte[IO_BUFFER_SIZE];
			int len = in.read(buffer, 0, IO_BUFFER_SIZE);

			if (len <= 0) {
				break;
			}

			result.append(new String(buffer, 0, len));
		}

		in.close();

		return result.toString();
	}
	
	
	private static void render(InputStream is, OutputStream os, String format) throws IOException {

		mxGraph graph = new mxGraph();

		Document doc = mxXmlUtils.parseXml(IOUtils.toString(is, "UTF-8"));

		Node mxNode = doc.getChildNodes().item(0);
		if (!mxNode.getNodeName().equals("mxfile")) {
			throw new IOException("Not an mxFile");
		}
		Node diagramNode = mxNode.getChildNodes().item(0);
		if (!diagramNode.getNodeName().equals("diagram")) {
			throw new IOException("Not an mxFile");
		}

		
		String decoded =  URLDecoder.decode(inflate(Base64.getDecoder().decode(diagramNode.getChildNodes().item(0).getNodeValue())), CHARSET_FOR_URL_ENCODING );
 
		doc = mxXmlUtils.parseXml(decoded);
		mxCodec codec = new mxCodec(doc);
		codec.decode(doc.getDocumentElement(), graph.getModel());
		
		if (format.equalsIgnoreCase("svg") ) {
			Document svgDoc = mxCellRenderer.createSvgDocument(graph, null, 1, Color.WHITE, null);
			
			try {
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(svgDoc);
				StreamResult result = new StreamResult(os);
				transformer.transform(source, result);
			} catch (TransformerException e) {
				throw new IOException("Failed to write SVG", e);
			}
		} else {			
			RenderedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, false, null);
			ImageIO.write(image, format, os);
		}

	}

}
