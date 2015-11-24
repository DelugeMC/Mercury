package vg.civcraft.mc.mercury.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import vg.civcraft.mc.mercury.events.EventManager;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RabbitConsumer extends DefaultConsumer {

	private final RabbitHandler handler_;
	private final String queueName_;
	private Map<String, String> channelMap_ = new HashMap<String, String>();

	public RabbitConsumer(RabbitHandler handler, Channel channel, String queueName) {
		super(channel);
		handler_ = handler;
		queueName_ = queueName;
	}

	@Override
	public void handleDelivery(java.lang.String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
			String channelName = envelope.getExchange();
			if (channelMap_.containsKey(channelName)) {
				channelName = channelMap_.get(channelName);
			} else {
				if (!channelName.startsWith("mc.")) {
					return;
				}
				final boolean global = channelName.endsWith(".global");
				String newName;
				if (global) {
					newName = channelName.substring(3, channelName.length() - 7);
				} else {
					newName = channelName.substring(3);
				}
				channelMap_.put(channelName, newName);
				channelName = newName;
			}
			String message;
			try {
				message = new String(body, "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}
			EventManager.fireMessage(channelName, message);
	}
}