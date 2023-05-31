package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {
    private static final Long ACCOUNT_ID = 12345678L;
    private static final int ADULT_PRICE = 20;
    private static final int CHILD_PRICE = 10;
    private static final int INFANT_PRICE = 0;
    
    TicketServiceImpl ticketService;
    
    @Mock
    TicketPaymentServiceImpl ticketPaymentServiceMock;
    
    @Mock
    SeatReservationServiceImpl seatReservationServiceMock;
    
    @Before
    public void setUp(){
        ticketService = new TicketServiceImpl();    
        
        ticketService.ticketPaymentService = ticketPaymentServiceMock;
        ticketService.seatReservationService = seatReservationServiceMock;    
    }
    
    @Test
    public void puchaseTicket_OnlyAdults(){
        //Given
        TicketTypeRequest request = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        //When
        ticketService.purchaseTickets(ACCOUNT_ID, request);
        
        //Then
        verify(ticketPaymentServiceMock, times(1)).makePayment(ACCOUNT_ID, ADULT_PRICE);
        verify(seatReservationServiceMock, times(1)).reserveSeat(ACCOUNT_ID, 1);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void puchaseTicket_OnlyChildren(){
        //Given
        TicketTypeRequest request = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        //When/Then
        ticketService.purchaseTickets(ACCOUNT_ID, request);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void puchaseTicket_OnlyInfants(){
        //Given
        TicketTypeRequest request = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        //When/Then        
        ticketService.purchaseTickets(ACCOUNT_ID, request);
    }
    
    @Test
    public void purchaseTickets_AllTicketTypes(){        
        //Given
        TicketTypeRequest[] requests = new TicketTypeRequest[3];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        requests[2] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        
        int totalPrice = (ADULT_PRICE * 4) + (CHILD_PRICE * 3) + (INFANT_PRICE * 2);

        //When
        ticketService.purchaseTickets(ACCOUNT_ID, requests);
        
        //Then
        verify(ticketPaymentServiceMock, times(1)).makePayment(ACCOUNT_ID, totalPrice);
        verify(seatReservationServiceMock, times(1)).reserveSeat(ACCOUNT_ID, 7);
    }   
    
    @Test
    public void purchaseTickets_MultipleRequestOfSameTicketTypes(){        
        //Given
        TicketTypeRequest[] requests = new TicketTypeRequest[4];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);
        requests[2] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        requests[3] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        
        int totalPrice = (ADULT_PRICE * 7) + (CHILD_PRICE * 3);

        //When
        ticketService.purchaseTickets(ACCOUNT_ID, requests);
        
        //Then
        verify(ticketPaymentServiceMock, times(1)).makePayment(ACCOUNT_ID, totalPrice);
        verify(seatReservationServiceMock, times(1)).reserveSeat(ACCOUNT_ID, 10);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_OverTwentyTickets(){
        TicketTypeRequest[] requests = new TicketTypeRequest[3];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 7);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 7);
        requests[2] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 7);

        //When/Then
        ticketService.purchaseTickets(ACCOUNT_ID, requests);
    }   
    
    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_MoreInfantsThanAdults(){
        TicketTypeRequest[] requests = new TicketTypeRequest[2];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);

        //When/Then
        ticketService.purchaseTickets(ACCOUNT_ID, requests);
    } 
    
    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_NoAdults(){
        TicketTypeRequest[] requests = new TicketTypeRequest[2];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        //When/Then
        ticketService.purchaseTickets(ACCOUNT_ID, requests);
    }     
    
    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_NoTickets(){
        TicketTypeRequest[] requests = new TicketTypeRequest[1];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);

        //When/Then
        ticketService.purchaseTickets(ACCOUNT_ID, requests);
    }  
}
