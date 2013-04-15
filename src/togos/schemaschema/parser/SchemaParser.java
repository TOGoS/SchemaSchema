package togos.schemaschema.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import togos.lang.ParseError;
import togos.lang.SourceLocation;
import togos.schemaschema.ComplexType;
import togos.schemaschema.EnumType;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.ForeignKeySpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.Properties;
import togos.schemaschema.Property;
import togos.schemaschema.PropertyUtil;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.Type;
import togos.schemaschema.parser.ast.Block;
import togos.schemaschema.parser.ast.Command;
import togos.schemaschema.parser.ast.Parameterized;
import togos.schemaschema.parser.ast.Phrase;
import togos.schemaschema.parser.asyncstream.BaseStreamSource;
import togos.schemaschema.parser.asyncstream.StreamDestination;
import togos.schemaschema.parser.asyncstream.StreamUtil;

public class SchemaParser extends BaseStreamSource<SchemaObject> implements StreamDestination<Command>
{
	protected static String singleString( Parameterized p, String contextDescription ) throws ParseError {
		if( p.parameters.length != 0 ) {
			throw new ParseError( contextDescription + " cannot take arguments", p.sLoc );
		}
		return p.subject.unquotedText();
	}
	
	interface ModifierSpec {
		public Modifier bind( SchemaParser sp, Parameterized[] params, SourceLocation sLoc ) throws ParseError;
	}
	
	interface Modifier {
		public void apply( SchemaObject subject );
	}
	
	interface FieldModifier extends Modifier {
		public void apply( ComplexType classObject, FieldSpec fieldSpec );
	}
	
	public static class SimplePropertyModifierSpec implements ModifierSpec {
		final Property property;
		
		public SimplePropertyModifierSpec( Property p ) {
			this.property = p;
		}
		
		@Override public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws ParseError {
			final Object value;
			if( params.length == 0 ) {
				value = Boolean.TRUE;
			} else if( params.length == 1 ) {
				// TODO: Define atom expressions and stuff
				value = params[0].subject.unquotedText();
			} else {
				throw new ParseError(property.name+" modifier takes 0 or 1 argument, but "+params.length+" given", params[1].sLoc);
			}
			
			return new Modifier() {
				public void apply(SchemaObject subject) {
					PropertyUtil.add( subject.getPropertyValues(), property, value );					
				}
			};
		}
	}
	
	/** Modifier that is equivalent to a set of property -> value pairs */
	public static class AliasModifier implements Modifier, ModifierSpec {
		final String name;
		final Map<Property,Set<Object>> propertyValues;
		
		public AliasModifier( String name, Map<Property,Set<Object>> propertyValues ) {
			this.name = name;
			this.propertyValues = propertyValues;
		}
		
		@Override
		public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws ParseError {
			if( params.length > 0 ) {
				throw new ParseError(name+" modifier takes no arguments", params[0].sLoc);
			}
			return this;
		}
		
		@Override
		public void apply(SchemaObject subject) {
			PropertyUtil.addAll( subject.getPropertyValues(), propertyValues );
		}
	}
	
	public static class FieldIndexModifierSpec implements ModifierSpec {
		public static FieldIndexModifierSpec INSTANCE = new FieldIndexModifierSpec();
		
		@Override
		public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws ParseError {
			final ArrayList<String> indexNames = new ArrayList<String>();
			
			if( params.length == 0 ) {
				throw new ParseError("Index modifier with no arguments is useless", sLoc);
			}
			for( Parameterized indexParam : params ) {
				for( Parameterized indexParamParam : indexParam.parameters ) {
					throw new ParseError("Index name takes no parameters, but parameters given", indexParamParam.sLoc);
				}
				indexNames.add( indexParam.subject.unquotedText() );
			}

			return new FieldModifier() {
				@Override
				public void apply(SchemaObject subject) {
					throw new UnsupportedOperationException("Index field modifier doesn't support apply(subject) -- use the other apply method");
				}
				
				@Override
				public void apply(ComplexType classObject, FieldSpec fieldSpec) {
					for( String indexName : indexNames ) {
						IndexSpec index = classObject.indexesByName.get(indexName);
						if( index == null ) {
							index = new IndexSpec(indexName);
							classObject.indexesByName.put(indexName, index);
						}
						index.fields.add( fieldSpec );
					}
				}
			};
		}
	}
	
