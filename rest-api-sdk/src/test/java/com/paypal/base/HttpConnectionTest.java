package com.paypal.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import com.paypal.base.HttpConfiguration.FollowRedirect;
import com.paypal.base.exception.ClientActionRequiredException;
import com.paypal.base.exception.HttpErrorException;
import com.paypal.base.exception.InvalidResponseDataException;

import static com.paypal.base.HttpConfiguration.FollowRedirect.NO_DO_NOT_FOLLOW_REDIRECT;

public class HttpConnectionTest {
	HttpConnection connection;
	HttpConfiguration httpConfiguration;

	@BeforeClass
	public void beforeClass() throws MalformedURLException, IOException {
		ConnectionManager connectionMgr = ConnectionManager.getInstance();
		connection = connectionMgr.getConnection();
		httpConfiguration = new HttpConfiguration();

	}

	@Test(expectedExceptions = MalformedURLException.class)
	public void checkMalformedURLExceptionTest() throws Exception {
		httpConfiguration.setEndPointUrl("ww.paypal.in");
		connection.createAndconfigureHttpConnection(httpConfiguration);
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void readMethodTest() throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, IOException {
		Method readMethod = HttpConnection.class.getDeclaredMethod("read",
				BufferedReader.class);
		readMethod.setAccessible(true);
		BufferedReader br = null;
		readMethod.invoke(connection, br);
	}

	@Test
	public void executeTest() throws InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, IOException,
			InterruptedException {
		httpConfiguration
				.setEndPointUrl("https://svcs.sandbox.paypal.com/AdaptivePayments/ConvertCurrency");
		connection.createAndconfigureHttpConnection(httpConfiguration);
		String response = connection.execute("url", "payload", null);
		Assert.assertTrue(response.contains("<ack>Failure</ack>"));
	}

	@Test(expectedExceptions = HttpErrorException.class,
			expectedExceptionsMessageRegExp = "Response code: 500\tError response: Internal server error..*")
	public void Http500Test() throws InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, IOException,
			InterruptedException {
		httpConfiguration
				.setEndPointUrl("https://svcs.sandbox.paypal.com/AdaptivePayments");
		connection.createAndconfigureHttpConnection(httpConfiguration);
		connection.execute("url", "payload", null);
	}

	@Test(expectedExceptions = ClientActionRequiredException.class,
			expectedExceptionsMessageRegExp = ".*esponse code: 302.*")
	public void Http3xxTest() throws InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, IOException,
			InterruptedException {
		httpConfiguration.setInstanceFollowRedirects(NO_DO_NOT_FOLLOW_REDIRECT);
		httpConfiguration
				.setEndPointUrl("https://www.sandbox.paypal.com/wdfunds");
		connection.createAndconfigureHttpConnection(httpConfiguration);
		connection.execute("url", "payload", null);
	}

	// Expecting this test to have a certificate issue that prevents connection
	// Example: unable to find valid certification path to requested target
	@Test(expectedExceptions = HttpErrorException.class,
			expectedExceptionsMessageRegExp = "Response code: -1\tError response: .+")
	public void connectionFailureTest() throws InvalidResponseDataException,
			HttpErrorException, ClientActionRequiredException, IOException,
			InterruptedException {
		httpConfiguration
				.setEndPointUrl("https://example.com/AdaptivePayments/ConvertCurrency");
		connection.createAndconfigureHttpConnection(httpConfiguration);
		connection.execute("url", "payload", null);
	}

	@AfterClass
	public void afterClass() {
		connection = null;
		httpConfiguration = null;
	}

}
