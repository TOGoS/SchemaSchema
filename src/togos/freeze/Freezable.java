package togos.freeze;

public interface Freezable<X extends PossiblyImmutable>
{
	public X freeze();
}
