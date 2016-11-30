package per.chzopen.network.pressure.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 字节处理实用工具类,基本数据类型转换(主要是byte和其它类型之间的互转).
 *
 * @auther Awens
 * 创建日期:2015-05-08
 * 修改日期:2015-05-08
 */
public class ByteUtils
{

	//private static final String HEX_STRING = "0123456789ABCDEF";

	//<<<<< 字节转换函数
	
	
	public static boolean equals(byte[] bytes1, int offset1, int length1, byte[] bytes2, int offset2, int length2)
	{
		if( bytes1==bytes2 )
		{
			return true;
		}
		if( bytes1==null || bytes2==null )
		{
			return false;
		}
		if( length1!=length2 )
		{
			return false;
		}
		for( int i=0; i<length1; i++ )
		{
			byte b1 = bytes1[offset1+i];
			byte b2 = bytes2[offset2+i];
			if( b1!=b2 )
			{
				return false;
			}
		}
		return true;
	}
	

	public static boolean equals(byte[] bytes1, byte[] bytes2)
	{
		return equals(bytes1, 0, bytes1.length, bytes2, 0, bytes2.length);
	}

	public static boolean equals(byte[] bytes1, int offset1, int length1, byte[] bytes2)
	{
		return equals(bytes1, offset1, length1, bytes2, 0, bytes2.length);
	}

	public static boolean equals(byte[] bytes1, byte[] bytes2, int offset2, int length2)
	{
		return equals(bytes1, 0, bytes1.length, bytes2, offset2, length2);
	}

	/**
	 * 字节转换为十六进制字符串
	 *
	 * @param value byte,字节
	 * @return 十六进制字符串
	 * @author Awens
	 * @version 1.0
	 */
	public static String byteToHexStr(byte value)
	{
		String result = "";
		int iv = value & 0xFF;
		;
		result = Integer.toHexString(iv).toUpperCase();
		if (result.length() == 1)
		{
			result = '0' + result;
		}
		return result;
	}

	/**
	 * 
	 */
	public static String intToHexString(int data)
	{
		return bytesToHexString(intToByte4(data), false);
	}

	/**
	 * 
	 */
  	public static String bytesToHexString(byte[] value, boolean isReverse)
	{
		return bytesToHexString(value, 0, value.length, isReverse);
	}
  	
  	/**
  	 * 将字节数组转换成16进制字符串表示。
  	 *
  	 * @param value       byte{],字节数组
  	 * @param isReverse boolean,是否反转字节数组输出，即后进先出
  	 * @return 十六进制字符串
  	 * @author Awens
  	 * @version 1.0
  	 */
  	public static String bytesToHexString(byte[] value, int start, int end, boolean isReverse)
	{
		StringBuilder stringBuilder = new StringBuilder("");
		if (value == null )
		{
			return null;
		}
		if( value.length==0 )
		{
			return "";
		}

		if( isReverse )
		{
			for( int i=end-1; i>=start; i-- )
			{
				int v = value[i] & 0xFF;
				stringBuilder.append( StringUtils.leftPad(Integer.toHexString(v), 2, '0') );
			}
		}
		else
		{
			for( int i=start; i<end; i++ )
			{
				int v = value[i] & 0xFF;
				stringBuilder.append( StringUtils.leftPad(Integer.toHexString(v), 2, '0') );
			}
		}
		return stringBuilder.toString().toUpperCase();
	}

  	/**
  	 * 将十六进制的字节字符串转换为字节数组
  	 *
  	 * @param value     string to convert
  	 * @param isReverse 生成的数组是否反向排序，限字节数组从高字节开始排
  	 * @return byte[], 字节数组
  	 * @author Awens
  	 * @version 1.0
  	 */
	public static byte[] bytesFromHexStr(String value, boolean isReverse)
	{
		String s = value.toUpperCase();
		if (s.length() % 2 != 0)
		{
			s = "0" + value;
		}
		byte[] b = new byte[s.length() / 2];
		int vi;
		int hexStartIndex, hexEndIndex;

		int bytesLength = b.length;
		for (int i = 0; i < bytesLength; ++i)
		{
			// < 高字节在前
			if (isReverse)
			{
				hexStartIndex = (bytesLength - 1 - i) * 2;
				hexEndIndex = (bytesLength - i) * 2;
			}
			// > 高字节在前
			// < 低字节在前
			else
			{
				hexStartIndex = i * 2;
				hexEndIndex = (i + 1) * 2;
			}
			// > 低字节在前
			vi = Integer.valueOf(s.substring(hexStartIndex, hexEndIndex), 16);
			b[i] = (byte) vi;
		}
		return b;
	}

