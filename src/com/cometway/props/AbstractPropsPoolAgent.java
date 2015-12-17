
package com.cometway.props;


import com.cometway.ak.ServiceAgent;


public abstract class AbstractPropsPoolAgent extends ServiceAgent
{
	protected PropsList propsList;
	protected PropsPoolImpl propsPool;


	public void initProps()
	{
		setDefault("service_name", "props_pool");
		setDefault("initial_size", "10");
	}


	/**
	* Sets up the Props pool and registers the PropsPoolImpl using service_name.
	*/

	public void start()
	{
		String service_name = getTrimmedString("service_name");
		int size = getInteger("initial_size");

		propsList = new PropsList();

		for (int i = 0; i < size; i++)
		{
			addNewPropsToPool();
		}

		propsPool = new PropsPoolImpl();

		registerService(service_name, propsPool);
	}


	/**
	* Unregisters the PropsPoolImpl and releases it and the PropsList.
	*/

	public void stop()
	{
		String service_name = getTrimmedString("service_name");

		unregisterService(service_name, propsPool);

		propsList = null;
		propsPool = null;
	}


	/**
	* Creates a new Props and adds it to the pool.
	*/

	public abstract void addNewPropsToPool();
	

	/**
	* Takes a Props from the pool based on the specified Object parameter.
	* Passing null *may* return a default value.
	*/

	public abstract Props takeFromPool(Object o);


	/**
	* Returns a Props to the pool based on the specified Object parameter.
	* A Props that is not returned to the pool may never be reused.
	*/

	public abstract void returnToPool(Props Props, Object o);
	

	public class PropsPoolImpl
	{
		/**
		* Takes a Props from the pool based on the specified Object parameter.
		* Passing null *may* return a default value.
		*/

		public Props takeProps(Object o)
		{
			return (takeFromPool(o));
		}


		/**
		* Returns a Props to the pool based on the specified Object parameter.
		* A Props that is not returned to the pool may never be reused.
		*/

		public void returnProps(Props p, Object o)
		{
			returnToPool(p, o);
		}
	}
}



