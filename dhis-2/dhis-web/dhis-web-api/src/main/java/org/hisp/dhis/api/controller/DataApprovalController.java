package org.hisp.dhis.api.controller;

/*
 * Copyright (c) 2004-2013, University of Oslo
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

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.hisp.dhis.api.utils.ContextUtils;
import org.hisp.dhis.api.utils.InputUtils;
import org.hisp.dhis.dataapproval.DataApproval;
import org.hisp.dhis.dataapproval.DataApprovalService;
import org.hisp.dhis.dataapproval.DataApprovalState;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Lars Helge Overland
 */
@Controller
@RequestMapping(value = DataApprovalController.RESOURCE_PATH)
public class DataApprovalController
{
    public static final String RESOURCE_PATH = "/dataApprovals";

    @Autowired
    private DataApprovalService dataApprovalService;

    @Autowired
    private DataSetService dataSetService;
    
    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private DataElementCategoryService categoryService;
    
    @Autowired
    private CurrentUserService currentUserService;
    
    @Autowired
    private InputUtils inputUtils;

    @PreAuthorize( "hasRole('ALL') or hasRole('F_APPROVE_DATA')" )
    @RequestMapping( method = RequestMethod.POST, produces = "text/plain" )
    public void saveApproval(
        @RequestParam String ds,
        @RequestParam String pe,
        @RequestParam String ou,
        @RequestParam( required = false ) String cc, 
        @RequestParam( required = false ) String cp, HttpServletResponse response )
    {
        DataSet dataSet = dataSetService.getDataSet( ds );
        
        if ( dataSet == null )
        {
            ContextUtils.conflictResponse( response, "Illegal data set identifier: " + ds );
            return;
        }

        Period period = PeriodType.getPeriodFromIsoString( pe );

        if ( period == null )
        {
            ContextUtils.conflictResponse( response, "Illegal period identifier: " + pe );
            return;
        }

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( ou );

        if ( organisationUnit == null )
        {
            ContextUtils.conflictResponse( response, "Illegal organisation unit identifier: " + ou );
            return;
        }
        
        DataElementCategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo( response, cc, cp );
        
        if ( attributeOptionCombo == null )
        {
            return;
        }
        
        User user = currentUserService.getCurrentUser();
        
        DataApprovalState state = dataApprovalService.getDataApprovalState( dataSet, period, organisationUnit, attributeOptionCombo );
        
        if ( DataApprovalState.READY_FOR_APPROVAL.equals( state ) )
        {
            DataApproval approval = new DataApproval( dataSet, period, organisationUnit, attributeOptionCombo, new Date(), user );
            
            dataApprovalService.addDataApproval( approval );
        }
    }

    @PreAuthorize( "hasRole('ALL') or hasRole('F_APPROVE_DATA')" )
    @RequestMapping( method = RequestMethod.DELETE, produces = "text/plain" )
    public void removeApproval(
        @RequestParam String ds,
        @RequestParam String pe,
        @RequestParam String ou,
        @RequestParam( required = false ) String cc, 
        @RequestParam( required = false ) String cp, HttpServletResponse response )
    {
        DataSet dataSet = dataSetService.getDataSet( ds );
        
        if ( dataSet == null )
        {
            ContextUtils.conflictResponse( response, "Illegal data set identifier: " + ds );
            return;
        }

        Period period = PeriodType.getPeriodFromIsoString( pe );

        if ( period == null )
        {
            ContextUtils.conflictResponse( response, "Illegal period identifier: " + pe );
            return;
        }

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( ou );

        if ( organisationUnit == null )
        {
            ContextUtils.conflictResponse( response, "Illegal organisation unit identifier: " + ou );
            return;
        }
        
        DataElementCategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo( response, cc, cp );
        
        if ( attributeOptionCombo == null )
        {
            return;
        }
        
        DataApproval approval = dataApprovalService.getDataApproval( dataSet, period, organisationUnit, attributeOptionCombo );
        
        if ( approval != null )
        {
            dataApprovalService.deleteDataApproval( approval );
        }
    }
}
