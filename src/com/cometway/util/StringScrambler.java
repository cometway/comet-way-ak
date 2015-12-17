package com.cometway.util;


/**
 * This class provides static methods to obsfucate Strings using bitwise shifting and
 * radix mapping.
 */
public class StringScrambler
{
	public static final char[] defaultKey = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N',
								'O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1',
								'2','3','4','5'};

	public static String scramble(String source, int shift, int mod, char[] key)
	{
		StringBuffer rval = null;
		try {
			int base = 0;
			if(key.length>=2) {
				base++;
				if(key.length>=4) {
					base++;
					if(key.length>=8) {
						base++;
						if(key.length>=16) {
							base++;
							if(key.length>=32) {
								base++;
								if(key.length>=64) {
									base++;
									if(key.length>=128) {
										base++;
										if(key.length>=256) {
											base++;
										}
									}
								}
							}
						}
					}
				}
			}

			int counter = 0;
			boolean[] sourceBits = new boolean[source.length()*8];
			for(int x=0;x<source.length();x++) {
				boolean[] tmp = intToBin((int)source.charAt(x),8);
				for(int z=0;z<8;z++) {
					sourceBits[z+(x*8)] = tmp[z];
				}
				//System.out.print("Char is: "+source.charAt(x)+" : ");
				//printBin(tmp);
			}
			if(mod>0) {
				//System.out.print("source bits before shift: ");
				//printBin(sourceBits);
				while(true) {
					if(sourceBits.length>counter+mod) {
						boolean[] tmp = new boolean[mod];
						for(int x=0;x<tmp.length;x++) {
							tmp[x] = sourceBits[counter+x];
						}
						for(int x=0;x<tmp.length;x++) {
							int tmpInt = (x+shift)%mod;
							if(tmpInt<0) {
								tmpInt = tmpInt+mod;
							}
							sourceBits[counter+x] = tmp[tmpInt];
						}
						counter = counter+mod;
					}
					else {
						if(sourceBits.length-counter>0) {
							int tmpLength = sourceBits.length-counter;
							boolean[] tmp = new boolean[tmpLength];
							for(int x=0;x<tmp.length;x++) {
								tmp[x] = sourceBits[counter+x];
							}
							for(int x=0;x<tmp.length;x++) {
								int tmpInt = (x+shift)%tmpLength;
								if(tmpInt<0) {
									tmpInt = tmpInt+tmpLength;
								}
								sourceBits[counter+x] = tmp[tmpInt];
							}
						}
						break;
					}
				}
			}

			//System.out.print("source bits: ");
			//printBin(sourceBits);

			rval = new StringBuffer();
			counter = 0;
			while(true) {
				if(sourceBits.length>counter+base) {
					boolean[] tmp = new boolean[base];
					for(int x=0;x<tmp.length;x++) {
						tmp[x] = sourceBits[x+counter];
					}
					int tmpInt = binToInt(tmp);
					rval.append(key[tmpInt]);
					counter = counter+base;
				}
				else {
					if(sourceBits.length-counter>0) {
						boolean[] tmp = new boolean[base];
						for(int x=0;x<(sourceBits.length-counter);x++) {
							tmp[x] = sourceBits[x+counter];
						}
						for(int x=(sourceBits.length-counter);x<tmp.length;x++) {
							tmp[x] = false;
						}
						int tmpInt = binToInt(tmp);
						rval.append(key[tmpInt]);
					}
					break;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(rval!=null) {
			return(rval.toString());
		}
		else {
			return(null);
		}
	}


	public static String unscramble(String source, int shift, int mod, char[] key)
	{
		StringBuffer rval = null;

		try {
			int base = 0;
			if(key.length>=2) {
				base++;
				if(key.length>=4) {
					base++;
					if(key.length>=8) {
						base++;
						if(key.length>=16) {
							base++;
							if(key.length>=32) {
								base++;
								if(key.length>=64) {
									base++;
									if(key.length>=128) {
										base++;
										if(key.length>=256) {
											base++;
										}
									}
								}
							}
						}
					}
				}
			}

			int counter = 0;
			boolean[] sourceBits = new boolean[((source.length()*base)/8)*8];
			try {
				for(int x=0;x<source.length();x++) {
					int index = 0;
					while(index<key.length && key[index]!=source.charAt(x)) {index++;}
					boolean[] tmp = intToBin(index,base);
					for(int z=0;z<base;z++) {
						sourceBits[z+(x*base)] = tmp[z];
					}
				}
			}
			catch(Exception e) {;}

			if(mod>0) {
				while(true) {
					if(sourceBits.length>counter+mod) {
						boolean[] tmp = new boolean[mod];
						for(int x=0;x<tmp.length;x++) {
							tmp[x] = sourceBits[counter+x];
						}
						for(int x=0;x<tmp.length;x++) {
							int tmpInt = (x-shift)%mod;
							if(tmpInt<0) {
								tmpInt = tmpInt+mod;
							}
							sourceBits[counter+x] = tmp[tmpInt];
						}
						counter = counter+mod;
					}
					else {
						if(sourceBits.length-counter>0) {
							int tmpLength = sourceBits.length-counter;
							boolean[] tmp = new boolean[tmpLength];
							for(int x=0;x<tmp.length;x++) {
								tmp[x] = sourceBits[counter+x];
							}
							for(int x=0;x<tmp.length;x++) {
								int tmpInt = (x-shift)%tmpLength;
								if(tmpInt<0) {
									tmpInt = tmpInt+tmpLength;
								}
								sourceBits[counter+x] = tmp[tmpInt];
							}
						}
						break;
					}
				}
			}

			rval = new StringBuffer();
			counter = 0;
			/*
			while(true) {
				if(sourceBits.length>counter+8) {
					boolean[] tmp = new boolean[8];
					for(int x=0;x<tmp.length;x++) {
						tmp[x] = sourceBits[x+counter];
					}
					int tmpInt = binToInt(tmp);
					rval.append((char)tmpInt);
					counter = counter+8;
				}
				else {
					break;
				}
			}
			*/
			for(int x=0;x<sourceBits.length/8;x++) {
				boolean[] tmp = new boolean[8];
				for(int z=0;z<8;z++) {
					tmp[z] = sourceBits[x*8+z];
				}
				rval.append((char)binToInt(tmp));
			}

		}
		catch(Exception e) {
			e.printStackTrace();
		}

		if(rval!=null) {
			return(rval.toString());
		}
		else {
			return(null);
		}
	}



	/**
	* Converts an integer to a binary value.
	*/
	public static boolean[] intToBin(int in, int radix)
	{
		boolean[] rval = new boolean[radix];
		int index = radix-1;
		
		while(in>1) {
			if(in%2==0) {
				rval[index]=false;
			}
			else {
				rval[index]=true;
			}
			index--;
			in = in/2;
		}
		if(in%2==0) {
			rval[index]=false;
		}
		else {
			rval[index]=true;
		}

		return(rval);
	}


	/**
	* Converts a binary value to an integer.
	*/
	public static int binToInt(boolean[] bin)
	{
		int rval = 0;

		for(int x=0;x<bin.length;x++) {
			if(bin[(bin.length-1)-x]) {
				rval = rval + (int)Math.pow(2,x);
			}
		}

		return(rval);
	}

	/**
	* Prints a binary number. 
	*/
	protected static void printBin(boolean[] bin)
	{
		for(int x=0;x<bin.length;x++) {
			if(bin[x]) {
				System.out.print("1");
			}
			else {
				System.out.print("0");
			}
		}
		System.out.println();
	}

	public static void main(String[] args) 
	{
		char[] key = {'0','1','2','3','4','5','6','7','8','9'};

		String s = scramble(args[0],Integer.parseInt(args[1]),
											 Integer.parseInt(args[2]),
											 key);
		System.out.print("Encoded String: ");
		System.out.println(s);
		System.out.print("Decoded String: ");
		String s2 = unscramble(s,Integer.parseInt(args[1]),
											 Integer.parseInt(args[2]),
											 key);
		System.out.println(s2);
		if(s2.equals(args[0])) {
			System.out.println("Decoded successfully");
		}
		else {
			System.out.println("You fucked up BIG time");
		}
	}

}

