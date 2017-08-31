package org.osc.controller.nsc.entities;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionHookElement;

@Entity
public class InspectionHookEntity implements InspectionHookElement {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "elementId", unique = true)
    private String elementId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "inspectedPortId", nullable = true, updatable = true)
    private NetworkElementEntity inspectedPort;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "inspectionPortId", nullable = true, updatable = true)
    private InspectionPortEntity inspectionPort;
    private Long tag;
    private Long hookOrder;

    @Enumerated(STRING)
    private TagEncapsulationType encType;

    @Enumerated(STRING)
    private FailurePolicyType failurePolicyType;

    public InspectionHookEntity() {
    }

    @Override
    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public NetworkElementEntity getInspectedPort() {
        return this.inspectedPort;
    }

    public void setInspectedPort(NetworkElementEntity inspectedPort) {
        this.inspectedPort = inspectedPort;
    }

    @Override
    public InspectionPortEntity getInspectionPort() {
        return this.inspectionPort;
    }

    public void setInspectionPort(InspectionPortEntity inspectionPort) {
        this.inspectionPort = inspectionPort;
    }

    @Override
    public Long getTag() {
        return this.tag;
    }

    public void setTag(Long tag) {
        this.tag = tag;
    }

    public Long getHookOrder() {
        return this.hookOrder;
    }

    public void setHookOrder(Long hookOrder) {
        this.hookOrder = hookOrder;
    }

    @Override
    public TagEncapsulationType getEncType() {
        return this.encType;
    }

    public void setEncType(TagEncapsulationType encType) {
        this.encType = encType;
    }

    @Override
    public FailurePolicyType getFailurePolicyType() {
        return this.failurePolicyType;
    }

    public void setFailurePolicyType(FailurePolicyType failurePolicyType) {
        this.failurePolicyType = failurePolicyType;
    }

    @Override
    public Long getOrder() {
        // TODO Auto-generated method stub
        return null;
    }
}
