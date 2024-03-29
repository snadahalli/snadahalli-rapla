/**
 *
 */
package org.rapla.entities.dynamictype.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.rapla.components.util.DateTools;
import org.rapla.components.util.TimeInterval;
import org.rapla.entities.Category;
import org.rapla.entities.IllegalAnnotationException;
import org.rapla.entities.Named;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.RaplaType;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.Classification;
/** 
 * Enables text replacement of variables like {name} {email} with corresponding attribute values
 * Also some functions like {substring(name,1,2)} are available for simple text processing  
 *
 */
public class ParsedText implements Serializable {
    private static final long serialVersionUID = 1;

    /** the terminal format elements*/
    List<String> nonVariablesList;
    /** the variable format elements*/
    List<Function> variablesList ;
    // used for fast storage of text without variables
    private String first;
    
    public ParsedText(String formatString, ParseContext context) throws IllegalAnnotationException 
    {
	        variablesList = new ArrayList<Function>();
	        nonVariablesList = new ArrayList<String>();
	        int pos = 0;
	        int length = formatString.length();
	        List<String> variableContent = new ArrayList<String>();
	        while (pos < length)
	        {
	            int start = formatString.indexOf('{',pos) + 1;
	            if (start < 1) {
	                nonVariablesList.add(formatString.substring(pos, length ));
	                break;
	            }
	            int end = formatString.indexOf('}',start);
	            if (end < 1 )
	                throw new IllegalAnnotationException("Closing bracket } missing! in " + formatString);
	
	            nonVariablesList.add(formatString.substring(pos, start -1));
	           
	            String key = formatString.substring(start,end).trim();
	            variableContent.add( key );
	           
	            pos = end + 1;
	        }
	        for ( String content: variableContent)
	        {
	        	Function func  =parseFunctions(context,content);
	        	variablesList.add( func);
	        }
	        if ( variablesList.isEmpty() )
	        {
	            if (nonVariablesList.size()>0)
	            {
	                first = nonVariablesList.iterator().next();
	            }
	            variablesList = null;
	            nonVariablesList = null;
	        }
	        
	        
    }

