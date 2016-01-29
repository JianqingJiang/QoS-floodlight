package edu.wzu.steve.proritypath;


import java.util.Map;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Link;

public interface IFlowDispatchService extends IFloodlightService {
	public Map<Link, Integer> getLinkWeight();
	// public
}

