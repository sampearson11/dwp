package uk.gov.dwp.uc.pairtest;

import java.util.HashMap;
import java.util.Map;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    private static final int ADULT_PRICE = 20;
    private static final int CHILD_PRICE = 10;
    private static final int INFANT_PRICE = 0;
    
    TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
    SeatReservationService seatReservationService = new SeatReservationServiceImpl(); 
    
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        HashMap<TicketTypeRequest.Type, Integer> ticketTypes = new HashMap(); 
        
        int totalTickets = 0;
        
        for(TicketTypeRequest request : ticketTypeRequests){
            TicketTypeRequest.Type type = request.getTicketType();
            int noOfTickets = request.getNoOfTickets();
            
            totalTickets += noOfTickets;
            ticketTypes.put(type, ticketTypes.getOrDefault(type, 0) + noOfTickets);
        }
        
        if(totalTickets <= 0){
            throw new InvalidPurchaseException();  
            
        } else if (totalTickets > 20){
            throw new InvalidPurchaseException();
            
        } else if(ticketTypes.getOrDefault(TicketTypeRequest.Type.ADULT, 0) <= 0){
            throw new InvalidPurchaseException();
            
        } else if(ticketTypes.getOrDefault(TicketTypeRequest.Type.ADULT, 0) < 
                ticketTypes.getOrDefault(TicketTypeRequest.Type.INFANT, 0)){
            throw new InvalidPurchaseException();
        }
        
        int totalPrice = calculateTotalPrice(ticketTypes);
        int totalSeats = calculateNumberOfSeats(ticketTypes);
        
        ticketPaymentService.makePayment(accountId, totalPrice);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }  
    
    private int calculateTotalPrice(HashMap<TicketTypeRequest.Type, Integer> ticketTypes){
        int total = 0;
        for(Map.Entry<TicketTypeRequest.Type, Integer> entry : ticketTypes.entrySet()){
            total += (getTicketPrice(entry.getKey()) * entry.getValue());
        }
        return total;
    }    
    
    private int getTicketPrice(TicketTypeRequest.Type type){
        switch (type) {
            case ADULT:
                return ADULT_PRICE;
            case CHILD:
                return CHILD_PRICE;
            case INFANT:
                return INFANT_PRICE;
        }
        throw new InvalidPurchaseException();
    }
    
    private int calculateNumberOfSeats(HashMap<TicketTypeRequest.Type, Integer> ticketTypes){
        int totalSeats = 0;
        for(Map.Entry<TicketTypeRequest.Type, Integer> entry : ticketTypes.entrySet()){
            if(TicketTypeRequest.Type.INFANT.equals(entry.getKey()))
                continue;
            
            totalSeats += entry.getValue();            
        }
        return totalSeats;
    }
}
