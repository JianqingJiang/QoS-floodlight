package edu.wzu.steve.proritypath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.topology.ITopologyService;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFType;
import org.openflow.protocol.statistics.OFPortStatisticsReply;
import org.openflow.protocol.statistics.OFPortStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlowDispatcher implements IOFMessageListener, IOFSwitchListener,
		IFlowDispatchService, IFloodlightModule {
	protected static Logger log = LoggerFactory.getLogger(FlowDispatcher.class);
	// flow-mod - for use in the cookie
	// public static final int FORWARDING_APP_ID = 2; // TODO: This must be
	// managed
	// // by a global APP_ID class
	// public long appCookie = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
	// protected OFMessageDamper messageDamper;
	// protected static int OFMESSAGE_DAMPER_CAPACITY = 50000; // TODO: find
	// sweet
	// // spot
	// protected static int OFMESSAGE_DAMPER_TIMEOUT = 250; // ms
	//
	// public static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 5; // in seconds
	// public static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite

	protected IFloodlightProviderService floodlightProvider;
	protected ILinkDiscoveryService linkDiscoveryService;
	// protected ITopologyService topologyService;
	private ConcurrentMap<Long, List<OFStatistics>> ofstaticsMap = new ConcurrentHashMap<Long, List<OFStatistics>>();
	private ConcurrentMap<Long, List<OFStatistics>> preofstaticsMap = new ConcurrentHashMap<Long, List<OFStatistics>>();
	private Map<Link, Integer> linkCost = new ConcurrentHashMap<Link, Integer>();

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "flowDispatcher";
	}

	@Override
	/*
	 * ????????ģ?????˳??
	 */
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return (name.equals("linkdiscovery"));
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return name.equals("topology");
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFlowDispatchService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		// m.put(IPktinHistoryService.class, this);
		// return m;
		m.put(IFlowDispatchService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// ?ṩ???Ͻ??ṩ?????????????У????㡣??????????
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(ILinkDiscoveryService.class);
		// l.add(ITopologyService.class);
		return l;
	}

	@Override
	/*
	 * ??ʼ????Floodlight?ܹ??е?һ?֣???Ҫ???ڽ???ط???????????м???
	 */
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProvider = context
				.getServiceImpl(IFloodlightProviderService.class);
		linkDiscoveryService = context
				.getServiceImpl(ILinkDiscoveryService.class);
		System.out.println(">>>>>>Start init flow dispatcher");
		// topologyService = context.getServiceImpl(ITopologyService.class);
		// messageDamper = new OFMessageDamper(OFMESSAGE_DAMPER_CAPACITY,
		// EnumSet.of(OFType.FLOW_MOD), OFMESSAGE_DAMPER_TIMEOUT);
	}

	public static void write_to_file(String fileName, String content) {
		try {
			File file = new File(fileName);
			FileWriter writer = null;
			if (!file.exists()) {
				if (file.createNewFile()) {
					writer = new FileWriter(file);
					writer.write(content);
					writer.close();
					return;
				}

			}
			writer = new FileWriter(file);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	/*
	 * ?????Ϣ?????ౣ֤?????????ָ??OF??Ϣ
	 */
	public void startUp(FloodlightModuleContext context) {
		// TODO Auto-generated method stub
		log.debug("=====================FlowDispatcher starting");
		// ????PacketIn??Ϣ
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		log.debug("=====================FlowDispatcher started");
		// ???̣߳?Ϊ?˿?ͳ?ƿ????߳?
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// String file_path = "E:\\portStatistics.txt";
				// File file = new File("E:\","portStatistics.txt");
				while (true) {
					// 2. query weight information
					ofstaticsMap = query();
					if (ofstaticsMap != null && !ofstaticsMap.isEmpty()
							&& preofstaticsMap != null
							&& !preofstaticsMap.isEmpty()) {
						List<OFStatistics> list = null;
						OFPortStatisticsReply ofps = null;
						Map<Long, Set<Link>> linkMap = linkDiscoveryService
								.getSwitchLinks();
						Set<Link> setLink = new HashSet<Link>();
						for (Set<Link> links : linkMap.values()) {
							setLink.addAll(links);
						}
						long src;
						short srcPort;
						for (Link link : setLink) {
							src = link.getSrc();
							srcPort = link.getSrcPort();
							List<OFStatistics> preofsList = preofstaticsMap
									.get(src);
							List<OFStatistics> ofsList = ofstaticsMap.get(src);
							OFStatistics srcPortOfs = null;
							for (OFStatistics ofs : ofsList) {
								if (((OFPortStatisticsReply) ofs)
										.getPortNumber() == srcPort) {
									srcPortOfs = ofs;
									break;
								}
							}
							OFStatistics preSrcPortOfs = null;
							for (OFStatistics ofs : preofsList) {
								if (((OFPortStatisticsReply) ofs)
										.getPortNumber() == srcPort) {
									preSrcPortOfs = ofs;
									break;
								}
							}
							long result =((OFPortStatisticsReply) srcPortOfs)
									.getTransmitBytes()
									- ((OFPortStatisticsReply) preSrcPortOfs)
											.getTransmitBytes();
							System.out.println("------>>>>result = " + result);
//							if(result = )
							double dbresult = 10000000000D/result;
							System.out.println("------>>>>dbresult = " + dbresult);
							linkCost.put(link, (int) dbresult);
						}

						for (Map.Entry<Long, List<OFStatistics>> entry : ofstaticsMap
								.entrySet()) {
							System.out.println("===========Switch:"
									+ entry.getKey() + "==================");
							list = entry.getValue();
							System.out.println("OFPortStatisticsReply:");
							for (OFStatistics ofs : list) {
								ofps = (OFPortStatisticsReply) ofs;
								System.out.println("Port="
										+ ofps.getPortNumber()
										+ ",ReceiveByte="
										+ ofps.getReceiveBytes()
										+ ",TransmitByte="
										+ ofps.getTransmitBytes());
							}
						}
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});
		th.setName("Query-port-statics");
		th.start();
	}

	public Map<Link, Integer> getLinkWeight() {
		return linkCost;
	}


	public void addedSwitch(IOFSwitch sw) {
		// TODO Auto-generated method stub

	}


	public void removedSwitch(IOFSwitch sw) {
		// TODO Auto-generated method stub

	}

	public void switchPortChanged(Long switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	/*
	 * ????????OpenFlow??Ϣ?ģ?һ??ģ??ע?????OpenFlow??Ϣ???????OpenFlow??Ϣ????
	 * Floodlight?ͻ???ø?ģ???receive????
	 */
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// // TODO Auto-generated method stub
		// log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>received type of msg is "
		// + msg.getType());
		// log.debug("receive msg " + msg);
		// // ?????Ϣ????
		// if (OFType.PACKET_IN.equals(msg.getType())) {
		// Long linkSrc;
		// Long linkDst;
		// Map<String, Station> switchMap = new HashMap<String, Station>();
		// List<Line> switchLinks = new ArrayList<Line>();
		//
		// System.out.println(OFMessage.getDataAsString(sw, msg, cntx));
		//
		// for (Long switchId : linkDiscoveryService.getSwitchLinks().keySet())
		// {
		// switchMap.put(switchId.toString(),
		// new Station(switchId.toString()));
		// }
		//
		// for (Link link : linkDiscoveryService.getLinks().keySet()) {
		// linkSrc = link.getSrc();
		// linkDst = link.getDst();
		//
		// switchLinks.add(new Line(linkSrc.toString(),
		// linkDst.toString(), 10));
		// }
		//
		// // 1: count shortest path
		// System.out.println("---------->>>sw.getStringId()<<<----------" +
		// sw.getId());
		// Long swid = sw.getId();
		// Dijkstra.dijkstra(switchMap, switchLinks, swid.toString());
		//
		// boolean isPrintRoot = true;
		// for (Long switchId : linkDiscoveryService.getSwitchLinks().keySet())
		// {
		// Dijkstra.showResult(switchMap, switchId.toString(), isPrintRoot);
		// }
		//
		// // sendFlowMod((OFPacketIn) msg, sw,cntx);
		//
		// }

		return Command.CONTINUE;
	}

	// private void sendFlowMod(OFPacketIn pi, IOFSwitch sw, FloodlightContext
	// cntx) {
	// OFMatch match = new OFMatch();
	// match.loadFromPacket(pi.getPacketData(), pi.getInPort());
	//
	// // Create flow-mod based on packet-in and src-switch
	// OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory()
	// .getMessage(OFType.FLOW_MOD);
	// List<OFAction> actions = new ArrayList<OFAction>(); // Set no action to
	// // drop
	//
	// long cookie = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
	//
	// fm.setCookie(cookie).setHardTimeout((short) 0)
	// .setIdleTimeout((short) 88)
	// .setBufferId(OFPacketOut.BUFFER_ID_NONE).setMatch(match)
	// .setActions(actions).setLengthU(OFFlowMod.MINIMUM_LENGTH); //
	// +OFActionOutput.MINIMUM_LENGTH);
	//
	// try {
	// if (log.isDebugEnabled()) {
	// log.debug("write output flow-mod sw={} match={} flow-mod={}",
	// new Object[] { sw, match, fm });
	// }
	// messageDamper.write(sw, fm, cntx);
	// } catch (IOException e) {
	// log.error("Failure writing drop flow mod", e);
	// }
	// }

	// private void OutputFlowMod() {
	// OFMatch match = new OFMatch();
	// // match.setInputPort();
	// // match.setNetworkDestination(networkDestination);
	//
	// // Create flow-mod based on packet-in and src-switch
	// OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory()
	// .getMessage(OFType.FLOW_MOD);
	// List<OFAction> actions = new ArrayList<OFAction>(); // Set no action to
	// // drop
	//
	// long cookie = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
	//
	// fm.setCookie(cookie).setHardTimeout((short) 0)
	// .setIdleTimeout((short) 88)
	// .setBufferId(OFPacketOut.BUFFER_ID_NONE).setMatch(match)
	// .setActions(actions).setLengthU(OFFlowMod.MINIMUM_LENGTH); //
	// +OFActionOutput.MINIMUM_LENGTH);
	//
	// try {
	// if (log.isDebugEnabled()) {
	// log.debug("write output flow-mod sw={} match={} flow-mod={}",
	// new Object[] { sw, match, fm });
	// }
	// messageDamper.write(sw, fm, cntx);
	// } catch (IOException e) {
	// log.error("Failure writing drop flow mod", e);
	// }
	// }
	/*
	 * ͳ????Ϣ??ţ?????ͳ????Ϣ??????Ч??Ϣ??ȡ
	 */

	public ConcurrentMap<Long, List<OFStatistics>> query() {
		Map<Long, Set<Link>> linkMap = linkDiscoveryService.getSwitchLinks();
		for (Long switchId : linkMap.keySet()) {
		//	IOFSwitch sw = floodlightProvider.getSwitches().get(switchId);
			IOFSwitch sw = floodlightProvider.getSwitch(switchId);
			// System.out.println("sw = &&&&&&&&&&&&&&&&&&&" + switchId);
			Future<List<OFStatistics>> future;
			List<OFStatistics> values = null;
			if (sw != null) {
				OFStatisticsRequest req = new OFStatisticsRequest();
				req.setStatisticType(OFStatisticsType.PORT);
				OFPortStatisticsRequest specificReq = new OFPortStatisticsRequest();
				specificReq.setPortNumber((short) OFPort.OFPP_NONE.getValue());
				req.setStatistics(Collections
						.singletonList((OFStatistics) specificReq));
				int requestLength = req.getLengthU();
				requestLength += specificReq.getLength();
				req.setLengthU(requestLength);
				try {
					future = sw.queryStatistics(req);
					values = future.get(10, TimeUnit.SECONDS);
					// System.out
					// .println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<get port statics info="
					// + values);
					if (ofstaticsMap.get(switchId) != null) {
						preofstaticsMap.put(switchId,
								ofstaticsMap.get(switchId));
					}
					ofstaticsMap.put(switchId, values);
					// System.out
					// .println("------------->>>>>>switchid<<<<<<--------------"
					// + switchId + ofstaticsMap.get(switchId));
					// System.out
					// .println("------------->>>>>>switchid<<<<<<--------------"
					// + switchId + values);
				} catch (Exception e) {
					log.error(
							"Failure retrieving statistics from switch " + sw,
							e);
				}
			}
		}
		return ofstaticsMap;
	}
}
