package togos.schemaschema.parser;


public class CommandInterpreters
{
	private CommandInterpreters() { }
	
	public static void defineTypeDefinitionCommands( SchemaInterpreter sp ) {
		sp.defineCommand("class", sp.new ClassDefinitionCommandInterpreter());
		sp.defineCommand("enum", sp.new EnumDefinitionCommandInterpreter());
		sp.defineCommand("property", sp.new PropertyDefinitionCommandInterpreter(sp.generalModifiers));
		sp.defineCommand("modifier", sp.new ModifierDefinitionCommandInterpreter(sp.generalModifiers));
		sp.defineCommand("field property", sp.new PropertyDefinitionCommandInterpreter(sp.fieldModifiers));
		sp.defineCommand("class property", sp.new PropertyDefinitionCommandInterpreter(sp.classModifiers));
		sp.defineCommand("field modifier", sp.new ModifierDefinitionCommandInterpreter(sp.fieldModifiers));
		sp.defineCommand("class modifier", sp.new ModifierDefinitionCommandInterpreter(sp.classModifiers));
	}
}
