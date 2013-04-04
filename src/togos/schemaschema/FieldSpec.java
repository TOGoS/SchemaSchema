package togos.schemaschema;

import togos.schemaschema.parser.ast.Phrase;
import togos.schemaschema.parser.ast.Word;

public class FieldSpec
{
	public final String name;
	public final Type type;
	public final boolean isNullable;
	
	public FieldSpec( String name, Type type, boolean isNullable ) {
		this.name = name;
		this.type = type;
		this.isNullable = isNullable;
	}
	
	public String toString() {
		String s = Phrase.quoteIfNecessary(name) + " : " + Word.quoteIfNecessary(type.getName()) + (isNullable ? " : nullable" : "");
		if( type instanceof ForeignKeyReferenceType ) {
			s += "{\n" + StringUtil.indent("\t", ((ForeignKeyReferenceType)type).keySpec.toString()) + "\n}";
		}
		return s;
	}
}
