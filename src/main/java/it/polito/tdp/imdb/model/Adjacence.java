package it.polito.tdp.imdb.model;

public class Adjacence
{
	private final Director director1;
	private final Director director2;
	private final double numActors;
	
	
	public Adjacence(Director director1, Director director2, double numActors)
	{
		this.director1 = director1;
		this.director2 = director2;
		this.numActors = numActors;
	}

	public Director getDirector1()
	{
		return this.director1;
	}

	public Director getDirector2()
	{
		return this.director2;
	}

	public double getNumActors()
	{
		return this.numActors;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((director1 == null) ? 0 : director1.hashCode());
		result = prime * result + ((director2 == null) ? 0 : director2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Adjacence other = (Adjacence) obj;
		if (director1 == null)
		{
			if (other.director1 != null)
				return false;
		}
		else
			if (!director1.equals(other.director1))
				return false;
		if (director2 == null)
		{
			if (other.director2 != null)
				return false;
		}
		else
			if (!director2.equals(other.director2))
				return false;
		return true;
	}
}
