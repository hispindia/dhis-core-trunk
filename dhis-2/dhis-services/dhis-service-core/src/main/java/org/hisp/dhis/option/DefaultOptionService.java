package org.hisp.dhis.option;

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

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.i18n.I18nService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 */
@Transactional
public class DefaultOptionService
    implements OptionService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OptionStore optionStore;

    public void setOptionStore( OptionStore optionStore )
    {
        this.optionStore = optionStore;
    }

    private GenericIdentifiableObjectStore<Option> optionValueStore;

    public void setOptionValueStore( GenericIdentifiableObjectStore<Option> optionValueStore )
    {
        this.optionValueStore = optionValueStore;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    // -------------------------------------------------------------------------
    // Implementation methods -
    // -------------------------------------------------------------------------

    public int saveOptionSet( OptionSet optionSet )
    {
        return optionStore.save( optionSet );
    }

    public void updateOptionSet( OptionSet optionSet )
    {
        optionStore.update( optionSet );
    }

    public OptionSet getOptionSet( int id )
    {
        return i18n( i18nService, optionStore.get( id ) );
    }

    public OptionSet getOptionSet( String uid )
    {
        return i18n( i18nService, optionStore.getByUid( uid ) );
    }

    public OptionSet getOptionSetByName( String name )
    {
        return i18n( i18nService, optionStore.getByName( name ) );
    }

    public void deleteOptionSet( OptionSet optionSet )
    {
        optionStore.delete( optionSet );
    }

    public Collection<OptionSet> getAllOptionSets()
    {
        return i18n( i18nService, optionStore.getAll() );
    }

    public List<Option> getOptions( String optionSetUid, String key, Integer max )
    {
        OptionSet optionSet = getOptionSet( optionSetUid );

        return getOptions( optionSet.getId(), key, max );
    }

    public List<Option> getOptions( int optionSetId, String key, Integer max )
    {
        List<Option> options = null;

        if ( key != null || max != null )
        {
            // Use query as option set size might be very high

            options = optionStore.getOptions( optionSetId, key, max );
        }
        else
        {
            // Return all from object association to preserve custom order

            OptionSet optionSet = getOptionSet( optionSetId );

            options = new ArrayList<Option>( optionSet.getOptions() );
        }

        return options;
    }

    public Integer getOptionSetsCountByName( String name )
    {
        return optionStore.getCountLikeName( name );
    }

    public Collection<OptionSet> getOptionSetsBetweenByName( String name, int first, int max )
    {
        return new HashSet<OptionSet>( i18n( i18nService, optionStore.getAllLikeNameOrderedName( name, first, max ) ) );
    }

    public Collection<OptionSet> getOptionSetsBetween( int first, int max )
    {
        return new HashSet<OptionSet>( i18n( i18nService, optionStore.getAllOrderedName( first, max ) ) );
    }

    public Integer getOptionSetCount()
    {
        return optionStore.getCount();
    }

    // -------------------------------------------------------------------------
    // Option
    // -------------------------------------------------------------------------

    public void updateOption( Option option )
    {
        optionValueStore.update( option ); 
    }
    
    public Option getOption( int id )
    {
        return i18n( i18nService, optionValueStore.get( id ) );
    }

    public Option getOptionByCode( String code )
    {
        return i18n( i18nService, optionValueStore.getByCode( code ) );
    }

    public Option getOptionValueByName( OptionSet optionSet, String name )
    {
        return i18n( i18nService, optionStore.getOptionValueByName( optionSet, name ) );
    }
}
