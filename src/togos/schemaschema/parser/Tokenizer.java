package togos.schemaschema.parser;

import java.io.IOException;
import java.io.InputStreamReader;

import togos.asyncstream.BaseStreamSource;
import togos.asyncstream.StreamDestination;
import togos.asyncstream.StreamUtil;
import togos.lang.BaseSourceLocation;
import togos.lang.ParseError;
import togos.lang.ScriptError;
import togos.lang.SourceLocation;

public class Tokenizer extends BaseStreamSource<Token,ScriptError> implements StreamDestination<char[],ScriptError>
{
	enum State {
		NO_TOKEN,
		WORD_BOUNDARY, // Just read a bareword or quoted string
		SYMBOL( NO_TOKEN, Token.Type.SYMBOL ),
		BAREWORD( NO_TOKEN, Token.Type.BAREWORD ),
		SINGLE_QUOTED_STRING( NO_TOKEN, Token.Type.SINGLE_QUOTED_STRING ),
		SINGLE_QUOTED_STRING_ESCAPE( SINGLE_QUOTED_STRING ),
		DOUBLE_QUOTED_STRING( NO_TOKEN, Token.Type.DOUBLE_QUOTED_STRING ),
		DOUBLE_QUOTED_STRING_ESCAPE( DOUBLE_QUOTED_STRING ),
		SINGLE_ANGLE_STRING( NO_TOKEN, Token.Type.SINGLE_QUOTED_STRING ),
		DOUBLE_ANGLE_STRING( NO_TOKEN, Token.Type.DOUBLE_QUOTED_STRING ),
		LINE_COMMENT;
		
		public final State parentState;
		public final Token.Type tokenType;
		State( State parentState, Token.Type tokenType ) {
			this.parentState = parentState;
			this.tokenType = tokenType;
		}
		State( State parentState ) {
			this( parentState, null );
		}
		State() {
			this( null );
		}
	};
	
	public String filename = "unknown source";
	public int lineNumber = 1, columnNumber = 1;
	public int tabWidth = 4; // Because it's the default in Eclipse.
	protected StringBuilder tokenBuffer = new StringBuilder();
	protected State state = State.NO_TOKEN;
	protected int quoteDepth;
	
	public void setSourceLocation( String filename ) {
		setSourceLocation( filename, 1, 1 );
	}
	
	public void setSourceLocation( String filename, int l, int c ) {
		this.filename = filename;		
		this.lineNumber = l;
		this.columnNumber = c;
	}
	
	public void setSourceLocation( SourceLocation sLoc ) {
		setSourceLocation( sLoc.getSourceFilename(), sLoc.getSourceLineNumber(), sLoc.getSourceColumnNumber() );
	}
	
	public SourceLocation getSourceLocation() {
		return new BaseSourceLocation( filename, lineNumber, columnNumber );
	}
	
	protected static boolean isWhitespace( char c ) {
		switch( c ) {
		case ' ': case '\t': case '\r':
			return true;
		default:
			return false;
		}
	}
	
	protected static boolean isSymbol( char c ) {
		switch( c ) {
		case '[': case ']': case '(': case ')': case '{': case '}': case ',': case ';': case ':': case '=': case '@': case '\n':
			return true;
		default:
			return false;
		}
	}
	
	protected static boolean isQuote( char c ) {
		switch(c) {
		case '\'': case '"':
		case '‘': case '’':
		case '“': case '”':
		case '‹': case '›':
		case '«': case '»':
			return true;
		default:
			return false;
		}
	}
	
	protected static boolean isComment( char c ) {
		return c == '#';
	}
	
	public static boolean isWordChar( char c ) {
		return !isSymbol(c) && !isWhitespace(c) && !isQuote(c) && !isComment(c);
	}
	
