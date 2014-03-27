package org.hisp.dhis.acl;

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

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dashboard.Dashboard;
import org.hisp.dhis.schema.AuthorityType;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupAccess;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static org.springframework.util.CollectionUtils.containsAny;

/**
 * Default ACL implementation that uses SchemaDescriptors to get authorities / sharing flags.
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultAclService implements AclService
{
    @Autowired
    private SchemaService schemaService;

    @Override
    public boolean isSupported( String type )
    {
        Schema schema = schemaService.getSchemaBySingularName( type );
        return schema != null && schema.isShareable();
    }

    @Override
    public boolean isSupported( Class<?> klass )
    {
        Schema schema = schemaService.getSchema( klass );
        return schema != null && schema.isShareable();
    }

    @Override
    public boolean isShareable( String type )
    {
        Schema schema = schemaService.getSchemaBySingularName( type );
        return schema != null && schema.isShareable();
    }

    @Override
    public boolean isShareable( Class<?> klass )
    {
        Schema schema = schemaService.getSchema( klass );
        return schema != null && schema.isShareable();
    }

    @Override
    public boolean canWrite( User user, IdentifiableObject object )
    {
        Schema schema = schemaService.getSchema( object.getClass() );

        if ( schema == null || !schema.isShareable() )
        {
            return false;
        }

        //TODO ( (object instanceof User) && canCreatePrivate( user, object ) ): review possible security breaches and best way to give update access upon user import
        if ( haveOverrideAuthority( user )
            || (object.getUser() == null && canCreatePublic( user, object.getClass() ) && !schema.getAuthorityByType( AuthorityType.CREATE_PRIVATE ).isEmpty())
            || (user != null && user.equals( object.getUser() ))
            //|| authorities.contains( PRIVATE_AUTHORITIES.get( object.getClass() ) )
            || ((object instanceof User) && canCreatePrivate( user, object.getClass() ))
            || AccessStringHelper.canWrite( object.getPublicAccess() ) )
        {
            return true;
        }

        for ( UserGroupAccess userGroupAccess : object.getUserGroupAccesses() )
        {
            /* Is the user allowed to write to this object through group access? */
            if ( AccessStringHelper.canWrite( userGroupAccess.getAccess() )
                && userGroupAccess.getUserGroup().getMembers().contains( user ) )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canRead( User user, IdentifiableObject object )
    {
        Schema schema = schemaService.getSchema( object.getClass() );

        if ( schema == null || !schema.isShareable() )
        {
            return false;
        }

        if ( haveOverrideAuthority( user )
            || UserGroup.class.isAssignableFrom( object.getClass() )
            || object.getUser() == null
            || user.equals( object.getUser() )
            || AccessStringHelper.canRead( object.getPublicAccess() ) )
        {
            return true;
        }

        for ( UserGroupAccess userGroupAccess : object.getUserGroupAccesses() )
        {
            /* Is the user allowed to read this object through group access? */
            if ( AccessStringHelper.canRead( userGroupAccess.getAccess() )
                && userGroupAccess.getUserGroup().getMembers().contains( user ) )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canUpdate( User user, IdentifiableObject object )
    {
        Schema schema = schemaService.getSchema( object.getClass() );

        if ( schema == null )
        {
            return false;
        }

        if ( schema.isShareable() )
        {
            return canAccess( user, schema.getAuthorityByType( AuthorityType.UPDATE ) ) && canWrite( user, object );
        }

        return canAccess( user, schema.getAuthorityByType( AuthorityType.UPDATE ) );
    }

    @Override
    public boolean canDelete( User user, IdentifiableObject object )
    {
        Schema schema = schemaService.getSchema( object.getClass() );

        if ( schema == null )
        {
            return false;
        }

        if ( schema.isShareable() )
        {
            return canAccess( user, schema.getAuthorityByType( AuthorityType.DELETE ) ) && canWrite( user, object );
        }

        return canAccess( user, schema.getAuthorityByType( AuthorityType.DELETE ) );
    }

    @Override
    public boolean canManage( User user, IdentifiableObject object )
    {
        Schema schema = schemaService.getSchema( object.getClass() );

        if ( schema == null || !schema.isShareable() )
        {
            return false;
        }

        if ( haveOverrideAuthority( user )
            || user.equals( object.getUser() )
            || (object.getUser() == null && canCreatePublic( user, object.getClass() ) && !schema.getAuthorityByType( AuthorityType.CREATE_PRIVATE ).isEmpty())
            || AccessStringHelper.canWrite( object.getPublicAccess() ) )
        {
            return true;
        }

        for ( UserGroupAccess userGroupAccess : object.getUserGroupAccesses() )
        {
            /* Is the user allowed to write to this object through group access? */
            if ( AccessStringHelper.canWrite( userGroupAccess.getAccess() )
                && userGroupAccess.getUserGroup().getMembers().contains( user ) )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T extends IdentifiableObject> boolean canCreatePublic( User user, Class<T> klass )
    {
        Schema schema = schemaService.getSchema( klass );
        return !(schema == null || !schema.isShareable()) && canAccess( user, schema.getAuthorityByType( AuthorityType.CREATE_PUBLIC ) );
    }

    @Override
    public <T extends IdentifiableObject> boolean canCreatePrivate( User user, Class<T> klass )
    {
        Schema schema = schemaService.getSchema( klass );
        return !(schema == null || !schema.isShareable()) && canAccess( user, schema.getAuthorityByType( AuthorityType.CREATE_PRIVATE ) );
    }

    @Override
    public <T extends IdentifiableObject> boolean canExternalize( User user, Class<T> klass )
    {
        Schema schema = schemaService.getSchema( klass );
        return !(schema == null || !schema.isShareable()) && canAccess( user, schema.getAuthorityByType( AuthorityType.EXTERNALIZE ) );
    }

    @Override
    public <T extends IdentifiableObject> boolean defaultPublic( Class<T> klass )
    {
        // TODO this is quite nasty, should probably be added to schema
        return !Dashboard.class.isAssignableFrom( klass );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Class<? extends IdentifiableObject> classForType( String type )
    {
        Schema schema = schemaService.getSchemaBySingularName( type );

        if ( schema != null && schema.isIdentifiableObject() )
        {
            return (Class<? extends IdentifiableObject>) schema.getKlass();
        }

        return null;
    }

    private boolean haveOverrideAuthority( User user )
    {
        return user == null || containsAny( user.getUserCredentials().getAllAuthorities(), ACL_OVERRIDE_AUTHORITIES );
    }

    private boolean canAccess( User user, Collection<String> requiredAuthorities )
    {
        return haveOverrideAuthority( user ) || requiredAuthorities.isEmpty() || containsAny( user.getUserCredentials().getAllAuthorities(), requiredAuthorities );
    }
}
