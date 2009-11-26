package org.hisp.dhis.system.objectmapper;

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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.amplecode.quick.mapper.RowMapper;
import org.hisp.dhis.aggregation.AggregatedIndicatorValue;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class AggregatedIndicatorValueRowMapper
    implements RowMapper<AggregatedIndicatorValue>
{
    public AggregatedIndicatorValue mapRow( ResultSet resultSet )
        throws SQLException
    {
        final AggregatedIndicatorValue value = new AggregatedIndicatorValue();
        
        value.setIndicatorId( resultSet.getInt( 1 ) );
        value.setPeriodId( resultSet.getInt( 2 ) );
        value.setOrganisationUnitId( resultSet.getInt( 3 ) );
        value.setPeriodTypeId( resultSet.getInt( resultSet.getInt( 4 ) ) );
        value.setLevel( resultSet.getInt( 5 ) );
        value.setAnnualized( resultSet.getString( 6 ) );
        value.setFactor( resultSet.getDouble( 7 ) );
        value.setValue( resultSet.getDouble( 8 ) );
        value.setNumeratorValue( resultSet.getDouble( 9 ) );
        value.setDenominatorValue( resultSet.getDouble( 10 ) );
        
        return value;
    }
}
