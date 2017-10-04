package com.mulesoft.training.demo.components;

import org.apache.log4j.Logger;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import com.mulesoft.training.demo.exceptions.ConnectionNotAvailbleException;

public class ConnectionComponent implements Callable {
	
	private static Logger log = Logger.getLogger(ConnectionComponent.class);

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		log.info("Executing ConnectionComponent");
		
		System.out.println("Connecting to external resource...");
		
		//Emulate some processing time
		Thread.sleep(3000);
		
		//Emulate resource not available by throwing an exception
		throw new ConnectionNotAvailbleException();
	}

}
