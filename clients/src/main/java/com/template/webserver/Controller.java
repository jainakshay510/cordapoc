package com.template.webserver;

import com.template.dto.*;
import com.template.flows.*;
import com.template.schema.TomatoFarmStateSchemaV1;
import com.template.states.*;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.*;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
@CrossOrigin("*")
public class Controller {

    @Autowired
    private MappingJackson2HttpMessageConverter converter;

    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
    }

    @GetMapping(value = "/templateendpoint", produces = "text/plain")
    private String templateendpoint() {
        return "Define an endpoint here.";
    }


    @PostMapping(value = "/tomatoBatchFarmGenerate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    private ResponseEntity<MainResponseDTO<String>> tomatoBatchFarmGenerate(@RequestBody FarmBatchCreateDto requestDto) {

        MainResponseDTO<String> response=new MainResponseDTO();
        String batchNo = requestDto.getBatchNo();
        if (batchNo == null) {
            response.setResponse("Query parameter 'iouValue' must be non-negative.\n");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            SignedTransaction signedTx = proxy.startTrackedFlowDynamic(TomatoBatchIssueFarmInitiator.class,
                    requestDto.getFarmId(),
                    requestDto.getBatchNo(),
                    requestDto.getLocation(),
                    requestDto.getHarvestDate(),
                    requestDto.getMeanTemp(),
                    requestDto.getMeanNitrogen(),
                    requestDto.getCategory()).getReturnValue().get();
            System.out.println("Transaction Done" + batchNo);
            logger.info(signedTx.getTx().getCommands().get(0).toString());

            response.setResponse("Transaction id "+signedTx.getId()+"committed to ledger.\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            response.setResponse(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PostMapping(value = "/farmLogisticFlow", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<MainResponseDTO<String>> farmLogisticFlow(@RequestBody Farm_LogisticFlowDto requestDto) {

        MainResponseDTO<String> responseDTO=new MainResponseDTO<>();
        String batchNo = requestDto.getBatchNo();
        String carrierName = requestDto.getCarrierName();


        if (carrierName == null) {
            responseDTO.setResponse("Query parameter 'supplierName' must not be null.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        if (batchNo == null) {
            responseDTO.setResponse("Query parameter 'iouValue' must be non-negative.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }
        CordaX500Name partyX500Name = CordaX500Name.parse(carrierName);
        Party carrier = proxy.wellKnownPartyFromX500Name(partyX500Name);

        if (!(carrier instanceof Party)) {
            responseDTO.setResponse("Party named " + carrierName +" cannot be found.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }


        try {
            SignedTransaction signedTx = proxy.startTrackedFlowDynamic(TomatoFarm_LogisticInitiator.class,
                    requestDto.getLogisticId(),
                    requestDto.getWaybillNo(),
                    requestDto.getPickupDate(),
                    requestDto.getPickupLocation(),
                    requestDto.getTempInTransit(),
                    requestDto.getDropDate(),
                    requestDto.getDropLocation(),
                    requestDto.getBatchNo(),
                    carrier).getReturnValue().get();
            System.out.println("Transaction Done" + batchNo);
            logger.info(signedTx.getTx().getCommands().get(0).toString());
            responseDTO.setResponse("Transaction id "+signedTx.getId()+"committed to ledger.\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            responseDTO.setResponse(ex.getMessage());
            return ResponseEntity.badRequest().body(responseDTO);
        }

    }
    @PostMapping(value = "/logisticDistributorFlow", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<MainResponseDTO<String>> logisticDistributorFlow(@RequestBody Logistic_DistributorFlowDto requestDto) {

        MainResponseDTO<String> responseDTO=new MainResponseDTO<>();
        String batchNo = requestDto.getBatchNo();
        String distributorName = requestDto.getDistributorName();


        if (distributorName == null) {
            responseDTO.setResponse("Query parameter 'supplierName' must not be null.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        if (batchNo == null) {
            responseDTO.setResponse("Query parameter 'iouValue' must be non-negative.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }
        CordaX500Name partyX500Name = CordaX500Name.parse(distributorName);
        Party distributor = proxy.wellKnownPartyFromX500Name(partyX500Name);

        if (!(distributor instanceof Party)) {
            responseDTO.setResponse("Party named "+ distributorName+" cannot be found.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }


        try {
            SignedTransaction signedTx = proxy.startTrackedFlowDynamic(TomatoLogistic_DistributorInitiator.class,
                   requestDto.getDistributorId(),
                    requestDto.getWayBillNo(),
                    requestDto.getReceivedDate(),
                    requestDto.getStorageTemp(),
                    requestDto.getBatchNo(),
                    distributor).getReturnValue().get();
            System.out.println("Transaction Done" + batchNo);
            logger.info(signedTx.getTx().getCommands().get(0).toString());
            responseDTO.setResponse("Transaction id "+signedTx.getId()+"committed to ledger.\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            responseDTO.setResponse(ex.getMessage());
            return ResponseEntity.badRequest().body(responseDTO);
        }

    }

    @PostMapping(value = "/distributorRestaurantFlow", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<MainResponseDTO<String>> distributorRestaurantFlow(@RequestBody Distributor_RestaurantFlowDto requestDto) {

        MainResponseDTO<String> responseDTO=new MainResponseDTO<>();
        String batchNo = requestDto.getBatchNo();
        String restaurantPartyName = requestDto.getRestaurantPartyName();


        if (restaurantPartyName == null) {
            responseDTO.setResponse("Query parameter 'supplierName' must not be null.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        if (batchNo == null) {
            responseDTO.setResponse("Query parameter 'iouValue' must be non-negative.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }
        CordaX500Name partyX500Name = CordaX500Name.parse(restaurantPartyName);
        Party restaurant = proxy.wellKnownPartyFromX500Name(partyX500Name);

        if (!(restaurant instanceof Party)) {
            responseDTO.setResponse("Party named " +restaurantPartyName +" cannot be found.\n");
            return ResponseEntity.badRequest().body(responseDTO);
        }


        try {
            SignedTransaction signedTx = proxy.startTrackedFlowDynamic(TomatoDistributor_RestaurantInitiator.class,
                   requestDto.getRestaurantId(),
                    requestDto.getName(),
                    requestDto.getWareHouseInfo(),
                    requestDto.getPurchaseOrder(),
                    requestDto.getReceivedDate(),
                    requestDto.getBatchNo(),
                    restaurant).getReturnValue().get();
            System.out.println("Transaction Done" + batchNo);
            logger.info(signedTx.getTx().getCommands().get(0).toString());
            responseDTO.setResponse("Transaction id "+signedTx.getId()+"committed to ledger.\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            responseDTO.setResponse(ex.getMessage());
            return ResponseEntity.badRequest().body(responseDTO);
        }

    }


    /*Farm states fetching api*/

    @GetMapping(value = "/farmstates/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FarmBatchCreateDtoResponse>> getAllFarmState(@PathVariable("status") String status) {

        try {
            Vault.StateStatus stateStatus;
            switch(status.toLowerCase()) {
                case "unconsumed":
                    stateStatus = Vault.StateStatus.UNCONSUMED;
                    break;
                case "consumed":
                    stateStatus = Vault.StateStatus.CONSUMED;
                    break;
                case "all":
                    stateStatus=Vault.StateStatus.ALL;
                    break;
                default:
                    throw new IllegalArgumentException("Status entered is wrong");
            }
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(stateStatus);
            // FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
            // CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
            //QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria);

            //QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<TomatoFarmState> results = proxy.vaultQueryByCriteria(generalCriteria, TomatoFarmState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            List<FarmBatchCreateDtoResponse> farmStates=results.getStates().stream().map(i->i.getState().getData()).collect(Collectors.toList()).stream().map(j->new FarmBatchCreateDtoResponse(
                    j.getFarmId(),
                    j.getBatchNo(),j.getLocation(),j.getHarvestDate(),j.getMeanTemp(),j.getMeanNitrogen(),j.getCategory(),Optional.ofNullable(j.getSupplier()).isPresent()?j.getSupplier().toString():null,
                    Optional.ofNullable(j.getSuppliedTo()).isPresent()?j.getSuppliedTo().toString():null
            )).collect(Collectors.toList());
            TomatoFarmState farmState = results.getStates().get(0).getState().getData();


//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
            //FarmBatchCreateDtoResponse response = new FarmBatchCreateDtoResponse(farmState.getFarmId(), farmState.getBatchNo(), farmState.getLocation(), farmState.getHarvestDate(), farmState.getMeanTemp(), farmState.getMeanNitrogen(), farmState.getCategory(), farmState.getSupplier().toString(), Optional.ofNullable(farmState.getSuppliedTo()).isPresent() ? farmState.getSuppliedTo().toString() : null);
            return ResponseEntity.status(HttpStatus.OK).body(farmStates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList());
        }
    }

    @GetMapping(value = "/farmbatchdetailstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FarmBatchDetailsState> getFarmBatchDetailsState(@PathVariable("batchNo") String batchNo) {

        try {

            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
//             FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
//             CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
//            QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria, Vault.StateStatus.ALL, ImmutableSet.of(FarmBatchDetailsState.class));

            //QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<FarmBatchDetailsState> tempResults=proxy.vaultQueryByCriteria(generalCriteria,FarmBatchDetailsState.class);
            // Vault.Page<FarmBatchDetailsState> results = proxy.vaultQueryByCriteria(customCriteria, FarmBatchDetailsState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());

            //FarmBatchDetailsState farmState = results.getStates().get(0).getState().getData();

            List<StateAndRef<FarmBatchDetailsState>> farmBatchDetailsStates=tempResults.getStates();
//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
            StateAndRef<FarmBatchDetailsState> inputFarmStateAndRef = farmBatchDetailsStates
                    .stream().filter(farmStateAndRef-> {
                        FarmBatchDetailsState farmState = farmStateAndRef.getState().getData();

                        return (farmState.getBatchNo().equals(batchNo));
                    }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
            FarmBatchDetailsState farmBatchState=inputFarmStateAndRef.getState().getData();
            FarmBatchDetailsState convertedFarmState=converter.getObjectMapper().convertValue(farmBatchState,FarmBatchDetailsState.class);
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
            //FarmBatchCreateDtoResponse response = new FarmBatchCreateDtoResponse(farmState.getFarmId(), farmState.getBatchNo(), farmState.getLocation(), farmState.getHarvestDate(), farmState.getMeanTemp(), farmState.getMeanNitrogen(), farmState.getCategory(), farmState.getSupplier().toString(), Optional.ofNullable(farmState.getSuppliedTo()).isPresent() ? farmState.getSuppliedTo().toString() : null);
            return ResponseEntity.status(HttpStatus.OK).body(convertedFarmState);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("No filed batchNo found");
        }
    }

    @GetMapping(value = "/farmstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TomatoFarmState> getFarmState(@PathVariable("batchNo") String batchNo) {

        try {

            //QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
            FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
            CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
            QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria, Vault.StateStatus.ALL);
            //QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<TomatoFarmState> results = proxy.vaultQueryByCriteria(customCriteria, TomatoFarmState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            TomatoFarmState farmState = results.getStates().get(0).getState().getData();
            TomatoFarmState convertedFarmState=converter.getObjectMapper().convertValue(farmState,TomatoFarmState.class);



//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
            //FarmBatchCreateDtoResponse response = new FarmBatchCreateDtoResponse(farmState.getFarmId(), farmState.getBatchNo(), farmState.getLocation(), farmState.getHarvestDate(), farmState.getMeanTemp(), farmState.getMeanNitrogen(), farmState.getCategory(), farmState.getSupplier().toString(), Optional.ofNullable(farmState.getSuppliedTo()).isPresent() ? farmState.getSuppliedTo().toString() : null);
            return ResponseEntity.status(HttpStatus.OK).body(convertedFarmState);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Bad request");
        }
    }




    @GetMapping(value = "/logisticstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LogisticStateResponseDto>> getLogisticState(@PathVariable("batchNo") String batchNo,@RequestParam(name="status" ,required= true, defaultValue = "ALL") String status) {

        try {
            Vault.StateStatus stateStatus;
            switch(status.toLowerCase()) {
                case "unconsumed":
                    stateStatus = Vault.StateStatus.UNCONSUMED;
                    break;
                case "consumed":
                    stateStatus = Vault.StateStatus.CONSUMED;
                    break;
                default:
                    stateStatus = Vault.StateStatus.ALL;
            }
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(stateStatus);
//            FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
//            CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
//            QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria);
//
//            QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<TomatoLogisticState> results = proxy.vaultQueryByCriteria(generalCriteria, TomatoLogisticState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            List<LogisticStateResponseDto> logisticStates=results.getStates().stream().map(i->i.getState().getData()).collect(Collectors.toList()).stream().map(j->new LogisticStateResponseDto(
                    j.getLogisiticId(),j.getWayBillNo(),j.getPickupDate(),j.getPickupLocation(),j.getTempInTransit(),j.getDropDate(),
                    j.getDropLocation(),j.getBatchNo(),Optional.ofNullable(j.getCarrier()).isPresent()?j.getCarrier().toString():null
            )).collect(Collectors.toList());
            //TomatoLogisticState logisticState = results.getStates().get(0).getState().getData();


//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
//            LogisticStateResponseDto response = new LogisticStateResponseDto(logisticState.getLogisiticId(),logisticState.getWayBillNo(),
//                    logisticState.getPickupDate(),
//                    logisticState.getPickupLocation(),
//                    logisticState.getTempInTransit(),
//                    logisticState.getDropDate(),
//                    logisticState.getDropLocation(),
//                    logisticState.getBatchNo(),
//                    Optional.ofNullable(logisticState.getCarrier()).isPresent() ? logisticState.getCarrier().toString() : null);
            return ResponseEntity.status(HttpStatus.OK).body(logisticStates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList());
        }
    }

    @GetMapping(value = "/distributorstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Logistic_DistributorFlowDto>> getDistributorState(@PathVariable("batchNo") String batchNo,@RequestParam(name="status" ,required= true, defaultValue = "ALL") String status) {

        try {
            Vault.StateStatus stateStatus;
            switch(status.toLowerCase()) {
                case "unconsumed":
                    stateStatus = Vault.StateStatus.UNCONSUMED;
                    break;
                case "consumed":
                    stateStatus = Vault.StateStatus.CONSUMED;
                    break;
                default:
                    stateStatus = Vault.StateStatus.ALL;
            }
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(stateStatus);
//            FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
//            CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
//            QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria);
//
//            QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<TomatoDistributorState> results = proxy.vaultQueryByCriteria(generalCriteria, TomatoDistributorState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            List<Logistic_DistributorFlowDto> distributorStates=results.getStates().stream().map(i->i.getState().getData()).collect(Collectors.toList()).stream().map(j->new Logistic_DistributorFlowDto(
                    Optional.ofNullable(j.getDistributor()).isPresent()?j.getDistributor().toString():null,
                    j.getDistributorId(),
                    j.getWayBillNo(),
                    j.getReceivedDate(),
                    j.getStorageTemp(),
                    j.getBatchNo())).collect(Collectors.toList());
            TomatoDistributorState distributorState = results.getStates().get(0).getState().getData();


//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
//            Logistic_DistributorFlowDto response=new Logistic_DistributorFlowDto(Optional.ofNullable(distributorState.getDistributor()).isPresent()?distributorState.getDistributor().toString():null,
//                                                                                distributorState.getDistributorId(),
//                                                                                distributorState.getWayBillNo(),
//                                                                                distributorState.getReceivedDate(),
//                                                                                distributorState.getStorageTemp(),
//                                                                                distributorState.getBatchNo());
          return ResponseEntity.status(HttpStatus.OK).body(distributorStates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList());
        }
    }

    @GetMapping(value = "/restaurantstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Distributor_RestaurantFlowDto>> getRestaurantState(@PathVariable("batchNo") String batchNo,@RequestParam(name="status" ,required= true, defaultValue = "ALL") String status) {

        try {
            Vault.StateStatus stateStatus;
            switch(status.toLowerCase()) {
                case "unconsumed":
                    stateStatus = Vault.StateStatus.UNCONSUMED;
                    break;
                case "consumed":
                    stateStatus = Vault.StateStatus.CONSUMED;
                    break;
                default:
                    stateStatus = Vault.StateStatus.ALL;
            }
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(stateStatus);
//            FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
//            CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
//            QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria);
//
//            QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<TomatoRestaurantState> results = proxy.vaultQueryByCriteria(generalCriteria, TomatoRestaurantState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            List<Distributor_RestaurantFlowDto> restaurantStates=results.getStates().stream().map(i->i.getState().getData()).collect(Collectors.toList()).stream().map(j->new Distributor_RestaurantFlowDto(
                              Optional.ofNullable(j.getRestaurant()).isPresent()?j.getRestaurant().toString():null,
                    j.getRestaurantId(),
                    j.getName(),
                    j.getWareHouseInfo(),
                    j.getPurchaseOrder(),
                    j.getReceivedDate(),
                    j.getBatchNo())).collect(Collectors.toList());
           // TomatoRestaurantState restaurantState = results.getStates().get(0).getState().getData();


//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
//           Distributor_RestaurantFlowDto response=new Distributor_RestaurantFlowDto(Optional.ofNullable(restaurantState.getRestaurant()).isPresent()?restaurantState.getRestaurant().toString():null,
//                   restaurantState.getRestaurantId(),
//                   restaurantState.getName(),
//                   restaurantState.getWareHouseInfo(),
//                   restaurantState.getPurchaseOrder(),
//                   restaurantState.getReceivedDate(),
//                   restaurantState.getBatchNo()
                   //);
            return ResponseEntity.status(HttpStatus.OK).body(restaurantStates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList());
        }
    }




    //For fetching batch details Logistic side

    @GetMapping(value = "/logisticbatchdetailstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LogisticBatchDetailsResponse>> getLogisticBatchDetailsState(@PathVariable("batchNo") String batchNo,@RequestParam(name="status" ,required= true, defaultValue = "ALL") String status) {

        try {
            Vault.StateStatus stateStatus;
            switch(status.toLowerCase()) {
                case "unconsumed":
                    stateStatus = Vault.StateStatus.UNCONSUMED;
                    break;
                case "consumed":
                    stateStatus = Vault.StateStatus.CONSUMED;
                    break;
                default:
                    stateStatus = Vault.StateStatus.ALL;
            }
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(stateStatus);
            // FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
            // CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
            //QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria);

            //QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<LogisticBatchDetailsState> results = proxy.vaultQueryByCriteria(generalCriteria, LogisticBatchDetailsState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            List<LogisticBatchDetailsResponse> logisticStates=results.getStates().stream().map(i->i.getState().getData()).collect(Collectors.toList()).
                    stream().map(j->converter.getObjectMapper().convertValue(j,LogisticBatchDetailsResponse.class)).collect(Collectors.toList());


            //  TomatoFarmState farmState = results.getStates().get(0).getState().getData();


//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
            //FarmBatchCreateDtoResponse response = new FarmBatchCreateDtoResponse(farmState.getFarmId(), farmState.getBatchNo(), farmState.getLocation(), farmState.getHarvestDate(), farmState.getMeanTemp(), farmState.getMeanNitrogen(), farmState.getCategory(), farmState.getSupplier().toString(), Optional.ofNullable(farmState.getSuppliedTo()).isPresent() ? farmState.getSuppliedTo().toString() : null);
            return ResponseEntity.status(HttpStatus.OK).body(logisticStates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList());
        }
    }

    @GetMapping(value = "/distributorbatchdetailstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DistributorBatchDetailsState>> getDistributorBatchDetailsState(@PathVariable("batchNo") String batchNo,@RequestParam(name="status" ,required= true, defaultValue = "ALL") String status) {

        try {
            Vault.StateStatus stateStatus;
            switch(status.toLowerCase()) {
                case "unconsumed":
                    stateStatus = Vault.StateStatus.UNCONSUMED;
                    break;
                case "consumed":
                    stateStatus = Vault.StateStatus.CONSUMED;
                    break;
                default:
                    stateStatus = Vault.StateStatus.ALL;
            }
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(stateStatus);
            // FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
            // CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
            //QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria);

            //QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<DistributorBatchDetailsState> results = proxy.vaultQueryByCriteria(generalCriteria, DistributorBatchDetailsState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            List<DistributorBatchDetailsState> distributorStates=results.getStates().stream().map(i->i.getState().getData()).map(k->converter.getObjectMapper().convertValue(k,DistributorBatchDetailsState.class)).collect(Collectors.toList());
            //  TomatoFarmState farmState = results.getStates().get(0).getState().getData();


//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
            //FarmBatchCreateDtoResponse response = new FarmBatchCreateDtoResponse(farmState.getFarmId(), farmState.getBatchNo(), farmState.getLocation(), farmState.getHarvestDate(), farmState.getMeanTemp(), farmState.getMeanNitrogen(), farmState.getCategory(), farmState.getSupplier().toString(), Optional.ofNullable(farmState.getSuppliedTo()).isPresent() ? farmState.getSuppliedTo().toString() : null);
            return ResponseEntity.status(HttpStatus.OK).body(distributorStates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList());
        }
    }

    @GetMapping(value = "/restaurantbatchdetailstate/{batchNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RestaurantBatchDetailsState>> getRestaurantBatchDetailsState(@PathVariable("batchNo") String batchNo,@RequestParam(name="status" ,required= true, defaultValue = "ALL") String status) {

        try {
            Vault.StateStatus stateStatus;
            switch(status.toLowerCase()) {
                case "unconsumed":
                    stateStatus = Vault.StateStatus.UNCONSUMED;
                    break;
                case "consumed":
                    stateStatus = Vault.StateStatus.CONSUMED;
                    break;
                default:
                    stateStatus = Vault.StateStatus.ALL;
            }
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(stateStatus);
            // FieldInfo info1BatchNo = QueryCriteriaUtils.getField("batchNo", TomatoFarmStateSchemaV1.PersistentToken.class);
            // CriteriaExpression infoBatchCriteria = Builder.equal(info1BatchNo, batchNo);
            //QueryCriteria customCriteria = new QueryCriteria.VaultCustomQueryCriteria(infoBatchCriteria);

            //QueryCriteria criteria = generalCriteria.and(customCriteria);
            Vault.Page<RestaurantBatchDetailsState> results = proxy.vaultQueryByCriteria(generalCriteria, RestaurantBatchDetailsState.class);
            //System.out.println(results.getStates().get(0).getState().getData().getBatchNo());
            List<RestaurantBatchDetailsState> restaurantStates=results.getStates().stream().map(i->i.getState().getData()).map(k->converter.getObjectMapper().convertValue(k,RestaurantBatchDetailsState.class)).collect(Collectors.toList());
            //  TomatoFarmState farmState = results.getStates().get(0).getState().getData();


//        List<StateAndRef<TomatoFarmState>> farmStateAndRefs=proxy.vaultQuery(TomatoFarmState.class).getStates();
//        StateAndRef<TomatoFarmState> inputFarmStateAndRef = farmStateAndRefs
//                .stream().filter(farmStateAndRef-> {
//                    TomatoFarmState farmState = farmStateAndRef.getState().getData();
//
//                    return (farmState.getBatchNo().equals(batchNo));
//                }).findAny().orElseThrow(() -> new IllegalArgumentException("The batch was not found."));
//        TomatoFarmState farmState=inputFarmStateAndRef.getState().getData();
//        System.out.println(inputFarmStateAndRef.getState().getData().getBatchNo());
            //FarmBatchCreateDtoResponse response = new FarmBatchCreateDtoResponse(farmState.getFarmId(), farmState.getBatchNo(), farmState.getLocation(), farmState.getHarvestDate(), farmState.getMeanTemp(), farmState.getMeanNitrogen(), farmState.getCategory(), farmState.getSupplier().toString(), Optional.ofNullable(farmState.getSuppliedTo()).isPresent() ? farmState.getSuppliedTo().toString() : null);
            return ResponseEntity.status(HttpStatus.OK).body(restaurantStates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList());
        }
    }
}