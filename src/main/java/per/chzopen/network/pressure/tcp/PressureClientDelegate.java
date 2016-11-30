package per.chzopen.network.pressure.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PressureClientDelegate
{

	private static Logger logger = LoggerFactory.getLogger(PressureClientDelegate.class);

	private Properties prop;
	private PressureClient client;
	
	public PressureClientDelegate()
	{
		
	}
	
	private void _loadProperties() throws IOException
	{
		try( InputStream in = ClassLoader.getSystemResourceAsStream("client.properties") )
		{
			prop = new Properties();
			prop.load(in);
		}
	}
	
	public void start() throws Exception
	{
		_loadProperties();
		
		String host     = prop.getProperty("host");
		int port        = Integer.parseInt(prop.getProperty("port"));
		
		int connections = Integer.parseInt(prop.getProperty("connections"));
		
		long sendDataFirstDelay = Long.parseLong(prop.getProperty("sendDataFirstDelay"));
		long sendDataInterval   = Long.parseLong(prop.getProperty("sendDataInterval"));
		int sentDataLength      = Integer.parseInt(prop.getProperty("sentDataLength"));
		
		AsynchronousChannelGroup group = PressureClient.getAsynchronousChannelGroup(1);
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		
		for( int i=0; i<connections; i++ )
		{
			client = new PressureClient(group, service);
			client.setSendDataFirstDelay(sendDataFirstDelay);
			client.setSendDataInterval(sendDataInterval);
			client.setSentDataLength(sentDataLength);
			client.connect(host, port);
		}
	}
	
	private static void print()
	{
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
			}
		}, 
		1, 1, TimeUnit.SECONDS);
	}
	
	public static void main(String[] args) throws Exception
	{
		print();
		new PressureClientDelegate().start();
		logger.info("PressureClientDelegate started");
		Thread.sleep(Long.MAX_VALUE);
	}
	
}
