package org.hisp.dhis.analytics.event;

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

import java.util.Set;

import org.hisp.dhis.analytics.SortOrder;
import org.hisp.dhis.common.AnalyticalObject;
import org.hisp.dhis.common.DisplayProperty;
import org.hisp.dhis.common.EventAnalyticalObject;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.i18n.I18nFormat;

/**
 * @author Lars Helge Overland
 */
public interface EventAnalyticsService
{    
    Grid getAggregatedEventData( EventQueryParams params );
    
    Grid getAggregatedEventData( AnalyticalObject object, I18nFormat format );
    
    Grid getEvents( EventQueryParams params );

    /**
     * Used for aggregate query.
     */
    EventQueryParams getFromUrl( String program, String stage, String startDate, String endDate, 
        Set<String> dimension, Set<String> filter, boolean skipMeta, boolean hierarchyMeta, SortOrder sortOrder, 
        Integer limit, boolean uniqueInstances, DisplayProperty displayProperty, I18nFormat format );

    /**
     * Used for event query.
     */
    EventQueryParams getFromUrl( String program, String stage, String startDate, String endDate, Set<String> dimension, Set<String> filter, 
        String ouMode, Set<String> asc, Set<String> desc, boolean skipMeta, boolean hierarchyMeta, boolean coordinatesOnly, 
        DisplayProperty displayProperty, Integer page, Integer pageSize, I18nFormat format );
    
    EventQueryParams getFromAnalyticalObject( EventAnalyticalObject object, I18nFormat format );
}
