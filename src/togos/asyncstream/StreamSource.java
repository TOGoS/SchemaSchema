package togos.asyncstream;

public interface StreamSource<T>
{
	public void pipe( StreamDestination<? super T> dest );
}
