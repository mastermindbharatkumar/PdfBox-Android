package org.apache.pdfboxandroid.pdmodel.interactive.form;

import java.io.IOException;

import org.apache.pdfboxandroid.cos.COSArray;
import org.apache.pdfboxandroid.cos.COSBase;
import org.apache.pdfboxandroid.cos.COSDictionary;
import org.apache.pdfboxandroid.cos.COSInteger;
import org.apache.pdfboxandroid.cos.COSName;
import org.apache.pdfboxandroid.cos.COSString;

/**
 * A class for handling the PDF field as a choicefield.
 *
 * @author sug
 * @version $Revision: 1.7 $
 */
public class PDChoiceField extends PDVariableText {
	/**
     * A Ff flag.
     */
    public static final int FLAG_COMBO = 1 << 17;

    /**
     * A Ff flag.
     */
    public static final int FLAG_EDIT = 1 << 18;
	
	/**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The field for this choice field.
     */
    public PDChoiceField( PDAcroForm theAcroForm, COSDictionary field)
    {
        super(theAcroForm, field);
    }
    
    /**
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField#setValue(java.lang.String)
     *
     * @param optionValue The new value for this text field.
     *
     * @throws IOException If there is an error calculating the appearance stream or the value in not one
     *   of the existing options.
     */
    public void setValue(String optionValue) throws IOException
    {
        int indexSelected = -1;
        COSArray options = (COSArray)getDictionary().getDictionaryObject( COSName.OPT );
        int fieldFlags = getFieldFlags();
        boolean isEditable = (FLAG_COMBO & fieldFlags) != 0 && (FLAG_EDIT & fieldFlags) != 0;
        
        if( options.size() == 0 && ! isEditable )
        {
            throw new IOException( "Error: You cannot set a value for a choice field if there are no options." );
        }
        else
        {
            // YXJ: Changed the order of the loops. Acrobat produces PDF's
            // where sometimes there is 1 string and the rest arrays.
            // This code works either way.
            for( int i=0; i<options.size() && indexSelected == -1; i++ ) 
            {
                COSBase option = options.getObject( i );
                if( option instanceof COSArray )
                {
                    COSArray keyValuePair = (COSArray)option;
                    COSString key = (COSString)keyValuePair.getObject( 0 );
                    COSString value = (COSString)keyValuePair.getObject( 1 );
                    if( optionValue.equals( key.getString() ) || optionValue.equals( value.getString() ) )
                    {
                        //have the parent draw the appearance stream with the value
                        super.setValue( value.getString() );
                        //but then use the key as the V entry
                        getDictionary().setItem( COSName.V, key );
                        indexSelected = i;
                    }
                }
                else
                {
                    COSString value = (COSString)option;
                    if( optionValue.equals( value.getString() ) )
                    {
                        super.setValue( optionValue );
                        indexSelected = i;
                    }
                }
            }
        }
        if( indexSelected == -1 && isEditable ) 
        {
            super.setValue( optionValue );
        }
        else if( indexSelected == -1 )
        {
            throw new IOException( "Error: '" + optionValue + "' was not an available option.");
        }
        else
        {
            COSArray indexArray = (COSArray)getDictionary().getDictionaryObject( COSName.I );
            if( indexArray != null )
            {
                indexArray.clear();
                indexArray.add( COSInteger.get( indexSelected ) );
            }
        }
    }
}
