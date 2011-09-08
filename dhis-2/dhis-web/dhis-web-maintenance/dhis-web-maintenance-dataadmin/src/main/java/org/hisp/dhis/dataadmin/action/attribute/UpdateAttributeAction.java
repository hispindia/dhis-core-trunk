/*
 * Copyright (c) 2004-2011, University of Oslo
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

package org.hisp.dhis.dataadmin.action.attribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeOption;
import org.hisp.dhis.attribute.AttributeService;

import com.opensymphony.xwork2.Action;

/**
 * @author mortenoh
 */
public class UpdateAttributeAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private AttributeService attributeService;

    public void setAttributeService( AttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String valueType;

    public void setValueType( String valueType )
    {
        this.valueType = valueType;
    }

    private Boolean mandatory = false;

    public void setMandatory( Boolean mandatory )
    {
        this.mandatory = mandatory;
    }

    private Boolean dataElement = false;

    public void setDataElement( Boolean dataElement )
    {
        this.dataElement = dataElement;
    }

    private Boolean indicator = false;

    public void setIndicator( Boolean indicator )
    {
        this.indicator = indicator;
    }

    private Boolean organisationUnit = false;

    public void setOrganisationUnit( Boolean organisationUnit )
    {
        this.organisationUnit = organisationUnit;
    }

    private List<Integer> selectedAttributeOptions = new ArrayList<Integer>();

    public void setSelectedAttributeOptions( List<Integer> selectedAttributeOptions )
    {
        this.selectedAttributeOptions = selectedAttributeOptions;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
    {
        Attribute attribute = attributeService.getAttribute( id );

        if ( attribute != null )
        {
            attribute.setName( name );
            attribute.setValueType( valueType );
            attribute.setMandatory( mandatory );
            attribute.setDataElement( dataElement );
            attribute.setIndicator( indicator );
            attribute.setOrganisationUnit( organisationUnit );

            Set<AttributeOption> attributeOptions = new HashSet<AttributeOption>();

            if ( valueType.compareTo( "multiple_choice" ) == 0 )
            {
                for ( Integer id : selectedAttributeOptions )
                {
                    attributeOptions.add( attributeService.getAttributeOption( id ) );
                }
            }

            attribute.setAttributeOptions( attributeOptions );

            attributeService.updateAttribute( attribute );

            return SUCCESS;
        }

        return ERROR;
    }
}
