/*
 * Copyright (c) 2004-2009, University of Oslo
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

package org.hisp.dhis.dd.action.indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeService;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.attribute.comparator.AttributeNameComparator;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.indicator.IndicatorType;
import org.hisp.dhis.indicator.comparator.IndicatorTypeNameComparator;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * @version $ ShowUpdateIndicatorFormAction.java May 30, 2011 2:34:10 PM $
 * 
 */
public class ShowUpdateIndicatorFormAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private AttributeService attributeService;

    public void setAttributeService( AttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    // -------------------------------------------------------------------------
    // Input/output
    // -------------------------------------------------------------------------

    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }

    private Indicator indicator;

    public Indicator getIndicator()
    {
        return indicator;
    }

    private int selectedIndicatorType;

    public int getSelectedIndicatorType()
    {
        return selectedIndicatorType;
    }

    private String indicatorTypeName;

    public String getIndicatorTypeName()
    {
        return indicatorTypeName;
    }

    private List<IndicatorType> indicatorTypes;

    public List<IndicatorType> getIndicatorTypes()
    {
        return indicatorTypes;
    }

    public Map<Attribute, AttributeValue> attributeMap = new HashMap<Attribute, AttributeValue>();

    public Map<Attribute, AttributeValue> getAttributeMap()
    {
        return attributeMap;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        indicator = indicatorService.getIndicator( id );

        if ( indicator.getIndicatorType() != null )
        {
            selectedIndicatorType = indicator.getIndicatorType().getId();

            indicatorTypeName = indicator.getIndicatorType().getName();
        }

        indicatorTypes = new ArrayList<IndicatorType>( indicatorService.getAllIndicatorTypes() );

        Collections.sort( indicatorTypes, new IndicatorTypeNameComparator() );

        List<AttributeValue> indicatorAttributeValues = new ArrayList<AttributeValue>( indicator.getAttributeValues() );

        List<Attribute> attributes = new ArrayList<Attribute>( attributeService.getIndicatorAttributes() );

        Collections.sort( attributes, new AttributeNameComparator() );

        // TODO fix this.. quite ugly and slow
        for ( Attribute key : attributes )
        {
            AttributeValue value = null;

            for ( AttributeValue av : indicatorAttributeValues )
            {
                if ( value.getAttribute().equals( key ) )
                {
                    value = av;
                }
            }

            attributeMap.put( key, value );
        }

        return SUCCESS;
    }

}