	/**
	 * Used to specify an new (anonymous) enum type for a single field
	 */
	public static class EnumFieldModifierSpec implements ModifierSpec {
		public static final EnumFieldModifierSpec INSTANCE = new EnumFieldModifierSpec(); 
		
		@Override
		public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws ParseError {
			final LinkedHashSet<String> valueNames = new LinkedHashSet<String>(); 
			for( Parameterized p : params ) {
				for( Parameterized pp : p.parameters ) {
					throw new ParseError( "Enum values cannot themselves take parameters", pp.sLoc);
				}
			}
			
			return new Modifier() {
				@Override public void apply(SchemaObject subject) {
					EnumType er = new EnumType(subject.getName());
					for( String valueName : valueNames ) er.addValue(valueName);
					PropertyUtil.add( subject.getPropertyValues(), Properties.TYPE, er );
				}
			};
		}
		
	}
	
	public static class ExtendsModifierSpec implements ModifierSpec {
		public static ExtendsModifierSpec INSTANCE = new ExtendsModifierSpec();
		
		@Override
		public Modifier bind( SchemaParser sp, Parameterized[] params, SourceLocation sLoc ) throws ParseError {
			for( Parameterized p : params ) {
				for( Parameterized pp : p.parameters ) {
					throw new ParseError( "Parameterized types not yet supported", pp.sLoc );
				}
				String parentName = p.subject.unquotedText();
				Type parent = sp.types.get( parentName );
				if( parent == null ) {
					throw new ParseError( "Parent type '"+parentName+"' is not defined", p.sLoc );
				}
			}
			
			return new Modifier() {
				@Override public void apply(SchemaObject subject) {
					
				}
			};
		}
	}
	
	////
	
	protected Map<String,Type> types = new HashMap<String,Type>();
	protected Map<String,Property> generalProperties = new HashMap<String,Property>();
	protected Map<String,ModifierSpec> generalModifiers = new HashMap<String,ModifierSpec>();
	protected Map<String,Property> fieldProperties = new HashMap<String,Property>();
	protected Map<String,Property> classProperties = new HashMap<String,Property>();
	protected Map<String,ModifierSpec> fieldModifiers = new HashMap<String,ModifierSpec>();
	protected Map<String,ModifierSpec> classModifiers = new HashMap<String,ModifierSpec>();
	
	public SchemaParser() { }
	
	public void defineType( Type t ) throws Exception {
		types.put( t.getName(), t );
		HashMap<Property,Set<Object>> appliedProperties = new HashMap<Property,Set<Object>>();
		PropertyUtil.add(appliedProperties, Properties.TYPE, t);
		generalModifiers.put( t.getName(), new AliasModifier(Properties.TYPE.name, appliedProperties) );
		_data( t );
	}
	
	public void defineFieldProperty( Property property ) throws Exception {
		fieldProperties.put( property.name, property );
		fieldModifiers.put( property.name, new SimplePropertyModifierSpec(property) );
		_data( property );
	}
	
	public void defineClassProperty( Property property ) throws Exception {
		classProperties.put( property.name, property );
		classModifiers.put( property.name, new SimplePropertyModifierSpec(property) );
		_data( property );
	}
	
	public void defineClassModifier( String name, ModifierSpec spec ) {
		classModifiers.put( name, spec );
	}
	
	public void defineFieldModifier( String name, ModifierSpec spec ) {
		fieldModifiers.put( name, spec );
	}
	
	private ModifierSpec findClassModifierSpec(Phrase subject) {
		String name = subject.unquotedText();
		ModifierSpec m;
		if( (m = classModifiers.get(name)) != null ) return m;
		if( (m = generalModifiers.get(name)) != null ) return m;
		return null;
	}
	
	private ModifierSpec findFieldModifierSpec(Phrase subject) {
		String name = subject.unquotedText();
		ModifierSpec m;
		if( (m = fieldModifiers.get(name)) != null ) return m;
		if( (m = generalModifiers.get(name)) != null ) return m;
		return null;
	}
	
