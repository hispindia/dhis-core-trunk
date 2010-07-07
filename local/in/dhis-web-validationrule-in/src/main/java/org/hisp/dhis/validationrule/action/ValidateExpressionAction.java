package org.hisp.dhis.validationrule.action;

/*
 * Copyright (c) 2004-2007, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18n;

import com.opensymphony.xwork2.Action;

/**
 * @author Lars Helge Overland
 * @version $Id: ValidateExpressionAction.java 5730 2008-09-20 14:32:22Z brajesh $
 */
public class ValidateExpressionAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }
    
    private String expression;

    public void setExpression( String expression )
    {
        this.expression = expression;
    }
    
    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private String message;

    public String getMessage()
    {
        return message;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        // ---------------------------------------------------------------------
        // Description
        // ---------------------------------------------------------------------

        if ( description == null || description.trim().length() == 0 )
        {
            message = i18n.getString( "specify_description" );
            
            return INPUT;
        }
        //System.out.println("expression = " +expression);
        // ---------------------------------------------------------------------
        // Expression
        // ---------------------------------------------------------------------

        if ( expression == null || expression.trim().length() == 0 )
        {
            message = i18n.getString( "specify_expression" );
            
            return INPUT;
        }
        
        String result = expressionService.expressionIsValid( expression );
        
        if ( result.equals( ExpressionService.VALID ) )
        {
            message = i18n.getString( "could_not_save" ) + ": ";
            
            if ( result.equals( ExpressionService.DATAELEMENT_ID_NOT_NUMERIC ) )
            {
                message += i18n.getString( "dataelement_id_must_be_number" );
            }
            else if ( result.equals( ExpressionService.CATEGORYOPTIONCOMBO_ID_NOT_NUMERIC ) )
            {
                message += i18n.getString( "category_option_combo_id_must_be_number" );
            }
            else if ( result.equals( ExpressionService.DATAELEMENT_DOES_NOT_EXIST ) )
            {
                message += i18n.getString( "id_does_not_reference_dataelement" );
            }
            else if ( result.equals( ExpressionService.CATEGORYOPTIONCOMBO_DOES_NOT_EXIST ) )
            {
                message += i18n.getString( "id_does_not_reference_category_option_combo" );
            }
            else if ( result.equals( ExpressionService.EXPRESSION_NOT_WELL_FORMED ) )
            {
                message += i18n.getString( "expression_not_well_formed" );
            }
                        
            return INPUT;
        }
        
        message = "Valid";
        
        return SUCCESS;
    }
}
