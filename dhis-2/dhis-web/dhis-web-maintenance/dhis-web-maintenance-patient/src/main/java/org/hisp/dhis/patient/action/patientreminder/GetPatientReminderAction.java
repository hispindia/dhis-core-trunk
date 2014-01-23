/*
 * Copyright (c) 2004-2013, University of Oslo
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

package org.hisp.dhis.patient.action.patientreminder;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientReminder;
import org.hisp.dhis.patient.PatientReminderService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * 
 * @version $ GetPatientReminderAction.java Jan 6, 2014 9:32:56 AM $
 */
public class GetPatientReminderAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramService programService;

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    private PatientReminderService patientReminderService;

    public void setPatientReminderService( PatientReminderService patientReminderService )
    {
        this.patientReminderService = patientReminderService;
    }

    private UserGroupService userGroupService;

    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }

    // -------------------------------------------------------------------------
    // Input && Output
    // -------------------------------------------------------------------------

    private int id;

    public void setId( int id )
    {
        this.id = id;
    }

    private int programId;

    public void setProgramId( int programId )
    {
        this.programId = programId;
    }

    private Program program;

    public Program getProgram()
    {
        return program;
    }

    private PatientReminder reminder;

    public PatientReminder getReminder()
    {
        return reminder;
    }

    private List<UserGroup> userGroups;

    public List<UserGroup> getUserGroups()
    {
        return userGroups;
    }

    public List<PatientAttribute> attributes;

    public List<PatientAttribute> getAttributes()
    {
        return attributes;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        reminder = patientReminderService.getPatientReminder( id );

        program = programService.getProgram( programId );

        userGroups = new ArrayList<UserGroup>( userGroupService.getAllUserGroups() );

        attributes = new ArrayList<PatientAttribute>( program.getAttributes() );
        
        return SUCCESS;
    }
}
