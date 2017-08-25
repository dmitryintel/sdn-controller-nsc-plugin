package org.osc.controller.nsc.utils;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.osc.controller.nsc.entities.InspectionHookNSCEntity;
import org.osc.controller.nsc.entities.InspectionPortNSCEntity;
import org.osc.controller.nsc.entities.MacAddressNSCEntity;
import org.osc.controller.nsc.entities.NetworkElementNSCEntity;
import org.osc.controller.nsc.entities.PortIpNSCEntity;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osgi.service.transaction.control.TransactionControl;

public class NSCUtils {

	private static final Logger LOGGER = Logger.getLogger(NSCUtils.class);	
	
	private TransactionControl txControl;
	private EntityManager em;

	public NSCUtils(EntityManager em, TransactionControl txControl) {
		this.em = em;
		this.txControl = txControl;
	}

	
	public static MacAddressNSCEntity makeMacAddr(String macAddressStr) {
    	MacAddressNSCEntity retVal = new MacAddressNSCEntity();
    	retVal.setMacAddress(macAddressStr != null ? macAddressStr : "");
    	return retVal;
    }
    
    public static PortIpNSCEntity makePortIp(String portIpStr) {
    	PortIpNSCEntity retVal = new PortIpNSCEntity ();
    	retVal.setPortIp(portIpStr != null ? portIpStr : "");
    	return retVal;
    }

    public static NetworkElementNSCEntity makeNetworkElementEntity(NetworkElement ne) {
    	NetworkElementNSCEntity retVal = new NetworkElementNSCEntity();
    	
    	if (ne == null) {
    		return retVal;
    	}
    	
    	retVal.setElementId(ne.getElementId());
    	
    	List<String> macAddrStrings = ne.getMacAddresses();
    	List<MacAddressNSCEntity> macAddrEntities = null;
    	if (macAddrStrings != null) {
    		macAddrEntities = macAddrStrings.stream().map(s -> makeMacAddr(s)).collect(toList());
    		macAddrEntities.stream().forEach(p -> { p.setElement(retVal);});
    	} 		
    	
    	List<String> portIpStrings = ne.getPortIPs();
    	List<PortIpNSCEntity> portIpEntities = null;
    	if (portIpStrings != null) {
    		portIpEntities = portIpStrings.stream().map(s -> makePortIp(s)).collect(toList());
    		portIpEntities.stream().forEach(p -> { p.setElement(retVal);});
    	} 
    	
    	retVal.setElementId(ne.getElementId());
    	retVal.setMacAddressEntities(macAddrEntities);
    	retVal.setPortIpEntities(portIpEntities);
    	
    	return retVal;
    }
    
    public static InspectionPortNSCEntity makeInspectionPortEntity(InspectionPortElement ipe) {
    	InspectionPortNSCEntity retVal  = new InspectionPortNSCEntity();
    	
    	NetworkElement ingress = ipe.getIngressPort();
    	if (ingress != null) {
    		NetworkElementNSCEntity ingressEntity = makeNetworkElementEntity(ingress);
    		ingressEntity.setIngressInspectionPort(retVal);
    		retVal.setIngress(ingressEntity);
    	}
    	NetworkElement egress = ipe.getEgressPort();
    	if (egress != null) {
    		NetworkElementNSCEntity egressEntity = makeNetworkElementEntity(egress);
    		egressEntity.setEgressInspectionPort(retVal);
    		retVal.setIngress(egressEntity);
    	}
    	
    	return retVal;    	
    }

 
    public InspectionHookNSCEntity makeInspectionHookEntity(NetworkElement inspectedPort, 
    																  InspectionPortElement inspectionPort, 
    																  Long tag,
															          TagEncapsulationType encType, 
															          Long order, 
															          FailurePolicyType failurePolicyType) {
    	InspectionPortNSCEntity inspPortEntity = makeInspectionPortEntity(inspectionPort);
    	final String elementId = inspectedPort.getElementId();
    	
    	NetworkElementNSCEntity inspected = null;
    	try {
    		inspected = this.txControl.requiresNew(() ->  em.find(NetworkElementNSCEntity.class, elementId));
    	} catch (Exception e) {
    		LOGGER.error(e.getMessage());
    		LOGGER.error(e);
    	}
    	
    	InspectionHookNSCEntity retVal = new InspectionHookNSCEntity();
    	
    	retVal.setInspectedPort(inspected);
    	retVal.setInspectionPort(inspPortEntity);
    	retVal.setHookOrder(order);
    	retVal.setTag(tag);
    	retVal.setEncType(encType.name());
    	retVal.setFailurePolicyType(failurePolicyType.name());
    	
    	inspPortEntity.setInspectionHook(retVal);
    	
    	return retVal;    	
    }

