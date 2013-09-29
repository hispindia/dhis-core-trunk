package org.hisp.dhis.analytics.event;

/*
 * Copyright (c) 2004-2013, University of Oslo
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.BaseDimensionalObject;
import org.hisp.dhis.common.DimensionType;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.common.NameableObjectUtils;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;

/**
 * @author Lars Helge Overland
 */
public class EventQueryParams
{
    public static final String OU_MODE_SELECTED = "selected";
    public static final String OU_MODE_CHILDREN = "children";
    public static final String OU_MODE_DESCENDANTS = "descendants";
    
    private Program program;
    
    private ProgramStage programStage;
    
    private Date startDate;
    
    private Date endDate;
    
    private List<QueryItem> items = new ArrayList<QueryItem>();

    private List<DimensionalObject> dimensions = new ArrayList<DimensionalObject>();
    
    private List<String> asc = new ArrayList<String>();
    
    private List<String> desc = new ArrayList<String>();
    
    private String organisationUnitMode;
    
    private String tableName;
    
    private Integer page;
    
    private Integer pageSize;

    // -------------------------------------------------------------------------
    // Transient properties
    // -------------------------------------------------------------------------
    
    private String periodType;
    
    private int organisationUnitLevel;
    
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EventQueryParams()
    {
    }
    
    public EventQueryParams instance()
    {
        EventQueryParams params = new EventQueryParams();
        
        params.program = this.program;
        params.programStage = this.programStage;
        params.startDate = this.startDate;
        params.endDate = this.endDate;
        params.items = new ArrayList<QueryItem>( this.items );
        params.dimensions = new ArrayList<DimensionalObject>( this.dimensions );            
        params.asc = new ArrayList<String>( this.asc );
        params.desc = new ArrayList<String>( this.desc );
        params.organisationUnitMode = this.organisationUnitMode;
        params.tableName = this.tableName;
        params.page = this.page;
        params.pageSize = this.pageSize;
        params.periodType = this.periodType;
        params.organisationUnitLevel = this.organisationUnitLevel;
        
        return params;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public boolean isOrganisationUnitMode( String mode )
    {
        return organisationUnitMode != null && organisationUnitMode.equalsIgnoreCase( mode );
    }

    public List<OrganisationUnit> getOrganisationUnits()
    {
        int index = dimensions.indexOf( new BaseDimensionalObject( DimensionalObject.ORGUNIT_DIM_ID ) );
        return index != -1 ? NameableObjectUtils.asTypedList( dimensions.get( index ).getItems(), OrganisationUnit.class ) : null;
    }

    public void setOrganisationUnits( List<OrganisationUnit> organisationUnits )
    {
        setDimensionOptions( DimensionalObject.ORGUNIT_DIM_ID, DimensionType.ORGANISATIONUNIT, null, organisationUnits );
    }
    
    public List<Period> getPeriods()
    {
        int index = dimensions.indexOf( new BaseDimensionalObject( DimensionalObject.PERIOD_DIM_ID ) );
        return index != -1 ? NameableObjectUtils.asTypedList( dimensions.get( index ).getItems(), Period.class ) : null;
    }
    
    public void setPeriods( List<Period> periods )
    {
        setDimensionOptions( DimensionalObject.PERIOD_DIM_ID, DimensionType.PERIOD, null, periods );
    }
    
    public boolean hasStartEndDate()
    {
        return startDate != null && endDate != null;
    }
    
    private EventQueryParams setDimensionOptions( String dimension, DimensionType type, String dimensionName, List<? extends NameableObject> options )
    {
        int index = dimensions.indexOf( new BaseDimensionalObject( dimension ) );
        
        if ( index != -1 )
        {
            dimensions.set( index, new BaseDimensionalObject( dimension, type, dimensionName, options ) );
        }
        else
        {
            dimensions.add( new BaseDimensionalObject( dimension, type, dimensionName, options ) );
        }
        
        return this;
    }
    
    public Set<OrganisationUnit> getOrganisationUnitChildren()
    {
        Set<OrganisationUnit> children = new HashSet<OrganisationUnit>();
        
        for ( OrganisationUnit unit : getOrganisationUnits() )
        {
            children.addAll( unit.getChildren() );
        }
        
        return children;
    }
    
    public boolean isSorting()
    {
        return ( asc != null && !asc.isEmpty() ) || ( desc != null && !desc.isEmpty() );
    }
    
    public boolean isPaging()
    {
        return page != null || pageSize != null;
    }

    public int getPageWithDefault()
    {
        return page != null && page > 0 ? page : 1;
    }
    
    public int getPageSizeWithDefault()
    {
        return pageSize != null && pageSize >= 0 ? pageSize : 50;
    }

    public int getOffset()
    {
        return ( getPageWithDefault() - 1 ) * getPageSizeWithDefault();
    }
    
    public String toString()
    {
        return "[" +
            "Program: " + program + ", " +
            "Stage: " + programStage + ", " +
            "Start date: " + startDate + ", " +
            "End date: " + endDate + ", " +
            "Items " + items + ", " +
            "Dimensions " + dimensions + "]";
    }
    
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Program getProgram()
    {
        return program;
    }

    public void setProgram( Program program )
    {
        this.program = program;
    }

    public ProgramStage getProgramStage()
    {
        return programStage;
    }

    public void setProgramStage( ProgramStage programStage )
    {
        this.programStage = programStage;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate( Date startDate )
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate( Date endDate )
    {
        this.endDate = endDate;
    }

    public List<QueryItem> getItems()
    {
        return items;
    }

    public void setItems( List<QueryItem> items )
    {
        this.items = items;
    }

    public List<String> getAsc()
    {
        return asc;
    }

    public List<DimensionalObject> getDimensions()
    {
        return dimensions;
    }

    public void setDimensions( List<DimensionalObject> dimensions )
    {
        this.dimensions = dimensions;
    }

    public void setAsc( List<String> asc )
    {
        this.asc = asc;
    }

    public List<String> getDesc()
    {
        return desc;
    }

    public void setDesc( List<String> desc )
    {
        this.desc = desc;
    }

    public String getOrganisationUnitMode()
    {
        return organisationUnitMode;
    }

    public void setOrganisationUnitMode( String organisationUnitMode )
    {
        this.organisationUnitMode = organisationUnitMode;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName( String tableName )
    {
        this.tableName = tableName;
    }

    public Integer getPage()
    {
        return page;
    }

    public void setPage( Integer page )
    {
        this.page = page;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public void setPageSize( Integer pageSize )
    {
        this.pageSize = pageSize;
    }

    public String getPeriodType()
    {
        return periodType;
    }

    public void setPeriodType( String periodType )
    {
        this.periodType = periodType;
    }

    public int getOrganisationUnitLevel()
    {
        return organisationUnitLevel;
    }

    public void setOrganisationUnitLevel( int organisationUnitLevel )
    {
        this.organisationUnitLevel = organisationUnitLevel;
    }
}