    public String getExternalRepresentation(ParseContext context) {
        if ( nonVariablesList == null)
        {
            return first;
        }
        
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<nonVariablesList.size(); i++) {
            buf.append(nonVariablesList.get(i));
            if ( i < variablesList.size() ) {
                Function variable = variablesList.get(i);
                String representation = variable.getRepresentation(context);
                buf.append("{");
                buf.append( representation);
                buf.append("}");
            }
        }
        return buf.toString();
    }

    public String formatName(EvalContext context) {
        if ( nonVariablesList == null)
        {
            return first;
        }
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<nonVariablesList.size(); i++) {
            buf.append(nonVariablesList.get(i));
            if ( i < variablesList.size()) {
                Function function = variablesList.get(i);
				Object result = function.eval(context);
				String stringResult = toString(result, context);
                buf.append( stringResult);
            }
        }
        return buf.toString();
    }
   
	Function parseFunctions(ParseContext context,String content)
			throws IllegalAnnotationException {
		StringBuffer functionName = new StringBuffer();
		for ( int i=0;i<content.length();i++)
		{
			char c = content.charAt(i);
			if ( c == '(' )
			{
				int depth = 0;
				for ( int j=i+1;j<content.length();j++)
				{
					if ( functionName.length() == 0)
					{
						throw new IllegalAnnotationException("Function name missing");
					}
					char c2 = content.charAt(j);
					
					if ( c2== ')')
					{
						if ( depth == 0)
						{
							String recursiveContent = content.substring(i+1,j);
							String function = functionName.toString();
							return parseArguments( context,function,recursiveContent);
						}
						else
						{
							depth--; 
						}
					}
					if ( c2 == '(' )
					{
						depth++;
					}
				}
			}
			else if ( c== ')')
			{
				throw new IllegalAnnotationException("Opening parenthis missing.");
			}
			else
			{
				functionName.append( c);
			}
		}
		String variableName = functionName.toString();
		Function varFunction = context.resolveVariableFunction( variableName);
		if (varFunction != null)
		{
			return varFunction;
		}
        else 
        {
        	try
        	{
        		Long l =  Long.parseLong( variableName.trim() );
        		return new IntVariable( l);
        	} 
        	catch (NumberFormatException ex)
        	{
        	}
//        	try
//        	{
//        		Double d = Double.parseDouble( variableName);
//        	} catch (NumberFormatException ex)
//        	{
//        	}
        	throw new IllegalAnnotationException("Attribute for key '" 
                                                 + variableName 
                                                 + "' not found. You have probably deleted or renamed the attribute. "
                                            );
        }
	
	}

	private Function parseArguments(ParseContext context,String functionName, String content) throws IllegalAnnotationException {
		int depth = 0;
		List<Function> args = new ArrayList<Function>();
		StringBuffer currentArg = new StringBuffer();
		for ( int i=0;i<content.length();i++)
		{
			char c = content.charAt(i);
			if ( c == '(' )
			{
				depth++;
			}
			else if ( c == ')' )
			{
				depth --;
			}
			if ( c != ',' ||  depth > 0)
			{
				currentArg.append( c);
			}
			if ( depth == 0)
			{
				if ( c == ',' || i == content.length()-1)
				{
					String arg = currentArg.toString();
					Function function = parseFunctions(context,arg);
					args.add(function);
					currentArg = new StringBuffer();
				}
			}
			
		}
		if ( functionName.equals("key"))
		{
			return new KeyFunction(args);
		}
		if ( functionName.equals("parent"))
		{
			return new ParentFunction(args);
		}
		if ( functionName.equals("substring"))
		{
			return new SubstringFunction(args);
		}
		else
		{
			 throw new IllegalAnnotationException("Unknown function '"+ functionName + "'" );
		}
		//return new SubstringFunction(functionName, args);
	}
	
	
	static public abstract class Function
	{
		String name;
		List<Function> args;
		
		public Function( String name,List<Function> args )
		{
			this.name = name;
			this.args = args;
		}
		
		public Function(String name) {
			this.name = name;
			this.args = Collections.emptyList();
		}
		
		protected String getName() {
            return name;
        }

		public abstract Object eval(EvalContext context);
		
		public String getRepresentation( ParseContext context)
		{
			StringBuffer buf = new StringBuffer();
			buf.append(getName());
			for (  int i=0;i<args.size();i++)
			{
				if ( i == 0)
				{
					buf.append("(");
				}
				else
				{
					buf.append(",");
				}
				buf.append( args.get(i).getRepresentation(context));
				if ( i == args.size() - 1)
				{
					buf.append(")");
				}
			}
			return buf.toString();
		}
		
		public String toString()
		{
			StringBuffer buf = new StringBuffer();
			buf.append(getName());
			for (  int i=0;i<args.size();i++)
			{
				if ( i == 0)
				{
					buf.append("(");
				}
				else
				{
					buf.append(", ");
				}
				buf.append( args.get(i).toString());
				if ( i == args.size() - 1)
				{
					buf.append(")");
				}
			}
			return buf.toString();
		}
		
	}

	class KeyFunction extends Function
	{
		Function arg;
		public KeyFunction( List<Function> args) throws IllegalAnnotationException {
			super( "key", args);
			if ( args.size() != 1)
			{
				throw new IllegalAnnotationException("Key Function expects one argument!");
			}
			arg = args.get(0);
			testMethod();
		}

		private void testMethod() throws IllegalAnnotationException {
//			Method method;
//			try {
//				Class<? extends Function> class1 = arg.getClass();
//				method = class1.getMethod("eval", new Class[] {EvalContext.class});
//			} catch (Exception e) {
//				String message = e.getMessage();
//				throw new IllegalAnnotationException( "Could not parse method for internal error : " + message);
//			}
//			if ( !method.getReturnType().isAssignableFrom(Attribute.class))
//			{
//				if ( !method.getReturnType().isAssignableFrom(Category.class))
//				{
//					throw new IllegalAnnotationException("Key Function expects an attribute variable or a function which returns a category");
//				}
//			}
		}

		@Override
		public String eval(EvalContext context) 
		{
			Object obj = arg.eval( context);
			if ( obj == null || !(obj instanceof RaplaObject))
			{
				return "";
			}
			RaplaObject raplaObject = (RaplaObject) obj;
			RaplaType raplaType = raplaObject.getRaplaType();
			if ( raplaType == Category.TYPE)
			{
				Category category = (Category) raplaObject;
				String key = category.getKey();
				return key;
			
			}
			else if ( raplaType == Attribute.TYPE)
			{
				Classification classification = context.getClassification();
				Object result = classification.getValue((Attribute) raplaObject);
				if ( result instanceof Category)
				{
					String key = ((Category) result).getKey();
					return key;
				}
			}
			
			return "";
		}
	}
	
	
	class IntVariable extends Function
	{
		Long l;
		public IntVariable( Long l) {
			super( "long");
			this.l = l;
		}

		@Override
		public Long eval(EvalContext context) 
		{
			return l;
		}
		
		public String getRepresentation( ParseContext context)
		{
			return l.toString();
		}
		
		public String toString()
		{
			return l.toString();
		}
		
	}
	class ParentFunction extends Function
	{
		Function arg;
		public ParentFunction( List<Function> args) throws IllegalAnnotationException {
			super( "parent", args);
			if ( args.size() != 1)
			{
				throw new IllegalAnnotationException("Parent Function expects one argument!");
			}
			arg = args.get(0);
			testMethod();
		}

		private void testMethod() throws IllegalAnnotationException {
//			Method method;
//			try {
//				Class<? extends Function> class1 = arg.getClass();
//				method = class1.getMethod("eval", new Class[] {EvalContext.class});
//			} catch (Exception e) {
//				throw new IllegalAnnotationException( "Could not parse method for internal error : " + e.getMessage());
//			}
//			if ( !method.getReturnType().isAssignableFrom(Attribute.class))
//			{
//				if ( !method.getReturnType().isAssignableFrom(Category.class))
//				{
//					throw new IllegalAnnotationException("Parent Function expects an attribute variable or a function which returns a category");
//				}
//			}
		}

		@Override
		public Category eval(EvalContext context) 
		{
			Object obj = arg.eval( context);
			if ( obj == null || !(obj instanceof RaplaObject))
			{
				return null;
			}
			RaplaObject raplaObject = (RaplaObject)obj;
			
			RaplaType raplaType = raplaObject.getRaplaType();
			if ( raplaType == Category.TYPE)
			{
				Category category = (Category) raplaObject;
				return category.getParent();
			}
			else if ( raplaType == Attribute.TYPE)
			{
				Classification classification = context.getClassification();
				Object result = classification.getValue((Attribute) raplaObject);
				if ( result instanceof Category)
				{
					return ((Category) result).getParent();
				}
			}
			return null;
			
		}
		
	}
	class SubstringFunction extends Function
	{
		Function content;
		Function start;
		Function end;
		public SubstringFunction( List<Function> args) throws IllegalAnnotationException {
			super( "substring", args);
			if ( args.size() != 3)
			{
				throw new IllegalAnnotationException("Substring Function expects 3 argument!");
			}
			content = args.get(0);
			start = args.get(1);
			end = args.get(2);
			testMethod();
		}

		private void testMethod() throws IllegalAnnotationException {
//			{
//				Method method;
//				try {
//					Class<? extends Function> class1 = start.getClass();
//					method = class1.getMethod("eval", new Class[] {EvalContext.class});
//				} catch (Exception e) {
//					throw new IllegalAnnotationException( "Could not parse method for internal error : " + e.getMessage());
//				}
//				if ( !method.getReturnType().isAssignableFrom(Long.class))
//				{
//					throw new IllegalAnnotationException( "Substring method expects a Long parameter as second argument");
//				}
//			}
//			{
//				Method method;
//				try {
//					Class<? extends Function> class1 = end.getClass();
//					method = class1.getMethod("eval", new Class[] {EvalContext.class});
//				} catch (Exception e) {
//					throw new IllegalAnnotationException( "Could not parse method for internal error : " + e.getMessage());
//				}
//				if ( !method.getReturnType().isAssignableFrom(Long.class))
//				{
//					throw new IllegalAnnotationException( "Substring method expects a Long parameter as third argument");
//				}
//			}
		}

		@Override
		public String eval(EvalContext context) 
		{
			Object result =content.eval( context);
			String stringResult = ParsedText.this.toString( result, context);
			if ( stringResult == null)
			{
				return stringResult;
			}
			Long firstIndex = (Long)start.eval( context);
			Long lastIndex = (Long) end.eval( context);
			if ( firstIndex == null)
			{
				return null;
			}
			else if ( lastIndex == null)
			{
				return stringResult.substring( Math.min(firstIndex.intValue(), stringResult.length()) );
			}
			else 
			{
				return stringResult.substring( Math.min(firstIndex.intValue(), stringResult.length())
						                     , Math.min(lastIndex.intValue(), stringResult.length()) 
						                     );
			}
			
		}
	}
	 
	private String toString(Object result, EvalContext context) {
		if ( result == null)
		{
			return "";
		}
		Locale locale = context.getLocale();
		if ( result instanceof Collection)
		{
			StringBuffer buf = new StringBuffer();
			Collection<?> collection = (Collection<?>) result;
			int i=0;
			for ( Object obj: collection)
			{
				if ( i>0)
				{
					buf.append(", ");
				}
				buf.append(toString(obj, context));
				i++;
			}
			return buf.toString();
		}
		else if ( result instanceof TimeInterval)
		{
			Date start = ((TimeInterval) result).getStart();
			Date end = ((TimeInterval) result).getEnd();
			if ( DateTools.cutDate( end ).equals( end))
			{
				end = DateTools.subDay(end);
			}
			StringBuffer buf = new StringBuffer();
			buf.append( DateTools.formatDate( start, locale));
			
			if ( end != null && end.after(start))
			{
				buf.append( "-");
				buf.append( DateTools.formatDate( end, locale));
			}
			return buf.toString();
		}
		else if ( result instanceof Date)
		{
			Date date = (Date) result;
			StringBuffer buf = new StringBuffer();
			buf.append( DateTools.formatDate( date, locale));
			return buf.toString();			
		}
		else if ( result instanceof Attribute)
		{
			Attribute attribute = (Attribute) result;
			Classification classification = context.getClassification();
			return classification.getValueAsString(attribute, locale);
		}
		else if ( result instanceof Named)
		{
			return ((Named) result).getName( locale);
		}
		return result.toString();
	}

	public interface ParseContext
	{
		Function resolveVariableFunction(String variableName) throws IllegalAnnotationException;
	}
	

	static public class EvalContext
	{
		private Locale locale;

		public EvalContext( Locale locale)
		{
			this.locale = locale;
		}
		
		/**  override this method if you can return a classification object in the context. Than the use of attributes is possible*/
		public Classification getClassification() 
		{
			return null;
		}
		
		public Locale getLocale() 
		{
			return locale;
		}
		
	}
}