	protected FieldSpec defineSimpleField(
		ComplexType objectType,
		Command fieldCommand
	) throws ParseError {
		String fieldName = singleString(fieldCommand.subject, "field name");
		if( objectType.fieldsByName.containsKey(fieldName) ) {
			throw new ParseError( "Field '"+fieldName+"' already defined", fieldCommand.sLoc );
		}
		
		FieldSpec fieldSpec = new FieldSpec( fieldName );
		
		for( Parameterized modifier : fieldCommand.modifiers ) {
			ModifierSpec ms;
			if( (ms = findFieldModifierSpec(modifier.subject)) != null ) {
				Modifier m = ms.bind( this, modifier.parameters, modifier.sLoc );
				if( m instanceof FieldModifier ) {
					((FieldModifier)m).apply( objectType, fieldSpec );
				} else {
					m.apply( fieldSpec );
				}
			} else {
				throw new ParseError(
					"Unrecognised field modifier: "+modifier.subject.toString(), modifier.sLoc
				);
			}
		}
		
		objectType.fieldsByName.put( fieldSpec.name, fieldSpec );
		return fieldSpec;
	}
	
	protected FieldSpec getSimpleField(
		ComplexType objectType,
		Command fieldCommand
	) throws ParseError {
		String fieldName = singleString(fieldCommand.subject, "field name");
		FieldSpec fieldSpec = objectType.fieldsByName.get(fieldName);
		if( fieldSpec == null ) {
			fieldSpec = defineSimpleField( objectType, fieldCommand );
		} else if( fieldCommand.modifiers.length > 0 ) {
			throw new ParseError( "Cannot redefine field '"+fieldName+"'", fieldCommand.sLoc );
		}
		return fieldSpec;
	}
	
	protected Property parseProperty( String name, Parameterized[] modifiers, Block body ) throws ParseError {
		// TODO: Read property properties, I guess!
		return new Property(name);
	}
	
	private ModifierSpec parseFieldModifierSpec(final String name, Parameterized[] modifierModifiers, Block body) throws ParseError {
		final ArrayList<Modifier> subModifiers = new ArrayList<Modifier>();
		if( body.commands.length != 1 ) {
			throw new ParseError("Field modifier must have exactly 1 command, "+body.commands.length+" given",
				body.commands.length == 0 ? body.sLoc : body.commands[1].sLoc );
		}
		for( Command cmd : body.commands ) {
			for( Parameterized p : cmd.modifiers ) {
				ModifierSpec ms = findFieldModifierSpec(p.subject);
				if( ms == null ) {
					throw new ParseError("Unrecognised field modifier: "+p.subject.unquotedText(), p.sLoc);
				}
			}
		}
		return new ModifierSpec() {
			@Override
			public Modifier bind(SchemaParser sp, Parameterized[] params, SourceLocation sLoc) throws ParseError {
				if( params.length > 0 ) {
					throw new ParseError("Custom field modifier "+name+" takes no parameters", sLoc);
				}
				return new FieldModifier() {
					@Override
					public void apply( ComplexType classObject, FieldSpec fieldSpec ) {
						for( Modifier m : subModifiers ) {
							if( m instanceof FieldModifier ) {
								((FieldModifier)m).apply(classObject, fieldSpec);
							} else {
								m.apply(fieldSpec);
							}
						}
					}
					
					@Override public void apply( SchemaObject fieldSpec ) {
						for( Modifier m : subModifiers ) {
							m.apply(fieldSpec);
						}
					}
				};
			}
		};
	}
	
