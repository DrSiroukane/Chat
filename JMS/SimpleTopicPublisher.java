/*
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 * 
 */
/**
 * The SimpleQueueSender class consists only of a main method, 
 * which sends several messages to a queue.
 * 
 * Run this program in conjunction with SimpleQueueReceiver.
 * Specify a queue name on the command line when you run the
 * program.  By default, the program sends one message.  Specify
 * a number after the queue name to send that number of messages.
 */
import java.util.Properties;

import javax.jms.*;
import javax.naming.*;

public class SimpleTopicPublisher {

	/**
	 * Main method.
	 * 
	 * @param args
	 *            the topic used by the example and, optionally, the number of
	 *            messages to send
	 */
	public static void main(String[] args) {
		final int NUM_MSGS;
		if ((args.length < 1) || (args.length > 2)) {
			System.out.println("Usage: java SimpleTopicPublisher "
					+ "topic-name> [<number-of-messages>]");
			System.exit(1);
		}
		String topicName = new String(args[0]);
		System.out.println("Topic name is " + topicName);
		if (args.length == 2) {
			NUM_MSGS = (new Integer(args[1])).intValue();
		} else {
			NUM_MSGS = 1;
		}

		/*
		 * Create a JNDI API InitialContext object if none exists yet.
		 */
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.exolab.jms.jndi.InitialContextFactory");
		props.put(Context.PROVIDER_URL, "tcp://localhost:3035/");

		Context jndiContext = null;
		TopicConnectionFactory topicConnectionFactory = null;
		TopicConnection topicConnection = null;
		TopicSession topicSession = null;
		Topic topic = null;
		TopicPublisher topicPublisher = null;
		TextMessage message = null;

		try {
			jndiContext = new InitialContext(props);
		} catch (NamingException e) {
			System.out.println("Could not create JNDI API " + "context: "
					+ e.toString());
			System.exit(1);
		}

		System.out.println("Got jndiContext");

		/*
		 * Look up connection factory and topic. If either does not exist, exit.
		 */

		try {
			topicConnectionFactory = (TopicConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			topic = (Topic) jndiContext.lookup(topicName);

		} catch (NamingException e) {
			System.out.println("JNDI API lookup failed: " + e.toString());
			System.exit(1);
		}

		/* Create connection. */
		try {
			topicConnection = topicConnectionFactory.createTopicConnection();
			topicConnection.start();
			topicSession = topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			topicPublisher = topicSession.createPublisher(topic);
			message = topicSession.createTextMessage();
			for (int i = 0; i < NUM_MSGS; i++) {
				message.setText("This is message " + (i + 1));
				System.out.println("Sending message: " + message.getText());
				topicPublisher.send(message);
			}
			/*
			 * Send a non-text control message indicating end of messages.
			 */
			topicPublisher.send(topicSession.createMessage());

		} catch (JMSException e) {
			System.out.println("Exception occurred: " + e.toString());
		} finally {
			if (topicConnection != null) {
				try {
					topicConnection.close();
				} catch (JMSException e) {
				}
			}
		}
		// must call this as apparently OpenJM InitialCOntext is holding onto a
		// connection preventing this thread from exiting...
		System.exit(0);
	}
}