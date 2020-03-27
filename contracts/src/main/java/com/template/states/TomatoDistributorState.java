package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.TomatoDistributorContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;

@BelongsToContract(TomatoDistributorContract.class)
@CordaSerializable
public class TomatoDistributorState implements LinearState {

    private AbstractParty distributor;
    private AbstractParty customer;

    private String distributorId;
    private String WayBillNo;
    private LocalDate receivedDate;
    private Double storageTemp;
    private String dispatchInfo;
    private LocalDate dispatchDate;
    private String batchNo;

    public TomatoDistributorState(AbstractParty distributor, AbstractParty customer, String distributorId, String wayBillNo, LocalDate receivedDate, Double storageTemp, String dispatchInfo, LocalDate dispatchDate, String batchNo) {
        this.distributor = distributor;
        this.customer = customer;
        this.distributorId = distributorId;
        WayBillNo = wayBillNo;
        this.receivedDate = receivedDate;
        this.storageTemp = storageTemp;
        this.dispatchInfo = dispatchInfo;
        this.dispatchDate = dispatchDate;
        this.batchNo = batchNo;
    }

    public AbstractParty getDistributor() {
        return distributor;
    }

    public void setDistributor(AbstractParty distributor) {
        this.distributor = distributor;
    }

    public AbstractParty getCustomer() {
        return customer;
    }

    public void setCustomer(AbstractParty customer) {
        this.customer = customer;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(String distributorId) {
        this.distributorId = distributorId;
    }

    public String getWayBillNo() {
        return WayBillNo;
    }

    public void setWayBillNo(String wayBillNo) {
        WayBillNo = wayBillNo;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Double getStorageTemp() {
        return storageTemp;
    }

    public void setStorageTemp(Double storageTemp) {
        this.storageTemp = storageTemp;
    }

    public String getDispatchInfo() {
        return dispatchInfo;
    }

    public void setDispatchInfo(String dispatchInfo) {
        this.dispatchInfo = dispatchInfo;
    }

    public LocalDate getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(LocalDate dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    private static UniqueIdentifier uniqueLinearId=new UniqueIdentifier("135");

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return uniqueLinearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(distributor);
    }
}
