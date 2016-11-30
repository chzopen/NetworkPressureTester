package per.chzopen.network.pressure.threading;

public class ThreadingEvent
{

	private boolean flag = false;
	
	private boolean waitIfFalse = true;
	
	public ThreadingEvent()
	{
		this.flag = false;
	}
	
	public ThreadingEvent(Boolean flag)
	{
		this.flag = flag;
	}
	
	
	public synchronized boolean isSet()
	{
		return flag;
	}
	
	public synchronized boolean waitIfFalse(long timeout)
	{
		waitIfFalse = true;
		if( !flag )
		{
			justWait(timeout);
		}
		return flag;
	}

	public synchronized boolean waitIfTrue(long timeout)
	{
		waitIfFalse = false;
		if( flag )
		{
			justWait(timeout);
		}
		return flag;
	}
	
	private synchronized void justWait(long timeout)
	{
		try
		{
			if( timeout>0 )
			{
				this.wait(timeout);
			}
			else
			{
				this.wait();	// wait for ever
			}
		}
		catch (InterruptedException e)
		{
		}
	}
	
	public synchronized void clear()
	{
		flag = false;
		checkNotify();
	}
	
	public synchronized void set(boolean clear)
	{
		flag = clear ? false : true;
		checkNotify();
	}
	
	private synchronized void checkNotify()
	{
		if( waitIfFalse && flag )
		{
			this.notifyAll();
		}
		else if( !waitIfFalse && !flag )
		{
			this.notifyAll();
		}
	}
	
	public synchronized void set()
	{
		set(false);
	}
	
}
