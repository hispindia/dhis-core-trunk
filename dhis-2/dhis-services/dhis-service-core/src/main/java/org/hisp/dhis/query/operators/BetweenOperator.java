package org.hisp.dhis.query.operators;

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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.query.Typed;

import java.util.Collection;
import java.util.Date;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class BetweenOperator extends Operator
{
    public BetweenOperator( String arg0, String arg1 )
    {
        super( Typed.from( String.class, Number.class, Date.class ), arg0, arg1 );
    }

    @Override
    public Criterion getHibernateCriterion( String propertyName )
    {
        return Restrictions.between( propertyName, args.get( 0 ), args.get( 1 ) );
    }

    @Override
    public boolean test( Object value )
    {
        if ( args.isEmpty() || value == null )
        {
            return false;
        }

        if ( Integer.class.isInstance( value ) )
        {
            Integer s1 = getValue( Integer.class, value );
            Integer min = getValue( Integer.class, 0 );
            Integer max = getValue( Integer.class, 1 );

            return s1 >= min && s1 <= max;
        }
        else if ( Float.class.isInstance( value ) )
        {
            Float s1 = getValue( Float.class, value );
            Integer min = getValue( Integer.class, 0 );
            Integer max = getValue( Integer.class, 1 );

            return s1 >= min && s1 <= max;
        }
        else if ( Collection.class.isInstance( value ) )
        {
            Collection<?> collection = (Collection<?>) value;
            Integer min = getValue( Integer.class, 0 );
            Integer max = getValue( Integer.class, 1 );

            return collection.size() >= min && collection.size() <= max;
        }

        return false;
    }
}
