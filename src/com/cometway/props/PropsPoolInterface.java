
package com.cometway.props;


/**
* This interface is for getting and returning a Props from a pool.
*/

public interface PropsPoolInterface
{
	/**
	* Takes a Props from the pool based on the specified Object parameter.
	* Passing null *may* return a default value.
	*/

	public Props takeProps(Object o);


	/**
	* Returns a Props from the pool based on the specified Object parameter.
	* A Props that is not returned to the pool may never be reused.
	*/

	public void returnProps(Props Props, Object o);
}