  //byte 与 int 的相互转换
  public static byte intToByte(int x) {
    return (byte) x;
  }

  public static int byteToInt(byte b) {
    //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
    return b & 0xFF;
  }

  //byte 数组与 int 的相互转换
  public static int byteArrayToInt(byte[] b) {
    return b[3] & 0xFF |
        (b[2] & 0xFF) << 8 |
        (b[1] & 0xFF) << 16 |
        (b[0] & 0xFF) << 24;
  }

  public static byte[] intToByteArray(int a) {
    return new byte[]{
        (byte) ((a >> 24) & 0xFF),
        (byte) ((a >> 16) & 0xFF),
        (byte) ((a >> 8) & 0xFF),
        (byte) (a & 0xFF)
    };
  }

  //byte 数组与 long 的相互转换
  public static long byteArrayTolong(byte[] b) {
    return
        b[7] & 0xFF |
            (b[6] & 0xFF) << 8 |
            (b[5] & 0xFF) << 16 |
            (b[4] & 0xFF) << 24 |
            (b[3] & 0xFF) << 32 |
            (b[2] & 0xFF) << 40 |
            (b[1] & 0xFF) << 48 |
            (b[0] & 0xFF) << 56;
  }

  public static byte[] longToByteArray(int a) {
    return new byte[]{
        (byte) ((a >> 56) & 0xFF),
        (byte) ((a >> 48) & 0xFF),
        (byte) ((a >> 40) & 0xFF),
        (byte) ((a >> 32) & 0xFF),
        (byte) ((a >> 24) & 0xFF),
        (byte) ((a >> 16) & 0xFF),
        (byte) ((a >> 8) & 0xFF),
        (byte) (a & 0xFF)
    };
  }

  /**
   * 将4个byte数字组成的数组合并为一个float数.
   *
   * @param arr
   * @return
   */
  public static float byte4ToFloat(byte[] arr) {
    if (arr == null || arr.length != 4) {
      throw new IllegalArgumentException("byte数组必须不为空,并且是4位!");
    }
    int i = byte4ToInt(arr);
    return Float.intBitsToFloat(i);
  }

  /**
   * 将一个float数字转换为4个byte数字组成的数组.
   *
   * @param f
   * @return
   */
  public static byte[] floatToByte4(float f) {
    int i = Float.floatToIntBits(f);
    return intToByte4(i);
  }

  /**
   * 将八个byte数字组成的数组转换为一个double数字.
   * </pre>
   *
   * @param arr
   * @return
   */
  public static double byte8ToDouble(byte[] arr) {
    if (arr == null || arr.length != 8) {
      throw new IllegalArgumentException("byte数组必须不为空,并且是8位!");
    }
    long l = byte8ToLong(arr);
    return Double.longBitsToDouble(l);
  }

  /**
   * 将一个double数字转换为8个byte数字组成的数组.
   *
   * @param i
   * @return
   */
  public static byte[] doubleToByte8(double i) {
    long j = Double.doubleToLongBits(i);
    return longToByte8(j);
  }

  /**
   * 将一个char字符转换为两个byte数字转换为的数组.
   * </pre>
   *
   * @param c
   * @return
   */
  public static byte[] charToByte2(char c) {
    byte[] arr = new byte[2];
    arr[0] = (byte) (c >> 8);
    arr[1] = (byte) (c & 0xff);
    return arr;
  }

  /**
   * 将2个byte数字组成的数组转换为一个char字符.
   *
   * @param arr
   * @return
   */
  public static char byte2ToChar(byte[] arr) {
    if (arr == null || arr.length != 2) {
      throw new IllegalArgumentException("byte数组必须不为空,并且是2位!");
    }
    return (char) (((char) (arr[0] << 8)) | ((char) arr[1]));
  }