    public static NetworkElement makeNetworkElement(final NetworkElementNSCEntity entity) {
    	
    	if (entity == null) {
    		return null;
    	}
    	
    	InspectionPortNSCEntity iiPe = entity.getIngressInspectionPort();
    	InspectionPortNSCEntity eiPe = entity.getEgressInspectionPort();
    	
    	final String parent;
    	if (iiPe != null) {
    		parent = iiPe.getId() != null ? iiPe.getId().toString() : null;
    	} else {
    		parent = eiPe.getId() != null ? eiPe.getId().toString() : null;
    	}
    		
    	NetworkElement retVal = new NetworkElement() {
			private List<String> macAddresses = entity.getMacAddresses();
			private List<String> portIps = entity.getPortIps();
    		private String parentId = parent; 
    		private String elementId = entity.getElementId();
    		
			@Override
			public List<String> getPortIPs() {
				return portIps;
			}
			
			@Override
			public String getParentId() {
				return parentId;
			}
			
			@Override
			public List<String> getMacAddresses() {
				return macAddresses;
			}
			
			@Override
			public String getElementId() {
				return elementId;
			}
		};
		
		return retVal;
    }

    public static InspectionPortElement makeInspectionPortElement(InspectionPortNSCEntity entity) {

    	if (entity == null) {
    		return null;
    	}
    	
    	InspectionPortElement retVal = new InspectionPortElement() {
			private NetworkElement ingressPort = makeNetworkElement(entity.getIngress());
			private NetworkElement egressPort = makeNetworkElement(entity.getIngress());
			
			@Override
			public NetworkElement getIngressPort() {
				return ingressPort;
			}
			
			@Override
			public NetworkElement getEgressPort() {
				return egressPort;
			}
		};
		
		return retVal;
    }
    
    public static InspectionHookElement makeInspectionHookElement(final InspectionHookNSCEntity entity) { 
    	return new InspectionHookElement() {
			
			@Override
			public Long getTag() {
				return entity.getTag();
			}
			
			@Override
			public Long getOrder() {
				return entity.getHookOrder();
			}
			
			@Override
			public InspectionPortElement getInspectionPort() {
				return null; // TODO
			}
			
			@Override
			public NetworkElement getInspectedPort() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getHookId() {
				return entity.getHookId();
			}
			
			@Override
			public FailurePolicyType getFailurePolicyType() {
				
				return FailurePolicyType.fromText(entity.getFailurePolicyType());
			}
			
			@Override
			public TagEncapsulationType getEncType() {

				return TagEncapsulationType.fromText(entity.getEncType());
			}
		};
    };
    
    public NetworkElementNSCEntity networkElementEntityByElementId(String elementId) {
    	CriteriaBuilder cb = this.em.getCriteriaBuilder();
    	
        CriteriaQuery<NetworkElementNSCEntity> q = cb.createQuery(NetworkElementNSCEntity.class);
        Root<NetworkElementNSCEntity> r = q.from(NetworkElementNSCEntity.class);
        q.where(cb.equal(r.get("elementId"), elementId));

        try {
            return this.em.createQuery(q).getSingleResult();
        } catch (Exception e) {
            LOGGER.error(String.format("Finding Network Element %s ", elementId), e); // TODO
            return null;
        }
    }

    public InspectionPortNSCEntity inspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
    	return txControl.requiresNew(() -> txInspPortByNetworkElements(ingress, egress));
    }
    
    private InspectionPortNSCEntity txInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
    	String ingressId = ingress != null ? ingress.getElementId() : null;
    	String egressId = ingress != null ? egress.getElementId() : null;

    	Query q = this.em.createQuery("FROM InspectionPortNSCEntity where ingressId = :ingId and egressId = :egId ");
    	q.setParameter("ingId", ingressId);
    	q.setParameter("egId", egressId);
    	
