package org.hisp.dhis.orgunitdistribution.impl;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hisp.dhis.chart.ChartService;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.organisationunit.comparator.OrganisationUnitGroupNameComparator;
import org.hisp.dhis.organisationunit.comparator.OrganisationUnitNameComparator;
import org.hisp.dhis.orgunitdistribution.OrgUnitDistributionService;
import org.hisp.dhis.system.grid.ListGrid;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author Lars Helge Overland
 */
public class DefaultOrgUnitDistributionService
    implements OrgUnitDistributionService
{
    private static final Comparator<OrganisationUnit> ORGUNIT_COMPARATOR = new OrganisationUnitNameComparator();
    private static final Comparator<OrganisationUnitGroup> ORGUNIT_GROUP_COMPARATOR = new OrganisationUnitGroupNameComparator();

    private static final String TITLE_SEP = " - ";
    private static final String FIRST_COLUMN_TEXT = "Organisation unit";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitService organisationUnitService;
    
    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private ChartService chartService;
    
    public void setChartService( ChartService chartService )
    {
        this.chartService = chartService;
    }

    // -------------------------------------------------------------------------
    // OrgUnitDistributionService implementation
    // -------------------------------------------------------------------------

    public JFreeChart getOrganisationUnitDistributionChart( OrganisationUnitGroupSet groupSet, OrganisationUnit organisationUnit )
    {
        Map<String, Double> categoryValues = new HashMap<String, Double>();
                
        Grid grid = getOrganisationUnitDistribution( groupSet, organisationUnit, true );
        
        Assert.isTrue( grid != null && grid.getHeight() == 1 );
        
        for ( int i = 1; i < grid.getWidth(); i++ ) // Skip name column
        {
            categoryValues.put( grid.getHeaders().get( i ).getName(), Double.valueOf( grid.getRow( 0 ).get( i ) ) );
        }
        
        String title = groupSet.getName() + TITLE_SEP + organisationUnit.getName();
        
        JFreeChart chart = chartService.getJFreeChart( title, PlotOrientation.VERTICAL, CategoryLabelPositions.DOWN_45, categoryValues );
        
        return chart;
    }
    
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Grid getOrganisationUnitDistribution( OrganisationUnitGroupSet groupSet, OrganisationUnit organisationUnit, boolean organisationUnitOnly  )
    {
        Grid grid = new ListGrid();
        grid.setTitle( groupSet.getName() + TITLE_SEP + organisationUnit.getName() );
        
        List<OrganisationUnit> units = organisationUnitOnly ? Arrays.asList( organisationUnit ) : new ArrayList<OrganisationUnit>( organisationUnit.getChildren() );
        List<OrganisationUnitGroup> groups = new ArrayList<OrganisationUnitGroup>( groupSet.getOrganisationUnitGroups() );
        
        Collections.sort( units, ORGUNIT_COMPARATOR );
        Collections.sort( groups, ORGUNIT_GROUP_COMPARATOR );
        
        grid.addHeader( new GridHeader( FIRST_COLUMN_TEXT, FIRST_COLUMN_TEXT, null, false, true ) );
        
        for ( OrganisationUnitGroup group : groups )
        {
            grid.addHeader( new GridHeader( group.getName(), false, false )  );
        }
        
        for ( OrganisationUnit unit : units )
        {            
            grid.nextRow();
            grid.addValue( unit.getName() );
            
            Collection<OrganisationUnit> subTree = organisationUnitService.getOrganisationUnitWithChildren( unit.getId() ); 
            
            for ( OrganisationUnitGroup group : groups )
            {
                Collection<OrganisationUnit> result = CollectionUtils.intersection( subTree, group.getMembers() );
                
                grid.addValue( result != null ? String.valueOf( result.size() ) : String.valueOf( 0 ) );
            }
        }
        
        return grid;
    }
}
