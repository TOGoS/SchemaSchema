package togos.schemaschema.parser;

import togos.lang.SourceLocation;

public class Token implements SourceLocation
{
	public static enum Type {
		SYMBOL, // 'special' characters
		BAREWORD,
		SINGLE_QUOTED_STRING,
		DOUBLE_QUOTED_STRING,
	};
	
	public final Type type;
	public final String text;
	public final String filename;
	public final int lineNumber;
	public final int columnNumber;
	
	public Token( Type t, String text, String filename, int lineNumber, int columnNumber ) {
		this.type = t;
		this.text = text;
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}
	
	@Override public String getSourceFilename() { return filename; }
	@Override public int getSourceLineNumber() { return lineNumber; }
	@Override public int getSourceColumnNumber() { return columnNumber; }
	
	public String toString() {
		if( type == Type.SYMBOL ) return text;
		
		return super.toString();
	}
}
