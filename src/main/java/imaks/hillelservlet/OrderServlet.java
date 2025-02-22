package imaks.hillelservlet;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import imaks.hillelservlet.entity.Order;
import imaks.hillelservlet.entity.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

public class OrderServlet extends HttpServlet {

    protected final Map<Integer, Order> orderDatabase = new HashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Order order = objectMapper.readValue(request.getReader(), Order.class);
        orderDatabase.put(order.getId(), order);

        double totalCost = order.getProducts().stream()
                .mapToDouble(Product::getCost)
                .sum();
        order.setCost(totalCost);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(objectMapper.writeValueAsString(order));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(orderDatabase.values()));
            return;
        }

        int orderId = Integer.parseInt(pathInfo.substring(1));
        Order order = orderDatabase.get(orderId);

        if (order == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(order));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int orderId = Integer.parseInt(request.getPathInfo().substring(1));
        if (!orderDatabase.containsKey(orderId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Order updatedOrder = objectMapper.readValue(request.getReader(), Order.class);

        double totalCost = updatedOrder.getProducts().stream()
                .mapToDouble(Product::getCost)
                .sum();
        updatedOrder.setCost(totalCost);

        orderDatabase.put(orderId, updatedOrder);

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(updatedOrder));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int orderId = Integer.parseInt(request.getPathInfo().substring(1));
        if (!orderDatabase.containsKey(orderId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        orderDatabase.remove(orderId);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }


}