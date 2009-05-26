package org.hisp.dhis.webwork.interceptor;

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

import static org.hisp.dhis.options.SystemSettingManager.KEY_APPLICATION_TITLE;
import static org.hisp.dhis.options.SystemSettingManager.KEY_FLAG;
import static org.hisp.dhis.options.SystemSettingManager.KEY_FORUM_INTEGRATION;
import static org.hisp.dhis.options.SystemSettingManager.KEY_START_MODULE;

import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.options.SystemSettingManager;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class WebWorkSystemSettingInterceptor
    implements Interceptor
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }
    
    // -------------------------------------------------------------------------
    // AroundInterceptor implementation
    // -------------------------------------------------------------------------

    public void destroy()
    {
        // TODO Auto-generated method stub
        
    }

    public void init()
    {
        // TODO Auto-generated method stub
        
    }

    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>( 2 );
        
        String applicationTitle = (String) systemSettingManager.getSystemSetting( KEY_APPLICATION_TITLE );
        String flag = (String) systemSettingManager.getSystemSetting( KEY_FLAG );
        Boolean forumIntegration = (Boolean) systemSettingManager.getSystemSetting( KEY_FORUM_INTEGRATION );
        String startModule = (String) systemSettingManager.getSystemSetting( KEY_START_MODULE );
        
        map.put( KEY_APPLICATION_TITLE, applicationTitle );
        map.put( KEY_FLAG, flag );
        map.put( KEY_FORUM_INTEGRATION, forumIntegration );
        map.put( KEY_START_MODULE, startModule );
        
        invocation.getStack().push( map );
        
        return invocation.invoke();
    }
}
