package org.hisp.dhis.dataelement;

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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Abyot Asalefew
 * @version $Id$
 */
public class DefaultDataElementCategoryComboService
    implements DataElementCategoryComboService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementCategoryComboStore dataElementCategoryComboStore;

    public void setDataElementCategoryComboStore( DataElementCategoryComboStore dataElementCategoryComboStore )
    {
        this.dataElementCategoryComboStore = dataElementCategoryComboStore;
    }

    private DataElementDimensionRowOrderService dataElementDimensionRowOrderService;

    public void setDataElementDimensionRowOrderService(
        DataElementDimensionRowOrderService dataElementDimensionRowOrderService )
    {
        this.dataElementDimensionRowOrderService = dataElementDimensionRowOrderService;
    }

    // -------------------------------------------------------------------------
    // DataElementCategoryCombo
    // -------------------------------------------------------------------------

    public int addDataElementCategoryCombo( DataElementCategoryCombo dataElementCategoryCombo )
    {
        return dataElementCategoryComboStore.addDataElementCategoryCombo( dataElementCategoryCombo );
    }

    public void deleteDataElementCategoryCombo( DataElementCategoryCombo dataElementCategoryCombo )
    {
        dataElementCategoryComboStore.deleteDataElementCategoryCombo( dataElementCategoryCombo );
    }

    public Collection<DataElementCategoryCombo> getAllDataElementCategoryCombos()
    {
        return dataElementCategoryComboStore.getAllDataElementCategoryCombos();
    }

    public DataElementCategoryCombo getDataElementCategoryCombo( int id )
    {
        return dataElementCategoryComboStore.getDataElementCategoryCombo( id );
    }

    public DataElementCategoryCombo getDataElementCategoryComboByName( String name )
    {
        return dataElementCategoryComboStore.getDataElementCategoryComboByName( name );
    }

    public void updateDataElementCategoryCombo( DataElementCategoryCombo dataElementCategoryCombo )
    {
        dataElementCategoryComboStore.updateDataElementCategoryCombo( dataElementCategoryCombo );
    }

    public Collection<DataElementCategory> getOrderCategories( DataElementCategoryCombo dataElementCategoryCombo )
    {
        Map<Integer, DataElementCategory> categoryMap = new TreeMap<Integer, DataElementCategory>();

        for ( DataElementCategory category : dataElementCategoryCombo.getCategories() )
        {
            DataElementDimensionRowOrder rowOrder = dataElementDimensionRowOrderService
                .getDataElementDimensionRowOrder( dataElementCategoryCombo, category );

            if ( rowOrder != null )
            {
                categoryMap.put( rowOrder.getDisplayOrder(), category );
            }
            else
            {
                categoryMap.put( category.getId(), category );
            }
        }

        return categoryMap.values();
    }
}
