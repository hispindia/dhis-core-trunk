package org.hisp.dhis.common;

/*
 * Copyright (c) 2004-2014, University of Oslo
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lars Helge Overland
 */
public class NameableObjectUtils
{
    /**
     * Returns a list of NameableObjects.
     *
     * @param objects the NameableObjects to include in the list.
     * @return a list of NameableObjects.
     */
    public static List<NameableObject> getList( NameableObject... objects )
    {
        List<NameableObject> list = new ArrayList<NameableObject>();

        if ( objects != null )
        {
            Collections.addAll( list, objects );
        }

        return list;
    }

    /**
     * Returns a list with erasure NameableObject based on the given collection.
     *
     * @param collection the collection.
     * @return a list of NameableObjects.
     */
    public static List<NameableObject> asList( Collection<? extends NameableObject> collection )
    {
        List<NameableObject> list = new ArrayList<NameableObject>();
        list.addAll( collection );
        return list;
    }

    /**
     * Returns a list typed with the desired erasure based on the given collection.
     * This operation implies an unchecked cast and it is the responsibility of
     * the caller to make sure the cast is valid.
     *
     * @param collection the collection.
     * @return a list.
     */
    @SuppressWarnings("unchecked")
    public static <T extends NameableObject> List<T> asTypedList( Collection<NameableObject> collection )
    {
        List<T> list = new ArrayList<T>();

        if ( collection != null )
        {
            for ( NameableObject object : collection )
            {
                list.add( (T) object );
            }
        }

        return list;
    }

    /**
     * Returns a list typed with the desired erasure based on the given collection.
     * This operation implies an unchecked cast and it is the responsibility of
     * the caller to make sure the cast is valid.
     *
     * @param collection the collection.
     * @param the        class type.
     * @return a list.
     */
    public static <T extends NameableObject> List<T> asTypedList( Collection<NameableObject> collection, Class<T> clazz )
    {
        return asTypedList( collection );
    }
    
    /**
     * Returns a list of BaseNameableObjects based on the given list of values,
     * where the name, code and short name of each BaseNameableObject is set to
     * the value of each list item.
     * 
     * @param values the list of object values.
     * @return a list of BaseNameableObejcts.
     */
    public static List<NameableObject> getNameableObjects( Collection<Object> values )
    {
        List<NameableObject> objects = new ArrayList<>();
        
        for ( Object value : values )
        {
            if ( value != null )
            {
                String val = String.valueOf( value );
                
                BaseNameableObject nameableObject = new BaseNameableObject( val, val, val );
                nameableObject.setShortName( val );
                objects.add( nameableObject );
            }
        }
        
        return objects;
    }

    /**
     * Returns a mapping between the uid and the nameable objects. The order of
     * the objects are preserved.
     *
     * @param objects the nameable objects.
     * @return ordered mapping between the uid and the nameable objects.
     */
    public static Map<String, NameableObject> getUidObjectMap( List<? extends NameableObject> objects )
    {
        Map<String, NameableObject> map = new LinkedHashMap<String, NameableObject>();

        if ( objects != null )
        {
            for ( NameableObject object : objects )
            {
                map.put( object.getUid(), object );
            }
        }

        return map;
    }
}
