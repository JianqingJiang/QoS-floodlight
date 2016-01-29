
package edu.wzu.steve.trafficanalyser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import net.floodlightcontroller.counter.CounterValue;
import net.floodlightcontroller.counter.ICounter;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import org.restlet.resource.Get;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.counter.ICounterStoreService;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class TrafficAnalyser extends ServerResource implements IOFMessageListener, IFloodlightModule{

	public final static String BROADCAST = "broadcast";
    public final static String MULTICAST = "multicast";
    public final static String UNICAST = "unicast";


	protected ICounterStoreService counterStore;
	protected IFloodlightProviderService floodlightProvider;
	protected static Logger logger;
	

	
	 @Override
	    public String getName() {
	        // TODO Auto-generated method stub
		 return TrafficAnalyser.class.getSimpleName();
	    }
	 
	    @Override
	    public boolean isCallbackOrderingPrereq(OFType type, String name) {
	        // TODO Auto-generated method stub
	        return false;
	    }
	 
	    @Override
	    public boolean isCallbackOrderingPostreq(OFType type, String name) {
	        // TODO Auto-generated method stub
	        return false;
	    }
	 
	    @Override
	    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
	        // TODO Auto-generated method stub
	        return null;
	    }
	 
	    @Override
	    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
	        // TODO Auto-generated method stub
	        return null;
	    }
	 
	    @Override
	    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
	        // TODO Auto-generated method stub
	    	 Collection<Class<? extends IFloodlightService>> l =
	    		        new ArrayList<Class<? extends IFloodlightService>>();
	    		    l.add(IFloodlightProviderService.class);
	    		    return l;
	    }
	 
	    @Override
	    public void init(FloodlightModuleContext context)
	            throws FloodlightModuleException {
	    	this.floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	    	this.counterStore = context.getServiceImpl(ICounterStoreService.class);
	    	
	     
	        logger = LoggerFactory.getLogger(TrafficAnalyser.class);
	 
	    }
	 
	    @Override
	    public void startUp(FloodlightModuleContext context) {
	    	floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	 
	    }
	    
	    @Get("json")
	    public Map<String, Object> retrieve(){
	    
	        Map<String, Object> model = new HashMap<String,Object>();
	        CounterValue v;
	     
	            Map<String, ICounter> counters = this.counterStore.getAll();
	            if (counters != null) {
	                Iterator<Map.Entry<String, ICounter>> it = 
	                    counters.entrySet().iterator();
	                while (it.hasNext()) {
	                    Entry<String, ICounter> entry = it.next();
	                    String counterName = entry.getKey();
	                    v = entry.getValue().getCounterValue();

	                    if (CounterValue.CounterType.LONG == v.getType()) {
	                        model.put(counterName, v.getLong());
	                    } else if (v.getType() == CounterValue.CounterType.DOUBLE) {
	                        model.put(counterName, v.getDouble());
	                    }   
	                }   
	            }   
	        
	        return model;
	    }
	   protected List<String> PacketInType(IOFSwitch sw, OFMessage m, Ethernet eth) {
	        
	       
	    	
	        int l3type = eth.getEtherType() & 0xffff;
	        String switchIdHex = sw.getStringId();
	        String etherType = String.format("%04x", eth.getEtherType());
	        String packetName = m.getType().toClass().getName();
	        packetName = packetName.substring(packetName.lastIndexOf('.')+1);
	        
	        
	      

	        // L2 Type
	        String l2Type = null;
	        if (eth.isBroadcast()) {
	            l2Type = BROADCAST;
	        }
	        else if (eth.isMulticast()) {
	            l2Type = MULTICAST;
	        }
	        else {
	            l2Type = UNICAST;
	        }

	        
	       //  Use alias for L3 type
	       //  Valid EtherType must be greater than or equal to 0x0600
	       //  It is V1 Ethernet Frame if EtherType < 0x0600
	         
	        if (l3type < 0x0600) {
	            etherType = "0599";
	        }
	        if (TypeAnalyser.l3TypeAliasMap != null &&
	        	TypeAnalyser.l3TypeAliasMap.containsKey(etherType)) {
	            etherType = TypeAnalyser.l3TypeAliasMap.get(etherType);
	        }
	        else {
	            etherType = "L3_" + etherType;
	        }

 
	        // L4 counters
	        if (eth.getPayload() instanceof IPv4) {

	            // resolve protocol alias
	            IPv4 ipV4 = (IPv4)eth.getPayload();
	            String l4name = String.format("%02x", ipV4.getProtocol());
	            if (TypeAnalyser.l4TypeAliasMap != null &&
	            	TypeAnalyser.l4TypeAliasMap.containsKey(l4name)) {
	                l4name = TypeAnalyser.l4TypeAliasMap.get(l4name);
	            }
	            else {
	                l4name = "L4_" + l4name;
	            }
	            
              List<String>list = new ArrayList<String>();
              list.add(l2Type);
              list.add(etherType);
              list.add(l4name);
              
	  	      //使用Iterator迭代器遍历出集合的元素并打印
	  	      for(Iterator<String> i = list.iterator();i.hasNext(); ){
	  	      String str = (String) i.next();
	  	      System.out.println(str);
	  	      }
	        }
			return null;
	  	      
	  	     
	    }


	    
	    
	    
	    
	    
	    protected void doInit() throws ResourceException {
	        super.doInit();
	        counterStore = 
	            (ICounterStoreService)getContext().getAttributes().
	                get(ICounterStoreService.class.getCanonicalName());
	    }
	   
	    @Override
	    public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
	         
	 
   
	    	System.out.println(retrieve().toString());
	    	
	 		
	 		return Command.CONTINUE;	
	        	 
	     }
	 
	}
