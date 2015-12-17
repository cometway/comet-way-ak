
package com.cometway.util;

/**
 * This class provides a suite of methods to perform tests.
 * If the test fails, a RuntimeError is thrown.
 */
public class Assert
{
	public static void fail()
	{
		String  s = "Assertion failed.";

		System.err.println(s);

		throw new RuntimeException(s);
	}


	public static void fail(String message)
	{
		System.err.println(message);

		throw new RuntimeException(message);
	}


	public static void isFalse(boolean b)
	{
		if (b)
		{
			fail("Assert.isFalse failed.");
		}
	}


	public static void isGreaterThanZero(int i)
	{
		if (i <= 0)
		{
			fail("Assert.isGreaterThanZero failed.");
		}
	}


	public static void isGreaterThanZero(long l)
	{
		if (l <= 0)
		{
			fail("Assert.isGreaterThanZero failed.");
		}
	}


	public static void isGreaterThanZero(double d)
	{
		if (d <= 0)
		{
			fail("Assert.isGreaterThanZero failed.");
		}
	}


	public static void isGreaterThanZero(float f)
	{
		if (f <= 0)
		{
			fail("Assert.isGreaterThanZero failed.");
		}
	}


	public static void isNotNull(Object obj)
	{
		if (obj == null)
		{
			fail("Assert.isNotNull failed.");
		}
	}


	public static void isNull(Object obj)
	{
		if (obj != null)
		{
			fail("Assert.isNull failed.");
		}
	}


	public static void isTrue(boolean b)
	{
		if (b == false)
		{
			fail("Assert.isTrue failed.");
		}
	}


	public static void isNotEmpty(String s)
	{
		if ((s == null) || (s.length() == 0))
		{
			fail("Assert.isNotEmpty failed.");
		}
	}


	public static void isNotEmpty(Object o[])
	{
		if ((o == null) || (o.length == 0))
		{
			fail("Assert.isNotEmpty failed.");
		}
	}


	public static void main(String args[])
	{
		Assert.isNotEmpty(args);

		if (args[0].equals("fail"))
		{
			if (args.length == 2)
			{
				Assert.fail(args[1]);
			}
			else
			{
				Assert.fail();
			}
		}

		if (args[0].equals("isFalse"))
		{
			Assert.isFalse(true);
		}

		if (args[0].equals("isGreaterThanZero"))
		{
			Assert.isGreaterThanZero(-1);
		}

		if (args[0].equals("isNotNull"))
		{
			Assert.isNotNull(null);
		}

		if (args[0].equals("isNull"))
		{
			Assert.isNull("");
		}

		if (args[0].equals("isTrue"))
		{
			Assert.isTrue(false);
		}

		if (args[0].equals("isNotEmpty"))
		{
			Assert.isNotEmpty("");
		}
	}


}

