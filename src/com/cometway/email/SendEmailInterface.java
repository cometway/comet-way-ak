
package com.cometway.email;

/**
* This interface describes functionality for sending email messages.
*/

public interface SendEmailInterface
{
	/**
	* Sends an email message using information from the passed
	* com.cometway.message.IMessage instance.
	*/

        public void sendEmailMessage(IMessage m);
}