  /**
   * 将一个16位的short转换为长度为2的8位byte数组.*
   *
   * @param s
   * @return
   */
  public static byte[] shortToByte2(Short s) {
    byte[] arr = new byte[2];
    arr[0] = (byte) (s >> 8);
    arr[1] = (byte) (s & 0xff);
    return arr;
  }

  /**
   * 长度为2的8位byte数组转换为一个16位short数字.
   *
   * @param arr
   * @return
   */
  public static short byte2ToShort(byte[] arr) {
    if (arr != null && arr.length != 2) {
      throw new IllegalArgumentException("byte数组必须不为空,并且是2位!");
    }
    return (short) (((short) arr[0] << 8) | ((short) arr[1] & 0xff));
  }

	/**
	 * 长度为2的8位byte数组转换为一个16位short数字.
	 *
	 * @param arr
	 * @return
	 */
	public static short byte2ToShort(byte[] arr, int offset)
	{
		return 	(short)(
				((short) arr[offset] << 8) | ((short) arr[offset + 1] & 0xff)
				);
	}

  /**
   * 将short转换为长度为16的byte数组.
   * 实际上每个8位byte只存储了一个0或1的数字
   * 比较浪费.
   *
   * @param s
   * @return
   */
  public static byte[] shortToByte16(short s) {
    byte[] arr = new byte[16];
    for (int i = 15; i >= 0; i--) {
      arr[i] = (byte) (s & 1);
      s >>= 1;
    }
    return arr;
  }

  public static short byte16ToShort(byte[] arr) {
    if (arr == null || arr.length != 16) {
      throw new IllegalArgumentException("byte数组必须不为空,并且长度为16!");
    }
    short sum = 0;
    for (int i = 0; i < 16; ++i) {
      sum |= (arr[i] << (15 - i));
    }
    return sum;
  }

  /**
   * 将32位int转换为由四个8位byte数字.
   *
   * @param sum
   * @return
   */
  public static byte[] intToByte4(int sum) {
    byte[] arr = new byte[4];
    arr[0] = (byte) (sum >> 24);
    arr[1] = (byte) (sum >> 16);
    arr[2] = (byte) (sum >> 8);
    arr[3] = (byte) (sum & 0xff);
    return arr;
  }

  /**
   * 将长度为4的8位byte数组转换为32位int.
   *
   * @param arr
   * @return
   */
  public static int byte4ToInt(byte[] arr) {
    if (arr == null || arr.length != 4) {
      throw new IllegalArgumentException("byte数组必须不为空,并且是4位!");
    }
    return (int) (((arr[0] & 0xff) << 24) | ((arr[1] & 0xff) << 16) | ((arr[2] & 0xff) << 8) | ((arr[3] & 0xff)));
  }

	/**
	 * 将长度为4的8位byte数组转换为32位int.
	 *
	 * @param arr
	 * @return
	 */
	public static int byte4ToInt(byte[] arr, int offset)
	{
		return 	(int) (
				((arr[offset] & 0xff) << 24) | 
				((arr[offset + 1] & 0xff) << 16) | 
				((arr[offset + 2] & 0xff) << 8) | 
				((arr[offset + 3] & 0xff))
				);
	}
  
  /**
   * 将长度为8的8位byte数组转换为64位long.
   * </pre>
   * <p/>
   * 0xff对应16进制,f代表1111,0xff刚好是8位 byte[]
   * arr,byte[i]&0xff刚好满足一位byte计算,不会导致数据丢失. 如果是int计算. int[] arr,arr[i]&0xffff
   *
   * @param arr
   * @return
   */
  public static long byte8ToLong(byte[] arr) {
    if (arr == null || arr.length != 8) {
      throw new IllegalArgumentException("byte数组必须不为空,并且是8位!");
    }
    return (long) (((long) (arr[0] & 0xff) << 56) | ((long) (arr[1] & 0xff) << 48) | ((long) (arr[2] & 0xff) << 40)
        | ((long) (arr[3] & 0xff) << 32) | ((long) (arr[4] & 0xff) << 24)
        | ((long) (arr[5] & 0xff) << 16) | ((long) (arr[6] & 0xff) << 8) | ((long) (arr[7] & 0xff)));
  }

