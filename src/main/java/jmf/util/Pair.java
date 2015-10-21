package jmf.util;

/**
 * Simple Pair container.
 * Created on 7/25/15.
 * @author Jan Strauß
 */
public class Pair<FirstType, SecondType> {
	public final FirstType first;
	public final SecondType second;

	private Pair(final FirstType first, final SecondType second) {
		this.first = first;
		this.second = second;
	}

	public static <FirstType, SecondType> Pair<FirstType, SecondType> of(final FirstType first, final SecondType second) {
		return new Pair<>(first, second);
	}
}