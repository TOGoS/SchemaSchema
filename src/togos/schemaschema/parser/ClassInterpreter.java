package togos.schemaschema.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import togos.lang.ParseError;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.ForeignKeySpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.ObjectType;
import togos.schemaschema.Type;
import togos.schemaschema.Types;
import togos.schemaschema.parser.ast.Block;
import togos.schemaschema.parser.ast.Command;
import togos.schemaschema.parser.ast.Parameterized;
import togos.schemaschema.parser.asyncstream.BaseStreamSource;
import togos.schemaschema.parser.asyncstream.StreamDestination;

public class ClassInterpreter<V extends ObjectType> extends BaseStreamSource<V> implements StreamDestination<Command>
{
	protected static String singleString( Parameterized p, String contextDescription ) throws ParseError {
		if( p.parameters.length != 0 ) {
			throw new ParseError( contextDescription + " cannot take arguments", p.sLoc );
		}
		return p.subject.unquotedText();
	}
	
	protected V process( ObjectType c ) {
		return (V)c;
	}
	
	protected void defineSimpleField( Command fieldNode,
		LinkedHashMap<String,FieldSpec> fieldSpecs,
		LinkedHashMap<String,IndexSpec> indexSpecs,
		LinkedHashMap<String,ForeignKeySpec> fkSpecs
	) throws ParseError {
		String fieldName = singleString(fieldNode.subject, "field name");
		if( fieldSpecs.containsKey(fieldName) ) {
			throw new ParseError( "Field '"+fieldName+"' already defined", fieldNode.sLoc );
		}
		/* TODO */
	}
	
	public ObjectType parseClass( String name, Parameterized[] modifiers, Block body ) throws ParseError {
		LinkedHashMap<String,FieldSpec> fieldSpecs = new LinkedHashMap<String,FieldSpec>();
		LinkedHashMap<String,IndexSpec> indexSpecs = new LinkedHashMap<String,IndexSpec>();
		LinkedHashMap<String,ForeignKeySpec> fkSpecs = new LinkedHashMap<String,ForeignKeySpec>();
		
		for( Command fieldCommand : body.commands ) {
			for( Parameterized fieldNameParameter : fieldCommand.subject.parameters ) {
				throw new ParseError("Field name cannot have parameters", fieldNameParameter.sLoc );
			}
			String fieldName = fieldCommand.subject.subject.unquotedText();
			boolean isReference;
			Type fieldType = null;
			for( Parameterized mod : fieldCommand.modifiers ) {
				if( "reference".equals(mod.subject.unquotedText()) ) {
					isReference = true;
					if( mod.parameters.length != 1 ) {
						throw new ParseError(
							"'reference' field modifier takes a single parameter: "+
							"the name of the type being referenced.  Got "+
							mod.parameters.length+" parameters", mod.sLoc
						);
					}
					if( fieldCommand.body == null ) {
						throw new ParseError(
							"'reference' field specification requires a block", fieldCommand.sLoc
						);
					}
					if( fieldCommand.body.commands.length == 0 ) {
						throw new ParseError(
							"'reference' field specificatino requires at least one foreign key component",
							fieldCommand.body.sLoc
						);
					}
					ArrayList<ForeignKeySpec.Component> fkComponents = new ArrayList<ForeignKeySpec.Component>();
					for( Command fkCommand : fieldCommand.body.commands ) {
						String foreignFieldName = singleString( fkCommand.subject, "foreign field name" );
						
						Command localFieldNode;
						if( fkCommand.body != null ) {
							if( fkCommand.body.commands.length != 1 ) {
								throw new ParseError(
									"Foreign key component requires exactly 0 or 1 local field specifications; given "+
									fkCommand.body.commands.length, fkCommand.body.sLoc
								);
							}
							if( fkCommand.modifiers.length != 0 ){
								throw new ParseError(
									"Modifiers not allowed for foreign field specification",
									fkCommand.modifiers[0].sLoc
								);
							}
							localFieldNode = fkCommand.body.commands[0];
						} else {
							localFieldNode = fkCommand;
						}
						
						String localFieldName = singleString(localFieldNode.subject, "local field name");
						if( fieldSpecs.containsKey(localFieldName) && localFieldNode.modifiers.length > 0 ) {
							throw new ParseError(
								"Cannot modify already-defined local field '"+localFieldName+
								"' in foreign key specification", localFieldNode.modifiers[0].sLoc
							);
						}
						defineSimpleField( localFieldNode, fieldSpecs, indexSpecs, fkSpecs );
					}
					//fkSpec
					//fieldType = new ForeignKeyReferenceType( singleString(mod.parameters[0], "referenced class name"), Types.REFERENCE, fkSpec);
				}
			}
			if( fieldCommand.body != null ) {
				
			}
		}
		
		boolean selfKeyed = false;
		for( Parameterized mod : modifiers ) {
			if( "self-keyed".equals(mod.subject.unquotedText()) ) {
				selfKeyed = true;
			} else {
				throw new ParseError("Unrecognised class modifier: '"+mod.subject+"'", mod.sLoc);
			}
		}
		
		if( selfKeyed ) {
			indexSpecs.put("primary", new IndexSpec("primary", fieldSpecs));
		}
		
		return new ObjectType( name, Types.OBJECT, fieldSpecs, indexSpecs, fkSpecs );
	}
	
	@Override public void data(Command value) throws Exception {
		if( "class".equals(value.subject.subject.words[0].text) ) {
			for( Parameterized classNameParameter : value.subject.parameters ) {
				throw new ParseError("Class name cannot have parameters", classNameParameter.sLoc );
			}
			_data( process(parseClass( value.subject.subject.tail().unquotedText(), value.modifiers, value.body )) );
		}
	}

	@Override public void end() throws Exception {
		_end();
	}
}