	/**
	 * 将长度为8的8位byte数组转换为64位long.
	 * </pre>
	 * <p/>
	 * 0xff对应16进制,f代表1111,0xff刚好是8位 byte[]
	 * arr,byte[i]&0xff刚好满足一位byte计算,不会导致数据丢失. 如果是int计算. int[] arr,arr[i]&0xffff
	 *
	 * @param arr
	 * @return
	 */
	public static long byte8ToLong(byte[] arr, int offset)
	{
		return 	(long) (
				((long) (arr[offset + 0] & 0xff) << 56) | 
				((long) (arr[offset + 1] & 0xff) << 48) |
				((long) (arr[offset + 2] & 0xff) << 40) | 
				((long) (arr[offset + 3] & 0xff) << 32) | 
				((long) (arr[offset + 4] & 0xff) << 24) | 
				((long) (arr[offset + 5] & 0xff) << 16) | 
				((long) (arr[offset + 6] & 0xff) << 8)  | 
				((long) (arr[offset + 7] & 0xff))
				);
	}

  /**
   * 将一个long数字转换为8个byte数组组成的数组.
   */
  public static byte[] longToByte8(long sum) {
    byte[] arr = new byte[8];
    arr[0] = (byte) (sum >> 56);
    arr[1] = (byte) (sum >> 48);
    arr[2] = (byte) (sum >> 40);
    arr[3] = (byte) (sum >> 32);
    arr[4] = (byte) (sum >> 24);
    arr[5] = (byte) (sum >> 16);
    arr[6] = (byte) (sum >> 8);
    arr[7] = (byte) (sum & 0xff);
    return arr;
  }

  /**
   * 将int转换为32位byte.
   * 实际上每个8位byte只存储了一个0或1的数字
   * 比较浪费.
   * </pre>
   *
   * @param num
   * @return
   */
  public static byte[] intToByte32(int num) {
    byte[] arr = new byte[32];
    for (int i = 31; i >= 0; i--) {
      // &1 也可以改为num&0x01,表示取最地位数字.
      arr[i] = (byte) (num & 1);
      // 右移一位.
      num >>= 1;
    }
    return arr;
  }

  /**
   * 将长度为32的byte数组转换为一个int类型值.
   * 每一个8位byte都只存储了0或1的数字.
   *
   * @param arr
   * @return
   */
  public static int byte32ToInt(byte[] arr) {
    if (arr == null || arr.length != 32) {
      throw new IllegalArgumentException("byte数组必须不为空,并且长度是32!");
    }
    int sum = 0;
    for (int i = 0; i < 32; ++i) {
      sum |= (arr[i] << (31 - i));
    }
    return sum;
  }

  /**
   * 将长度为64的byte数组转换为一个long类型值.
   * 每一个8位byte都只存储了0或1的数字.
   *
   * @param arr
   * @return
   */
  public static long byte64ToLong(byte[] arr) {
    if (arr == null || arr.length != 64) {
      throw new IllegalArgumentException("byte数组必须不为空,并且长度是64!");
    }
    long sum = 0L;
    for (int i = 0; i < 64; ++i) {
      sum |= ((long) arr[i] << (63 - i));
    }
    return sum;
  }

  /**
   * 将一个long值转换为长度为64的8位byte数组.
   * 每一个8位byte都只存储了0或1的数字.
   *
   * @param sum
   * @return
   */
  public static byte[] longToByte64(long sum) {
    byte[] arr = new byte[64];
    for (int i = 63; i >= 0; i--) {
      arr[i] = (byte) (sum & 1);
      sum >>= 1;
    }
    return arr;
  }

  //>>>>> 字节转换函数
  
  	public static byte[] copy(byte[] src, int offset, int length)
  	{
  		byte[] bytes = new byte[length];
  		System.arraycopy(src, offset, bytes, 0, length);
  		return bytes;
  	}
  
  	public static void copyTo(byte[] dest, byte[] src)
	{
		System.arraycopy(src, 0, dest, 0, src.length);
	}

  	public static void copyTo(byte[] dest, int destOffset, byte[] src, int srcOffset, int length)
	{
		System.arraycopy(src, srcOffset, dest, destOffset, length);
	}
  	
	public static String fromBCD(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<bytes.length; i++ )
		{
			sb.append(String.format("%02X", bytes[i]));
		}
		return sb.toString();
	}
	
	//----------
	
	public static void main(String[] args)
	{
		System.out.println(equals(null, null));
	}
	
}