	public ComplexType parseClass( String name, Parameterized[] modifiers, Block body ) throws ParseError {
		ComplexType t = new ComplexType( name );
		
		ModifierSpec m;
		for( Command fieldCommand : body.commands ) {
			ArrayList<Modifier> _fieldModifiers = new ArrayList<Modifier>();
			for( Parameterized fieldNameParameter : fieldCommand.subject.parameters ) {
				throw new ParseError("Field name cannot have parameters", fieldNameParameter.sLoc );
			}
			Block referenceBody = null;
			for( Parameterized mod : fieldCommand.modifiers ) {
				if( "reference".equals(mod.subject.unquotedText()) ) {
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
					referenceBody = fieldCommand.body;
				} else if( (m = findClassModifierSpec(mod.subject)) != null ) {
					_fieldModifiers.add(m.bind(this, mod.parameters, mod.sLoc));
				}
			}
			
			FieldSpec f;
			if( referenceBody != null ) {
				ArrayList<ForeignKeySpec.Component> fkComponents = new ArrayList<ForeignKeySpec.Component>();
				for( Command fkCommand : referenceBody.commands ) {
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
					FieldSpec localField = getSimpleField( t, localFieldNode );;
					
					// TODO: Implement rest of this
				}
				//fkSpec
				//fieldType = new ForeignKeyReferenceType( singleString(mod.parameters[0], "referenced class name"), Types.REFERENCE, fkSpec);
			} else {
				f = defineSimpleField( t, fieldCommand );
				for( Modifier _m : _fieldModifiers ) {
					_m.apply(f);
				}
			}
		}
		
		for( Parameterized mod : modifiers ) {
			if( (m = findClassModifierSpec(mod.subject)) != null ) {
				m.bind(this, mod.parameters, mod.sLoc).apply(t);
			} else {
				throw new ParseError("Unrecognised class modifier: '"+mod.subject+"'", mod.sLoc);
			}
		}
		
		if( PropertyUtil.isTrue(t.getPropertyValues(), Properties.SELF_KEYED) ) {
			t.indexesByName.put("primary", new IndexSpec("primary", t.fieldsByName.values()));
		}
		
		return t;
	}
	
	private EnumType parseEnum(String name, Parameterized[] modifiers, Block body) throws ParseError {
		EnumType t = new EnumType(name);
		
		ModifierSpec m;
		for( Parameterized mod : modifiers ) {
			if( (m = findClassModifierSpec(mod.subject)) != null ) {
				m.bind(this, mod.parameters, mod.sLoc).apply(t);
			} else {
				throw new ParseError("Unrecognised class modifier: '"+mod.subject+"'", mod.sLoc);
			}
		}
		
		for( Command c : body.commands ) {
			ensureNoParameters(c.subject, "enum value");
			for( Parameterized mod : c.modifiers ) {
				throw new ParseError("Enum value modifiers are ignored", mod.sLoc);
			}
			if( c.body.commands.length > 0 ) {
				throw new ParseError("Enum value body is ignored", c.body.sLoc);
			}
			
			t.addValue(c.subject.subject.unquotedText());
		}
		
		return t;
	}
	
	protected void ensureNoParameters( Parameterized s, String context ) throws ParseError {
		for( Parameterized classNameParameter : s.parameters ) {
			throw new ParseError(context + " cannot have parameters", classNameParameter.sLoc );
		}
	}
	
	@Override public void data(Command value) throws Exception {
		Phrase cmd = value.subject.subject;
		if( cmd.startsWithWords("field","property") ) {
			defineFieldProperty( parseProperty( cmd.tail(2).unquotedText(), value.modifiers, value.body ) );
		} else if( cmd.startsWithWords("class","property") ) {
			defineClassProperty( parseProperty( cmd.tail(2).unquotedText(), value.modifiers, value.body ) );
		} else if( cmd.startsWithWords("field","modifier") ) {
			String name = cmd.tail(2).unquotedText();
			defineFieldModifier( name, parseFieldModifierSpec(name, value.modifiers, value.body) );
			// TODO
		} else if( cmd.startsWithWord("class") ) {
			ensureNoParameters(value.subject, "class name");
			defineType( parseClass( value.subject.subject.tail().unquotedText(), value.modifiers, value.body ) );
		} else if( cmd.startsWithWord("enum") ) {
			ensureNoParameters(value.subject, "enum name");
			defineType( parseEnum( value.subject.subject.tail().unquotedText(), value.modifiers, value.body ) );
		} else {
			throw new ParseError("Unrecognised command: '"+cmd+"'", value.sLoc);
		}
	}

	@Override public void end() throws Exception {
		_end();
	}
	
	//// Convenience methods for when you don't feel like setting
	//// up your own tokenizers and yaddah yaddah yaddah
	
	public void parse( Reader r, String sourceName ) throws IOException, ParseError {
		Tokenizer t = new Tokenizer();
		if( sourceName != null ) t.setSourceLocation( sourceName, 1, 1 );
		Parser p = new Parser();
		p.pipe(this);
		t.pipe(p);
		try {
			StreamUtil.pipe( r, t );
		} catch( ParseError e ) {
			throw e;
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
	
	public void parse( String source, String sourceName ) throws ParseError {
		try {
			parse( new StringReader(source), sourceName );
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
	}
}
