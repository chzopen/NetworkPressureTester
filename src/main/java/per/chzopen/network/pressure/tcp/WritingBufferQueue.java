package per.chzopen.network.pressure.tcp;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

public class WritingBufferQueue
{

	private LinkedList<ByteBuffer> bufferList = new LinkedList<>();
	
	
	/**
	 * @param buffer: need to be added to inner list;
	 * @return size of the inner list after added parameter buffer into;
	 */
	public synchronized int add( ByteBuffer buffer )
	{
		if( buffer!=null )
		{
			bufferList.add(buffer); 
		}
		return bufferList.size();
	}
	
	public synchronized ByteBuffer[] getRemainingBufferList(int needBytes)
	{
		int totalBytes = 0;
		LinkedList<ByteBuffer> resultList = new LinkedList<>();
		
		Iterator<ByteBuffer> iter = bufferList.iterator();
		while( iter.hasNext() )
		{
			ByteBuffer buffer = iter.next();
			if( buffer==null || buffer.hasRemaining()==false )
			{
				iter.remove();
				continue;
			}
			resultList.addLast(buffer);
			totalBytes += buffer.remaining();
			if( totalBytes>=needBytes || resultList.size()>=128 )
			{
				break;
			}
		}
		return resultList.toArray(new ByteBuffer[0]);
	}
	
	public synchronized void clear()
	{
		bufferList.clear();
	}
	
}
