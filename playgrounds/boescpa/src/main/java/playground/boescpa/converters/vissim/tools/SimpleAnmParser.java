/*
 * *********************************************************************** *
 * project: org.matsim.*                                                   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package playground.boescpa.converters.vissim.tools;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.core.utils.misc.Counter;
import org.xml.sax.Attributes;

import java.util.Stack;

/**
 * Parses a Visum-ANM-File and returns all nodes and links to node- and link-handler respectively.
 * The returned nodes and links are held very simple.
 *
 * This implementation follows playground.scnadine.converters.osmCore.OsmXmlParser by mrieser.
 *
 * @author boescpa
 */
public class SimpleAnmParser extends MatsimXmlParser {

	private final AnmNodeHandler nodeHandler;
	private final AnmLinkHandler linkHandler;
	private AnmNode currentNode = null;
	private AnmLink currentLink = null;
	private final Counter nodeCounter = new Counter("node ");
	private final Counter linkCounter = new Counter("link ");

	public SimpleAnmParser(AnmHandler handler) {
		super();
		if (handler instanceof AnmNodeHandler) {
			this.nodeHandler = (AnmNodeHandler) handler;
		} else {
			this.nodeHandler = null;
		}
		if (handler instanceof AnmLinkHandler) {
			this.linkHandler = (AnmLinkHandler) handler;
		} else {
			this.linkHandler = null;
		}
		this.setValidating(false);
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if ("NODE".equals(name) & this.nodeHandler != null) {
			Id id = new IdImpl(Long.parseLong(atts.getValue("NO")));
			double xcoord = Double.parseDouble(atts.getValue("XCOORD"));
			double ycoord = Double.parseDouble(atts.getValue("YCOORD"));
			this.currentNode = new AnmNode(id, new CoordImpl(xcoord, ycoord));
		} else if ("LINK".equals(name) & this.linkHandler != null) {
			Id id = new IdImpl(atts.getValue("ID"));
			Id fromNode = new IdImpl(Long.parseLong(atts.getValue("FROMNODENO")));
			Id toNode = new IdImpl(Long.parseLong(atts.getValue("TONODENO")));
			this.currentLink = new AnmLink(id, fromNode, toNode);
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
		if ("NODE".equals(name) & this.nodeHandler != null) {
			this.nodeHandler.handleNode(this.currentNode);
			this.currentNode = null;
			this.nodeCounter.incCounter();
		} else if ("LINK".equals(name) & this.linkHandler != null) {
			this.linkHandler.handleLink(this.currentLink);
			this.currentLink = null;
			this.linkCounter.incCounter();
		}
	}

	public interface AnmHandler{}

	public interface AnmNodeHandler extends AnmHandler {
		void handleNode(AnmNode anmNode);
	}

	public interface AnmLinkHandler extends AnmHandler {
		void handleLink(AnmLink anmLink);
	}

	public static class AnmNode {
		public final Id id;
		public final Coord coord;
		public AnmNode(Id id, Coord coord) {
			this.id = id;
			this.coord = coord;
		}
	}

	public static class AnmLink {
		public final Id id;
		public final Id fromNode;
		public final Id toNode;
		public AnmLink(Id id, Id fromNode, Id toNode) {
			this.id = id;
			this.fromNode = fromNode;
			this.toNode = toNode;
		}
	}
}
