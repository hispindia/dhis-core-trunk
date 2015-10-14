package org.hisp.dhis.user;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.hisp.dhis.common.DimensionalObject;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author Torgeir Lorange Ostby
 */
@Transactional
public class DefaultUserSettingService
    implements UserSettingService
{
    /**
     * Cache for user settings. Does not accept nulls. Key is name + username.
     */
    private static Cache<String, Optional<Serializable>> SETTING_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess( 15, TimeUnit.MINUTES )
        .initialCapacity( 200 )
        .maximumSize( 10000 )
        .build();
    
    private String getCacheKey( String settingName, String username )
    {
        return settingName + DimensionalObject.ITEM_SEP + username;
    }
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private UserSettingStore userSettingStore;

    public void setUserSettingStore( UserSettingStore userSettingStore )
    {
        this.userSettingStore = userSettingStore;
    }

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // UserSettingService implementation
    // -------------------------------------------------------------------------

    @Override
    public void addUserSetting( UserSetting userSetting )
    {
        SETTING_CACHE.invalidate( getCacheKey( userSetting.getName(), userSetting.getUser().getUsername() ) );
        
        userSettingStore.addUserSetting( userSetting );
    }

    @Override
    public void saveUserSetting( String name, Serializable value, String username )
    {
        UserCredentials credentials = userService.getUserCredentialsByUsername( username );
        
        if ( credentials != null )
        {        
            saveUserSetting( name, value, credentials.getUserInfo() );
        }
    }

    @Override
    public void saveUserSetting( String name, Serializable value )
    {
        User currentUser = currentUserService.getCurrentUser();
        
        saveUserSetting( name, value, currentUser );
    }

    @Override
    public void saveUserSetting( String name, Serializable value, User user )
    {
        if ( user == null )
        {
            return;
        }

        SETTING_CACHE.invalidate( getCacheKey( name, user.getUsername() ) );
        
        UserSetting userSetting = userSettingStore.getUserSetting( user, name );

        if ( userSetting == null )
        {
            userSetting = new UserSetting();
            userSetting.setUser( user );
            userSetting.setName( name );
            userSetting.setValue( value );

            addUserSetting( userSetting );
        }
        else
        {
            userSetting.setValue( value );

            userSettingStore.updateUserSetting( userSetting );
        }
    }

    @Override
    public void deleteUserSetting( UserSetting userSetting )
    {
        SETTING_CACHE.invalidate( getCacheKey( userSetting.getName(), userSetting.getUser().getUsername() ) );
        
        userSettingStore.deleteUserSetting( userSetting );
    }
    
    @Override
    public void deleteUserSetting( String name )
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser != null )
        {
            UserSetting setting = userSettingStore.getUserSetting( currentUser, name );
            
            if ( setting != null )
            {
                deleteUserSetting( setting );
            }
        }
    }
    
    @Override
    public void deleteUserSetting( String name, User user )
    {
        UserSetting setting = userSettingStore.getUserSetting( user, name );
        
        if ( setting != null )
        {
            deleteUserSetting( setting );
        }
    }
    
    @Override
    public Serializable getUserSetting( String name, Serializable defaultValue )
    {
        User user = currentUserService.getCurrentUser();

        if ( user == null )
        {
            return defaultValue;
        }

        Optional<Serializable> userSetting = getUserSetting( name, user );

        return userSetting.orElse( defaultValue );
    }

    @Override
    public Serializable getUserSetting( String name )
    {
        User user = currentUserService.getCurrentUser();
        
        if ( user == null )
        {
            return null;
        }

        Optional<Serializable> setting = getUserSetting( name, user );
        
        return setting.orElse( null );
    }

    @Override
    public Serializable getUserSetting( String name, Serializable defaultValue, User user )
    {
        Optional<Serializable> setting = getUserSetting( name, user );

        return setting.orElse( defaultValue );
    }

    private Optional<Serializable> getUserSetting( String name, User user )
    {
        try
        {
            String cacheKey = getCacheKey( name, user.getUsername() );
            
            Optional<Serializable> setting = SETTING_CACHE.get( cacheKey, () -> getUserSettingOptional( user, name ) );
            
            return setting;
        }
        catch ( ExecutionException ignored )
        {
            return Optional.empty();
        }
    }

    private Optional<Serializable> getUserSettingOptional( User user, String name )
    {
        UserSetting setting = userSettingStore.getUserSetting( user, name );
        
        return setting != null && setting.getValue() != null ? Optional.of( setting.getValue() ) : Optional.empty();
    }
    
    @Override
    public List<UserSetting> getAllUserSettings()
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser == null )
        {
            return new ArrayList<>();
        }

        return getAllUserSettings( currentUser );
    }
    
    @Override
    public List<UserSetting> getAllUserSettings( User user )
    {
        return userSettingStore.getAllUserSettings( user );
    }

    @Override
    public void invalidateCache()
    {
        SETTING_CACHE.invalidateAll();
    }
}
