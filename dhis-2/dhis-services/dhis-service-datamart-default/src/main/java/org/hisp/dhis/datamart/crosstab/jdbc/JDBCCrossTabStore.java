package org.hisp.dhis.datamart.crosstab.jdbc;

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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.dataelement.Operand;
import org.hisp.dhis.jdbc.StatementHolder;
import org.hisp.dhis.jdbc.StatementManager;

/**
 * @author Lars Helge Overland
 * @version $Id: JDBCCrossTabStore.java 6216 2008-11-06 18:06:42Z eivindwa $
 */
public class JDBCCrossTabStore
    implements CrossTabStore
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private StatementManager statementManager;
    
    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }
    
    // -------------------------------------------------------------------------
    // CrossTabStore implementation
    // -------------------------------------------------------------------------

    public void createCrossTabTable( final List<Operand> operands )
    {
        final StatementHolder holder = statementManager.getHolder();
        
        try
        {
            final StringBuffer sql = new StringBuffer( "CREATE TABLE datavaluecrosstab ( " );
            
            sql.append( "periodid INTEGER NOT NULL, " );
            sql.append( "sourceid INTEGER NOT NULL, " );
            
            for ( Operand operand : operands )
            {
                sql.append( COLUMN_PREFIX ).append( operand.getDataElementId() ).append(
                    SEPARATOR ).append( operand.getOptionComboId() ).append( " VARCHAR(30), " );
            }
            
            sql.append( "PRIMARY KEY ( periodid, sourceid ) );" );
            
            holder.getStatement().executeUpdate( sql.toString() );
        }
        catch ( SQLException ex )
        {
            throw new RuntimeException( "Failed to create datavalue crosstab table", ex );
        }
        finally
        {
            holder.close();
        }
    }
    
    public void dropCrossTabTable()
    {
        final StatementHolder holder = statementManager.getHolder();
        
        try
        {
            final String sql = "DROP TABLE IF EXISTS datavaluecrosstab";
            
            holder.getStatement().executeUpdate( sql );
        }
        catch ( SQLException ex )
        {
            throw new RuntimeException( "Failed to drop datavalue crosstab table", ex );
        }
        finally
        {
            holder.close();
        }
    }
    
    public Map<Integer, String> getCrossTabTableColumns()
    {
        final StatementHolder holder = statementManager.getHolder();
        
        try
        {
            final String sql = "SELECT * FROM datavaluecrosstab LIMIT 0";
            
            final ResultSetMetaData metaData = holder.getStatement().executeQuery( sql ).getMetaData();
            
            final Map<Integer, String> indexColumnNameMap = new HashMap<Integer, String>();
            
            for ( int i = 0; i < metaData.getColumnCount(); i++ )
            {
                final int index = i + 1;
                
                final String columnName = metaData.getColumnName( index ).toLowerCase();
                
                indexColumnNameMap.put( index, columnName );
            }
            
            return indexColumnNameMap;
        }
        catch ( SQLException ex )
        {
            throw new RuntimeException( "Failed to get crosstab table columns", ex );
        }
        finally
        {
            holder.close();
        }
    }
        
    public void dropCrossTabColumn( final String columnName )
    {
        final StatementHolder holder = statementManager.getHolder();
        
        try
        {
            final String sql = "ALTER TABLE datavaluecrosstab DROP COLUMN " + columnName;
            
            holder.getStatement().executeUpdate( sql );
        }
        catch ( SQLException ex )
        {
            throw new RuntimeException( "Failed to drop datavalue crosstab column", ex );
        }
        finally
        {
            holder.close();
        }            
    }
}
