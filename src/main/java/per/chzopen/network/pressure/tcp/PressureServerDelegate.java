package per.chzopen.network.pressure.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PressureServerDelegate
{
	
	private static Logger logger = LoggerFactory.getLogger(PressureServerDelegate.class);

	private Properties prop;
	private PressureServer server;
	
	public PressureServerDelegate()
	{
		
	}
	
	private void _loadProperties() throws IOException
	{
		try( InputStream in = ClassLoader.getSystemResourceAsStream("server.properties") )
		{
			prop = new Properties();
			prop.load(in);
		}
	}
	
	public void start() throws IOException
	{
		_loadProperties();
		
		String host = prop.getProperty("host");
		int port    = Integer.parseInt(prop.getProperty("port"));
		
		server = new PressureServer();
		server.setEcho(true);
		server.bind(host, port);
		server.listen();
	}
	
	private static void print()
	{
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(new Runnable()
		{
			
			long lastRecv = 0;
			long lastRecvBytes = 0;
			
			public void run()
			{
				
				long deltaRecv      = PressureServer.recvCounter.get() - lastRecv;
				long deltaRecvBytes = PressureServer.recvBytesCounter.get() - lastRecvBytes;
				
				logger.info("conn={}, recv={}(+{}), recvBytes={}KB(+{}KB)", 
							PressureServer.connectionCounter, 
							PressureServer.recvCounter, 
							deltaRecv,
							PressureServer.recvBytesCounter.get()/1024,
							deltaRecvBytes/1024);
				
				lastRecv = PressureServer.recvCounter.get();
				lastRecvBytes = PressureServer.recvBytesCounter.get();
			}
		}, 
		1, 1, TimeUnit.SECONDS);
	}
	
	public static void main(String[] args) throws Exception
	{
		print();
		new PressureServerDelegate().start();
		logger.info("PressureServerDelegate started");
		Thread.sleep(Long.MAX_VALUE);
	}
	
}









