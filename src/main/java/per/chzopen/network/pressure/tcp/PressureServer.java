package per.chzopen.network.pressure.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PressureServer
{

	private static Logger logger = LoggerFactory.getLogger(PressureServer.class);
	
	public static AtomicLong connectionCounter = new AtomicLong(0);
	public static AtomicLong recvCounter = new AtomicLong(0);
	public static AtomicLong recvBytesCounter = new AtomicLong(0);
	
	//-----------

	private PressureServer _this = this;
	
	private final ConcurrentHashMap<String, PressureChannel> mapChannels = new ConcurrentHashMap<>();

	private boolean echo = false;
	private String host;
	private int port;
	
	private AsynchronousServerSocketChannel server;

	public PressureServer()
	{
	}
	
	//----------

	public boolean isEcho()
	{
		return echo;
	}

	public void setEcho(boolean echo)
	{
		this.echo = echo;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}
	
	//----------

	public void bind(String _host, int _port)
	{
		host = _host;
		port = _port;
	}

	public void listen()
	{
		try
		{
			ExecutorService executorService = Executors.newFixedThreadPool(1);
			AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withThreadPool(executorService);

			server = AsynchronousServerSocketChannel.open(threadGroup);
			server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			server.bind(new InetSocketAddress(host, port));
			logger.info("Echo server listening on {}:{}", host, port);
			server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>()
			{
				public void completed(AsynchronousSocketChannel channel, Object attachment)
				{
					try
					{
						channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
						channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
						PressureChannel echoChannel = new PressureChannel(_this, channel, echo);
						mapChannels.put(echoChannel.getRemoteAddress(), echoChannel);
						connectionCounter.set(mapChannels.size());
						logger.info("a channel connected (C:{} - S:{}), conn={}", echoChannel.getLocalAddress(), echoChannel.getRemoteAddress(), connectionCounter);
						echoChannel.read();
						server.accept(null, this);
					}
					catch (IOException e)
					{
						logger.error("", e);
					}
				}

				public void failed(Throwable exc, Object attachment)
				{
					logger.error("server accept failed", exc);
				}
			});
		
		}
		catch (IOException e)
		{
			logger.info("listen() error", e);
		}
	}

	void _friendly_onChannelClosed(PressureChannel _channel)
	{
		mapChannels.remove(_channel.getRemoteAddress().toString());
		connectionCounter.set(mapChannels.size());
		logger.info("a channel disconnected (C:{} - S:{}), conn={}", _channel.getLocalAddress(), _channel.getRemoteAddress(), connectionCounter);
	}
	
}