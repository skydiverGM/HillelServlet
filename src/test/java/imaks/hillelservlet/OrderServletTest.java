package imaks.hillelservlet;

import imaks.hillelservlet.entity.Order;
import imaks.hillelservlet.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServletTest {

    private OrderServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new OrderServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateOrder() throws Exception {
        Order order = new Order(1, new Date(), 0.0, List.of(
                new Product(101, "iPhone 15", 1200.50)
        ));
        String jsonOrder = objectMapper.writeValueAsString(order);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonOrder)));
        servlet.doPost(request, response);

        verify(response).setContentType("application/json");
        Order createdOrder = objectMapper.readValue(responseWriter.toString(), Order.class);

        assertEquals(1200.50, createdOrder.getCost(), 0.01);
    }

    @Test
    void testGetOrderById() throws Exception {
        Order order = new Order(1, new Date(), 1200.50, List.of(
                new Product(101, "iPhone 15", 1200.50)
        ));
        servlet.orderDatabase.put(1, order);

        when(request.getPathInfo()).thenReturn("/1");
        servlet.doGet(request, response);

        verify(response).setContentType("application/json");
        Order returnedOrder = objectMapper.readValue(responseWriter.toString(), Order.class);

        assertEquals(1, returnedOrder.getId());
        assertEquals(1200.50, returnedOrder.getCost());
    }

    @Test
    void testUpdateOrder() throws Exception {
        Order existingOrder = new Order(1, new Date(), 1000.0, List.of());
        servlet.orderDatabase.put(1, existingOrder);

        Order updatedOrder = new Order(1, new Date(), 0.0, List.of(
                new Product(103, "AirPods Pro", 249.99)
        ));
        String jsonUpdatedOrder = objectMapper.writeValueAsString(updatedOrder);

        when(request.getPathInfo()).thenReturn("/1");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonUpdatedOrder)));
        servlet.doPut(request, response);

        verify(response).setContentType("application/json");
        Order resultOrder = objectMapper.readValue(responseWriter.toString(), Order.class);

        assertEquals(1, resultOrder.getId());
        assertEquals(249.99, resultOrder.getCost());
    }

    @Test
    void testDeleteOrder() throws Exception {
        Order order = new Order(1, new Date(), 1000.0, List.of());
        servlet.orderDatabase.put(1, order);

        when(request.getPathInfo()).thenReturn("/1");
        servlet.doDelete(request, response);

        assertFalse(servlet.orderDatabase.containsKey(1));
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
