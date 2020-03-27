package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.TomatoLogisticContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;

@BelongsToContract(TomatoLogisticContract.class)
@CordaSerializable
public class TomatoLogisticState implements LinearState {

    private AbstractParty carrier;
    private String logisiticId;
    private String wayBillNo;
    private LocalDate pickupDate;
    private String pickupLocation;
    private Double tempInTransit;
    private LocalDate dropDate;
    private String dropLocation;
    private String batchNo;


    public TomatoLogisticState(AbstractParty carrier, String logisiticId, String wayBillNo, LocalDate pickupDate, String pickupLocation, Double tempInTransit, LocalDate dropDate, String dropLocation,String batchNo) {
        this.carrier = carrier;
        this.logisiticId = logisiticId;
        this.wayBillNo = wayBillNo;
        this.pickupDate = pickupDate;
        this.pickupLocation = pickupLocation;
        this.tempInTransit = tempInTransit;
        this.dropDate = dropDate;
        this.dropLocation = dropLocation;
        this.batchNo=batchNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public String getLogisiticId() {
        return logisiticId;
    }

    public String getWayBillNo() {
        return wayBillNo;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setCarrier(AbstractParty carrier) {
        this.carrier = carrier;
    }

    public void setLogisiticId(String logisiticId) {
        this.logisiticId = logisiticId;
    }

    public void setWayBillNo(String wayBillNo) {
        this.wayBillNo = wayBillNo;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public void setTempInTransit(Double tempInTransit) {
        this.tempInTransit = tempInTransit;
    }

    public void setDropDate(LocalDate dropDate) {
        this.dropDate = dropDate;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public Double getTempInTransit() {
        return tempInTransit;
    }

    public LocalDate getDropDate() {
        return dropDate;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public AbstractParty getCarrier() {
        return carrier;
    }



    private static UniqueIdentifier uniqueLinearId=new UniqueIdentifier("456");

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return uniqueLinearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(carrier);
    }
}