	protected void data( char c ) throws ScriptError {
		switch( state ) {
		case SINGLE_QUOTED_STRING_ESCAPE:
		case DOUBLE_QUOTED_STRING_ESCAPE:
			switch( c ) {
			case '\\': case '\'': case '"':
				tokenBuffer.append(c);
				break;
			case 'n':
				tokenBuffer.append('\n');
				break;
			case 'r':
				tokenBuffer.append('\r');
				break;
			case 't':
				tokenBuffer.append('\t');
				break;
			default:
				throw new ParseError("Invalid escape character: '"+c+"'", getSourceLocation());
			}
			state = state.parentState;
			break;
		case SINGLE_QUOTED_STRING:
			switch( c ) {
			case '\'':
				flushToken( State.WORD_BOUNDARY );
				break;
			case '\\':
				state = State.SINGLE_QUOTED_STRING_ESCAPE;
				break;
			default:
				tokenBuffer.append(c);
			}
			break;
		case SINGLE_ANGLE_STRING:
			if( c == '›' && --quoteDepth == 0 ) {
				flushToken( State.WORD_BOUNDARY );
			} else {
				if( c == '‹' ) ++quoteDepth;
				tokenBuffer.append(c);
			}
			break;
		case DOUBLE_ANGLE_STRING:
			if( c == '»' && --quoteDepth == 0 ) {
				flushToken( State.WORD_BOUNDARY );
			} else {
				if( c == '«' ) ++quoteDepth;
				tokenBuffer.append(c);
			}
			break;
		case DOUBLE_QUOTED_STRING:
			switch( c ) {
			case '"':
				flushToken( State.WORD_BOUNDARY );
				break;
			case '\\':
				state = State.DOUBLE_QUOTED_STRING_ESCAPE;
				break;
			default:
				tokenBuffer.append(c);
			}
			break;
		case LINE_COMMENT:
			if( c == '\n' ) {
				state = State.SYMBOL;
				tokenBuffer.setLength(0);
				tokenBuffer.append(c);
				flushToken( State.NO_TOKEN );
			}
			break;
		case BAREWORD:
			if( isQuote(c) ) {
				throw new ParseError("No quotes allowed here; add some whitespace!", getSourceLocation());
			} else if( isSymbol(c) ) {
				flushToken( State.SYMBOL );
				tokenBuffer.append(c);
				flushToken( State.NO_TOKEN );
			} else if( isWhitespace(c) ) {
				flushToken( State.NO_TOKEN );
			} else if( isComment(c) ) {
				flushToken( State.LINE_COMMENT );
			} else {
				tokenBuffer.append(c);
			}
			break;
		case WORD_BOUNDARY:
			if( isQuote(c) || isWordChar(c) ) {
				throw new ParseError("No quotes allowed here; add some whitespace!", getSourceLocation());
			}
			// Intentional fall-through!
		case NO_TOKEN:
			if( c == '#' ) {
				state = State.LINE_COMMENT;
			} else if( c == '"' ) {
				state = State.DOUBLE_QUOTED_STRING;
			} else if( c == '\'' ) {
				state = State.SINGLE_QUOTED_STRING;
			} else if( c == '‹' ) {
				state = State.SINGLE_ANGLE_STRING;
				quoteDepth = 1;
			} else if( c == '«' ) {
				state = State.DOUBLE_ANGLE_STRING;
				quoteDepth = 1;
			} else if( isSymbol(c) ) {
				state = State.SYMBOL;
				tokenBuffer.append(c);
				flushToken( State.NO_TOKEN );
			} else if( isComment(c) ) {
				flushToken( State.LINE_COMMENT );
			} else if( isWhitespace(c) ) {
				state = State.NO_TOKEN;
			} else {
				state = State.BAREWORD;
				tokenBuffer.append(c);
			}
			break;
		default:
			throw new ParseError("Invalid tokenizer state: "+state, getSourceLocation());
		}
		
		if( c == '\t' ) {
			columnNumber += tabWidth;
		} else if( c == '\n' ) {
			lineNumber += 1;
			columnNumber = 1;
		} else {
			++columnNumber;
		}
	}
	
	protected void flushToken( final State newState ) throws ScriptError {
		if( newState == null ) {
			throw new RuntimeException("Oh you'd better not flushToken(null)!");
		}
		if( state == null ) {
			throw new RuntimeException("Oh no, somehow tokenizer state became null.  >:(");
		}
		if( state.tokenType != null ) {
			_data( new Token( state.tokenType, tokenBuffer.toString(), filename, lineNumber, columnNumber ) );
		}
		state = newState;
		tokenBuffer.setLength(0);
	}
	
	@Override
    public void data( char[] value ) throws ScriptError {
		for( char c : value ) data(c);
    }
	
	@Override
    public void end() throws ScriptError {
		flushToken( State.NO_TOKEN );
		_end();
    }
	
	public static void main(String[] args) throws IOException, ScriptError {
		Tokenizer t = new Tokenizer();
		t.pipe(new StreamDestination<Token,ScriptError>() {
			@Override public void data( Token value ) {
				System.out.println("Token: '"+value.text+"'");
            }

			@Override public void end() { }
		});
		StreamUtil.pipe( new InputStreamReader(System.in), t, true );
	}
}
