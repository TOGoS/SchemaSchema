package togos.freeze;

/**
 * Since the default in Java is for objects to be mutable,
 * our interface marks when they are not.  Otherwise I'd rather
 * have immutability be the default and have to ask objects if they are mutable.
 */
public interface PossiblyImmutable
{
	public boolean isImmutable();
}
