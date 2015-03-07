package org.hisp.dhis.sqlview;

/*
 * Copyright (c) 2004-2015, University of Oslo
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.system.grid.ListGrid;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Dang Duy Hieu
 */
@Transactional
public class DefaultSqlViewService
    implements SqlViewService
{
    private static final Log log = LogFactory.getLog( DefaultSqlViewService.class );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SqlViewStore sqlViewStore;

    public void setSqlViewStore( SqlViewStore sqlViewStore )
    {
        this.sqlViewStore = sqlViewStore;
    }

    // -------------------------------------------------------------------------
    // Implement methods
    // -------------------------------------------------------------------------

    @Override
    public void deleteSqlView( SqlView sqlViewObject )
    {
        dropViewTable( sqlViewObject.getViewName() );
        
        sqlViewStore.delete( sqlViewObject );
    }

    @Override
    public Collection<SqlView> getAllSqlViews()
    {
        return sqlViewStore.getAll();
    }

    @Override
    public Collection<SqlView> getAllSqlViewsNoAcl()
    {
        return sqlViewStore.getAllNoAcl();
    }

    @Override
    public SqlView getSqlView( int viewId )
    {
        return sqlViewStore.get( viewId );
    }

    @Override
    public SqlView getSqlViewByUid( String uid )
    {
        return sqlViewStore.getByUid( uid );
    }

    @Override
    public SqlView getSqlView( String viewName )
    {
        return sqlViewStore.getByName( viewName );
    }

    @Override
    public int saveSqlView( SqlView sqlViewObject )
    {
        return sqlViewStore.save( sqlViewObject );
    }

    @Override
    public void updateSqlView( SqlView sqlViewObject )
    {
        sqlViewStore.update( sqlViewObject );
    }

    @Override
    public int getSqlViewCount()
    {
        return sqlViewStore.getCount();
    }

    @Override
    public Collection<SqlView> getSqlViewsBetween( int first, int max )
    {
        return sqlViewStore.getAllOrderedName( first, max );
    }

    @Override
    public Collection<SqlView> getSqlViewsBetweenByName( String name, int first, int max )
    {
        return sqlViewStore.getAllLikeName( name, first, max );
    }

    @Override
    public int getSqlViewCountByName( String name )
    {
        return sqlViewStore.getCountLikeName( name );
    }
    
    // -------------------------------------------------------------------------
    // SqlView expanded
    // -------------------------------------------------------------------------

    @Override
    public boolean viewTableExists( String viewTableName )
    {
        return sqlViewStore.viewTableExists( viewTableName );
    }

    @Override
    public String createViewTable( SqlView sqlView )
    {
        return sqlViewStore.createViewTable( sqlView );
    }
    
    @Override
    public Grid getSqlViewGrid( SqlView sqlView, Map<String, String> criteria, Map<String, String> variables )
    {
        Grid grid = new ListGrid();
        grid.setTitle( sqlView.getName() );

        validateSqlView( sqlView, criteria, variables );
        
        if ( sqlView.isQuery() )
        {
            final String sql = substituteSql( sqlView.getSqlQuery(), variables );
            
            sqlViewStore.executeQuery( grid, sql );
        }
        else
        {
            sqlViewStore.setUpDataSqlViewTable( grid, sqlView.getViewName(), criteria );
        }
        
        return grid;
    }
    
    @Override
    public String substituteSql( String sql, Map<String, String> variables )
    {
        String sqlQuery = sql;
     
        if ( variables != null )
        {
            for ( String key : variables.keySet() )
            {
                if ( key != null && StringUtils.isAlphanumericSpace( key ) )
                {
                    final String regex = "\\$\\{(" + key + ")\\}";
                    final String var = variables.get( key );
                    
                    if ( var != null && StringUtils.isAlphanumericSpace( var ) )
                    {
                        sqlQuery = sqlQuery.replaceAll( regex, var );
                    }
                }
            }
        }
        
        return sqlQuery;
    }

    @Override
    public Set<String> getVariables( String sql )
    {
        Set<String> variables = new HashSet<>();
        
        Matcher matcher = VARIABLE_PATTERN.matcher( sql );
        
        while ( matcher.find() )
        {
            variables.add( matcher.group( 1 ) );
        }
        
        return variables;
    }

    @Override
    public void validateSqlView( SqlView sqlView, Map<String, String> criteria, Map<String, String> variables )
        throws IllegalQueryException
    {
        String violation = null;
        
        if ( sqlView == null || sqlView.getSqlQuery() == null )
        {
            throw new IllegalQueryException( "SQL query is null" );
        }
        
        final Set<String> sqlVars = getVariables( sqlView.getSqlQuery() );
        final String sql = sqlView.getSqlQuery();
        
        if ( !SELECT_PATTERN.matcher( sqlView.getSqlQuery() ).matches() )
        {
            violation = "SQL query must be a select query";
        }
        
        if ( sql.contains( ";" ) && !sql.trim().endsWith( ";" ) )
        {
            violation = "SQL query can only contain a single semi-colon at the end of the query";
        }
        
        if ( variables != null && variables.keySet().contains( null ) )
        {
            violation = "Variables contains null key";
        }

        if ( variables != null && variables.values().contains( null ) )
        {
            violation = "Variables contains null value";
        }
        
        if ( sqlView.isQuery() && !sqlVars.isEmpty() && ( variables == null || !variables.keySet().containsAll( sqlVars ) ) )
        {
            violation = "SQL query contains variables which were not supplied in request: " + sqlVars;
        }
        
        if ( sql.matches( SqlView.getProtectedTablesRegex() ) )
        {
            violation = "SQL query contains references to protected tables";
        }
        
        if ( sql.matches( SqlView.getIllegalKeywordsRegex() ) )
        {
            violation = "SQL query contains illegal keywords";
        }
        
        if ( violation != null )
        {
            log.warn( "Validation failed: " + violation );
            
            throw new IllegalQueryException( violation );
        }
    }

    @Override
    public String testSqlGrammar( String sql )
    {
        return sqlViewStore.testSqlGrammar( sql );
    }

    @Override
    public void dropViewTable( String sqlViewTableName )
    {
        sqlViewStore.dropViewTable( sqlViewTableName );
    }
}