package togos.asyncstream;

public interface StreamDestination<T>
{
	public void data( T value ) throws Exception;
	public void end() throws Exception;
}
