package org.keycloak.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.example.ws.Product;
import org.keycloak.example.ws.UnknownProductFault;
import org.keycloak.common.util.KeycloakUriBuilder;

/**
 * Servlet for receiving informations about products from backend JAXWS service
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ProductPortalServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        // Send jaxws request
        PrintWriter out = resp.getWriter();
        out.println("<html><head><title>Product Portal Page</title></head><body>");

        String logoutUri = KeycloakUriBuilder.fromUri("http://localhost:8080/auth").path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH)
                .queryParam("redirect_uri", "http://localhost:8181/product-portal").build("demo").toString();
        String acctUri = KeycloakUriBuilder.fromUri("http://localhost:8080/auth").path(ServiceUrlConstants.ACCOUNT_SERVICE_PATH)
                .queryParam("referrer", "product-portal").build("demo").toString();

        out.println("<p>Goto: <a href=\"/customer-portal\">customers</a> | <a href=\"" + logoutUri + "\">logout</a> | <a href=\"" + acctUri + "\">manage acct</a></p>");
        out.println("Servlet User Principal <b>" + req.getUserPrincipal() + "</b> made this request.");

        String unsecuredWsClientResponse = sendWsReq(req, "1", false);
        String securedWsClientResponse = sendWsReq(req, "1", true);
        String securedWsClient2Response = sendWsReq(req, "2", true);

        out.println("<p>Product with ID 1 - unsecured request (it should end with failure): <b>" + unsecuredWsClientResponse + "</b></p><br>");
        out.println("<p>Product with ID 1 - secured request: <b>" + securedWsClientResponse + "</b></p><br>");
        out.println("<p>Product with ID 2 - secured request: <b>" + securedWsClient2Response + "</b></p><br>");
        out.println("</body></html>");
        out.flush();
        out.close();
    }

    private String sendWsReq(HttpServletRequest req, String productId, boolean secured) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(Product.class);
        factory.setAddress("http://localhost:8282/ProductServiceCF");

        Product simpleClient = (Product)factory.create();
        java.lang.String _getProduct_productIdVal = productId;
        javax.xml.ws.Holder<java.lang.String> _getProduct_productId = new javax.xml.ws.Holder<java.lang.String>(_getProduct_productIdVal);
        javax.xml.ws.Holder<java.lang.String> _getProduct_name = new javax.xml.ws.Holder<java.lang.String>();

        // Attach Authorization header
        if (secured) {
            Client clientProxy = ClientProxy.getClient(simpleClient);

            KeycloakSecurityContext session = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            headers.put("Authorization", Arrays.asList("Bearer " + session.getTokenString()));

            clientProxy.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);
        }

        try {
            simpleClient.getProduct(_getProduct_productId, _getProduct_name);
            return String.format("Product received: id=%s, name=%s", _getProduct_productId.value, _getProduct_name.value);
        } catch (UnknownProductFault upf) {
            return "UnknownProductFault has occurred. Details: " + upf.toString();
        } catch (WebServiceException wse) {
            String error = "Can't receive product. Reason: " + wse.getMessage();
            if (wse.getCause() != null) {
                Throwable cause = wse.getCause();
                error = error + " Details: " + cause.getClass().getName() + ": " + cause.getMessage();
            }
            return error;
        }
    }
}
