package togos.schemaschema.parser.asyncstream;

import java.util.ArrayList;

public class BaseStreamSource<T> implements StreamSource<T>
{
	ArrayList<StreamDestination<? super T>> pipes = new ArrayList<StreamDestination<? super T>>();
	
	@Override public void pipe( StreamDestination<? super T> dest ) {
		pipes.add(dest);
	}
	
	public void _data( T value ) throws Exception {
		for( StreamDestination<? super T> dest : pipes ) {
			dest.data( value );
		}
	}
	
	public void _end() throws Exception {
		for( StreamDestination<? super T> dest : pipes ) {
			dest.end();
		}
	}
}