//    	CriteriaBuilder cb = this.em.getCriteriaBuilder();
//        CriteriaQuery<InspectionPortNSCEntity> q = cb.createQuery(InspectionPortNSCEntity.class);
//        Root<InspectionPortNSCEntity> r = q.from(InspectionPortNSCEntity.class);
//        Predicate byIngress = cb.equal(r.get("ingress"), ingress);
//        Predicate byEgress = cb.equal(r.get("egress"), egress);
//        q.where(paramExpression)

        try {
            @SuppressWarnings("unchecked")
			List<InspectionPortNSCEntity> ports = q.getResultList();
            if (ports == null || ports.size() == 0) {
            	LOGGER.warn(String.format("No Inspection ports by ingress %s and egress %s", 
						ingress.getElementId(), egress.getElementId()));
            	return null;
            } else if (ports.size() < 0) {
            	LOGGER.warn(String.format("Multiple results! Inspection ports by ingress %s and egress %s", 
						ingress.getElementId(), egress.getElementId()));            	
            }
            return ports.get(0);
            
        } catch (Exception e) {
            LOGGER.error(String.format("Finding Inspection ports by ingress %s and egress %s", 
            							ingress.getElementId(), egress.getElementId()), e); // TODO
            return null;
        }
    	
    }

    public InspectionHookNSCEntity inspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement  element) {
    	return txControl.requiresNew(() -> txInspHookByInspectedAndPort(inspected, element));
    }

    private InspectionHookNSCEntity txInspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement element) {
    	CriteriaBuilder cb = this.em.getCriteriaBuilder();
    	
    	// Paranoid
    	NetworkElement ingress = element != null ? element.getIngressPort() : null;
    	NetworkElement egress = element != null ? element.getEgressPort() : null;    	
    	
    	String inspectedId = inspected != null ? inspected.getElementId() : null;
    	
    	InspectionPortNSCEntity inspPort = inspPortByNetworkElements(ingress, egress);
    	
    	Long portId = inspPort != null ? inspPort.getId() : null;
    	
        CriteriaQuery<InspectionHookNSCEntity> q = cb.createQuery(InspectionHookNSCEntity.class);
        Root<InspectionHookNSCEntity> r = q.from(InspectionHookNSCEntity.class);
        Predicate byPort = cb.equal(r.get("inspectionPortId"), portId);        
        Predicate byInspected = cb.equal(r.get("inspectedPortId"), egress.getElementId());
        q.where(cb.and(byPort, byInspected));

        try {
            List<InspectionHookNSCEntity> ports = this.em.createQuery(q).getResultList();
            if (ports == null || ports.size() == 0) {
            	LOGGER.warn(String.format("No Inspection hooks by inspected %s and port %d", 
						inspectedId, inspPort.getId()));
            	return null;
            } else if (ports.size() < 0) {
            	LOGGER.warn(String.format("Multiple results! Inspection hooks by inspected %s and port %d", 
											inspectedId, inspPort.getId()));            	
            }
            return ports.get(0);
            
        } catch (Exception e) {
            LOGGER.error(String.format("Finding Inspection hooks by inspected %s and port %d", 
										inspectedId, inspPort.getId()), e); // TODO
            return null;
        }

    }
    
    public InspectionPortNSCEntity inspectionPortEntityById(Long id) {
    	return em.find(InspectionPortNSCEntity.class, id);
    }

    public NetworkElementNSCEntity networkElementEntityById(Long id) {
    	return em.find(NetworkElementNSCEntity.class, id);
    }

    public static NetworkElement makeNetworkElement(List<MacAddressNSCEntity> mas, 
    		List<PortIpNSCEntity> ips, 
    		Long pid, Long eid) {

    	final List<String> macAddresses = mas != null ? 
    			mas.stream().map(s -> s.getMacAddress()).collect(Collectors.toList()) : null;
		final List<String> portIps = ips != null ? 
				ips.stream().map(s -> s.getPortIp()).collect(Collectors.toList()) : null;
		final String parentId = pid != null ? pid.toString() : null;
		final String elementId = eid != null ? eid.toString() : null;

		NetworkElement retVal = new NetworkElement() {
			@Override
			public List<String> getPortIPs() {
				return portIps;
			}

			@Override
			public String getParentId() {
				return parentId;
			}

			@Override
			public List<String> getMacAddresses() {
				return macAddresses;
			}

			@Override
			public String getElementId() {
				return elementId;
			}
		};

		return retVal;
    }
    
}

