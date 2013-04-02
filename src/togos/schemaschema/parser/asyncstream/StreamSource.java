package togos.schemaschema.parser.asyncstream;

public interface StreamSource<T>
{
	public void pipe( StreamDestination<T> dest );
}
