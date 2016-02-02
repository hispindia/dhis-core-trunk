package org.hisp.dhis.userkeyjsonvalue;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.user.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Stian Sandvold.
 */
public class UserKeyJsonValueStoreTest 
    extends DhisSpringTest
{
    @Autowired
    private UserKeyJsonValueStore userKeyJsonValueStore;

    private User user = userService.getAllUsers().get( 0 );

    @Test
    public void testAddUserKeyJsonValue()
    {
        UserKeyJsonValue userKeyJsonValue = new UserKeyJsonValue();

        userKeyJsonValue.setValue( "{}" );
        userKeyJsonValue.setKey( "test" );
        userKeyJsonValue.setUser( user );

        int id = userKeyJsonValueStore.save( userKeyJsonValue );

        assertNotNull( userKeyJsonValue );
        assertEquals( userKeyJsonValue, userKeyJsonValueStore.get( id ) );
    }

    @Test
    public void testAddUserKeyJsonValuesAndGetKeysByUser()
    {
        UserKeyJsonValue userKeyJsonValueA = new UserKeyJsonValue();

        userKeyJsonValueA.setValue( "{}" );
        userKeyJsonValueA.setKey( "test_a" );
        userKeyJsonValueA.setUser( user );

        UserKeyJsonValue userKeyJsonValueB = new UserKeyJsonValue();

        userKeyJsonValueB.setValue( "{}" );
        userKeyJsonValueB.setKey( "test_b" );
        userKeyJsonValueB.setUser( user );

        List<String> list = userKeyJsonValueStore.getKeysByUser( user );

        assertTrue( list.contains( "test_a" ) );
        assertTrue( list.contains( "test_b" ) );
    }

    @Test
    public void testAddUserKeyJsonValuesAndGetUserKEyJsonValuesByUser()
    {
        UserKeyJsonValue userKeyJsonValueA = new UserKeyJsonValue();

        userKeyJsonValueA.setValue( "{}" );
        userKeyJsonValueA.setKey( "test_a" );
        userKeyJsonValueA.setUser( user );

        UserKeyJsonValue userKeyJsonValueB = new UserKeyJsonValue();

        userKeyJsonValueB.setValue( "{}" );
        userKeyJsonValueB.setKey( "test_b" );
        userKeyJsonValueB.setUser( user );

        List<UserKeyJsonValue> list = userKeyJsonValueStore.getUserKeyJsonValueByUser( user );

        assertTrue( list.contains( userKeyJsonValueA ) );
        assertTrue( list.contains( userKeyJsonValueB ) );
    }
}
