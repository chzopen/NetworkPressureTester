package per.chzopen.network.pressure.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.channels.WritePendingException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PressureChannel
{
	
	private static Logger logger = LoggerFactory.getLogger(PressureChannel.class);

	//----------------

	private PressureChannel _this = this;
	
	private boolean echo;
	
	private ByteBuffer readBuf = ByteBuffer.allocate(1024);
	
	private PressureServer echoServer;
	private AsynchronousSocketChannel channel;
	private WritingBufferQueue sendQueue;
	
	
	private String localAddress;
	private String remoteAddress;
	
	
	public PressureChannel(PressureServer _echoServer, AsynchronousSocketChannel _channel, boolean _echo) throws IOException
	{
		echoServer = _echoServer;
		channel    = _channel;
		echo       = _echo;
		
		InetSocketAddress soLocalAddr  = (InetSocketAddress)channel.getLocalAddress();
		InetSocketAddress soRemoteAddr = (InetSocketAddress)channel.getRemoteAddress();
		
		localAddress  = soLocalAddr.getHostName()+":"+soLocalAddr.getPort();
		remoteAddress = soRemoteAddr.getHostName()+":"+soRemoteAddr.getPort();
		
        sendQueue = new WritingBufferQueue();
        sendQueue.clear();
	}
	
	//--------------

	public AsynchronousSocketChannel getChannel()
	{
		return channel;
	}

	public String getLocalAddress()
	{
		return localAddress;
	}

	public String getRemoteAddress()
	{
		return remoteAddress;
	}

	//------------

	public void read()
	{
		_read();
	}
	
	private void _read()
	{
		try
		{
			readBuf.clear();
			channel.read(readBuf, null, new CompletionHandler<Integer, Object>()
			{
				public void completed(Integer result, Object attachment)
				{
					if( result<0 )
					{
						logger.error("received {} bytes", result);
						_closeChannel(channel);
						echoServer._friendly_onChannelClosed(_this);
						return ;
					}
					else
					{

						PressureServer.recvCounter.incrementAndGet();
						PressureServer.recvBytesCounter.addAndGet(result);
						
						if( echo )
						{
							write(readBuf.array(), 0, readBuf.position());
						}
						
						readBuf.clear();
						_read();
					}
				}

				public void failed(Throwable exc, Object attachment)
				{
					logger.error("pressure channel read fail: ", exc);
					_closeChannel(channel);
					echoServer._friendly_onChannelClosed(_this);
				};
			});
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}
	
	public void write(byte[] _data, int _offset, int _length)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(_data, _offset, _length);
		int size = sendQueue.add(byteBuffer);
		if( size==1 )
		{
			startWriteToChannel();
		}
	}
	
	private void startWriteToChannel()
	{
		try
		{
			if( channel.isOpen()==false )
			{
				return ;
			}
			
			ByteBuffer[] bufAry = sendQueue.getRemainingBufferList(1024*64);
			if( bufAry.length==0 )
			{
				return ;
			}
			
			channel.write(bufAry, 0, bufAry.length, Long.MAX_VALUE, TimeUnit.MILLISECONDS, null, new CompletionHandler<Long, ByteBuffer[]>()
			{
				public void completed(Long result, ByteBuffer[] attachment)
				{
					startWriteToChannel();
				}

				public void failed(Throwable e, ByteBuffer[] attachment)
				{
					logger.error("", e);
				}
			});
			
		}
		catch(ShutdownChannelGroupException e)
		{
			logger.error("", e);
			_closeChannel(channel);
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
	
	private static void _closeChannel(AsynchronousSocketChannel _channel)
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
