package org.hisp.dhis.common;

/*
 * Copyright (c) 2004-2010, University of Oslo
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

import java.util.List;

/**
 * @author Lars Helge Overland
 */
public interface Grid
{
    /**
     * Returns the grid title.
     */
    String getTitle();
    
    /**
     * Sets the grid title.
     */
    Grid setTitle( String title );
    
    /**
     * Returns the grid subtitle.
     */
    String getSubtitle();
    
    /**
     * Sets the grid subtitle.
     */
    Grid setSubtitle( String subtitle );

    /**
     * Returns all header values.
     */
    List<GridHeader> getHeaders();
    
    /**
     * Returns all visible headers, ie. headers which are not hidden.
     */
    List<GridHeader> getVisibleHeaders();
    
    /**
     * Adds a header value.
     */
    Grid addHeader( GridHeader header );
        
    /**
     * Returns the current height / number of rows in the grid.
     */
    int getHeight();

    /**
     * Returns the current width / number of columns in the grid.
     */
    int getWidth();
    
    /**
     * Returns the current width / number of visible columns in the grid.
     */
    int getVisibleWidth();

    /**
     * Adds a new row the the grid and moves the cursor accordingly.
     */
    Grid nextRow();

    /**
     * Adds the value to the end of the current row.
     * 
     * @param value the value to add.
     */
    Grid addValue( String value );

    /**
     * Returns the row with the given index.
     * 
     * @param rowIndex the index of the row.
     */
    List<String> getRow( int rowIndex );

    /**
     * Returns all rows.
     */
    List<List<String>> getRows();
    
    /**
     * Returns all visible rows, ie. rows with a corresponding header that is
     * not hidden.
     */
    List<List<String>> getVisibleRows();

    /**
     * Returns the column with the given index.
     * 
     * @param columnIndex the index of the column.
     */
    List<String> getColumn( int columnIndex );
    
    /**
     * Return the value at the given row index and the given column index.
     * 
     * @param rowIndex the row index.
     * @param columnIndex the column index.
     * @return the column value.
     * @throws IllegalArgumentException if the grid does not contain the requested row / column.
     */
    String getValue( int rowIndex, int columnIndex );

    /**
     * Adds a new column at the end of the grid.
     * 
     * @param columnValues the column values to add.
     * @throws IllegalStateException if the columnValues has different length
     *         than the rows in grid, or if the grid rows are not of the same length.
     */
    Grid addColumn( List<String> columnValues );
    
    /**
     * Removes the header and column at the given index.
     */
    Grid removeColumn( int columnIndex );
    
    /**
     * Removes the header and the column at the index of the given header if it
     * exists.
     */
    Grid removeColumn( GridHeader header );
    
    /**
     * Adds a regression column to the grid. Column must hold numeric data.
     * 
     * @param columnIndex the index of the base column.
     */
    Grid addRegressionColumn( int columnIndex );
}
