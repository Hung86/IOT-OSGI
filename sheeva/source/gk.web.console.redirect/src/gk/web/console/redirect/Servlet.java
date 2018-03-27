package gk.web.console.redirect;

//import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.PrintWriter;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Dictionary;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
import javax.servlet.ServletException;
//import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
//import org.osgi.framework.InvalidSyntaxException;
//import org.osgi.framework.ServiceReference;

public class Servlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BundleContext bc;
	
	public Servlet(BundleContext bc) throws IOException
	{
		this.bc = bc;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.sendRedirect("/system/console/gatewaystatus");
	}
}