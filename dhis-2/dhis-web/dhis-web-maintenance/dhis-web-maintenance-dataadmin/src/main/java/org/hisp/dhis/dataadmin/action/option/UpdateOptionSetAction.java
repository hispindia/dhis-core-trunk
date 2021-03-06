package org.hisp.dhis.dataadmin.action.option;

/*
 * Copyright (c) 2004-2016, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import com.opensymphony.xwork2.Action;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.attribute.AttributeService;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Chau Thu Tran
 * @version $UpdateOptionSetAction.java Feb 3, 2012 9:28:11 PM$
 */
public class UpdateOptionSetAction
    implements Action
{
    // -------------------------------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------------------------------

    private OptionService optionService;

    public void setOptionService( OptionService optionService )
    {
        this.optionService = optionService;
    }

    @Autowired
    private AttributeService attributeService;

    // -------------------------------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------------------------------

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

    private String code;

    public void setCode( String code )
    {
        this.code = code;
    }

    private List<String> jsonAttributeValues;

    public void setJsonAttributeValues( List<String> jsonAttributeValues )
    {
        this.jsonAttributeValues = jsonAttributeValues;
    }

    // -------------------------------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        OptionSet optionSet = optionService.getOptionSet( id );
        optionSet.setName( StringUtils.trimToNull( name ) );
        optionSet.setCode( StringUtils.trimToNull( code ) );

        if ( jsonAttributeValues != null )
        {
            attributeService.updateAttributeValues( optionSet, jsonAttributeValues );
        }

        optionService.updateOptionSet( optionSet );

        return SUCCESS;
    }

}
