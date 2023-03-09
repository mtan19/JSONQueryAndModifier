package excomm_test;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TestingContext implements HttpHandler{

	String response;
	
	public TestingContext(String response){
		this.response = response;
	}
	
	@Override
	public void handle(HttpExchange exchange){
		try {
			exchange.sendResponseHeaders(200, response.length());
			 OutputStream os = exchange.getResponseBody();
			 os.write(response.getBytes());
		     os.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
}
