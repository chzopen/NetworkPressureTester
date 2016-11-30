package per.chzopen.network.pressure.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.WritePendingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import per.chzopen.network.pressure.threading.ThreadingEvent;

public class PressureClient
{
	
	//-------------------
	
	private static final Logger logger = LoggerFactory.getLogger(PressureClient.class);
	
	public static AtomicLong connectionCounter = new AtomicLong(0);
	public static AtomicLong recvCounter = new AtomicLong(0);
	public static AtomicLong recvBytesCounter = new AtomicLong(0);
	
	//------------------
	
	public static AsynchronousChannelGroup getAsynchronousChannelGroup(int nThreads) throws IOException
	{
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
		return AsynchronousChannelGroup.withThreadPool(executorService);
	}
	
	//-------------------

	private String host;
	private int port;

	private long sendDataFirstDelay = 0;
	private long sendDataInterval   = 30000;
	private int  sentDataLength     = 1024;
	
	private AsynchronousChannelGroup channelGroup;
	private AsynchronousSocketChannel client;
	
	private ThreadingEvent connectedOrFailedEvent = new ThreadingEvent(false);
	
	private SocketAddress localAddr = null;
	private SocketAddress remoteAddr = null;
	
	private ScheduledExecutorService scheduledService;
	private ScheduledFuture<?> sendDataFuture;
	
	private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	
	private WritingBufferQueue dataQueue = new WritingBufferQueue();
	
	
	public PressureClient(AsynchronousChannelGroup _channelGroup, ScheduledExecutorService _scheduleExecutorService)
	{
		channelGroup     = _channelGroup;
		scheduledService = _scheduleExecutorService;
	}
	
	//---------------
	
	public long getSendDataFirstDelay()
	{
		return sendDataFirstDelay;
	}

	public void setSendDataFirstDelay(long sendDataFirstDelay)
	{
		this.sendDataFirstDelay = sendDataFirstDelay;
	}

	public long getSendDataInterval()
	{
		return sendDataInterval;
	}

	public void setSendDataInterval(long sendDataInterval)
	{
		this.sendDataInterval = sendDataInterval;
	}

	public int getSentDataLength()
	{
		return sentDataLength;
	}

	public void setSentDataLength(int sentDataLength)
	{
		this.sentDataLength = sentDataLength;
	}
	
	//---------------

	public void connect(String host, int port) throws Exception
	{
		this.host = host;
		this.port = port;
		_connect();
	}


	private void _connect()
	{
		try
		{
			_closeChannel(client);
			
			client = AsynchronousSocketChannel.open(channelGroup);
			client.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			client.connect(new InetSocketAddress(host, port), null, connectCallback);
		}
		catch (IOException e)
		{
			String errMsg = String.format("connect to '%s:%s' fail: %s", host, port, e.getMessage());
			logger.error(errMsg, e);
			// 过1秒重连
			scheduledService.schedule(connectRunnable, 1, TimeUnit.SECONDS);
		}
	}
	
	private CompletionHandler<Void, Void> connectCallback = new CompletionHandler<Void, Void>()
	{
		public void completed(Void result, Void attachment)
		{
			try
			{
				localAddr  = client.getLocalAddress();
				remoteAddr = client.getRemoteAddress();
				long connCount = connectionCounter.incrementAndGet();
				logger.info(String.format("a channel connected (C:%s <--> S:%s), connectionCount=%s", localAddr, remoteAddr, connCount));
				
				long firstDelay = (sendDataFirstDelay>=0) ? sendDataFirstDelay : ThreadLocalRandom.current().nextLong(sendDataInterval);
				sendDataFuture = scheduledService.scheduleWithFixedDelay(sendDataRunnable, firstDelay, sendDataInterval, TimeUnit.MILLISECONDS);
			}
			catch (IOException e)
			{
				logger.error("", e);
			}
			connectedOrFailedEvent.set();
			_read();
		}

		public void failed(Throwable e, Void attachment)
		{
			client = null;
			String errMsg = String.format("connect to '%s:%s' fail: %s", host, port, e.getMessage());
			logger.error(errMsg);
			// 过1秒重连
			scheduledService.schedule(connectRunnable, 1, TimeUnit.SECONDS);
		}
	};
	
	private Runnable connectRunnable = new Runnable()
	{
		public void run()
		{
			_connect();
		};
	};
	
	public void waitForConnected()
	{
		connectedOrFailedEvent.waitIfFalse(-1);
	}
	
	//------------
	
	private void _read()
	{
		client.read(readBuffer, null, new CompletionHandler<Integer, Object>()
		{
			public void completed(Integer length, Object attachment)
			{
				if( length<0 )
				{
					logger.error("received {} bytes", length);
					_closeChannel(client);
					// 过1秒重连
					scheduledService.schedule(connectRunnable, 1, TimeUnit.SECONDS);
					return ;
				}
				else
				{
					try
					{
						readBuffer.clear();
						client.read(readBuffer, null, this);
					}
					catch (Exception e)
					{
						logger.error("read data fail: ", e);
						// 过1秒重连
						scheduledService.schedule(connectRunnable, 1, TimeUnit.SECONDS);
					}
				}
			}

			public void failed(Throwable e, Object attachment)
			{
				long connCount = connectionCounter.decrementAndGet();
				logger.error("connection for 'C:{} - S:{}' broken, connCount={}", localAddr, remoteAddr, connCount);
				if( sendDataFuture!=null )
				{
					sendDataFuture.cancel(false);
					sendDataFuture = null;
				}
				_closeChannel(client);
				// 过1秒重连
				scheduledService.schedule(connectRunnable, 1, TimeUnit.SECONDS);
			}
		});
	}
	
	
	//---------
	
	
	public void write(byte[] data)
	{
		int size = dataQueue.add(ByteBuffer.wrap(data));
		if( size==1 )
		{
			_startWriteToChannel();
		}
	}
	
	
	private void _startWriteToChannel()
	{
		try
		{
			if( client.isOpen()==false )
			{
				return ;
			}
			
			ByteBuffer[] bufAry = dataQueue.getRemainingBufferList(1024*64);
			if( bufAry.length==0 )
			{
				return ;
			}
			
			client.write(bufAry, 0, bufAry.length, Long.MAX_VALUE, TimeUnit.MILLISECONDS, bufAry, new CompletionHandler<Long, ByteBuffer[]>()
			{
				public void completed(Long result, ByteBuffer[] attachment)
				{
					_startWriteToChannel();
				}

				public void failed(Throwable e, ByteBuffer[] attachment)
				{
					logger.error("", e);
				}
			});
			
		}
		catch (WritePendingException e)
		{
			logger.error("", e);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}
	
	private void _closeChannel(AsynchronousSocketChannel _channel)
	{
		dataQueue.clear();
		if( _channel!=null )
		{
			try
			{
				_channel.close();
			}
			catch (IOException e)
			{
				logger.error("", e);
			}
		}
	}
	
	//------------
	
	private Runnable sendDataRunnable = new Runnable()
	{
		public void run()
		{
			if( client.isOpen() )
			{
				write(new byte[sentDataLength]);
			}
		}
	};

}
